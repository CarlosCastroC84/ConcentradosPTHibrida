package com.example.concentradospt.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.network.admin.AdminApiService
import com.example.concentradospt.data.network.admin.AdminRetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalVentas: Double = 0.0,
    val ventasPendientes: Int = 0,
    val usuariosActivos: Int = 0,
    val error: String? = null
)

class DashboardViewModel : ViewModel() {

    private val api = AdminRetrofitClient.create<AdminApiService>()

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _state.value = DashboardUiState(isLoading = true)
            try {
                val ventasDeferred = async {
                    runCatching { api.getVentas(pagina = 0, tamano = 20) }.getOrNull()
                }
                val usuariosDeferred = async {
                    runCatching { api.getUsuarios() }.getOrElse { emptyList() }
                }

                val ventasPage = ventasDeferred.await()
                val usuarios = usuariosDeferred.await()
                val ventas = ventasPage?.content ?: emptyList()

                _state.value = DashboardUiState(
                    isLoading = false,
                    totalVentas = ventas.sumOf { it.total },
                    ventasPendientes = ventas.count {
                        it.estado.equals("BORRADOR", ignoreCase = true)
                    },
                    usuariosActivos = usuarios.count { it.isActive }
                )
            } catch (e: Exception) {
                _state.value = DashboardUiState(
                    isLoading = false,
                    error = "No se pudo cargar el panel. Verifica tu conexión."
                )
            }
        }
    }
}
