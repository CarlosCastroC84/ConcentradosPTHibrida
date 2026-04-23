package com.example.concentradospt.data.repository

import com.example.concentradospt.data.model.AdminUsuario
import com.example.concentradospt.data.model.EstadoUsuarioRequest
import com.example.concentradospt.data.model.Pedido
import com.example.concentradospt.data.model.Producto
import com.example.concentradospt.data.network.ApiService
import com.example.concentradospt.data.network.RetrofitClient

class AdminRepository {
    private val api = RetrofitClient.create<ApiService>()

    // Pedidos admin
    suspend fun getPedidos(): List<Pedido> = api.getPedidos()
    suspend fun actualizarPedido(id: String, pedido: Pedido): Pedido = api.actualizarPedido(id, pedido)

    // Productos CRUD
    suspend fun getProductos(): List<Producto> = api.getProductos()
    suspend fun crearProducto(producto: Producto): Producto = api.crearProducto(producto)
    suspend fun actualizarProducto(producto: Producto): Producto =
        api.actualizarProducto(producto.productoId, producto)
    suspend fun eliminarProducto(id: String) = api.eliminarProducto(id)

    // Usuarios admin
    suspend fun getUsuarios(): List<AdminUsuario> = api.getUsuarios()
    suspend fun cambiarEstado(username: String, activo: Boolean): AdminUsuario =
        api.cambiarEstadoUsuario(username, EstadoUsuarioRequest(if (activo) "ACTIVO" else "INACTIVO"))
    suspend fun resetPassword(username: String) = api.resetPasswordUsuario(username)
}
