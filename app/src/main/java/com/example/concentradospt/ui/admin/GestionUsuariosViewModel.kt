package com.example.concentradospt.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.admin.AdminUsuarioInfo
import com.example.concentradospt.data.network.admin.AdminApiService
import com.example.concentradospt.data.network.admin.AdminRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class GestionUsuariosState {
    object Loading : GestionUsuariosState()
    data class Success(val usuarios: List<AdminUsuarioInfo>) : GestionUsuariosState()
    data class Error(val message: String) : GestionUsuariosState()
}

class GestionUsuariosViewModel : ViewModel() {

    private val api = AdminRetrofitClient.create<AdminApiService>()

    private val _state = MutableStateFlow<GestionUsuariosState>(GestionUsuariosState.Loading)
    val state: StateFlow<GestionUsuariosState> = _state

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult: StateFlow<String?> = _actionResult

    init { loadUsuarios() }

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

    fun clearActionResult() { _actionResult.value = null }
}
