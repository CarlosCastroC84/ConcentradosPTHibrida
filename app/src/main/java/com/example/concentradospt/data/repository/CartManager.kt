package com.example.concentradospt.data.repository

import com.example.concentradospt.data.model.CartItem
import com.example.concentradospt.data.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor singleton del carrito de compras de la aplicación.
 *
 * Mantiene en memoria la lista de ítems del carrito mediante un [MutableStateFlow],
 * lo que permite a la interfaz de usuario observar los cambios de forma reactiva sin
 * necesidad de hacer polling. El estado se expone de forma inmutable como [StateFlow].
 *
 * Para identificar productos de forma unívoca se prioriza [Producto.productoId]; si este
 * campo está en blanco se utiliza [Producto.nombre] como identificador de respaldo.
 */
object CartManager {

    /**
     * Estado mutable interno que contiene la lista actual de ítems del carrito.
     * Solo es modificable desde dentro de este objeto.
     */
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())

    /**
     * Estado público e inmutable que expone la lista actual de ítems del carrito.
     * Los observadores (ViewModels, Composables) deben colectar este [StateFlow].
     */
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    /**
     * Añade un producto al carrito. Si el producto ya existe incrementa su cantidad en 1;
     * de lo contrario, agrega un nuevo [CartItem] con cantidad inicial de 1.
     *
     * La comparación se realiza usando [Producto.productoId] o, si está en blanco,
     * [Producto.nombre] como identificador de respaldo.
     *
     * @param producto El [Producto] a agregar o cuya cantidad se incrementará.
     */
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

    /**
     * Incrementa en 1 la cantidad del ítem identificado por [productoId].
     *
     * La búsqueda se realiza primero por [Producto.productoId] y, si no coincide,
     * por [Producto.nombre].
     *
     * @param productoId Identificador único del producto cuya cantidad se desea aumentar.
     */
    fun increase(productoId: String) {
        _items.value = _items.value.map { item ->
            if (item.producto.productoId == productoId || item.producto.nombre == productoId) {
                item.copy(cantidad = item.cantidad + 1)
            } else item
        }
    }

    /**
     * Decrementa en 1 la cantidad del ítem identificado por [productoId].
     * Si la cantidad llega a 1 y se decrementa, el ítem es eliminado del carrito.
     *
     * La búsqueda se realiza primero por [Producto.productoId] y, si no coincide,
     * por [Producto.nombre].
     *
     * @param productoId Identificador único del producto cuya cantidad se desea reducir.
     */
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

    /**
     * Vacía completamente el carrito eliminando todos los ítems.
     * Se usa típicamente tras confirmar un pedido exitoso.
     */
    fun clear() {
        _items.value = emptyList()
    }
}
