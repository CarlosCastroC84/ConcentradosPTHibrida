package com.example.concentradospt.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.Cliente
import com.example.concentradospt.data.model.Pedido
import com.example.concentradospt.data.repository.AuthRepository
import com.example.concentradospt.data.repository.ClienteRepository
import com.example.concentradospt.data.repository.PedidoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PerfilUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val cliente: Cliente? = null,
    val emailFallback: String? = null,
    val recentOrders: List<Pedido> = emptyList(),
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class PerfilViewModel : ViewModel() {

    private val clienteRepo = ClienteRepository()
    private val pedidoRepo = PedidoRepository()
    private val authRepo = AuthRepository()

    private val _state = MutableStateFlow(PerfilUiState())
    val state: StateFlow<PerfilUiState> = _state

    init {
        loadPerfil()
    }

    fun loadPerfil() {
        viewModelScope.launch {
            _state.value = PerfilUiState(isLoading = true)

            val clienteDeferred = async {
                runCatching { clienteRepo.getMiPerfil() }.getOrNull()
            }
            val pedidosDeferred = async {
                runCatching { pedidoRepo.getMisPedidos() }.getOrElse { emptyList() }
            }
            val emailDeferred = async {
                runCatching { authRepo.getCurrentUserEmail() }.getOrNull()
            }

            val cliente = clienteDeferred.await()
            val pedidos = pedidosDeferred.await()
            val emailFallback = emailDeferred.await()

            _state.value = PerfilUiState(
                isLoading = false,
                cliente = cliente,
                emailFallback = emailFallback,
                recentOrders = pedidos
                    .sortedByDescending { it.fechaCreacion }
                    .take(3),
                error = if (cliente == null) "No se pudo cargar el perfil" else null
            )
        }
    }

    fun actualizarPerfil(nombre: String, telefono: String, direccion: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null, saveSuccess = false)
            val result = runCatching {
                clienteRepo.actualizarPerfil(nombre.trim(), telefono.trim(), direccion.trim())
            }
            result.onSuccess { clienteActualizado ->
                _state.value = _state.value.copy(
                    isSaving = false,
                    cliente = clienteActualizado,
                    saveSuccess = true
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isSaving = false,
                    saveSuccess = false,
                    error = "No se pudo actualizar el perfil"
                )
            }
        }
    }

    fun consumeSaveSuccess() {
        _state.value = _state.value.copy(saveSuccess = false)
    }

    fun consumeError() {
        _state.value = _state.value.copy(error = null)
    }
}