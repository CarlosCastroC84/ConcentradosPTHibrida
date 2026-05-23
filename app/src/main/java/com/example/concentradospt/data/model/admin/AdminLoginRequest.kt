package com.example.concentradospt.data.model.admin

/**
 * DTO de request para autenticar a un usuario con rol administrativo en el sistema.
 * Contiene las credenciales mínimas necesarias para validar el acceso según el rol indicado.
 *
 * @property cedula Número de cédula o documento de identidad del usuario que intenta iniciar sesión.
 * @property rol Rol con el que el usuario intenta autenticarse (p. ej. "ADMIN", "VENDEDOR", "BODEGA").
 * @property pin Código PIN personal del usuario utilizado como contraseña de acceso.
 */
data class AdminLoginRequest(
    val cedula: String,
    val rol: String,
    val pin: String
)
