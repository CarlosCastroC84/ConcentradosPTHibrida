package com.example.concentradospt.data.model

import com.google.gson.annotations.SerializedName

data class Cliente(
    @SerializedName("clienteId") val clienteId: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("telefono") val telefono: String = "",
    @SerializedName("direccion") val direccion: String = "",
    @SerializedName("activo") val activo: Boolean = true
)

data class ActualizarClienteRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("direccion") val direccion: String
)