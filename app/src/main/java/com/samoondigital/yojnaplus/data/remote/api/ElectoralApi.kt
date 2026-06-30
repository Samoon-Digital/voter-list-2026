package com.samoondigital.yojnaplus.data.remote.api

import com.samoondigital.yojnaplus.data.remote.dto.CaptchaResponse
import com.samoondigital.yojnaplus.data.remote.dto.DetailsSearchRequest
import com.samoondigital.yojnaplus.data.remote.dto.EciVoterDto
import com.samoondigital.yojnaplus.data.remote.dto.EpicSearchRequest
import com.samoondigital.yojnaplus.data.remote.dto.MobileSearchRequest
import com.samoondigital.yojnaplus.data.remote.dto.SendOtpRequest
import com.samoondigital.yojnaplus.data.remote.dto.SendOtpResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ElectoralApi {

    @GET("api/v1/captcha-service/getCaptcha/sir")
    @Headers("appName: ELECTORAL-SEARCH")
    suspend fun getCaptcha(): CaptchaResponse

    @POST("api/v1/elastic-otp/send-otp-search-v1")
    @Headers("applicationName: ELECTORAL-SEARCH")
    suspend fun sendMobileOtp(@Body body: SendOtpRequest): SendOtpResponse

    @POST("api/v1/elastic/search-by-mobile-from-state-search-display-v1")
    @Headers("applicationName: ELECTORAL-SEARCH")
    suspend fun searchByMobile(@Body body: MobileSearchRequest): List<EciVoterDto>

    @POST("api/v1/elastic/search-by-epic-from-national-display-v1")
    @Headers("applicationName: ELECTORAL-SEARCH")
    suspend fun searchByEpic(@Body body: EpicSearchRequest): List<EciVoterDto>

    @POST("api/v1/elastic/search-by-details-from-state-display-v1")
    @Headers("applicationName: ELECTORAL-SEARCH")
    suspend fun searchByDetails(@Body body: DetailsSearchRequest): List<EciVoterDto>

    companion object {
        const val BASE_URL = "https://gateway-voters.eci.gov.in/"
    }
}
