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

        if (request.method == "GET" && (ApiConstants.PUBLIC_ENDPOINTS.any { path.contains(it) })) {
            return chain.proceed(request)
        }

        val token = fetchToken()
        
        return if (token != null) {
            Log.d("AuthInterceptor", "Inyectando ID Token en: $path")
            chain.proceed(
                request.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            )
        } else {
            Log.w("AuthInterceptor", "No se encontró token para la ruta: $path")
            chain.proceed(request)
        }
    }

    private fun fetchToken(): String? {
        var token: String? = null
        val latch = CountDownLatch(1)

        Amplify.Auth.fetchAuthSession(
            { session ->
                val cognitoSession = session as? AWSCognitoAuthSession
                if (session.isSignedIn) {
                    // idToken lleva las claims de identidad (email, sub) que Spring Boot usa para /clientes/me
                    token = cognitoSession?.userPoolTokensResult?.value?.idToken
                        ?: cognitoSession?.userPoolTokensResult?.value?.accessToken
                }
                latch.countDown()
            },
            { error ->
                Log.e("AuthInterceptor", "Error obteniendo sesión de Amplify: ${error.message}")
                latch.countDown()
            }
        )

        // Timeout de 5 segundos para no bloquear el hilo de red demasiado tiempo
        latch.await(5, TimeUnit.SECONDS)
        return token
    }
}