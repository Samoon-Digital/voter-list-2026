package com.samoondigital.yojnaplus.domain.repository

import com.samoondigital.yojnaplus.core.common.Resource
import com.samoondigital.yojnaplus.domain.model.SearchType
import com.samoondigital.yojnaplus.domain.model.Voter
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over voter data sources. The domain layer depends only on this
 * interface; the concrete implementation (network + Room) lives in the data layer.
 */
interface ElectoralRepository {

    /** Performs a voter lookup of the given [type] for [query]. */
    suspend fun searchVoters(type: SearchType, query: String): Resource<List<Voter>>

    /** Stream of the most recent search queries, newest first. */
    fun recentSearches(): Flow<List<String>>

    /** Clears stored recent searches. */
    suspend fun clearRecentSearches()
}
