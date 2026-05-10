package com.example.concentradospt.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class NeedsConfirmation(val email: String) : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val state: StateFlow<RegisterState> = _state

    fun signUp(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = RegisterState.Loading
            try {
                authRepository.signUp(email, password, fullName)
                _state.value = RegisterState.NeedsConfirmation(email)
            } catch (e: Exception) {
                _state.value = RegisterState.Error(friendlyError(e.message))
            }
        }
    }

    fun confirmSignUp(email: String, code: String) {
        viewModelScope.launch {
            _state.value = RegisterState.Loading
            try {
                authRepository.confirmSignUp(email, code)
                _state.value = RegisterState.Success
            } catch (e: Exception) {
                _state.value = RegisterState.Error(friendlyError(e.message))
            }
        }
    }

    private fun friendlyError(message: String?): String = when {
        message == null -> "Error desconocido"
        message.contains("UsernameExistsException") -> "Ya existe una cuenta con ese correo"
        message.contains("InvalidPasswordException") -> "La contraseña no cumple los requisitos de seguridad"
        message.contains("InvalidParameterException") -> "Correo electrónico inválido"
        message.contains("CodeMismatchException") -> "Código de verificación incorrecto"
        message.contains("ExpiredCodeException") -> "El código ha expirado. Solicita uno nuevo"
        message.contains("NetworkException") || message.contains("UnknownHostException") ->
            "Sin conexión a internet"
        else -> "Error en el registro. Intenta de nuevo"
    }
}