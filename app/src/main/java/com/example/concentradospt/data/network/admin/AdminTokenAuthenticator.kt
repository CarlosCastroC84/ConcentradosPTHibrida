package com.example.concentradospt.data.network.admin

import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AdminTokenAuthenticator : Authenticator {

    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = TokenManager.getRefreshToken() ?: return null

        // Avoid infinite loop: if the failing request already has no auth header, give up
        if (response.request.header("Authorization") == null) return null

        synchronized(this) {
            val currentToken = TokenManager.getAccessToken()
            // Another thread may have already refreshed; use the new token
            val tokenInRequest = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (currentToken != null && currentToken != tokenInRequest) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val body = """{"refreshToken":"$refreshToken"}"""
                .toRequestBody("application/json".toMediaTypeOrNull())
            val refreshRequest = Request.Builder()
                .url("http://100.25.124.3:8080/api/v1/auth/refresh")
                .post(body)
                .build()

            val refreshResponse = refreshClient.newCall(refreshRequest).execute()
            return if (refreshResponse.isSuccessful) {
                val json = JSONObject(refreshResponse.body?.string() ?: return null)
                val newAccess = json.optString("accessToken")
                val newRefresh = json.optString("refreshToken")
                if (newAccess.isNotEmpty()) {
                    TokenManager.saveTokens(newAccess, newRefresh)
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccess")
                        .build()
                } else {
                    TokenManager.clearTokens()
                    null
                }
            } else {
                TokenManager.clearTokens()
                null
            }
        }
    }
}
