package com.example.concentradospt.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.admin.Venta
import com.example.concentradospt.data.network.admin.AdminApiService
import com.example.concentradospt.data.network.admin.AdminRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DetalleVentaState {
    object Loading : DetalleVentaState()
    data class Success(val venta: Venta) : DetalleVentaState()
    data class Error(val message: String) : DetalleVentaState()
}

class DetalleVentaViewModel : ViewModel() {

    private val api = AdminRetrofitClient.create<AdminApiService>()

    private val _state = MutableStateFlow<DetalleVentaState>(DetalleVentaState.Loading)
    val state: StateFlow<DetalleVentaState> = _state

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
