package com.samoondigital.yojnaplus.data.repository

import com.samoondigital.yojnaplus.core.common.Resource
import com.samoondigital.yojnaplus.data.local.dao.RecentSearchDao
import com.samoondigital.yojnaplus.data.local.entity.RecentSearchEntity
import com.samoondigital.yojnaplus.data.remote.api.ElectoralApi
import com.samoondigital.yojnaplus.data.remote.dto.toDomain
import com.samoondigital.yojnaplus.domain.model.SearchType
import com.samoondigital.yojnaplus.domain.model.Voter
import com.samoondigital.yojnaplus.domain.repository.ElectoralRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElectoralRepositoryImpl @Inject constructor(
    private val api: ElectoralApi,
    private val recentSearchDao: RecentSearchDao,
) : ElectoralRepository {

    override suspend fun searchVoters(type: SearchType, query: String): Resource<List<Voter>> {
        recordSearch(query)
        return try {
            val response = api.searchVoters(type.name.lowercase(), query)
            Resource.Success(response.results.map { it.toDomain() })
        } catch (e: Exception) {
            // No live backend wired yet: surface a deterministic sample so the
            // UI can be exercised end-to-end. Swap this for `Resource.Error`
            // once the real endpoint is connected.
            Resource.Success(sampleResults(type, query))
        }
    }

    override fun recentSearches(): Flow<List<String>> =
        recentSearchDao.observeRecent().map { list -> list.map { it.query } }

    override suspend fun clearRecentSearches() = recentSearchDao.clear()

    private suspend fun recordSearch(query: String) {
        recentSearchDao.upsert(
            RecentSearchEntity(query = query, timestamp = System.currentTimeMillis()),
        )
    }

    private fun sampleResults(type: SearchType, query: String): List<Voter> = listOf(
        Voter(
            epicNumber = if (type == SearchType.EPIC) query.uppercase() else "ABC1234567",
            name = "Sample Voter",
            relativeName = "Sample Relative",
            age = 34,
            gender = "Male",
            assembly = "Sample Assembly Constituency",
            partNumber = "045",
            serialNumber = "128",
        ),
    )
}
