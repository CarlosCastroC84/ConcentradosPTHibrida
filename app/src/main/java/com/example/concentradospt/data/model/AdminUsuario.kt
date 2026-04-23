package com.example.concentradospt.data.model

import com.google.gson.annotations.SerializedName

data class AdminUsuario(
    @SerializedName("username") val username: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("estado") val estado: String = "ACTIVO",
    @SerializedName("grupos") val grupos: List<String> = emptyList()
) {
    val isActive: Boolean get() = estado.equals("ACTIVO", ignoreCase = true)
    val rolDisplay: String get() = grupos.firstOrNull() ?: "CLIENTE"
}

data class EstadoUsuarioRequest(
    @SerializedName("estado") val estado: String
)
