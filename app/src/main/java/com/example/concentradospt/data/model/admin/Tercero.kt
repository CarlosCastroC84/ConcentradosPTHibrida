package com.example.concentradospt.data.model.admin

/**
 * Representa un tercero en el sistema administrativo, que puede ser una persona natural o jurídica.
 * Los terceros actúan como clientes o contrapartes en las transacciones de venta registradas.
 *
 * @property id Identificador único del tercero en la base de datos.
 * @property tipoIdentificacion Tipo de documento de identidad (p. ej. "CC", "NIT", "CE").
 * @property identificacion Número del documento de identidad del tercero.
 * @property primerNombre Primer nombre del tercero (nullable, aplica para personas naturales).
 * @property segundoNombre Segundo nombre del tercero (nullable, opcional).
 * @property primerApellido Primer apellido del tercero (nullable, aplica para personas naturales).
 * @property segundoApellido Segundo apellido del tercero (nullable, opcional).
 * @property razonSocial Razón social o nombre legal para personas jurídicas (nullable).
 * @property nombreDisplay Nombre calculado o formateado para mostrar en la interfaz de usuario.
 * @property telefono Número de teléfono de contacto del tercero (nullable).
 * @property email Correo electrónico del tercero (nullable).
 * @property ciudad Ciudad de residencia o ubicación del tercero (nullable).
 * @property activo Indica si el tercero está habilitado para participar en transacciones.
 */
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
