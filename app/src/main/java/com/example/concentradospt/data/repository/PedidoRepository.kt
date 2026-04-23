package com.example.concentradospt.data.repository

import com.example.concentradospt.data.model.CrearPedidoRequest
import com.example.concentradospt.data.model.ItemPedido
import com.example.concentradospt.data.model.Pedido
import com.example.concentradospt.data.model.CartItem
import com.example.concentradospt.data.network.ApiService
import com.example.concentradospt.data.network.RetrofitClient

class PedidoRepository {
    private val api = RetrofitClient.create<ApiService>()

    suspend fun getMisPedidos(): List<Pedido> = api.getMisPedidos()

    suspend fun getMiPedido(id: String): Pedido = api.getMiPedido(id)

    suspend fun crearPedido(items: List<CartItem>, direccion: String): Pedido {
        val request = CrearPedidoRequest(
            items = items.map { cartItem ->
                ItemPedido(
                    productoId = cartItem.producto.productoId,
                    nombre = cartItem.producto.nombre,
                    cantidad = cartItem.cantidad,
                    precioUnitario = cartItem.producto.precio,
                    subtotal = cartItem.subtotal
                )
            },
            direccionEntrega = direccion
        )
        return api.crearPedido(request)
    }
}
