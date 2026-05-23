package com.example.concentradospt.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.Cliente
import com.example.concentradospt.data.model.Pedido
import com.example.concentradospt.data.repository.AuthRepository
import com.example.concentradospt.data.repository.ClienteRepository
import com.example.concentradospt.data.repository.PedidoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Estado de la interfaz de usuario de la pantalla de perfil del cliente.
 * Agrupa toda la información necesaria para renderizar la pantalla en un único objeto inmutable.
 *
 * @property isLoading Indica si se está cargando la información del perfil por primera vez.
 * @property isSaving Indica si se está procesando una actualización del perfil.
 * @property cliente Datos del perfil del cliente obtenidos desde el backend, o `null` si no se han cargado.
 * @property emailFallback Correo electrónico obtenido desde Cognito, usado como respaldo si no se carga el perfil.
 * @property recentOrders Últimos tres pedidos del usuario, ordenados por fecha descendente.
 * @property error Mensaje de error descriptivo, o `null` si no hay errores.
 * @property saveSuccess Bandera que indica que la actualización del perfil fue exitosa (se consume una sola vez).
 */
data class PerfilUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val cliente: Cliente? = null,
    val emailFallback: String? = null,
    val recentOrders: List<Pedido> = emptyList(),
    val error: String? = null,
    val saveSuccess: Boolean = false
)

/**
 * ViewModel que gestiona la carga y actualización del perfil del cliente autenticado.
 * Realiza tres solicitudes en paralelo al inicializar:
 * - Datos del perfil del cliente ([ClienteRepository]).
 * - Historial de pedidos recientes ([PedidoRepository]).
 * - Correo electrónico de Cognito como respaldo ([AuthRepository]).
 */
class PerfilViewModel : ViewModel() {

    /** Repositorio para operaciones sobre el perfil del cliente en el backend. */
    private val clienteRepo = ClienteRepository()

    /** Repositorio para operaciones sobre los pedidos del cliente. */
    private val pedidoRepo = PedidoRepository()

    /** Repositorio de autenticación para obtener datos del usuario en Cognito. */
    private val authRepo = AuthRepository()

    /** Estado mutable interno del perfil, modificable solo desde este ViewModel. */
    private val _state = MutableStateFlow(PerfilUiState())

    /** Estado público e inmutable del perfil, observado por la UI. */
    val state: StateFlow<PerfilUiState> = _state

    init {
        loadPerfil()
    }

    /**
     * Carga en paralelo el perfil del cliente, sus pedidos recientes y el email de Cognito.
     * Construye el estado de la UI consolidando los tres resultados.
     * Si el perfil no se puede cargar, genera un mensaje de error contextual
     * según el código HTTP recibido.
     */
    fun loadPerfil() {
        viewModelScope.launch {
            _state.value = PerfilUiState(isLoading = true)

            var perfilException: Exception? = null
            val clienteDeferred = async {
                try {
                    clienteRepo.getMiPerfil()
                } catch (e: Exception) {
                    perfilException = e
                    null
                }
            }
            val pedidosDeferred = async {
                runCatching { pedidoRepo.getMisPedidos() }.getOrElse { emptyList() }
            }
            val emailDeferred = async {
                runCatching { authRepo.getCurrentUserEmail() }.getOrNull()
            }

            val cliente = clienteDeferred.await()
            val pedidos = pedidosDeferred.await()
            val emailFallback = emailDeferred.await()

            val errorMessage = if (cliente == null) {
                when (val cause = perfilException) {
                    is HttpException -> when (cause.code()) {
                        401 -> "Error de autenticación. Cierra sesión y vuelve a ingresar."
                        403 -> "Tu cuenta aún no está activada en el sistema. Contacta al administrador."
                        404 -> "Perfil no encontrado. Contacta a soporte."
                        else -> "Error del servidor (${cause.code()}). Intenta de nuevo."
                    }
                    null -> "No se pudo cargar el perfil"
                    else -> "Error de conexión: ${cause.javaClass.simpleName}"
                }
            } else null

            _state.value = PerfilUiState(
                isLoading = false,
                cliente = cliente,
                emailFallback = emailFallback,
                recentOrders = pedidos
                    .sortedByDescending { it.fechaCreacion }
                    .take(3),
                error = errorMessage
            )
        }
    }

    /**
     * Envía al backend los datos actualizados del perfil del cliente.
     * Recorta los espacios en blanco antes de enviar cada campo.
     * Actualiza [PerfilUiState.saveSuccess] a `true` si la operación fue exitosa.
     *
     * @param nombre Nuevo nombre completo del cliente.
     * @param telefono Nuevo número de teléfono del cliente.
     * @param direccion Nueva dirección de entrega del cliente.
     */
    fun actualizarPerfil(nombre: String, telefono: String, direccion: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null, saveSuccess = false)
            val result = runCatching {
                clienteRepo.actualizarPerfil(nombre.trim(), telefono.trim(), direccion.trim())
            }
            result.onSuccess { clienteActualizado ->
                _state.value = _state.value.copy(
                    isSaving = false,
                    cliente = clienteActualizado,
                    saveSuccess = true
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isSaving = false,
                    saveSuccess = false,
                    error = "No se pudo actualizar el perfil"
                )
            }
        }
    }

    /**
     * Consume la bandera de éxito de guardado, restableciendo [PerfilUiState.saveSuccess] a `false`.
     * Debe llamarse desde la UI después de haber mostrado la confirmación al usuario,
     * para evitar que el evento se procese más de una vez.
     */
    fun consumeSaveSuccess() {
        _state.value = _state.value.copy(saveSuccess = false)
    }

    /**
     * Consume el error actual, restableciendo [PerfilUiState.error] a `null`.
     * Debe llamarse desde la UI después de haber mostrado el mensaje de error al usuario,
     * para evitar que el evento se procese más de una vez.
     */
    fun consumeError() {
        _state.value = _state.value.copy(error = null)
    }
}
