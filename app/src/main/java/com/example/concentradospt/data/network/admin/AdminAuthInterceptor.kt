package com.example.concentradospt.data.network.admin

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor de OkHttp que inyecta el token de acceso del administrador en la cabecera
 * `Authorization` de cada petición HTTP protegida del módulo de administración.
 *
 * Lógica general:
 * 1. Verifica si la URL de la petición corresponde a un endpoint público de autenticación
 *    (`auth/login` o `auth/refresh`).
 * 2. Si es una ruta pública, deja pasar la petición sin modificaciones.
 * 3. Si es una ruta protegida, obtiene el token de acceso desde [TokenManager] y lo añade
 *    como `Bearer <token>` en la cabecera `Authorization`.
 * 4. Si no existe token (sesión no iniciada), la petición continúa sin cabecera de autorización.
 */
class AdminAuthInterceptor : Interceptor {

    /**
     * Conjunto de segmentos de ruta que identifican endpoints públicos de autenticación.
     * Las peticiones cuya URL contenga alguno de estos valores no requieren token.
     */
    private val publicPaths = setOf("auth/login", "auth/refresh")

    /**
     * Intercepta cada petición saliente y, cuando corresponde, añade el token de acceso
     * del administrador obtenido de [TokenManager].
     *
     * @param chain Cadena de interceptores de OkHttp que permite inspeccionar y continuar
     *              la petición original.
     * @return La [Response] resultante de ejecutar la petición, con o sin cabecera de
     *         autorización según corresponda.
     */
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
