package com.samoondigital.yojnaplus.api

import com.samoondigital.yojnaplus.model.PublishedFileDto
import retrofit2.http.GET
import retrofit2.http.Query

interface EciPublishedFileApi {
    @GET("api/v1/ext-printing-publish/get-published-file")
    suspend fun getPublishedFile(@Query("fileId") fileId: String): PublishedFileDto
}
