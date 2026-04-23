package com.example.concentradospt.data.repository

import com.example.concentradospt.data.model.admin.AdminLoginRequest
import com.example.concentradospt.data.model.admin.AdminUsuarioInfo
import com.example.concentradospt.data.network.admin.AdminApiService
import com.example.concentradospt.data.network.admin.AdminRetrofitClient
import com.example.concentradospt.data.network.admin.TokenManager

class AdminAuthRepository {

    private val api = AdminRetrofitClient.create<AdminApiService>()

    suspend fun signIn(cedula: String, rol: String, pin: String): AdminUsuarioInfo {
        val response = api.login(AdminLoginRequest(cedula, rol, pin))
        TokenManager.saveTokens(response.accessToken, response.refreshToken)
        return response.usuario
    }

    suspend fun getMe(): AdminUsuarioInfo = api.getMe()

    fun signOut() = TokenManager.clearTokens()

    fun isSignedIn(): Boolean = TokenManager.isLoggedIn()

    fun getCurrentRol(): String? = TokenManager.getRolFromToken()
}
