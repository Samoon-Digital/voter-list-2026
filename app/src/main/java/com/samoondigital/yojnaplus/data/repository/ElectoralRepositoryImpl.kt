package com.samoondigital.yojnaplus.data.repository

import com.samoondigital.yojnaplus.core.common.Resource
import com.samoondigital.yojnaplus.data.local.dao.RecentSearchDao
import com.samoondigital.yojnaplus.data.local.dao.VoterResultDao
import com.samoondigital.yojnaplus.data.local.entity.RecentSearchEntity
import com.samoondigital.yojnaplus.data.local.entity.VoterResultEntity
import com.samoondigital.yojnaplus.data.remote.api.ElectoralApi
import com.samoondigital.yojnaplus.data.remote.dto.DetailsSearchRequest
import com.samoondigital.yojnaplus.data.remote.dto.EpicSearchRequest
import com.samoondigital.yojnaplus.data.remote.dto.MobileSearchRequest
import com.samoondigital.yojnaplus.data.remote.dto.SendOtpRequest
import com.samoondigital.yojnaplus.data.remote.dto.toDomain
import com.samoondigital.yojnaplus.domain.model.CaptchaData
import com.samoondigital.yojnaplus.domain.model.RecentSearchItem
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
    private val voterResultDao: VoterResultDao,
) : ElectoralRepository {

    override suspend fun getCaptcha(): Resource<CaptchaData> = try {
        val resp = api.getCaptcha()
        // API now returns {data: "sessionToken"} instead of {captcha, id}.
        // We use the token as the session ID and leave imageBase64 empty.
        Resource.Success(CaptchaData(id = resp.data, imageBase64 = ""))
    } catch (e: Exception) {
        Resource.Error("Failed to load captcha: ${e.message}")
    }

    override suspend fun sendMobileOtp(
        mobile: String,
        stateCd: String,
        captchaId: String,
        captchaData: String,
    ): Resource<Unit> = try {
        val resp = api.sendMobileOtp(
            SendOtpRequest(
                mobNo = mobile,
                stateCd = stateCd,
                captchaId = captchaId,
                captchaData = captchaData,
            ),
        )
        if (resp.statusCode == 200) {
            Resource.Success(Unit)
        } else {
            Resource.Error(resp.message.ifBlank { "OTP sending failed (${resp.statusCode})" })
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to send OTP")
    }

    override suspend fun searchByMobile(
        otp: String,
        mobile: String,
        stateCd: String?,
    ): Resource<List<Voter>> {
        recordSearch(mobile, "MOBILE")
        return try {
            val voters = api.searchByMobile(
                MobileSearchRequest(otp = otp, mobileNumber = mobile, stateCd = stateCd),
            ).map { it.toDomain() }
            Resource.Success(voters)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Search failed")
        }
    }

    override suspend fun searchByEpic(
        epicNumber: String,
        captchaId: String,
        captchaData: String,
    ): Resource<List<Voter>> {
        recordSearch(epicNumber, "EPIC")
        return try {
            val voters = api.searchByEpic(
                EpicSearchRequest(
                    epicNumber = epicNumber,
                    captchaId = captchaId,
                    captchaData = captchaData,
                ),
            ).map { it.toDomain() }
            Resource.Success(voters)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Search failed")
        }
    }

    override suspend fun searchByDetails(
        stateCd: String,
        firstName: String,
        lastName: String?,
        relationName: String,
        dob: String?,
        gender: String,
        captchaId: String,
        captchaData: String,
    ): Resource<List<Voter>> {
        recordSearch(firstName, "NAME_DOB")
        return try {
            val voters = api.searchByDetails(
                DetailsSearchRequest(
                    stateCd = stateCd,
                    firstName = firstName,
                    lastName = lastName,
                    relationName = relationName,
                    dob = dob,
                    gender = gender,
                    captchaId = captchaId,
                    captchaData = captchaData,
                ),
            ).map { it.toDomain() }
            Resource.Success(voters)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Search failed")
        }
    }

    override fun recentSearches(): Flow<List<RecentSearchItem>> =
        recentSearchDao.observeRecent().map { list ->
            list.map { RecentSearchItem(query = it.query, searchType = it.searchType) }
        }

    override suspend fun clearRecentSearches() = recentSearchDao.clear()

    override suspend fun saveVoterResults(voters: List<Voter>, query: String, searchType: String) {
        val ts = System.currentTimeMillis()
        voterResultDao.deleteBySearch(query, searchType)
        voterResultDao.insertAll(
            voters.map { v ->
                VoterResultEntity(
                    epicNumber = v.epicNumber,
                    name = v.name,
                    relativeName = v.relativeName,
                    age = v.age,
                    gender = v.gender,
                    assembly = v.assembly,
                    partNumber = v.partNumber,
                    serialNumber = v.serialNumber,
                    stateName = v.stateName,
                    pollingStation = v.pollingStation,
                    searchQuery = query,
                    searchType = searchType,
                    timestamp = ts,
                )
            },
        )
    }

    override fun observeVoterResults(query: String, searchType: String): Flow<List<Voter>> =
        voterResultDao.observeBySearch(query, searchType).map { list ->
            list.map { e ->
                Voter(
                    epicNumber = e.epicNumber,
                    name = e.name,
                    relativeName = e.relativeName,
                    age = e.age,
                    gender = e.gender,
                    assembly = e.assembly,
                    partNumber = e.partNumber,
                    serialNumber = e.serialNumber,
                    stateName = e.stateName,
                    pollingStation = e.pollingStation,
                )
            }
        }

    private suspend fun recordSearch(query: String, type: String) {
        recentSearchDao.upsert(
            RecentSearchEntity(
                query = query,
                timestamp = System.currentTimeMillis(),
                searchType = type,
            ),
        )
    }
}
