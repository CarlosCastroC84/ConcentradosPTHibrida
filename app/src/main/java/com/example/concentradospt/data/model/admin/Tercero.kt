package com.example.concentradospt.data.model.admin

data class Tercero(
    val id: Long = 0,
    val tipoIdentificacion: String = "",
    val identificacion: String = "",
    val primerNombre: String? = null,
    val segundoNombre: String? = null,
    val primerApellido: String? = null,
    val segundoApellido: String? = null,
    val razonSocial: String? = null,
    val nombreDisplay: String = "",
    val telefono: String? = null,
    val email: String? = null,
    val ciudad: String? = null,
    val activo: Boolean = true
)
