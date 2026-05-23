package com.example.concentradospt.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.admin.Venta
import com.example.concentradospt.data.network.admin.AdminApiService
import com.example.concentradospt.data.network.admin.AdminRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Representa los posibles estados de la pantalla de listado de ventas del panel administrativo.
 * Es una clase sellada que permite manejar de forma exhaustiva cada variante en la UI.
 */
sealed class VentasUiState {
    /** Estado de carga mientras se obtiene la lista de ventas desde el backend. */
    object Loading : VentasUiState()

    /**
     * Estado de éxito con la lista de ventas recuperada del backend.
     * @property ventas Lista de ventas/pedidos registrados en el sistema.
     */
    data class Success(val ventas: List<Venta>) : VentasUiState()

    /**
     * Estado de error con un mensaje descriptivo del problema ocurrido.
     * @property message Mensaje legible por el usuario que describe el error.
     */
    data class Error(val message: String) : VentasUiState()
}

/**
 * ViewModel que gestiona la carga y exposición del listado de ventas del panel administrativo.
 * Recupera hasta 50 ventas de la primera página del backend mediante [AdminApiService]
 * y expone el resultado a la UI a través del [StateFlow] [uiState].
 */
class VentasViewModel : ViewModel() {

    /** Servicio de API administrativo configurado con Retrofit y autenticación de empleado. */
    private val api = AdminRetrofitClient.create<AdminApiService>()

    /** Estado mutable interno de la lista de ventas, modificable solo desde este ViewModel. */
    private val _uiState = MutableStateFlow<VentasUiState>(VentasUiState.Loading)

    /** Estado público e inmutable de la lista de ventas, observado por la UI. */
    val uiState: StateFlow<VentasUiState> = _uiState

    init { loadVentas() }

    /**
     * Carga la primera página de ventas del sistema (hasta 50 registros).
     * Actualiza [uiState] con la lista obtenida o con un error si la solicitud falla.
     */
    fun loadVentas() {
        viewModelScope.launch {
            _uiState.value = VentasUiState.Loading
            try {
                val page = api.getVentas(pagina = 0, tamano = 50)
                _uiState.value = VentasUiState.Success(page.content)
            } catch (e: Exception) {
                _uiState.value = VentasUiState.Error("No se pudieron cargar las ventas")
            }
        }
    }
}
