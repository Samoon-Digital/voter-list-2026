package com.samoondigital.yojnaplus.domain.repository

import com.samoondigital.yojnaplus.core.common.Resource
import com.samoondigital.yojnaplus.domain.model.CaptchaData
import com.samoondigital.yojnaplus.domain.model.Voter
import kotlinx.coroutines.flow.Flow

interface ElectoralRepository {

    suspend fun getCaptcha(): Resource<CaptchaData>

    suspend fun sendMobileOtp(
        mobile: String,
        stateCd: String,
        captchaId: String,
        captchaData: String,
    ): Resource<Unit>

    suspend fun searchByMobile(
        otp: String,
        mobile: String,
        stateCd: String?,
    ): Resource<List<Voter>>

    suspend fun searchByEpic(
        epicNumber: String,
        captchaId: String,
        captchaData: String,
    ): Resource<List<Voter>>

    suspend fun searchByDetails(
        stateCd: String,
        firstName: String,
        lastName: String?,
        relationName: String,
        dob: String?,
        gender: String,
        captchaId: String,
        captchaData: String,
    ): Resource<List<Voter>>

    fun recentSearches(): Flow<List<String>>

    suspend fun clearRecentSearches()
}
