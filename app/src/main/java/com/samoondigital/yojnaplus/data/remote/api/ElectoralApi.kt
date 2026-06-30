package com.samoondigital.yojnaplus.data.remote.api

import com.samoondigital.yojnaplus.data.remote.dto.VoterSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** Retrofit definition for the (placeholder) electoral search backend. */
interface ElectoralApi {

    @GET("api/v1/voters/search")
    suspend fun searchVoters(
        @Query("type") type: String,
        @Query("q") query: String,
    ): VoterSearchResponse

    companion object {
        // Replace with the real Election Commission / Yojna+ endpoint.
        const val BASE_URL = "https://api.yojnaplus.samoondigital.com/"
    }
}
