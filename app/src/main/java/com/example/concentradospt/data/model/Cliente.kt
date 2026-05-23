package com.example.concentradospt.data.model

import com.google.gson.annotations.SerializedName

/**
 * Representa el perfil de un cliente registrado en la aplicación.
 * Almacena los datos de contacto y la dirección principal del cliente.
 *
 * @property clienteId Identificador único del cliente en el sistema.
 * @property nombre Nombre completo del cliente.
 * @property email Correo electrónico del cliente, utilizado también como credencial de acceso.
 * @property telefono Número de teléfono de contacto del cliente.
 * @property direccion Dirección principal de entrega registrada por el cliente.
 * @property activo Indica si la cuenta del cliente se encuentra habilitada.
 */
data class Cliente(
    @SerializedName("clienteId") val clienteId: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("telefono") val telefono: String = "",
    @SerializedName("direccion") val direccion: String = "",
    @SerializedName("activo") val activo: Boolean = true
)

/**
 * DTO de request para actualizar los datos de perfil de un cliente existente.
 * Solo expone los campos que el cliente puede modificar desde la aplicación.
 *
 * @property nombre Nuevo nombre completo que reemplazará al registrado actualmente.
 * @property telefono Nuevo número de teléfono de contacto.
 * @property direccion Nueva dirección principal de entrega del cliente.
 */
data class ActualizarClienteRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("direccion") val direccion: String
)
