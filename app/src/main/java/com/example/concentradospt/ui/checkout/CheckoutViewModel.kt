package com.example.concentradospt.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.CartItem
import com.example.concentradospt.data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Loading : CheckoutState()
    data class Success(val pedidoId: String) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

class CheckoutViewModel : ViewModel() {

    private val repository = PedidoRepository()

    private val _state = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val state: StateFlow<CheckoutState> = _state

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
                    direccion = buildString {
                        append(direccion)
                        append(" | Contacto: $nombre")
                        append(" | Tel: $telefono")
                    }
                )
                _state.value = CheckoutState.Success(pedido.pedidoId)
            } catch (e: Exception) {
                _state.value = CheckoutState.Error("No se pudo confirmar el pedido. Intenta de nuevo.")
            }
        }
    }

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

    fun resetState() {
        _state.value = CheckoutState.Idle
    }
}
