package com.example.concentradospt.data.model.admin

/**
 * DTO de respuesta devuelto por el backend tras una autenticación administrativa exitosa.
 * Contiene los tokens de sesión y la información básica del usuario autenticado.
 *
 * @property accessToken Token de acceso JWT utilizado para autorizar las peticiones a la API.
 * @property refreshToken Token de refresco que permite renovar el accessToken sin volver a autenticarse.
 * @property tipo Tipo de token emitido, generalmente "Bearer" según el estándar OAuth 2.0.
 * @property usuario Información del usuario administrador que inició sesión.
 */
data class AdminLoginResponse(
    val accessToken: String = "",
    val refreshToken: String = "",
    val tipo: String = "Bearer",
    val usuario: AdminUsuarioInfo = AdminUsuarioInfo()
)

/**
 * DTO de request para renovar el token de acceso usando el token de refresco.
 *
 * @property refreshToken Token de refresco previamente emitido durante el login.
 */
data class AdminRefreshRequest(val refreshToken: String)
