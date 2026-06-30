package com.samoondigital.yojnaplus.data.repository

import com.samoondigital.yojnaplus.core.common.Resource
import com.samoondigital.yojnaplus.data.local.dao.RecentSearchDao
import com.samoondigital.yojnaplus.data.local.entity.RecentSearchEntity
import com.samoondigital.yojnaplus.data.remote.api.ElectoralApi
import com.samoondigital.yojnaplus.data.remote.dto.DetailsSearchRequest
import com.samoondigital.yojnaplus.data.remote.dto.EpicSearchRequest
import com.samoondigital.yojnaplus.data.remote.dto.MobileSearchRequest
import com.samoondigital.yojnaplus.data.remote.dto.SendOtpRequest
import com.samoondigital.yojnaplus.data.remote.dto.toDomain
import com.samoondigital.yojnaplus.domain.model.CaptchaData
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

    override suspend fun getCaptcha(): Resource<CaptchaData> = try {
        val resp = api.getCaptcha()
        Resource.Success(CaptchaData(id = resp.id, imageBase64 = resp.captcha))
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
        recordSearch(mobile)
        return try {
            val voters = api.searchByMobile(
                MobileSearchRequest(otp = otp, mobileNumber = mobile, stateCd = stateCd),
            )
            Resource.Success(voters.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Search failed")
        }
    }

    override suspend fun searchByEpic(
        epicNumber: String,
        captchaId: String,
        captchaData: String,
    ): Resource<List<Voter>> {
        recordSearch(epicNumber)
        return try {
            val voters = api.searchByEpic(
                EpicSearchRequest(
                    epicNumber = epicNumber,
                    captchaId = captchaId,
                    captchaData = captchaData,
                ),
            )
            Resource.Success(voters.map { it.toDomain() })
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
        recordSearch(firstName)
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
            )
            Resource.Success(voters.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Search failed")
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
}
