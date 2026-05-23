package com.example.concentradospt.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.admin.AdminUsuarioInfo
import com.example.concentradospt.data.network.admin.AdminApiService
import com.example.concentradospt.data.network.admin.AdminRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Representa los posibles estados de la pantalla de gestión de usuarios del panel administrativo.
 * Es una clase sellada que permite manejar de forma exhaustiva cada variante en la UI.
 */
sealed class GestionUsuariosState {
    /** Estado de carga mientras se obtiene la lista de usuarios desde el backend. */
    object Loading : GestionUsuariosState()

    /**
     * Estado de éxito con la lista de usuarios del sistema.
     * @property usuarios Lista de empleados y administradores registrados en el backend.
     */
    data class Success(val usuarios: List<AdminUsuarioInfo>) : GestionUsuariosState()

    /**
     * Estado de error con un mensaje descriptivo del problema ocurrido.
     * @property message Mensaje legible por el usuario que describe el error.
     */
    data class Error(val message: String) : GestionUsuariosState()
}

/**
 * ViewModel que gestiona el CRUD de usuarios del panel administrativo.
 * Soporta operaciones de carga, creación, edición, eliminación, cambio de estado
 * y regeneración de PIN de empleados.
 * Las operaciones de creación y edición se aplican optimistamente sobre la lista local
 * sin esperar sincronización completa con el backend.
 */
class GestionUsuariosViewModel : ViewModel() {

    /** Servicio de API administrativo configurado con Retrofit y autenticación de empleado. */
    private val api = AdminRetrofitClient.create<AdminApiService>()

    /** Estado mutable interno de la lista de usuarios, modificable solo desde este ViewModel. */
    private val _state = MutableStateFlow<GestionUsuariosState>(GestionUsuariosState.Loading)

    /** Estado público e inmutable de la lista de usuarios, observado por la UI. */
    val state: StateFlow<GestionUsuariosState> = _state

    /**
     * Flujo mutable que transporta mensajes de resultado de operaciones CRUD.
     * `null` indica que no hay mensaje pendiente.
     */
    private val _actionResult = MutableStateFlow<String?>(null)

    /** Flujo público que expone los mensajes de resultado de operaciones CRUD a la UI. */
    val actionResult: StateFlow<String?> = _actionResult

    init { loadUsuarios() }

    /**
     * Carga la lista completa de usuarios del sistema desde el backend.
     * Actualiza [state] con la lista obtenida o con un error si la solicitud falla.
     */
    fun loadUsuarios() {
        viewModelScope.launch {
            _state.value = GestionUsuariosState.Loading
            try {
                _state.value = GestionUsuariosState.Success(api.getUsuarios())
            } catch (e: Exception) {
                _state.value = GestionUsuariosState.Error("No se pudo cargar los usuarios")
            }
        }
    }

    /**
     * Cambia el estado activo/inactivo del usuario indicado en el backend y recarga la lista.
     * Si el usuario está activo lo desactiva, y viceversa.
     * Actualiza [actionResult] con el resultado de la operación.
     *
     * @param usuario Usuario cuyo estado se desea alternar.
     */
    fun toggleEstado(usuario: AdminUsuarioInfo) {
        viewModelScope.launch {
            try {
                val nuevoEstado = if (usuario.isActive) "INACTIVO" else "ACTIVO"
                api.cambiarEstadoUsuario(usuario.id, mapOf("estado" to nuevoEstado))
                val accion = if (usuario.isActive) "desactivado" else "activado"
                _actionResult.value = "Usuario $accion"
                loadUsuarios()
            } catch (e: Exception) {
                _actionResult.value = "Error al cambiar el estado del usuario"
            }
        }
    }

    /**
     * Solicita al backend la regeneración del PIN de acceso del usuario indicado.
     * El nuevo PIN se enviará al correo electrónico del usuario.
     * Actualiza [actionResult] con el resultado de la operación.
     *
     * @param usuario Usuario al que se le regenerará el PIN.
     */
    fun regenerarPin(usuario: AdminUsuarioInfo) {
        viewModelScope.launch {
            try {
                api.regenerarPin(usuario.id)
                _actionResult.value = "PIN regenerado. El usuario recibirá un correo."
            } catch (e: Exception) {
                _actionResult.value = "Error al regenerar el PIN"
            }
        }
    }

    /**
     * Agrega un nuevo usuario a la lista de forma optimista (sin sincronización inmediata con el backend).
     * El usuario se inserta al inicio de la lista con un ID temporal basado en el tiempo actual.
     * Actualiza [actionResult] con el resultado de la operación.
     *
     * @param nuevo Datos del nuevo usuario que se desea agregar.
     */
    fun crearUsuario(nuevo: AdminUsuarioInfo) {
        viewModelScope.launch {
            try {
                val current = (_state.value as? GestionUsuariosState.Success)?.usuarios?.toMutableList()
                    ?: mutableListOf()
                val withId = nuevo.copy(id = System.currentTimeMillis())
                current.add(0, withId)
                _state.value = GestionUsuariosState.Success(current)
                _actionResult.value = "Usuario ${nuevo.nombreCompleto} creado (pendiente sincronización)"
            } catch (e: Exception) {
                _actionResult.value = "Error al crear el usuario"
            }
        }
    }

    /**
     * Actualiza los datos de un usuario existente en la lista local de forma optimista.
     * Reemplaza el usuario con el mismo `id` en la lista actual.
     * Actualiza [actionResult] con el resultado de la operación.
     *
     * @param actualizado Objeto [AdminUsuarioInfo] con los datos actualizados del usuario.
     */
    fun editarUsuario(actualizado: AdminUsuarioInfo) {
        viewModelScope.launch {
            try {
                val current = (_state.value as? GestionUsuariosState.Success)?.usuarios?.toMutableList()
                    ?: mutableListOf()
                val idx = current.indexOfFirst { it.id == actualizado.id }
                if (idx >= 0) current[idx] = actualizado
                _state.value = GestionUsuariosState.Success(current)
                _actionResult.value = "Usuario actualizado"
            } catch (e: Exception) {
                _actionResult.value = "Error al actualizar el usuario"
            }
        }
    }

    /**
     * Elimina un usuario de la lista local de forma optimista, identificándolo por su `id`.
     * Actualiza [actionResult] con el nombre o cédula del usuario eliminado.
     *
     * @param usuario Usuario que se desea eliminar de la lista.
     */
    fun eliminarUsuario(usuario: AdminUsuarioInfo) {
        viewModelScope.launch {
            try {
                val current = (_state.value as? GestionUsuariosState.Success)?.usuarios?.toMutableList()
                    ?: mutableListOf()
                current.removeAll { it.id == usuario.id }
                _state.value = GestionUsuariosState.Success(current)
                _actionResult.value = "Usuario ${usuario.nombreCompleto.ifBlank { usuario.cedula }} eliminado"
            } catch (e: Exception) {
                _actionResult.value = "Error al eliminar el usuario"
            }
        }
    }

    /**
     * Limpia el mensaje de resultado de la última acción CRUD.
     * Debe llamarse desde la UI después de haber mostrado el mensaje al usuario.
     */
    fun clearActionResult() { _actionResult.value = null }
}
