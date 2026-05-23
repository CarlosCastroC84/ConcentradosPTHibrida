package com.example.concentradospt.ui.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.Pedido
import com.example.concentradospt.data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Representa los posibles estados de la pantalla de listado de pedidos del cliente.
 * Es una clase sellada que permite manejar de forma exhaustiva cada variante en la UI.
 */
sealed class PedidosState {
    /** Estado de carga mientras se obtienen los pedidos desde el backend. */
    object Loading : PedidosState()

    /**
     * Estado de éxito con la lista de pedidos recuperada y ordenada.
     * @property pedidos Lista de pedidos del usuario autenticado, ordenados por fecha descendente.
     */
    data class Success(val pedidos: List<Pedido>) : PedidosState()

    /**
     * Estado de error con un mensaje descriptivo del problema ocurrido.
     * @property message Mensaje legible por el usuario que describe el error.
     */
    data class Error(val message: String) : PedidosState()
}

/**
 * ViewModel que gestiona la obtención y exposición del historial de pedidos
 * del cliente autenticado. Utiliza [PedidoRepository] para comunicarse con el
 * backend y expone el resultado mediante un [StateFlow] observado por la UI.
 */
class PedidosViewModel : ViewModel() {

    /** Repositorio encargado de las operaciones remotas relacionadas con pedidos. */
    private val repository = PedidoRepository()

    /** Estado mutable interno de la lista de pedidos, modificable solo desde este ViewModel. */
    private val _state = MutableStateFlow<PedidosState>(PedidosState.Loading)

    /** Estado público e inmutable de los pedidos, observado por la UI. */
    val state: StateFlow<PedidosState> = _state

    init {
        loadPedidos()
    }

    /**
     * Carga el historial de pedidos del usuario autenticado desde el backend.
     * Los pedidos se ordenan de forma descendente por fecha de creación para
     * mostrar los más recientes primero. En caso de error actualiza el estado
     * con un mensaje descriptivo.
     */
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
