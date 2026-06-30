package com.samoondigital.yojnaplus.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samoondigital.yojnaplus.core.common.Resource
import com.samoondigital.yojnaplus.core.common.UiState
import com.samoondigital.yojnaplus.domain.model.CaptchaData
import com.samoondigital.yojnaplus.domain.model.RecentSearchItem
import com.samoondigital.yojnaplus.domain.model.SearchType
import com.samoondigital.yojnaplus.domain.model.Voter
import com.samoondigital.yojnaplus.domain.repository.ElectoralRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchScreenState(
    val selectedType: SearchType = SearchType.MOBILE,
    val query: String = "",
    // Captcha
    val captcha: UiState<CaptchaData> = UiState.Idle,
    val captchaInput: String = "",
    // Captcha dialog visibility
    val showCaptchaDialog: Boolean = false,
    // Mobile OTP dialog
    val showOtpDialog: Boolean = false,
    val otpStep: Boolean = false,
    val otp: String = "",
    val otpSendState: UiState<Unit> = UiState.Idle,
    // Name/DOB extra fields
    val lastName: String = "",
    val relationName: String = "",
    val dob: String = "",
    val gender: String = "M",
    val selectedStateCd: String = "NA",
    // Results (in-memory, also persisted to Room)
    val searchState: UiState<List<Voter>> = UiState.Idle,
)

sealed interface NavEvent {
    data class GoToResults(val searchType: String, val query: String) : NavEvent
}

@HiltViewModel
class ElectoralSearchViewModel @Inject constructor(
    private val repository: ElectoralRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchScreenState())
    val state: StateFlow<SearchScreenState> = _state.asStateFlow()

    private val _navEvents = MutableSharedFlow<NavEvent>()
    val navEvents = _navEvents.asSharedFlow()

    val recentSearches: StateFlow<List<RecentSearchItem>> = repository.recentSearches()
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
                searchState = UiState.Idle,
                showCaptchaDialog = false,
                showOtpDialog = false,
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

    /** User tapped main Search/Send OTP button — open captcha dialog. */
    fun onSearch() {
        val s = _state.value
        when (s.selectedType) {
            SearchType.MOBILE -> {
                if (!s.query.trim().matches(Regex("\\d{10}"))) {
                    _state.update { it.copy(searchState = UiState.Error("Enter a valid 10-digit mobile number")) }
                    return
                }
            }
            SearchType.EPIC -> {
                if (s.query.trim().length < 6) {
                    _state.update { it.copy(searchState = UiState.Error("Enter a valid EPIC number")) }
                    return
                }
            }
            SearchType.NAME_DOB -> {
                if (s.selectedStateCd == "NA") {
                    _state.update { it.copy(searchState = UiState.Error("Please select a state")) }
                    return
                }
                if (s.query.trim().length < 2) {
                    _state.update { it.copy(searchState = UiState.Error("Enter at least 2 characters for first name")) }
                    return
                }
                if (s.relationName.isBlank()) {
                    _state.update { it.copy(searchState = UiState.Error("Please enter relative's name")) }
                    return
                }
                if (s.dob.isBlank()) {
                    _state.update { it.copy(searchState = UiState.Error("Please enter date of birth")) }
                    return
                }
            }
        }
        _state.update { it.copy(showCaptchaDialog = true, searchState = UiState.Idle) }
    }

    fun onCaptchaDialogDismiss() {
        _state.update { it.copy(showCaptchaDialog = false, captchaInput = "") }
    }

    /** Called when user taps Submit in captcha dialog. */
    fun onCaptchaSubmit() {
        val s = _state.value
        if (s.captchaInput.isBlank()) {
            _state.update { it.copy(searchState = UiState.Error("Please enter the captcha code")) }
            return
        }
        _state.update { it.copy(showCaptchaDialog = false) }
        when (s.selectedType) {
            SearchType.MOBILE -> sendOtp()
            SearchType.EPIC -> searchByEpic()
            SearchType.NAME_DOB -> searchByDetails()
        }
    }

    fun onOtpDialogDismiss() {
        _state.update { it.copy(showOtpDialog = false, otp = "") }
    }

    fun onVerifyOtp() {
        val s = _state.value
        val otp = s.otp.trim()
        if (otp.length < 4) {
            _state.update { it.copy(searchState = UiState.Error("Please enter a valid OTP")) }
            return
        }
        _state.update { it.copy(searchState = UiState.Loading, showOtpDialog = false) }
        viewModelScope.launch {
            val result = repository.searchByMobile(
                otp = otp,
                mobile = s.query.trim(),
                stateCd = if (s.selectedStateCd == "NA") null else s.selectedStateCd,
            )
            when (result) {
                is Resource.Success -> {
                    val voters = result.data
                    repository.saveVoterResults(voters, s.query.trim(), "MOBILE")
                    _state.update { it.copy(searchState = UiState.Success(voters)) }
                    _navEvents.emit(NavEvent.GoToResults("MOBILE", s.query.trim()))
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            searchState = UiState.Error(result.message),
                            showOtpDialog = true,
                        )
                    }
                }
            }
        }
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

    private fun sendOtp() {
        val s = _state.value
        val captcha = (s.captcha as? UiState.Success)?.data ?: return showCaptchaError()
        _state.update { it.copy(otpSendState = UiState.Loading) }
        viewModelScope.launch {
            val result = repository.sendMobileOtp(
                mobile = s.query.trim(),
                stateCd = if (s.selectedStateCd == "NA") "" else s.selectedStateCd,
                captchaId = captcha.id,
                captchaData = s.captchaInput,
            )
            when (result) {
                is Resource.Success -> _state.update {
                    it.copy(
                        otpSendState = UiState.Success(Unit),
                        otpStep = true,
                        captchaInput = "",
                        showOtpDialog = true,
                    )
                }
                is Resource.Error -> {
                    _state.update { it.copy(otpSendState = UiState.Error(result.message), captchaInput = "") }
                    refreshCaptcha()
                }
            }
        }
    }

    private fun searchByEpic() {
        val s = _state.value
        val captcha = (s.captcha as? UiState.Success)?.data ?: return showCaptchaError()
        _state.update { it.copy(searchState = UiState.Loading) }
        viewModelScope.launch {
            val result = repository.searchByEpic(
                epicNumber = s.query.trim(),
                captchaId = captcha.id,
                captchaData = s.captchaInput,
            )
            _state.update { it.copy(captchaInput = "") }
            when (result) {
                is Resource.Success -> {
                    repository.saveVoterResults(result.data, s.query.trim(), "EPIC")
                    _state.update { it.copy(searchState = UiState.Success(result.data)) }
                    _navEvents.emit(NavEvent.GoToResults("EPIC", s.query.trim()))
                }
                is Resource.Error -> {
                    _state.update { it.copy(searchState = UiState.Error(result.message)) }
                    refreshCaptcha()
                }
            }
        }
    }

    private fun searchByDetails() {
        val s = _state.value
        val captcha = (s.captcha as? UiState.Success)?.data ?: return showCaptchaError()
        _state.update { it.copy(searchState = UiState.Loading) }
        viewModelScope.launch {
            val result = repository.searchByDetails(
                stateCd = s.selectedStateCd,
                firstName = s.query.trim(),
                lastName = s.lastName.trim().ifBlank { null },
                relationName = s.relationName.trim(),
                dob = s.dob.trim(),
                gender = s.gender,
                captchaId = captcha.id,
                captchaData = s.captchaInput,
            )
            _state.update { it.copy(captchaInput = "") }
            when (result) {
                is Resource.Success -> {
                    repository.saveVoterResults(result.data, s.query.trim(), "NAME_DOB")
                    _state.update { it.copy(searchState = UiState.Success(result.data)) }
                    _navEvents.emit(NavEvent.GoToResults("NAME_DOB", s.query.trim()))
                }
                is Resource.Error -> {
                    _state.update { it.copy(searchState = UiState.Error(result.message)) }
                    refreshCaptcha()
                }
            }
        }
    }

    private fun showCaptchaError() {
        _state.update { it.copy(searchState = UiState.Error("Captcha not loaded. Please refresh.")) }
    }

    fun clearRecent() = viewModelScope.launch { repository.clearRecentSearches() }
}
