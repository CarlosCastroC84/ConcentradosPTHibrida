package com.example.concentradospt.data.model.admin

data class AdminLoginResponse(
    val accessToken: String = "",
    val refreshToken: String = "",
    val tipo: String = "Bearer",
    val usuario: AdminUsuarioInfo = AdminUsuarioInfo()
)

data class AdminRefreshRequest(val refreshToken: String)
