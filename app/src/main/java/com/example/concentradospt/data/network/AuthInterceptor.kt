package com.example.concentradospt.data.network

import android.util.Log
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Amplify
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Interceptor de OkHttp que inyecta el token de identidad (ID Token) de Amazon Cognito
 * en la cabecera `Authorization` de cada petición HTTP protegida.
 *
 * Lógica general:
 * 1. Determina si la petición apunta a un endpoint público (sin autenticación).
 * 2. Si es pública, la deja pasar sin modificaciones.
 * 3. Si es protegida, obtiene de forma síncrona el token de Cognito mediante [fetchToken]
 *    y lo añade como `Bearer <token>` en la cabecera `Authorization`.
 */
class AuthInterceptor : Interceptor {

    /**
     * Intercepta cada petición saliente y, cuando corresponde, añade el token de
     * autorización de Cognito.
     *
     * Se omite la inyección del token en peticiones GET que coincidan con alguno de los
     * endpoints definidos en [ApiConstants.PUBLIC_ENDPOINTS].
     *
     * @param chain Cadena de interceptores de OkHttp que permite inspeccionar y continuar
     *              la petición original.
     * @return La [Response] resultante de ejecutar la petición, con o sin cabecera de
     *         autorización según corresponda.
     */
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

    /**
     * Obtiene de forma síncrona el ID Token (o en su defecto el Access Token) de la
     * sesión activa de Amazon Cognito mediante Amplify.
     *
     * Usa un [CountDownLatch] con un timeout de 5 segundos para bloquear el hilo de red
     * mientras se espera la respuesta asíncrona de Amplify, evitando bloqueos indefinidos.
     *
     * Se prefiere el ID Token porque contiene los claims de identidad (email, sub) que
     * el backend Spring Boot utiliza para identificar al usuario en `/clientes/me`.
     *
     * @return El ID Token como [String] si el usuario está autenticado, o `null` si no
     *         hay sesión activa o se supera el tiempo de espera.
     */
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
