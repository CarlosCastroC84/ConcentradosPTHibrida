package com.example.concentradospt.data.network.admin

import okhttp3.Interceptor
import okhttp3.Response

class AdminAuthInterceptor : Interceptor {

    private val publicPaths = setOf("auth/login", "auth/refresh")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        if (publicPaths.any { url.contains(it) }) {
            return chain.proceed(request)
        }

        val token = TokenManager.getAccessToken()
        return if (token != null) {
            chain.proceed(
                request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            )
        } else {
            chain.proceed(request)
        }
    }
}
