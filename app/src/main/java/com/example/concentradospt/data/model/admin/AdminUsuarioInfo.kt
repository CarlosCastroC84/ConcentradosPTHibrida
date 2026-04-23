package com.example.concentradospt.data.model.admin

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
    val isActive: Boolean get() = estado.equals("ACTIVO", ignoreCase = true)
    val nombreCompleto: String get() = "$nombre $apellido".trim()
}
