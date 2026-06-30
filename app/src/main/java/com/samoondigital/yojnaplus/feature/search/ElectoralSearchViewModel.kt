package com.samoondigital.yojnaplus.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samoondigital.yojnaplus.core.common.Resource
import com.samoondigital.yojnaplus.core.common.UiState
import com.samoondigital.yojnaplus.domain.model.CaptchaData
import com.samoondigital.yojnaplus.domain.model.SearchType
import com.samoondigital.yojnaplus.domain.model.Voter
import com.samoondigital.yojnaplus.domain.repository.ElectoralRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchScreenState(
    val selectedType: SearchType = SearchType.MOBILE,
    // Shared query (mobile number for MOBILE, EPIC number for EPIC, first name for NAME_DOB)
    val query: String = "",
    // Captcha
    val captcha: UiState<CaptchaData> = UiState.Idle,
    val captchaInput: String = "",
    // Mobile OTP step
    val otpStep: Boolean = false,
    val otp: String = "",
    val otpSendState: UiState<Unit> = UiState.Idle,
    // Name/DOB extra fields
    val lastName: String = "",
    val relationName: String = "",
    val dob: String = "",
    val gender: String = "M",
    val selectedStateCd: String = "NA",
    // Results
    val results: UiState<List<Voter>> = UiState.Idle,
)

@HiltViewModel
class ElectoralSearchViewModel @Inject constructor(
    private val repository: ElectoralRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchScreenState())
    val state: StateFlow<SearchScreenState> = _state.asStateFlow()

    val recentSearches: StateFlow<List<String>> = repository.recentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        loadCaptcha()
    }

    fun onTypeChange(type: SearchType) {
        _state.update {
            it.copy(
                selectedType = type,
                query = "",
                captchaInput = "",
                otpStep = false,
                otp = "",
                otpSendState = UiState.Idle,
                results = UiState.Idle,
            )
        }
        loadCaptcha()
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }
    fun onCaptchaInputChange(text: String) = _state.update { it.copy(captchaInput = text) }
    fun onOtpChange(otp: String) = _state.update { it.copy(otp = otp) }
    fun onLastNameChange(v: String) = _state.update { it.copy(lastName = v) }
    fun onRelationNameChange(v: String) = _state.update { it.copy(relationName = v) }
    fun onDobChange(v: String) = _state.update { it.copy(dob = v) }
    fun onGenderChange(v: String) = _state.update { it.copy(gender = v) }
    fun onStateChange(stateCd: String) = _state.update { it.copy(selectedStateCd = stateCd) }

    fun refreshCaptcha() {
        _state.update { it.copy(captchaInput = "") }
        loadCaptcha()
    }

    private fun loadCaptcha() {
        _state.update { it.copy(captcha = UiState.Loading) }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    captcha = when (val r = repository.getCaptcha()) {
                        is Resource.Success -> UiState.Success(r.data)
                        is Resource.Error -> UiState.Error(r.message)
                    },
                )
            }
        }
    }

    /** Called when the user taps "Search" or "Send OTP" */
    fun onSearch() {
        val s = _state.value
        when (s.selectedType) {
            SearchType.MOBILE -> sendOtp()
            SearchType.EPIC -> searchByEpic()
            SearchType.NAME_DOB -> searchByDetails()
        }
    }

    private fun sendOtp() {
        val s = _state.value
        val mobile = s.query.trim()
        if (!mobile.matches(Regex("\\d{10}"))) {
            _state.update { it.copy(otpSendState = UiState.Error("Enter a valid 10-digit mobile number")) }
            return
        }
        val captcha = (s.captcha as? UiState.Success)?.data
            ?: return showCaptchaError()
        if (s.captchaInput.isBlank()) {
            _state.update { it.copy(otpSendState = UiState.Error("Please enter the captcha")) }
            return
        }
        _state.update { it.copy(otpSendState = UiState.Loading) }
        viewModelScope.launch {
            val result = repository.sendMobileOtp(
                mobile = mobile,
                stateCd = if (s.selectedStateCd == "NA") "" else s.selectedStateCd,
                captchaId = captcha.id,
                captchaData = s.captchaInput,
            )
            _state.update {
                when (result) {
                    is Resource.Success -> it.copy(
                        otpSendState = UiState.Success(Unit),
                        otpStep = true,
                        captchaInput = "",
                    )
                    is Resource.Error -> it.copy(
                        otpSendState = UiState.Error(result.message),
                        captchaInput = "",
                    ).also { refreshCaptcha() }
                }
            }
        }
    }

    fun onVerifyOtp() {
        val s = _state.value
        val otp = s.otp.trim()
        if (otp.length < 4) {
            _state.update { it.copy(results = UiState.Error("Please enter a valid OTP")) }
            return
        }
        _state.update { it.copy(results = UiState.Loading) }
        viewModelScope.launch {
            val result = repository.searchByMobile(
                otp = otp,
                mobile = s.query.trim(),
                stateCd = if (s.selectedStateCd == "NA") null else s.selectedStateCd,
            )
            _state.update {
                it.copy(
                    results = when (result) {
                        is Resource.Success -> UiState.Success(result.data)
                        is Resource.Error -> UiState.Error(result.message)
                    },
                    otpStep = result is Resource.Error,
                )
            }
        }
    }

    private fun searchByEpic() {
        val s = _state.value
        val epic = s.query.trim()
        if (epic.length < 6) {
            _state.update { it.copy(results = UiState.Error("Enter a valid EPIC number")) }
            return
        }
        val captcha = (s.captcha as? UiState.Success)?.data
            ?: return showCaptchaError()
        if (s.captchaInput.isBlank()) {
            _state.update { it.copy(results = UiState.Error("Please enter the captcha")) }
            return
        }
        _state.update { it.copy(results = UiState.Loading) }
        viewModelScope.launch {
            val result = repository.searchByEpic(
                epicNumber = epic,
                captchaId = captcha.id,
                captchaData = s.captchaInput,
            )
            _state.update {
                it.copy(
                    results = when (result) {
                        is Resource.Success -> UiState.Success(result.data)
                        is Resource.Error -> UiState.Error(result.message)
                    },
                    captchaInput = "",
                )
            }
            if (result is Resource.Error) refreshCaptcha()
        }
    }

    private fun searchByDetails() {
        val s = _state.value
        if (s.selectedStateCd == "NA") {
            _state.update { it.copy(results = UiState.Error("Please select a state")) }
            return
        }
        val firstName = s.query.trim()
        if (firstName.length < 2) {
            _state.update { it.copy(results = UiState.Error("Enter at least 2 characters for name")) }
            return
        }
        if (s.relationName.isBlank()) {
            _state.update { it.copy(results = UiState.Error("Please enter relative's name")) }
            return
        }
        if (s.dob.isBlank()) {
            _state.update { it.copy(results = UiState.Error("Please enter date of birth")) }
            return
        }
        val captcha = (s.captcha as? UiState.Success)?.data
            ?: return showCaptchaError()
        if (s.captchaInput.isBlank()) {
            _state.update { it.copy(results = UiState.Error("Please enter the captcha")) }
            return
        }
        _state.update { it.copy(results = UiState.Loading) }
        viewModelScope.launch {
            val result = repository.searchByDetails(
                stateCd = s.selectedStateCd,
                firstName = firstName,
                lastName = s.lastName.trim().ifBlank { null },
                relationName = s.relationName.trim(),
                dob = s.dob.trim(),
                gender = s.gender,
                captchaId = captcha.id,
                captchaData = s.captchaInput,
            )
            _state.update {
                it.copy(
                    results = when (result) {
                        is Resource.Success -> UiState.Success(result.data)
                        is Resource.Error -> UiState.Error(result.message)
                    },
                    captchaInput = "",
                )
            }
            if (result is Resource.Error) refreshCaptcha()
        }
    }

    private fun showCaptchaError() {
        _state.update { it.copy(results = UiState.Error("Captcha not loaded. Please refresh.")) }
    }

    fun clearRecent() = viewModelScope.launch { repository.clearRecentSearches() }
}
