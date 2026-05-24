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

/**
 * Autenticador de OkHttp que se ejecuta automáticamente cuando el servidor responde
 * con HTTP 401 (No autorizado) para renovar el token de acceso del administrador.
 *
 * Lógica general:
 * 1. Recupera el refresh token almacenado en [TokenManager].
 * 2. Si no existe refresh token o la petición fallida no tenía cabecera de autorización,
 *    devuelve `null` para evitar bucles infinitos.
 * 3. Usa un bloque `synchronized` para garantizar que solo un hilo realice el refresco
 *    simultáneamente; si otro hilo ya actualizó el token, reutiliza el nuevo.
 * 4. Realiza una petición de refresco directa al endpoint `/auth/refresh` usando un
 *    cliente HTTP independiente (sin interceptores de autenticación).
 * 5. Si el refresco es exitoso, guarda los nuevos tokens en [TokenManager] y reintenta
 *    la petición original con el nuevo access token.
 * 6. Si el refresco falla, limpia los tokens almacenados y devuelve `null`.
 */
class AdminTokenAuthenticator : Authenticator {

    /**
     * Cliente HTTP independiente utilizado exclusivamente para la petición de refresco de token.
     * No incluye interceptores de autenticación para evitar recursividad.
     */
    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Intenta renovar el token de acceso cuando el servidor devuelve HTTP 401.
     *
     * @param route Ruta de la petición fallida (puede ser `null`).
     * @param response La [Response] con código 401 que disparó la autenticación.
     * @return Una nueva [Request] con el token de acceso renovado, o `null` si no es posible
     *         renovar (sin refresh token, sin cabecera de autorización previa o error en el refresco).
     */
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

            val body = org.json.JSONObject().put("refreshToken", refreshToken)
                .toString()
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
