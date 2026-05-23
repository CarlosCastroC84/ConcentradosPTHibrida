package com.example.concentradospt.data.model.admin

/**
 * Contiene la información de perfil de un usuario del sistema administrativo.
 * Se usa principalmente como parte de la respuesta de autenticación y en listados de usuarios.
 *
 * @property id Identificador único del usuario en la base de datos del sistema.
 * @property nombre Primer nombre del usuario.
 * @property apellido Apellido del usuario.
 * @property email Correo electrónico del usuario (nullable si no fue registrado).
 * @property cedula Número de documento de identidad del usuario, usado como credencial de login.
 * @property tipoDocumento Tipo de documento de identidad (p. ej. "CC", "NIT") (nullable).
 * @property rol Rol asignado al usuario dentro del sistema: "ADMIN", "VENDEDOR" o "BODEGA".
 * @property estado Estado actual de la cuenta del usuario (p. ej. "ACTIVO", "INACTIVO").
 */
data class AdminUsuarioInfo(
    val id: Long = 0,
    val nombre: String = "",
    val apellido: String = "",
    val email: String? = null,
    val cedula: String = "",
    val tipoDocumento: String? = null,
    val rol: String = "",       // "ADMIN" | "VENDEDOR" | "BODEGA"
    val estado: String = "ACTIVO"
) {
    /**
     * Indica si el usuario se encuentra activo comparando el campo [estado] con "ACTIVO"
     * de forma insensible a mayúsculas.
     */
    val isActive: Boolean get() = estado.equals("ACTIVO", ignoreCase = true)

    /**
     * Retorna el nombre completo del usuario concatenando [nombre] y [apellido],
     * eliminando espacios sobrantes en caso de que alguno esté vacío.
     */
    val nombreCompleto: String get() = "$nombre $apellido".trim()
}
