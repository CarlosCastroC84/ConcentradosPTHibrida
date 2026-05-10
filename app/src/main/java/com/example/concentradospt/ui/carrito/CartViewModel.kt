package com.example.concentradospt.ui.carrito

import androidx.lifecycle.ViewModel
import com.example.concentradospt.data.model.CartItem
import com.example.concentradospt.data.model.Producto
import com.example.concentradospt.data.repository.CartManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class CartViewModel : ViewModel() {

    val items: StateFlow<List<CartItem>> = CartManager.items

    val totalItems: Int get() = CartManager.items.value.sumOf { it.cantidad }
    val subtotal: Double get() = CartManager.items.value.sumOf { it.subtotal }
    val iva: Double get() = subtotal * 0.19
    val total: Double get() = subtotal + iva

    fun addToCart(producto: Producto) = CartManager.addToCart(producto)
    fun increase(productoId: String) = CartManager.increase(productoId)
    fun decrease(productoId: String) = CartManager.decrease(productoId)
    fun clear() = CartManager.clear()
}