package com.example.concentradospt.ui.carrito

import androidx.lifecycle.ViewModel
import com.example.concentradospt.data.model.CartItem
import com.example.concentradospt.data.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class CartViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items

    val totalItems: Int get() = _items.value.sumOf { it.cantidad }
    val subtotal: Double get() = _items.value.sumOf { it.subtotal }
    val iva: Double get() = subtotal * 0.19
    val total: Double get() = subtotal + iva

    fun addToCart(producto: Producto) {
        val current = _items.value
        val idx = current.indexOfFirst { it.producto.productoId == producto.productoId }
        _items.value = if (idx >= 0) {
            current.toMutableList().apply {
                this[idx] = current[idx].copy(cantidad = current[idx].cantidad + 1)
            }
        } else {
            current + CartItem(producto)
        }
    }

    fun increase(productoId: String) {
        _items.value = _items.value.map { item ->
            if (item.producto.productoId == productoId) item.copy(cantidad = item.cantidad + 1)
            else item
        }
    }

    fun decrease(productoId: String) {
        val current = _items.value
        val item = current.find { it.producto.productoId == productoId } ?: return
        _items.value = if (item.cantidad <= 1) {
            current.filter { it.producto.productoId != productoId }
        } else {
            current.map { if (it.producto.productoId == productoId) it.copy(cantidad = it.cantidad - 1) else it }
        }
    }

    fun clear() {
        _items.value = emptyList()
    }
}