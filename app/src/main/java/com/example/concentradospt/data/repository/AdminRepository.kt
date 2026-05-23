package com.example.concentradospt.data.repository

import com.example.concentradospt.data.model.AdminUsuario
import com.example.concentradospt.data.model.EstadoUsuarioRequest
import com.example.concentradospt.data.model.Pedido
import com.example.concentradospt.data.model.Producto
import com.example.concentradospt.data.network.ApiService
import com.example.concentradospt.data.network.RetrofitClient

/**
 * Repositorio que centraliza las operaciones administrativas disponibles a través del
 * API Gateway de AWS, actuando como intermediario entre la capa de presentación y [ApiService].
 *
 * Agrupa funcionalidades de gestión de pedidos, productos y usuarios de Cognito para los
 * perfiles con privilegios de administrador. Todas las operaciones delegan directamente
 * en la instancia de [ApiService] generada por [RetrofitClient].
 */
class AdminRepository {

    /** Implementación de [ApiService] generada por Retrofit para realizar las peticiones HTTP. */
    private val api = RetrofitClient.create<ApiService>()

    // Pedidos admin

    /**
     * Obtiene la lista global de todos los pedidos del sistema.
     *
     * @return Lista de [Pedido] de todos los clientes registrados.
     */
    suspend fun getPedidos(): List<Pedido> = api.getPedidos()

    /**
     * Actualiza los datos de un pedido existente.
     *
     * @param id Identificador único del pedido a actualizar.
     * @param pedido Objeto [Pedido] con los campos modificados.
     * @return El [Pedido] actualizado devuelto por el backend.
     */
    suspend fun actualizarPedido(id: String, pedido: Pedido): Pedido = api.actualizarPedido(id, pedido)

    // Productos CRUD

    /**
     * Obtiene la lista completa de productos del catálogo.
     *
     * @return Lista de [Producto] disponibles en el catálogo.
     */
    suspend fun getProductos(): List<Producto> = api.getProductos()

    /**
     * Crea un nuevo producto en el catálogo.
     *
     * @param producto Objeto [Producto] con los datos del artículo a crear.
     * @return El [Producto] creado con su identificador asignado por el backend.
     */
    suspend fun crearProducto(producto: Producto): Producto = api.crearProducto(producto)

    /**
     * Actualiza los datos de un producto existente en el catálogo usando su propio
     * [Producto.productoId] como identificador.
     *
     * @param producto Objeto [Producto] con los datos modificados. Su campo [Producto.productoId]
     *                 se usa como identificador en la URL de la petición.
     * @return El [Producto] actualizado devuelto por el backend.
     */
    suspend fun actualizarProducto(producto: Producto): Producto =
        api.actualizarProducto(producto.productoId, producto)

    /**
     * Elimina un producto del catálogo de forma permanente.
     *
     * @param id Identificador único del producto a eliminar.
     */
    suspend fun eliminarProducto(id: String) = api.eliminarProducto(id)

    // Usuarios admin

    /**
     * Obtiene la lista de todos los usuarios registrados en el sistema (User Pool de Cognito).
     *
     * @return Lista de [AdminUsuario] con la información de cada usuario.
     */
    suspend fun getUsuarios(): List<AdminUsuario> = api.getUsuarios()

    /**
     * Cambia el estado activo/inactivo de un usuario en Cognito.
     *
     * Construye un [EstadoUsuarioRequest] con el valor `"ACTIVO"` o `"INACTIVO"` según
     * el parámetro booleano recibido.
     *
     * @param username Nombre de usuario (username) en Cognito del usuario a modificar.
     * @param activo `true` para activar al usuario; `false` para desactivarlo.
     * @return El [AdminUsuario] con el estado actualizado.
     */
    suspend fun cambiarEstado(username: String, activo: Boolean): AdminUsuario =
        api.cambiarEstadoUsuario(username, EstadoUsuarioRequest(if (activo) "ACTIVO" else "INACTIVO"))

    /**
     * Envía un correo de restablecimiento de contraseña al usuario indicado.
     *
     * @param username Nombre de usuario (username) en Cognito del usuario afectado.
     */
    suspend fun resetPassword(username: String) = api.resetPasswordUsuario(username)
}
