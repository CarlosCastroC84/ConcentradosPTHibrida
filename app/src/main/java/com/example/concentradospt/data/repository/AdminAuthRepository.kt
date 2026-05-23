package com.example.concentradospt.data.repository

import com.example.concentradospt.data.model.admin.AdminLoginRequest
import com.example.concentradospt.data.model.admin.AdminUsuarioInfo
import com.example.concentradospt.data.network.admin.AdminApiService
import com.example.concentradospt.data.network.admin.AdminRetrofitClient
import com.example.concentradospt.data.network.admin.TokenManager

/**
 * Repositorio que gestiona la autenticación de usuarios administrativos contra el backend
 * Spring Boot interno, actuando como intermediario entre la capa de presentación y
 * [AdminApiService].
 *
 * Tras un inicio de sesión exitoso, persiste los tokens de acceso y refresco en [TokenManager]
 * para que [AdminAuthInterceptor] y [AdminTokenAuthenticator] puedan usarlos en peticiones
 * posteriores. También expone métodos síncronos de consulta de estado de sesión que no
 * requieren comunicación con el servidor.
 */
class AdminAuthRepository {

    /** Implementación de [AdminApiService] generada por Retrofit para realizar peticiones HTTP. */
    private val api = AdminRetrofitClient.create<AdminApiService>()

    /**
     * Autentica a un usuario administrador con cédula, rol y PIN, y persiste los tokens
     * resultantes en [TokenManager].
     *
     * @param cedula Número de cédula del administrador.
     * @param rol Rol del administrador en el sistema (p. ej. `"ADMIN"`, `"VENDEDOR"`).
     * @param pin PIN de acceso del administrador.
     * @return [AdminUsuarioInfo] con los datos del usuario autenticado.
     * @throws Exception si las credenciales son incorrectas o el servidor no está disponible.
     */
    suspend fun signIn(cedula: String, rol: String, pin: String): AdminUsuarioInfo {
        val response = api.login(AdminLoginRequest(cedula, rol, pin))
        TokenManager.saveTokens(response.accessToken, response.refreshToken)
        return response.usuario
    }

    /**
     * Obtiene la información del usuario administrador actualmente autenticado consultando
     * el endpoint `/auth/me` con el token de acceso vigente.
     *
     * @return [AdminUsuarioInfo] con los datos del usuario asociado al token de acceso.
     * @throws Exception si el token ha expirado o el servidor no está disponible.
     */
    suspend fun getMe(): AdminUsuarioInfo = api.getMe()

    /**
     * Cierra la sesión del administrador eliminando los tokens almacenados en [TokenManager].
     * Esta operación es local y no realiza ninguna llamada al servidor.
     */
    fun signOut() = TokenManager.clearTokens()

    /**
     * Verifica si hay una sesión administrativa activa comprobando la existencia de un
     * access token en [TokenManager].
     *
     * @return `true` si existe un token de acceso almacenado; `false` en caso contrario.
     */
    fun isSignedIn(): Boolean = TokenManager.isLoggedIn()

    /**
     * Obtiene el rol del administrador actualmente autenticado decodificando el claim `rol`
     * del access token JWT almacenado en [TokenManager].
     *
     * @return El rol como [String] (p. ej. `"ADMIN"`), o `null` si no hay sesión activa o
     *         el token no contiene el claim.
     */
    fun getCurrentRol(): String? = TokenManager.getRolFromToken()
}
