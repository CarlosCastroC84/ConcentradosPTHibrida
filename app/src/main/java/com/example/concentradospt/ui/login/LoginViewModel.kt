package com.example.concentradospt.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.admin.AdminUsuarioInfo
import com.example.concentradospt.data.repository.AdminAuthRepository
import com.example.concentradospt.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val isAdmin: Boolean) : LoginState()
    data class AdminSuccess(val usuario: AdminUsuarioInfo) : LoginState()
    data class Error(val message: String) : LoginState()
    data class ForgotPasswordCodeSent(val email: String) : LoginState()
    object ForgotPasswordSuccess : LoginState()
    data class ForgotPasswordError(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val adminAuthRepository = AdminAuthRepository()

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun checkExistingSession() {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            // Primero verifica sesión admin activa
            if (adminAuthRepository.isSignedIn()) {
                val rol = adminAuthRepository.getCurrentRol() ?: "ADMIN"
                _state.value = LoginState.AdminSuccess(AdminUsuarioInfo(rol = rol))
                return@launch
            }
            // Luego verifica sesión cliente (Cognito)
            val signedIn = authRepository.isSignedIn()
            if (signedIn) {
                val groups = authRepository.getUserGroups()
                val isAdmin = groups.any { it in listOf("ADMIN", "GERENTE", "VENTAS", "BODEGA") }
                _state.value = LoginState.Success(isAdmin)
            } else {
                _state.value = LoginState.Idle
            }
        }
    }

    /** Login cliente con Cognito (email + contraseña) */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val result = authRepository.signIn(email, password)
                if (result.isSignedIn) {
                    val groups = authRepository.getUserGroups()
                    val isAdmin = groups.any { it in listOf("ADMIN", "GERENTE", "VENTAS", "BODEGA") }
                    _state.value = LoginState.Success(isAdmin)
                } else {
                    _state.value = LoginState.Error("Autenticación incompleta. Verifica tu correo.")
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error(friendlyClientError(e.message))
            }
        }
    }

    /** Login empleado con Lightsail (cédula + rol + PIN) */
    fun signInAdmin(cedula: String, rol: String, pin: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val usuario = adminAuthRepository.signIn(cedula, rol, pin)
                _state.value = LoginState.AdminSuccess(usuario)
            } catch (e: Exception) {
                _state.value = LoginState.Error(friendlyAdminError(e.message))
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                authRepository.forgotPassword(email)
                _state.value = LoginState.ForgotPasswordCodeSent(email)
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Error en forgotPassword: ${e.message}", e)
                _state.value = LoginState.ForgotPasswordError(friendlyResetError(e.message))
            }
        }
    }

    fun confirmForgotPassword(email: String, code: String, newPassword: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                authRepository.confirmForgotPassword(email, code, newPassword)
                _state.value = LoginState.ForgotPasswordSuccess
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Error en confirmForgotPassword: ${e.message}", e)
                _state.value = LoginState.ForgotPasswordError(friendlyResetError(e.message))
            }
        }
    }

    fun resetToIdle() {
        _state.value = LoginState.Idle
    }

    private fun friendlyClientError(message: String?): String = when {
        message == null -> "Error desconocido"
        message.contains("NotAuthorizedException") || message.contains("Incorrect") ->
            "Correo o contraseña incorrectos"
        message.contains("UserNotFoundException") ->
            "No existe una cuenta con ese correo"
        message.contains("UserNotConfirmedException") ->
            "Debes confirmar tu cuenta. Revisa tu correo"
        message.contains("NetworkException") || message.contains("UnknownHostException") ->
            "Sin conexión a internet"
        else -> "Error al iniciar sesión. Intenta de nuevo"
    }

    private fun friendlyAdminError(message: String?): String = when {
        message == null -> "Error desconocido"
        message.contains("401") || message.contains("Unauthorized") ->
            "Cédula, rol o PIN incorrectos"
        message.contains("connect") || message.contains("timeout") ->
            "No se pudo conectar al servidor"
        else -> "Error al iniciar sesión. Verifica tu conexión"
    }

    private fun friendlyResetError(message: String?): String = when {
        message == null -> "Error desconocido"
        message.contains("CodeMismatchException") -> "Código incorrecto. Verifica tu correo"
        message.contains("ExpiredCodeException") -> "El código ha expirado. Solicita uno nuevo"
        message.contains("LimitExceededException") -> "Demasiados intentos. Espera unos minutos"
        message.contains("UserNotFoundException") -> "No existe una cuenta con ese correo"
        message.contains("UserNotConfirmedException") -> "La cuenta no ha sido confirmada aún"
        message.contains("InvalidPasswordException") -> "La contraseña no cumple los requisitos de seguridad"
        message.contains("InvalidParameterException") -> "Correo electrónico inválido"
        message.contains("NetworkException") || message.contains("UnknownHostException") -> "Sin conexión a internet"
        else -> "Error: $message"
    }
}
