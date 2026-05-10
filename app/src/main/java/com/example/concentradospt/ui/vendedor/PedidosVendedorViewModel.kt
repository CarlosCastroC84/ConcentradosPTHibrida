package com.example.concentradospt.ui.vendedor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.admin.Venta
import com.example.concentradospt.data.network.admin.AdminApiService
import com.example.concentradospt.data.network.admin.AdminRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PedidosVendedorState {
    object Loading : PedidosVendedorState()
    data class Success(val pedidos: List<Venta>) : PedidosVendedorState()
    data class Error(val message: String) : PedidosVendedorState()
}

class PedidosVendedorViewModel : ViewModel() {

    private val api = AdminRetrofitClient.create<AdminApiService>()

    private val _state = MutableStateFlow<PedidosVendedorState>(PedidosVendedorState.Loading)
    val state: StateFlow<PedidosVendedorState> = _state

    private var allPedidos: List<Venta> = emptyList()

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult: StateFlow<String?> = _actionResult

    init { loadPedidos() }

    fun loadPedidos() {
        viewModelScope.launch {
            _state.value = PedidosVendedorState.Loading
            try {
                val page = api.getVentas(pagina = 0, tamano = 100)
                allPedidos = page.content
                _state.value = PedidosVendedorState.Success(allPedidos)
            } catch (e: Exception) {
                _state.value = PedidosVendedorState.Error("No se pudieron cargar los pedidos")
            }
        }
    }

    fun filtrar(estado: String?) {
        val filtered = if (estado == null) allPedidos
        else allPedidos.filter { it.estado.equals(estado, ignoreCase = true) }
        _state.value = PedidosVendedorState.Success(filtered)
    }

    fun clearActionResult() { _actionResult.value = null }
}
