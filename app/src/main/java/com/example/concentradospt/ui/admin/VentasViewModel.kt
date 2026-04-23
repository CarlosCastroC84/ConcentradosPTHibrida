package com.example.concentradospt.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.admin.Venta
import com.example.concentradospt.data.network.admin.AdminApiService
import com.example.concentradospt.data.network.admin.AdminRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class VentasUiState {
    object Loading : VentasUiState()
    data class Success(val ventas: List<Venta>) : VentasUiState()
    data class Error(val message: String) : VentasUiState()
}

class VentasViewModel : ViewModel() {

    private val api = AdminRetrofitClient.create<AdminApiService>()

    private val _uiState = MutableStateFlow<VentasUiState>(VentasUiState.Loading)
    val uiState: StateFlow<VentasUiState> = _uiState

    init { loadVentas() }

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
