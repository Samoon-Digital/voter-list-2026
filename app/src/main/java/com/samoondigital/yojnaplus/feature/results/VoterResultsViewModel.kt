package com.samoondigital.yojnaplus.feature.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.samoondigital.yojnaplus.domain.model.Voter
import com.samoondigital.yojnaplus.domain.repository.ElectoralRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class VoterResultsViewModel @Inject constructor(
    repository: ElectoralRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val searchType: String = savedStateHandle["searchType"] ?: ""
    private val query: String = savedStateHandle["query"] ?: ""

    val results: Flow<List<Voter>> = repository.observeVoterResults(query, searchType)
}
