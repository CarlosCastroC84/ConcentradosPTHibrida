package com.example.concentradospt.data.model

import com.google.gson.annotations.SerializedName

/**
 * Representa un usuario administrador gestionado a través de AWS Cognito.
 * Contiene la información de identidad, estado y grupos de permisos del usuario.
 *
 * @property username Nombre de usuario único en el pool de Cognito.
 * @property email Correo electrónico asociado a la cuenta del usuario.
 * @property name Nombre completo o alias del usuario administrador.
 * @property estado Estado actual de la cuenta (p. ej. "ACTIVO", "INACTIVO").
 * @property grupos Lista de grupos de Cognito a los que pertenece el usuario, que definen su rol.
 */
data class AdminUsuario(
    @SerializedName("username") val username: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("estado") val estado: String = "ACTIVO",
    @SerializedName("grupos") val grupos: List<String> = emptyList()
) {
    /**
     * Indica si el usuario se encuentra activo comparando el estado con "ACTIVO"
     * de forma insensible a mayúsculas.
     */
    val isActive: Boolean get() = estado.equals("ACTIVO", ignoreCase = true)

    /**
     * Devuelve el nombre del primer grupo asignado al usuario como etiqueta de rol visible.
     * Si el usuario no tiene grupos asignados, retorna "CLIENTE" como valor por defecto.
     */
    val rolDisplay: String get() = grupos.firstOrNull() ?: "CLIENTE"
}

/**
 * DTO de request para cambiar el estado de habilitación de un usuario administrador.
 *
 * @property estado Nuevo estado que se aplicará al usuario (p. ej. "ACTIVO" o "INACTIVO").
 */
data class EstadoUsuarioRequest(
    @SerializedName("estado") val estado: String
)
