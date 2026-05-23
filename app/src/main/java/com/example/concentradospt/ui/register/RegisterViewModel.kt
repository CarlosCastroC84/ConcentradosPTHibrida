package com.example.concentradospt.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Representa los posibles estados de la pantalla de registro de nuevos clientes.
 * Es una clase sellada que permite manejar de forma exhaustiva cada variante en la UI.
 */
sealed class RegisterState {
    /** Estado inicial antes de que el usuario inicie el proceso de registro. */
    object Idle : RegisterState()

    /** Estado de carga mientras se procesa la solicitud de registro o confirmación. */
    object Loading : RegisterState()

    /**
     * Estado intermedio que indica que el registro fue aceptado por Cognito
     * y se requiere confirmación del correo electrónico mediante un código.
     * @property email Correo electrónico al que se envió el código de confirmación.
     */
    data class NeedsConfirmation(val email: String) : RegisterState()

    /** Estado de éxito cuando la cuenta ha sido confirmada y está lista para usar. */
    object Success : RegisterState()

    /**
     * Estado de error con un mensaje descriptivo del problema ocurrido.
     * @property message Mensaje legible por el usuario que describe el error.
     */
    data class Error(val message: String) : RegisterState()
}

/**
 * ViewModel que gestiona el flujo de registro de nuevos clientes usando AWS Cognito.
 * Coordina el proceso en dos pasos:
 * 1. Registro inicial del usuario (email, contraseña y nombre).
 * 2. Confirmación del correo mediante el código enviado por Cognito.
 */
class RegisterViewModel : ViewModel() {

    /** Repositorio de autenticación que encapsula las operaciones de AWS Cognito. */
    private val authRepository = AuthRepository()

    /** Estado mutable interno del registro, modificable solo desde este ViewModel. */
    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)

    /** Estado público e inmutable del registro, observado por la UI. */
    val state: StateFlow<RegisterState> = _state

    /**
     * Inicia el proceso de registro de un nuevo cliente en AWS Cognito.
     * Si el registro es exitoso, emite [RegisterState.NeedsConfirmation] para
     * que la UI solicite el código de verificación al usuario.
     *
     * @param fullName Nombre completo del nuevo usuario.
     * @param email Correo electrónico que se usará como identificador de la cuenta.
     * @param password Contraseña elegida por el usuario (debe cumplir la política de Cognito).
     */
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

    /**
     * Confirma el registro del usuario usando el código de verificación enviado por correo.
     * Si el código es válido, emite [RegisterState.Success] y la cuenta queda activa.
     *
     * @param email Correo electrónico de la cuenta que se está confirmando.
     * @param code Código de verificación de 6 dígitos recibido por correo.
     */
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

    /**
     * Convierte los mensajes de excepción de Cognito en mensajes amigables para el usuario.
     *
     * @param message Mensaje original de la excepción capturada.
     * @return Cadena de texto legible para mostrar al usuario.
     */
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
