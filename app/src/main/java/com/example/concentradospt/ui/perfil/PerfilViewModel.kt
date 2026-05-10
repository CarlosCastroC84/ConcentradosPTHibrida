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
import retrofit2.HttpException

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

            var perfilException: Exception? = null
            val clienteDeferred = async {
                try {
                    clienteRepo.getMiPerfil()
                } catch (e: Exception) {
                    perfilException = e
                    null
                }
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

            val errorMessage = if (cliente == null) {
                when (val cause = perfilException) {
                    is HttpException -> when (cause.code()) {
                        401 -> "Error de autenticación. Cierra sesión y vuelve a ingresar."
                        403 -> "Tu cuenta aún no está activada en el sistema. Contacta al administrador."
                        404 -> "Perfil no encontrado. Contacta a soporte."
                        else -> "Error del servidor (${cause.code()}). Intenta de nuevo."
                    }
                    null -> "No se pudo cargar el perfil"
                    else -> "Error de conexión: ${cause.javaClass.simpleName}"
                }
            } else null

            _state.value = PerfilUiState(
                isLoading = false,
                cliente = cliente,
                emailFallback = emailFallback,
                recentOrders = pedidos
                    .sortedByDescending { it.fechaCreacion }
                    .take(3),
                error = errorMessage
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