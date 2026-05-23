package com.example.concentradospt.ui.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.Pedido
import com.example.concentradospt.data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Representa los posibles estados de la pantalla de detalle de un pedido específico.
 * Es una clase sellada que permite manejar de forma exhaustiva cada variante en la UI.
 */
sealed class DetallePedidoState {
    /** Estado de carga mientras se obtiene el pedido desde el backend. */
    object Loading : DetallePedidoState()

    /**
     * Estado de éxito con el pedido recuperado exitosamente.
     * @property pedido Pedido completo con todos sus ítems y datos de entrega.
     */
    data class Success(val pedido: Pedido) : DetallePedidoState()

    /**
     * Estado de error con un mensaje descriptivo del problema ocurrido.
     * @property message Mensaje legible por el usuario que describe el error.
     */
    data class Error(val message: String) : DetallePedidoState()
}

/**
 * ViewModel que gestiona la obtención y exposición del detalle de un pedido
 * específico del cliente autenticado. Utiliza [PedidoRepository] para
 * recuperar el pedido por su identificador y expone el resultado mediante
 * un [StateFlow] observado por la UI.
 */
class DetallePedidoViewModel : ViewModel() {

    /** Repositorio encargado de las operaciones remotas relacionadas con pedidos. */
    private val repository = PedidoRepository()

    /** Estado mutable interno del detalle del pedido, modificable solo desde este ViewModel. */
    private val _state = MutableStateFlow<DetallePedidoState>(DetallePedidoState.Loading)

    /** Estado público e inmutable del detalle del pedido, observado por la UI. */
    val state: StateFlow<DetallePedidoState> = _state

    /**
     * Carga el detalle de un pedido específico identificado por su [id].
     * Actualiza el estado a [DetallePedidoState.Loading] durante la solicitud
     * y emite [DetallePedidoState.Success] o [DetallePedidoState.Error] según el resultado.
     *
     * @param id Identificador único del pedido que se desea consultar.
     */
    fun loadPedido(id: String) {
        viewModelScope.launch {
            _state.value = DetallePedidoState.Loading
            try {
                _state.value = DetallePedidoState.Success(repository.getMiPedido(id))
            } catch (e: Exception) {
                _state.value = DetallePedidoState.Error("No se pudo cargar el pedido")
            }
        }
    }
}
