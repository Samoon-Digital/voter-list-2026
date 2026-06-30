package com.samoondigital.yojnaplus.domain.usecase

import com.samoondigital.yojnaplus.core.common.Resource
import com.samoondigital.yojnaplus.domain.model.SearchType
import com.samoondigital.yojnaplus.domain.model.Voter
import com.samoondigital.yojnaplus.domain.repository.ElectoralRepository
import javax.inject.Inject

/**
 * Validates input and delegates to the repository. Keeping validation here means
 * every caller (and future callers) gets the same rules for free.
 */
class SearchVotersUseCase @Inject constructor(
    private val repository: ElectoralRepository,
) {
    suspend operator fun invoke(type: SearchType, query: String): Resource<List<Voter>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return Resource.Error("Please enter a search value")
        }
        when (type) {
            SearchType.MOBILE -> if (!trimmed.matches(Regex("\\d{10}"))) {
                return Resource.Error("Enter a valid 10-digit mobile number")
            }
            SearchType.EPIC -> if (trimmed.length < 6) {
                return Resource.Error("Enter a valid EPIC number")
            }
            SearchType.NAME_DOB -> if (trimmed.length < 3) {
                return Resource.Error("Enter at least 3 characters")
            }
        }
        return repository.searchVoters(type, trimmed)
    }
}
