package com.samoondigital.yojnaplus.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samoondigital.yojnaplus.core.common.Resource
import com.samoondigital.yojnaplus.core.common.UiState
import com.samoondigital.yojnaplus.domain.model.SearchType
import com.samoondigital.yojnaplus.domain.model.Voter
import com.samoondigital.yojnaplus.domain.usecase.SearchVotersUseCase
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
    val query: String = "",
    val results: UiState<List<Voter>> = UiState.Idle,
)

@HiltViewModel
class ElectoralSearchViewModel @Inject constructor(
    private val searchVoters: SearchVotersUseCase,
    private val electoralRepository: com.samoondigital.yojnaplus.domain.repository.ElectoralRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchScreenState())
    val state: StateFlow<SearchScreenState> = _state.asStateFlow()

    val recentSearches: StateFlow<List<String>> = electoralRepository.recentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onTypeChange(type: SearchType) = _state.update {
        it.copy(selectedType = type, results = UiState.Idle)
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

    fun onSearch() {
        val current = _state.value
        _state.update { it.copy(results = UiState.Loading) }
        viewModelScope.launch {
            val result = searchVoters(current.selectedType, current.query)
            _state.update {
                it.copy(
                    results = when (result) {
                        is Resource.Success -> UiState.Success(result.data)
                        is Resource.Error -> UiState.Error(result.message)
                    },
                )
            }
        }
    }

    fun clearRecent() = viewModelScope.launch { electoralRepository.clearRecentSearches() }
}
