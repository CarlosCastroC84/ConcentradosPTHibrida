package com.example.concentradospt.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.admin.AdminUsuarioInfo
import com.example.concentradospt.data.repository.AdminAuthRepository
import com.example.concentradospt.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Representa los posibles estados de la pantalla de inicio de sesión.
 * Abarca tanto el flujo de clientes (Cognito) como el de empleados (Lightsail),
 * así como el flujo de recuperación de contraseña.
 */
sealed class LoginState {
    /** Estado inicial antes de que el usuario realice cualquier acción. */
    object Idle : LoginState()

    /** Estado de carga mientras se procesa una operación de autenticación. */
    object Loading : LoginState()

    /**
     * Inicio de sesión de cliente exitoso mediante Cognito.
     * @property isAdmin Indica si el usuario pertenece a un grupo administrativo en Cognito.
     */
    data class Success(val isAdmin: Boolean) : LoginState()

    /**
     * Inicio de sesión de empleado exitoso mediante el backend de Lightsail.
     * @property usuario Información del empleado autenticado incluyendo su rol.
     */
    data class AdminSuccess(val usuario: AdminUsuarioInfo) : LoginState()

    /**
     * Error general de autenticación.
     * @property message Mensaje amigable que describe el motivo del error.
     */
    data class Error(val message: String) : LoginState()

    /**
     * Estado intermedio del flujo de recuperación de contraseña que indica
     * que el código de verificación fue enviado al correo del usuario.
     * @property email Correo electrónico al que se envió el código.
     */
    data class ForgotPasswordCodeSent(val email: String) : LoginState()

    /** Recuperación de contraseña completada exitosamente. */
    object ForgotPasswordSuccess : LoginState()

    /**
     * Error ocurrido durante el flujo de recuperación de contraseña.
     * @property message Mensaje amigable que describe el motivo del error.
     */
    data class ForgotPasswordError(val message: String) : LoginState()
}

/**
 * ViewModel que gestiona toda la lógica de autenticación de la aplicación.
 * Soporta dos flujos de inicio de sesión:
 * - Clientes registrados usando AWS Cognito (email + contraseña).
 * - Empleados internos usando el backend de Lightsail (cédula + rol + PIN).
 * También gestiona el flujo completo de recuperación de contraseña.
 */
class LoginViewModel : ViewModel() {

    /** Repositorio de autenticación para clientes mediante AWS Cognito. */
    private val authRepository = AuthRepository()

    /** Repositorio de autenticación para empleados mediante el backend de Lightsail. */
    private val adminAuthRepository = AdminAuthRepository()

    /** Estado mutable interno del login, modificable solo desde este ViewModel. */
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)

    /** Estado público e inmutable del login, observado por la UI. */
    val state: StateFlow<LoginState> = _state

    /**
     * Verifica si existe una sesión activa al iniciar la aplicación.
     * Primero comprueba sesión de empleado (Lightsail) y luego sesión de cliente (Cognito).
     * Si encuentra sesión activa, emite el estado de éxito correspondiente;
     * de lo contrario, regresa al estado [LoginState.Idle].
     */
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
    /**
     * Autentica a un cliente registrado usando AWS Cognito.
     * Tras autenticarse, verifica los grupos del usuario para determinar
     * si tiene permisos administrativos.
     *
     * @param email Correo electrónico del cliente.
     * @param password Contraseña del cliente.
     */
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
    /**
     * Autentica a un empleado interno usando el backend de Lightsail.
     * Maneja errores HTTP con mensajes amigables según el código de respuesta.
     *
     * @param cedula Número de cédula del empleado.
     * @param rol Rol del empleado en el sistema (p. ej. ADMIN, VENTAS, BODEGA).
     * @param pin PIN de acceso asignado al empleado.
     */
    fun signInAdmin(cedula: String, rol: String, pin: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val usuario = adminAuthRepository.signIn(cedula, rol, pin)
                _state.value = LoginState.AdminSuccess(usuario)
            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                val errorBody = try { e.response()?.errorBody()?.string() ?: "" } catch (_: Exception) { "" }
                android.util.Log.e("LoginViewModel", "Admin login HTTP $code: $errorBody")
                _state.value = LoginState.Error(friendlyAdminError(code, errorBody))
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Admin login error: ${e.message}", e)
                _state.value = LoginState.Error(friendlyAdminError(message = e.message))
            }
        }
    }

    /**
     * Inicia el flujo de recuperación de contraseña enviando un código de verificación
     * al correo electrónico indicado mediante AWS Cognito.
     *
     * @param email Correo electrónico de la cuenta cuya contraseña se desea restablecer.
     */
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

    /**
     * Confirma el restablecimiento de contraseña usando el código enviado al correo.
     * Si el código y la nueva contraseña son válidos, emite [LoginState.ForgotPasswordSuccess].
     *
     * @param email Correo electrónico de la cuenta que se está recuperando.
     * @param code Código de verificación recibido por correo.
     * @param newPassword Nueva contraseña que el usuario desea establecer.
     */
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

    /**
     * Restablece el estado del login a [LoginState.Idle].
     * Se utiliza para limpiar el estado tras navegar de regreso o manejar un resultado en la UI.
     */
    fun resetToIdle() {
        _state.value = LoginState.Idle
    }

    /**
     * Convierte los mensajes de excepción de Cognito en mensajes amigables para el cliente.
     *
     * @param message Mensaje original de la excepción capturada.
     * @return Cadena de texto legible para mostrar al usuario.
     */
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

    /**
     * Convierte códigos de error HTTP o mensajes de excepción del backend de empleados
     * en mensajes amigables y orientativos para el usuario.
     *
     * @param code Código de respuesta HTTP, o `null` si el error no es HTTP.
     * @param body Cuerpo del error HTTP como texto, vacío por defecto.
     * @param message Mensaje de la excepción cuando el error no es HTTP.
     * @return Cadena de texto legible para mostrar al usuario.
     */
    private fun friendlyAdminError(code: Int? = null, body: String = "", message: String? = null): String {
        if (code != null) return when (code) {
            400 -> "Datos inválidos (400). Contacta soporte técnico."
            401 -> "Cédula, rol o PIN incorrectos"
            403 -> "Acceso denegado (403). Verifica que tu cuenta esté activa y el rol sea correcto"
            404 -> "Endpoint no encontrado (404). Revisa la URL del servidor"
            in 500..599 -> "El servidor no está disponible ($code). Intenta más tarde"
            else -> "Error del servidor ($code)"
        }
        return when {
            message == null -> "Error desconocido"
            message.contains("CLEARTEXT") || message.contains("SSL") ->
                "Error de conexión segura con el servidor"
            message.contains("connect") || message.contains("timeout") || message.contains("Unable to resolve") ->
                "No se pudo conectar al servidor. Verifica tu red"
            else -> "Error: ${message.takeLast(80)}"
        }
    }

    /**
     * Convierte mensajes de excepción del flujo de recuperación de contraseña
     * en mensajes amigables y orientativos para el usuario.
     *
     * @param message Mensaje original de la excepción capturada.
     * @return Cadena de texto legible para mostrar al usuario.
     */
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
