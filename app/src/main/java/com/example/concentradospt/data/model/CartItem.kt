package com.example.concentradospt.data.model

/**
 * Representa un ítem dentro del carrito de compras del usuario.
 * Asocia un producto con la cantidad seleccionada y calcula automáticamente el subtotal.
 *
 * @property producto Producto agregado al carrito con toda su información (precio, nombre, etc.).
 * @property cantidad Número de unidades del producto que el usuario desea comprar. Por defecto es 1.
 */
data class CartItem(
    val producto: Producto,
    val cantidad: Int = 1
) {
    /**
     * Calcula el subtotal de este ítem multiplicando el precio unitario del producto
     * por la cantidad seleccionada.
     */
    val subtotal: Double get() = producto.precio * cantidad
}
