package com.example.concentradospt.ui.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.Pedido
import com.example.concentradospt.data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PedidosState {
    object Loading : PedidosState()
    data class Success(val pedidos: List<Pedido>) : PedidosState()
    data class Error(val message: String) : PedidosState()
}

class PedidosViewModel : ViewModel() {

    private val repository = PedidoRepository()

    private val _state = MutableStateFlow<PedidosState>(PedidosState.Loading)
    val state: StateFlow<PedidosState> = _state

    init {
        loadPedidos()
    }

    fun loadPedidos() {
        viewModelScope.launch {
            _state.value = PedidosState.Loading
            try {
                val pedidos = repository.getMisPedidos()
                _state.value = PedidosState.Success(pedidos.sortedByDescending { it.fechaCreacion })
            } catch (e: Exception) {
                _state.value = PedidosState.Error("No se pudieron cargar los pedidos")
            }
        }
    }
}
