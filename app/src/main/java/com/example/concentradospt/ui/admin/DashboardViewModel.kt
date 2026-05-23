package com.example.concentradospt.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.network.admin.AdminApiService
import com.example.concentradospt.data.network.admin.AdminRetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Estado de la interfaz de usuario del panel principal (dashboard) administrativo.
 * Consolida los indicadores clave de negocio en un único objeto inmutable.
 *
 * @property isLoading Indica si los datos del panel están siendo cargados.
 * @property totalVentas Suma total en dinero de todas las ventas cargadas.
 * @property ventasPendientes Cantidad de ventas en estado "BORRADOR" pendientes de atención.
 * @property usuariosActivos Cantidad de empleados o administradores con estado activo.
 * @property error Mensaje de error descriptivo, o `null` si no hay errores.
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalVentas: Double = 0.0,
    val ventasPendientes: Int = 0,
    val usuariosActivos: Int = 0,
    val error: String? = null
)

/**
 * ViewModel que gestiona la carga de los indicadores del panel administrativo principal.
 * Realiza en paralelo dos solicitudes al backend:
 * - Lista de ventas recientes para calcular el total y las pendientes.
 * - Lista de usuarios para contar los que están activos.
 * Los resultados se consolidan en un único [DashboardUiState] expuesto mediante [state].
 */
class DashboardViewModel : ViewModel() {

    /** Servicio de API administrativo configurado con Retrofit y autenticación de empleado. */
    private val api = AdminRetrofitClient.create<AdminApiService>()

    /** Estado mutable interno del dashboard, modificable solo desde este ViewModel. */
    private val _state = MutableStateFlow(DashboardUiState())

    /** Estado público e inmutable del dashboard, observado por la UI. */
    val state: StateFlow<DashboardUiState> = _state

    init {
        loadDashboard()
    }

    /**
     * Carga en paralelo las ventas y los usuarios para calcular los indicadores del dashboard.
     * Las ventas en estado "BORRADOR" se cuentan como pendientes.
     * Los usuarios se filtran para contar solo los que tienen [isActive] en `true`.
     * En caso de error, emite un [DashboardUiState] con un mensaje descriptivo.
     */
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
