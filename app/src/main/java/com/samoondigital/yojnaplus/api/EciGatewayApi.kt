package com.samoondigital.yojnaplus.api

import com.samoondigital.yojnaplus.model.AcLanguageRequest
import com.samoondigital.yojnaplus.model.CaptchaEncryptedDto
import com.samoondigital.yojnaplus.model.EciEnvelope
import com.samoondigital.yojnaplus.model.EncryptedBody
import com.samoondigital.yojnaplus.model.PartDto
import com.samoondigital.yojnaplus.model.RollTypeDto
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EciGatewayApi {
    @GET("api/v1/common/states/")
    suspend fun getStates(): JsonElement

    @GET("api/v1/common/districts/{stateCd}")
    suspend fun getDistricts(@Path("stateCd") stateCd: String): JsonElement

    @GET("api/v1/common/acs/{districtCd}")
    suspend fun getAssemblies(@Path("districtCd") districtCd: String): JsonElement

    @GET("api/v1/captcha-service/getCaptcha/{purpose}")
    suspend fun getCaptcha(@Path("purpose") purpose: String = "EROLL"): CaptchaEncryptedDto

    @GET("api/v1/printing-publish/get-publish-eroll-type")
    suspend fun getPublishErollType(
        @Query("stateCd") stateCd: String,
        @Query("year") year: String,
        @Query("misKey") misKey: String,
        @Header("accept_yek") acceptYek: String,
        @Header("accept_rotcev") acceptRotcev: String,
    ): EciEnvelope<List<RollTypeDto>>

    @POST("api/v1/printing-publish/get-ac-languages")
    suspend fun getAcLanguages(@Body body: AcLanguageRequest): EciEnvelope<Map<String, String>>

    @POST("api/v1/printing-publish/get-publish-part-list")
    suspend fun getPublishPartList(@Body body: EncryptedBody): EciEnvelope<List<PartDto>>

    @POST("api/v1/printing-publish/generate-published-pdfs")
    suspend fun generatePublishedPdfs(@Body body: EncryptedBody): EciEnvelope<List<JsonElement>>
}
