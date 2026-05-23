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
 * Representa los posibles estados de la pantalla de detalle de una venta específica.
 * Es una clase sellada que permite manejar de forma exhaustiva cada variante en la UI.
 */
sealed class DetalleVentaState {
    /** Estado de carga mientras se obtiene el detalle de la venta desde el backend. */
    object Loading : DetalleVentaState()

    /**
     * Estado de éxito con la venta recuperada exitosamente.
     * @property venta Objeto [Venta] con todos los datos e ítems de la venta consultada.
     */
    data class Success(val venta: Venta) : DetalleVentaState()

    /**
     * Estado de error con un mensaje descriptivo del problema ocurrido.
     * @property message Mensaje legible por el usuario que describe el error.
     */
    data class Error(val message: String) : DetalleVentaState()
}

/**
 * ViewModel que gestiona la carga y exposición del detalle de una venta específica
 * en el panel administrativo. Utiliza [AdminApiService] para recuperar la venta por
 * su identificador numérico y expone el resultado mediante un [StateFlow].
 */
class DetalleVentaViewModel : ViewModel() {

    /** Servicio de API administrativo configurado con Retrofit y autenticación de empleado. */
    private val api = AdminRetrofitClient.create<AdminApiService>()

    /** Estado mutable interno del detalle de la venta, modificable solo desde este ViewModel. */
    private val _state = MutableStateFlow<DetalleVentaState>(DetalleVentaState.Loading)

    /** Estado público e inmutable del detalle de la venta, observado por la UI. */
    val state: StateFlow<DetalleVentaState> = _state

    /**
     * Carga el detalle de una venta específica identificada por su [id].
     * Actualiza el estado a [DetalleVentaState.Loading] durante la solicitud
     * y emite [DetalleVentaState.Success] o [DetalleVentaState.Error] según el resultado.
     *
     * @param id Identificador numérico único de la venta que se desea consultar.
     */
    fun loadVenta(id: Long) {
        viewModelScope.launch {
            _state.value = DetalleVentaState.Loading
            try {
                _state.value = DetalleVentaState.Success(api.getVenta(id))
            } catch (e: Exception) {
                _state.value = DetalleVentaState.Error("No se pudo cargar la venta")
            }
        }
    }
}
