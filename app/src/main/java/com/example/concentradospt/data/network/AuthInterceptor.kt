package com.example.concentradospt.data.network

import android.util.Log
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Amplify
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.pathSegments.lastOrNull { it.isNotEmpty() } ?: ""

        // Endpoints GET públicos no requieren token
        if (request.method == "GET" && ApiConstants.PUBLIC_ENDPOINTS.any { path.contains(it) }) {
            return chain.proceed(request)
        }

        val token = fetchIdToken()
        return if (token != null) {
            chain.proceed(
                request.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            )
        } else {
            chain.proceed(request)
        }
    }

    private fun fetchIdToken(): String? {
        var token: String? = null
        val latch = CountDownLatch(1)

        Amplify.Auth.fetchAuthSession(
            { session ->
                val cognitoSession = session as? AWSCognitoAuthSession
                if (session.isSignedIn) {
                    token = cognitoSession?.userPoolTokensResult?.value?.idToken
                }
                latch.countDown()
            },
            { error ->
                Log.e("AuthInterceptor", "Error obteniendo sesión: ${error.message}")
                latch.countDown()
            }
        )

        // Aumentamos el timeout para zonas con baja conectividad
        latch.await(10, TimeUnit.SECONDS)
        return token
    }
}