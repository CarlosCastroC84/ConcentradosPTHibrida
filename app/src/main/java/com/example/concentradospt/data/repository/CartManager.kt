package com.example.concentradospt.data.repository

import com.example.concentradospt.data.model.CartItem
import com.example.concentradospt.data.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CartManager {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun addToCart(producto: Producto) {
        val current = _items.value
        // Usamos productoId si existe, de lo contrario usamos el nombre para distinguir
        val productIdToCompare = if (producto.productoId.isNotBlank()) producto.productoId else producto.nombre
        
        val idx = current.indexOfFirst { 
            val idInList = if (it.producto.productoId.isNotBlank()) it.producto.productoId else it.producto.nombre
            idInList == productIdToCompare 
        }

        _items.value = if (idx >= 0) {
            current.toMutableList().apply {
                this[idx] = current[idx].copy(cantidad = current[idx].cantidad + 1)
            }
        } else {
            current + CartItem(producto, cantidad = 1)
        }
    }

    fun increase(productoId: String) {
        _items.value = _items.value.map { item ->
            if (item.producto.productoId == productoId || item.producto.nombre == productoId) {
                item.copy(cantidad = item.cantidad + 1)
            } else item
        }
    }

    fun decrease(productoId: String) {
        val current = _items.value
        val item = current.find { it.producto.productoId == productoId || it.producto.nombre == productoId } ?: return
        
        _items.value = if (item.cantidad <= 1) {
            current.filter { it.producto.productoId != productoId && it.producto.nombre != productoId }
        } else {
            current.map { 
                if (it.producto.productoId == productoId || it.producto.nombre == productoId) {
                    it.copy(cantidad = it.cantidad - 1)
                } else it 
            }
        }
    }

    fun clear() {
        _items.value = emptyList()
    }
}