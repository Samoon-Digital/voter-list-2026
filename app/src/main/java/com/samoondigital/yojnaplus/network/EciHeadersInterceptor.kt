package com.samoondigital.yojnaplus.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class EciHeadersInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("applicationName", "VSP")
            .header("appName", "VSP")
            .header("currentRole", "citizen")
            .header("PLATFORM-TYPE", "ECIWEB")
            .header("channelidobo", "VSP")
            .header("Accept", "application/json")
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/126 Mobile Safari/537.36",
            )
            .build()
        return chain.proceed(request)
    }
}
