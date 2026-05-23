package com.example.concentradospt.ui.checkout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.CartItem
import com.example.concentradospt.data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Representa los posibles estados de la pantalla de checkout (confirmación de pedido).
 * Es una clase sellada que garantiza exhaustividad al manejar cada estado posible en la UI.
 */
sealed class CheckoutState {
    /** Estado inicial, sin ninguna operación en curso. */
    object Idle : CheckoutState()

    /** Estado de carga mientras se procesa la creación del pedido. */
    object Loading : CheckoutState()

    /**
     * Estado de éxito cuando el pedido fue creado correctamente.
     * @property pedidoId Identificador único del pedido recién creado en el backend.
     */
    data class Success(val pedidoId: String) : CheckoutState()

    /**
     * Estado de error con un mensaje descriptivo del problema ocurrido.
     * @property message Mensaje legible por el usuario que describe el error.
     */
    data class Error(val message: String) : CheckoutState()
}

/**
 * ViewModel que gestiona la lógica de negocio del flujo de checkout.
 * Coordina la validación del formulario de entrega y la creación del pedido
 * a través del [PedidoRepository], exponiendo el resultado mediante un [StateFlow].
 */
class CheckoutViewModel : ViewModel() {

    /** Repositorio encargado de las operaciones remotas relacionadas con pedidos. */
    private val repository = PedidoRepository()

    /** Estado mutable interno del checkout, modificable solo desde este ViewModel. */
    private val _state = MutableStateFlow<CheckoutState>(CheckoutState.Idle)

    /** Estado público e inmutable del checkout, observado por la UI. */
    val state: StateFlow<CheckoutState> = _state

    /**
     * Inicia el proceso de confirmación de un pedido.
     * Primero valida el formulario y, si es correcto, envía el pedido al backend.
     * Maneja errores HTTP con mensajes amigables según el código de respuesta.
     *
     * @param items Lista de productos con sus cantidades que componen el pedido.
     * @param nombre Nombre completo del destinatario del pedido.
     * @param telefono Número de teléfono de contacto para la entrega.
     * @param direccion Dirección o municipio de entrega del pedido.
     */
    fun confirmarPedido(
        items: List<CartItem>,
        nombre: String,
        telefono: String,
        direccion: String
    ) {
        if (!validateForm(nombre, telefono, direccion)) return

        viewModelScope.launch {
            _state.value = CheckoutState.Loading
            try {
                val pedido = repository.crearPedido(
                    items = items,
                    fullName = nombre,
                    phone = telefono,
                    municipality = direccion
                )
                _state.value = CheckoutState.Success(pedido.pedidoId)
            } catch (e: HttpException) {
                val code = e.code()
                val body = e.response()?.errorBody()?.string() ?: ""
                Log.e("Checkout", "HTTP $code — $body")
                val msg = when (code) {
                    401 -> "Sesión expirada. Cierra sesión y vuelve a entrar."
                    403 -> "No tienes permiso para realizar pedidos."
                    400 -> "Datos del pedido incorrectos (HTTP 400)."
                    404 -> "Servicio de pedidos no disponible (HTTP 404)."
                    else -> "Error del servidor ($code). Intenta de nuevo."
                }
                _state.value = CheckoutState.Error(msg)
            } catch (e: Exception) {
                Log.e("Checkout", "Error inesperado: ${e.javaClass.simpleName} — ${e.message}")
                _state.value = CheckoutState.Error("Sin conexión o error inesperado: ${e.message?.take(80)}")
            }
        }
    }

    /**
     * Valida que los campos del formulario de entrega estén correctamente diligenciados.
     * En caso de error actualiza el estado con un mensaje descriptivo del campo faltante.
     *
     * @param nombre Nombre completo del destinatario.
     * @param telefono Número de teléfono de contacto.
     * @param direccion Dirección o municipio de entrega.
     * @return `true` si todos los campos son válidos, `false` en caso contrario.
     */
    private fun validateForm(nombre: String, telefono: String, direccion: String): Boolean {
        return when {
            nombre.isBlank() -> {
                _state.value = CheckoutState.Error("Ingresa tu nombre completo")
                false
            }
            telefono.isBlank() -> {
                _state.value = CheckoutState.Error("Ingresa tu número de teléfono")
                false
            }
            telefono.length < 7 -> {
                _state.value = CheckoutState.Error("Número de teléfono inválido")
                false
            }
            direccion.isBlank() -> {
                _state.value = CheckoutState.Error("Ingresa la dirección de entrega")
                false
            }
            else -> true
        }
    }

    /**
     * Restablece el estado del checkout a [CheckoutState.Idle].
     * Útil para limpiar el estado tras navegar de regreso a la pantalla de checkout
     * o después de manejar un error o éxito en la UI.
     */
    fun resetState() {
        _state.value = CheckoutState.Idle
    }
}
