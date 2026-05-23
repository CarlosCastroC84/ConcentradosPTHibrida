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

/**
 * Interfaz de servicio Retrofit que define todos los endpoints disponibles en el API Gateway
 * de AWS conectado al backend Spring Boot.
 *
 * Los métodos se agrupan por dominio funcional:
 * - **Productos**: consulta del catálogo público de productos.
 * - **Catálogo maestro**: listas de categorías, marcas y presentaciones.
 * - **Perfil del cliente**: lectura y actualización del perfil del usuario autenticado.
 * - **Pedidos del cliente**: historial y creación de pedidos propios.
 * - **Admin – pedidos**: gestión global de pedidos por parte del administrador.
 * - **Admin – productos**: operaciones CRUD sobre el catálogo de productos.
 * - **Admin – usuarios**: consulta y gestión de usuarios de Cognito.
 *
 * Todos los métodos son `suspend` y deben invocarse desde una corrutina o un ámbito
 * compatible con corrutinas.
 */
interface ApiService {

    // --- Productos (público) ---

    /**
     * Obtiene la lista completa de productos disponibles en el catálogo.
     *
     * @return Lista de [Producto] con todos los artículos del catálogo.
     */
    @GET("productos")
    suspend fun getProductos(): List<Producto>

    /**
     * Obtiene el detalle de un producto específico por su identificador.
     *
     * @param id Identificador único del producto.
     * @return El [Producto] correspondiente al [id] indicado.
     */
    @GET("productos/{id}")
    suspend fun getProducto(@Path("id") id: String): Producto

    // --- Catálogo maestro (público) ---

    /**
     * Obtiene la lista de categorías de productos disponibles en el sistema.
     *
     * @return Lista de [CategoriaProducto] con todas las categorías registradas.
     */
    @GET("categorias-producto")
    suspend fun getCategorias(): List<CategoriaProducto>

    /**
     * Obtiene la lista de marcas de productos disponibles en el sistema.
     *
     * @return Lista de [MarcaProducto] con todas las marcas registradas.
     */
    @GET("marcas-producto")
    suspend fun getMarcas(): List<MarcaProducto>

    /**
     * Obtiene la lista de presentaciones (formatos/tamaños) de productos disponibles.
     *
     * @return Lista de [PresentacionProducto] con todas las presentaciones registradas.
     */
    @GET("presentaciones-producto")
    suspend fun getPresentaciones(): List<PresentacionProducto>

    // --- Perfil del cliente autenticado ---

    /**
     * Obtiene el perfil del cliente actualmente autenticado mediante el token de Cognito.
     *
     * @return El objeto [Cliente] asociado al usuario autenticado.
     */
    @GET("clientes/me")
    suspend fun getMiPerfil(): Cliente

    /**
     * Actualiza los datos del perfil del cliente autenticado.
     *
     * @param body Objeto [ActualizarClienteRequest] con los campos a actualizar.
     * @return El objeto [Cliente] con los datos ya actualizados.
     */
    @PUT("clientes/me")
    suspend fun actualizarPerfil(@Body body: ActualizarClienteRequest): Cliente

    // --- Pedidos del cliente autenticado ---

    /**
     * Obtiene el historial de pedidos del cliente autenticado.
     *
     * @return Lista de [Pedido] realizados por el usuario autenticado.
     */
    @GET("clientes/pedidos")
    suspend fun getMisPedidos(): List<Pedido>

    /**
     * Obtiene el detalle de un pedido específico del cliente autenticado.
     *
     * @param id Identificador único del pedido.
     * @return El [Pedido] correspondiente al [id] indicado.
     */
    @GET("clientes/pedidos/{id}")
    suspend fun getMiPedido(@Path("id") id: String): Pedido

    /**
     * Crea un nuevo pedido para el cliente autenticado.
     *
     * @param body Objeto [CrearPedidoRequest] con los ítems del carrito y el snapshot
     *             de datos de entrega.
     * @return El [Pedido] creado con su identificador y estado inicial asignados por el backend.
     */
    @POST("clientes/pedidos")
    suspend fun crearPedido(@Body body: CrearPedidoRequest): Pedido

    // --- Admin: pedidos ---

    /**
     * Obtiene la lista global de todos los pedidos del sistema (solo administradores).
     *
     * @return Lista de [Pedido] de todos los clientes.
     */
    @GET("pedidos")
    suspend fun getPedidos(): List<Pedido>

    /**
     * Actualiza los datos de un pedido existente (solo administradores).
     *
     * @param id Identificador único del pedido a actualizar.
     * @param body Objeto [Pedido] con los campos modificados.
     * @return El [Pedido] actualizado.
     */
    @PUT("pedidos/{id}")
    suspend fun actualizarPedido(@Path("id") id: String, @Body body: Pedido): Pedido

    // --- Admin: productos (CRUD) ---

    /**
     * Crea un nuevo producto en el catálogo (solo administradores).
     *
     * @param body Objeto [Producto] con los datos del nuevo artículo.
     * @return El [Producto] creado con su identificador asignado por el backend.
     */
    @POST("productos")
    suspend fun crearProducto(@Body body: Producto): Producto

    /**
     * Actualiza los datos de un producto existente en el catálogo (solo administradores).
     *
     * @param id Identificador único del producto a actualizar.
     * @param body Objeto [Producto] con los campos modificados.
     * @return El [Producto] actualizado.
     */
    @PUT("productos/{id}")
    suspend fun actualizarProducto(@Path("id") id: String, @Body body: Producto): Producto

    /**
     * Elimina un producto del catálogo de forma permanente (solo administradores).
     *
     * @param id Identificador único del producto a eliminar.
     */
    @DELETE("productos/{id}")
    suspend fun eliminarProducto(@Path("id") id: String)

    // --- Admin: usuarios ---

    /**
     * Obtiene la lista de todos los usuarios registrados en el sistema (solo administradores).
     *
     * @return Lista de [AdminUsuario] con la información de cada usuario.
     */
    @GET("admin/usuarios")
    suspend fun getUsuarios(): List<AdminUsuario>

    /**
     * Cambia el estado activo/inactivo de un usuario en Cognito (solo administradores).
     *
     * @param username Nombre de usuario (username) en Cognito del usuario a modificar.
     * @param body Objeto [EstadoUsuarioRequest] con el nuevo estado deseado.
     * @return El [AdminUsuario] con el estado actualizado.
     */
    @PATCH("admin/usuarios/{username}/estado")
    suspend fun cambiarEstadoUsuario(
        @Path("username") username: String,
        @Body body: EstadoUsuarioRequest
    ): AdminUsuario

    /**
     * Envía un correo de restablecimiento de contraseña al usuario indicado (solo administradores).
     *
     * @param username Nombre de usuario (username) en Cognito del usuario afectado.
     */
    @POST("admin/usuarios/{username}/reset-password")
    suspend fun resetPasswordUsuario(@Path("username") username: String)
}
