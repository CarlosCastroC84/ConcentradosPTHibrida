package com.example.concentradospt.ui.vendedor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.admin.Venta
import com.example.concentradospt.data.network.admin.AdminApiService
import com.example.concentradospt.data.network.admin.AdminRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Representa los posibles estados de la pantalla de pedidos del vendedor.
 * Es una clase sellada que permite manejar de forma exhaustiva cada variante en la UI.
 */
sealed class PedidosVendedorState {
    /** Estado de carga mientras se obtienen los pedidos desde el backend. */
    object Loading : PedidosVendedorState()

    /**
     * Estado de éxito con la lista de pedidos (posiblemente filtrada por estado).
     * @property pedidos Lista de ventas/pedidos que se mostrarán en la UI.
     */
    data class Success(val pedidos: List<Venta>) : PedidosVendedorState()

    /**
     * Estado de error con un mensaje descriptivo del problema ocurrido.
     * @property message Mensaje legible por el usuario que describe el error.
     */
    data class Error(val message: String) : PedidosVendedorState()
}

/**
 * ViewModel que gestiona la visualización y filtrado de pedidos para el rol de vendedor.
 * Carga hasta 100 pedidos del backend y los almacena en memoria ([allPedidos]) para
 * permitir el filtrado local por estado sin necesidad de realizar nuevas solicitudes de red.
 */
class PedidosVendedorViewModel : ViewModel() {

    /** Servicio de API administrativo configurado con Retrofit y autenticación de empleado. */
    private val api = AdminRetrofitClient.create<AdminApiService>()

    /** Estado mutable interno de la lista de pedidos, modificable solo desde este ViewModel. */
    private val _state = MutableStateFlow<PedidosVendedorState>(PedidosVendedorState.Loading)

    /** Estado público e inmutable de la lista de pedidos, observado por la UI. */
    val state: StateFlow<PedidosVendedorState> = _state

    /**
     * Copia completa e inmutable de todos los pedidos cargados desde el backend.
     * Sirve como fuente de verdad para aplicar filtros locales sin volver a consultar la red.
     */
    private var allPedidos: List<Venta> = emptyList()

    /**
     * Flujo mutable que transporta mensajes de resultado de operaciones sobre pedidos.
     * `null` indica que no hay mensaje pendiente.
     */
    private val _actionResult = MutableStateFlow<String?>(null)

    /** Flujo público que expone los mensajes de resultado de operaciones a la UI. */
    val actionResult: StateFlow<String?> = _actionResult

    init { loadPedidos() }

    /**
     * Carga todos los pedidos del sistema desde el backend (hasta 100 registros).
     * Almacena la lista completa en [allPedidos] y actualiza [state] con todos los pedidos.
     * En caso de error, emite [PedidosVendedorState.Error] con un mensaje descriptivo.
     */
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

    /**
     * Filtra la lista de pedidos almacenada en memoria por estado y actualiza [state].
     * No realiza nuevas solicitudes de red; opera sobre [allPedidos].
     * La comparación de estado es insensible a mayúsculas y minúsculas.
     *
     * @param estado Estado por el que se desea filtrar (p. ej. "PENDIENTE", "ENTREGADO"),
     *               o `null` para mostrar todos los pedidos sin filtrar.
     */
    fun filtrar(estado: String?) {
        val filtered = if (estado == null) allPedidos
        else allPedidos.filter { it.estado.equals(estado, ignoreCase = true) }
        _state.value = PedidosVendedorState.Success(filtered)
    }

    /**
     * Limpia el mensaje de resultado de la última acción sobre pedidos.
     * Debe llamarse desde la UI después de haber mostrado el mensaje al usuario.
     */
    fun clearActionResult() { _actionResult.value = null }
}
