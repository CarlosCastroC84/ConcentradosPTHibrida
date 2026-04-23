package com.example.concentradospt.data.network

import com.example.concentradospt.data.model.ActualizarClienteRequest
import com.example.concentradospt.data.model.AdminUsuario
import com.example.concentradospt.data.model.CategoriaProducto
import com.example.concentradospt.data.model.Cliente
import com.example.concentradospt.data.model.CrearPedidoRequest
import com.example.concentradospt.data.model.EstadoUsuarioRequest
import com.example.concentradospt.data.model.MarcaProducto
import com.example.concentradospt.data.model.Pedido
import com.example.concentradospt.data.model.PresentacionProducto
import com.example.concentradospt.data.model.Producto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // --- Productos (público) ---
    @GET("productos")
    suspend fun getProductos(): List<Producto>

    @GET("productos/{id}")
    suspend fun getProducto(@Path("id") id: String): Producto

    // --- Catálogo maestro (público) ---
    @GET("categorias-producto")
    suspend fun getCategorias(): List<CategoriaProducto>

    @GET("marcas-producto")
    suspend fun getMarcas(): List<MarcaProducto>

    @GET("presentaciones-producto")
    suspend fun getPresentaciones(): List<PresentacionProducto>

    // --- Perfil del cliente autenticado ---
    @GET("clientes/me")
    suspend fun getMiPerfil(): Cliente

    @PUT("clientes/me")
    suspend fun actualizarPerfil(@Body body: ActualizarClienteRequest): Cliente

    // --- Pedidos del cliente autenticado ---
    @GET("clientes/pedidos")
    suspend fun getMisPedidos(): List<Pedido>

    @GET("clientes/pedidos/{id}")
    suspend fun getMiPedido(@Path("id") id: String): Pedido

    @POST("clientes/pedidos")
    suspend fun crearPedido(@Body body: CrearPedidoRequest): Pedido

    // --- Admin: pedidos ---
    @GET("pedidos")
    suspend fun getPedidos(): List<Pedido>

    @PUT("pedidos/{id}")
    suspend fun actualizarPedido(@Path("id") id: String, @Body body: Pedido): Pedido

    // --- Admin: productos (CRUD) ---
    @POST("productos")
    suspend fun crearProducto(@Body body: Producto): Producto

    @PUT("productos/{id}")
    suspend fun actualizarProducto(@Path("id") id: String, @Body body: Producto): Producto

    @DELETE("productos/{id}")
    suspend fun eliminarProducto(@Path("id") id: String)

    // --- Admin: usuarios ---
    @GET("admin/usuarios")
    suspend fun getUsuarios(): List<AdminUsuario>

    @PATCH("admin/usuarios/{username}/estado")
    suspend fun cambiarEstadoUsuario(
        @Path("username") username: String,
        @Body body: EstadoUsuarioRequest
    ): AdminUsuario

    @POST("admin/usuarios/{username}/reset-password")
    suspend fun resetPasswordUsuario(@Path("username") username: String)
}