package com.example.concentradospt.data.model.admin

data class AdminLoginRequest(
    val cedula: String,
    val rol: String,
    val pin: String
)
