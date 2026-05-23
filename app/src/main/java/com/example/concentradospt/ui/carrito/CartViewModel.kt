package com.example.concentradospt.ui.carrito

import androidx.lifecycle.ViewModel
import com.example.concentradospt.data.model.CartItem
import com.example.concentradospt.data.model.Producto
import com.example.concentradospt.data.repository.CartManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * ViewModel que expone el estado del carrito de compras y delega
 * todas las operaciones de modificación al [CartManager] singleton.
 * Actúa como puente entre la UI del carrito y la fuente de verdad central
 * del carrito, calculando los totales derivados de forma reactiva.
 */
class CartViewModel : ViewModel() {

    /**
     * Flujo reactivo con la lista actual de ítems en el carrito.
     * Se actualiza automáticamente cada vez que [CartManager] modifica los ítems.
     */
    val items: StateFlow<List<CartItem>> = CartManager.items

    /**
     * Cantidad total de unidades sumando todas las líneas del carrito.
     * Se recalcula en cada acceso leyendo el valor actual del flujo.
     */
    val totalItems: Int get() = CartManager.items.value.sumOf { it.cantidad }

    /**
     * Subtotal del carrito sin impuestos, calculado como la suma de
     * los subtotales individuales de cada [CartItem].
     */
    val subtotal: Double get() = CartManager.items.value.sumOf { it.subtotal }

    /**
     * Valor del IVA aplicado al subtotal con una tasa del 19 %.
     */
    val iva: Double get() = subtotal * 0.19

    /**
     * Total final a pagar, equivalente al subtotal más el IVA.
     */
    val total: Double get() = subtotal + iva

    /**
     * Agrega un producto al carrito. Si ya existe incrementa su cantidad en uno.
     * @param producto Producto que se desea agregar al carrito.
     */
    fun addToCart(producto: Producto) = CartManager.addToCart(producto)

    /**
     * Incrementa en uno la cantidad del ítem identificado por [productoId].
     * @param productoId Identificador único del producto cuya cantidad se incrementa.
     */
    fun increase(productoId: String) = CartManager.increase(productoId)

    /**
     * Decrementa en uno la cantidad del ítem identificado por [productoId].
     * Si la cantidad llega a cero, el ítem es eliminado del carrito.
     * @param productoId Identificador único del producto cuya cantidad se decrementa.
     */
    fun decrease(productoId: String) = CartManager.decrease(productoId)

    /**
     * Vacía por completo el carrito eliminando todos los ítems.
     */
    fun clear() = CartManager.clear()
}
