package com.samoondigital.yojnaplus.feature.oldsir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samoondigital.yojnaplus.model.OldSirAssemblyDto
import com.samoondigital.yojnaplus.model.OldSirDistrictDto
import com.samoondigital.yojnaplus.model.OldSirPartDto
import com.samoondigital.yojnaplus.model.StateDto
import com.samoondigital.yojnaplus.repository.ElectoralRollRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OldSirViewModel @Inject constructor(
    private val repository: ElectoralRollRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OldSirUiState())
    val state: StateFlow<OldSirUiState> = _state.asStateFlow()

    init {
        loadStates()
    }

    fun loadStates() = viewModelScope.launch {
        runLoading("Loading states") {
            _state.update { it.copy(states = repository.getStates(), message = null) }
        }
    }

    fun selectState(state: StateDto) {
        _state.update {
            it.resetAfterState().copy(
                step = OldSirStep.District,
                selectedState = state,
                message = null,
            )
        }
        loadDistricts(state.stateCd)
    }

    fun selectDistrict(district: OldSirDistrictDto) {
        val stateCd = _state.value.selectedState?.stateCd ?: return
        _state.update {
            it.resetAfterDistrict().copy(
                step = OldSirStep.Assembly,
                selectedDistrict = district,
                message = null,
            )
        }
        loadAssemblies(stateCd, district.districtNo)
    }

    fun selectAssembly(assembly: OldSirAssemblyDto) {
        val stateCd = _state.value.selectedState?.stateCd ?: return
        _state.update {
            it.resetAfterAssembly().copy(
                step = OldSirStep.PollingStation,
                selectedAssembly = assembly,
                message = null,
            )
        }
        loadParts(stateCd, assembly.acNo)
    }

    fun selectPart(part: OldSirPartDto) {
        _state.update { it.copy(selectedPart = part, message = null) }
    }

    fun goBack(): Boolean {
        val previous = when (_state.value.step) {
            OldSirStep.State -> return false
            OldSirStep.District -> OldSirStep.State
            OldSirStep.Assembly -> OldSirStep.District
            OldSirStep.PollingStation -> OldSirStep.Assembly
        }
        _state.update { it.copy(step = previous, message = null) }
        return true
    }

    private fun loadDistricts(stateCd: String) = viewModelScope.launch {
        runLoading(null) {
            val districts = repository.getOldSirDistricts(stateCd)
            _state.update {
                it.copy(
                    districts = districts,
                    message = if (districts.isEmpty()) "No Old SIR districts found for this state" else null,
                )
            }
        }
    }

    private fun loadAssemblies(stateCd: String, districtNo: Int) = viewModelScope.launch {
        runLoading(null) {
            val assemblies = repository.getOldSirAssemblies(stateCd, districtNo)
            _state.update {
                it.copy(
                    assemblies = assemblies,
                    message = if (assemblies.isEmpty()) "No assembly constituency found" else null,
                )
            }
        }
    }

    private fun loadParts(stateCd: String, acNumber: Int) = viewModelScope.launch {
        runLoading(null) {
            val parts = repository.getOldSirParts(stateCd, acNumber)
            _state.update {
                it.copy(
                    parts = parts,
                    message = if (parts.isEmpty()) "No polling station PDF found" else null,
                )
            }
        }
    }

    private suspend fun runLoading(message: String?, block: suspend () -> Unit) {
        _state.update { it.copy(isLoading = true, message = message) }
        runCatching { block() }
            .onFailure { error ->
                _state.update { it.copy(message = error.message?.takeIf(String::isNotBlank) ?: "Request failed") }
            }
        _state.update { it.copy(isLoading = false) }
    }
}

enum class OldSirStep {
    State,
    District,
    Assembly,
    PollingStation,
}

data class OldSirUiState(
    val step: OldSirStep = OldSirStep.State,
    val states: List<StateDto> = emptyList(),
    val districts: List<OldSirDistrictDto> = emptyList(),
    val assemblies: List<OldSirAssemblyDto> = emptyList(),
    val parts: List<OldSirPartDto> = emptyList(),
    val selectedState: StateDto? = null,
    val selectedDistrict: OldSirDistrictDto? = null,
    val selectedAssembly: OldSirAssemblyDto? = null,
    val selectedPart: OldSirPartDto? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
) {
    val stepNumber: Int
        get() = when (step) {
            OldSirStep.State -> 1
            OldSirStep.District -> 2
            OldSirStep.Assembly -> 3
            OldSirStep.PollingStation -> 4
        }

    fun resetAfterState(): OldSirUiState = copy(
        selectedDistrict = null,
        selectedAssembly = null,
        selectedPart = null,
        districts = emptyList(),
        assemblies = emptyList(),
        parts = emptyList(),
    )

    fun resetAfterDistrict(): OldSirUiState = copy(
        selectedAssembly = null,
        selectedPart = null,
        assemblies = emptyList(),
        parts = emptyList(),
    )

    fun resetAfterAssembly(): OldSirUiState = copy(
        selectedPart = null,
        parts = emptyList(),
    )
}
