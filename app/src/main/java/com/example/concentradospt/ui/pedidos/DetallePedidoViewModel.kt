package com.example.concentradospt.ui.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.Pedido
import com.example.concentradospt.data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DetallePedidoState {
    object Loading : DetallePedidoState()
    data class Success(val pedido: Pedido) : DetallePedidoState()
    data class Error(val message: String) : DetallePedidoState()
}

class DetallePedidoViewModel : ViewModel() {

    private val repository = PedidoRepository()

    private val _state = MutableStateFlow<DetallePedidoState>(DetallePedidoState.Loading)
    val state: StateFlow<DetallePedidoState> = _state

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