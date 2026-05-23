package com.example.concentradospt.data.network.admin

import com.example.concentradospt.data.model.admin.AdminLoginRequest
import com.example.concentradospt.data.model.admin.AdminLoginResponse
import com.example.concentradospt.data.model.admin.AdminPageResponse
import com.example.concentradospt.data.model.admin.AdminProducto
import com.example.concentradospt.data.model.admin.AdminRefreshRequest
import com.example.concentradospt.data.model.admin.AdminUsuarioInfo
import com.example.concentradospt.data.model.admin.Venta
import com.example.concentradospt.data.model.admin.Tercero
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz de servicio Retrofit para el módulo de administración interna del backend Spring Boot.
 *
 * Define los endpoints del panel de administración agrupados por dominio:
 * - **Auth**: inicio de sesión, renovación de token y consulta del usuario actual.
 * - **Dashboard**: indicadores clave de negocio (KPIs).
 * - **Usuarios**: listado y gestión de usuarios administrativos.
 * - **Productos**: operaciones CRUD sobre el catálogo con soporte de paginación.
 * - **Ventas**: consulta paginada del historial de ventas.
 * - **Terceros**: búsqueda y listado paginado de terceros (clientes/proveedores).
 *
 * Todos los métodos son `suspend` y requieren invocarse desde una corrutina.
 * Los endpoints de autenticación (`auth/login`, `auth/refresh`) son los únicos que no
 * necesitan cabecera de autorización; el resto son interceptados por [AdminAuthInterceptor].
 */
interface AdminApiService {

    // --- Auth ---

    /**
     * Autentica a un usuario administrador con cédula, rol y PIN.
     *
     * @param req Objeto [AdminLoginRequest] con las credenciales del administrador.
     * @return [AdminLoginResponse] con los tokens de acceso/refresco y los datos del usuario.
     */
    @POST("auth/login")
    suspend fun login(@Body req: AdminLoginRequest): AdminLoginResponse

    /**
     * Renueva el token de acceso usando el token de refresco almacenado.
     *
     * @param req Objeto [AdminRefreshRequest] que contiene el refresh token vigente.
     * @return [AdminLoginResponse] con el nuevo par de tokens y los datos del usuario.
     */
    @POST("auth/refresh")
    suspend fun refresh(@Body req: AdminRefreshRequest): AdminLoginResponse

    /**
     * Obtiene la información del usuario administrador actualmente autenticado.
     *
     * @return [AdminUsuarioInfo] con los datos del usuario asociado al token de acceso.
     */
    @GET("auth/me")
    suspend fun getMe(): AdminUsuarioInfo

    // --- Dashboard ---

    /**
     * Obtiene los indicadores clave de negocio (KPIs) para el panel de administración.
     *
     * @return Mapa genérico de clave-valor con las métricas del dashboard.
     */
    @GET("dashboard/kpis")
    suspend fun getKpis(): Map<String, Any>

    // --- Usuarios ---

    /**
     * Obtiene la lista de todos los usuarios administrativos registrados en el sistema.
     *
     * @return Lista de [AdminUsuarioInfo] con la información de cada usuario administrativo.
     */
    @GET("usuarios")
    suspend fun getUsuarios(): List<AdminUsuarioInfo>

    /**
     * Cambia el estado activo/inactivo de un usuario administrativo.
     *
     * @param id Identificador numérico del usuario a modificar.
     * @param body Mapa con la clave `"estado"` y el valor del nuevo estado (p. ej. `"ACTIVO"`).
     */
    @PATCH("usuarios/{id}/estado")
    suspend fun cambiarEstadoUsuario(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    )

    /**
     * Regenera el PIN de acceso de un usuario administrativo.
     *
     * @param id Identificador numérico del usuario al que se le regenerará el PIN.
     */
    @POST("usuarios/{id}/regenerar-pin")
    suspend fun regenerarPin(@Path("id") id: Long)

    // --- Productos (admin) ---

    /**
     * Obtiene una página de productos del catálogo administrativo con soporte de paginación.
     *
     * @param pagina Número de página a solicitar (base 0). Por defecto es `0`.
     * @param tamano Cantidad de elementos por página. Por defecto es `50`.
     * @return [AdminPageResponse] de [AdminProducto] con los resultados y metadatos de paginación.
     */
    @GET("admin/productos")
    suspend fun getAdminProductos(
        @Query("pagina") pagina: Int = 0,
        @Query("tamano") tamano: Int = 50
    ): AdminPageResponse<AdminProducto>

    /**
     * Crea un nuevo producto en el catálogo administrativo.
     *
     * @param body Objeto [AdminProducto] con los datos del producto a crear.
     * @return El [AdminProducto] creado con su identificador asignado por el backend.
     */
    @POST("admin/productos")
    suspend fun crearAdminProducto(@Body body: AdminProducto): AdminProducto

    /**
     * Actualiza los datos de un producto existente en el catálogo administrativo.
     *
     * @param id Identificador numérico del producto a actualizar.
     * @param body Objeto [AdminProducto] con los campos modificados.
     * @return El [AdminProducto] actualizado.
     */
    @PUT("admin/productos/{id}")
    suspend fun actualizarAdminProducto(
        @Path("id") id: Long,
        @Body body: AdminProducto
    ): AdminProducto

    /**
     * Elimina un producto del catálogo administrativo de forma permanente.
     *
     * @param id Identificador numérico del producto a eliminar.
     */
    @DELETE("admin/productos/{id}")
    suspend fun eliminarAdminProducto(@Path("id") id: Long)

    // --- Ventas ---

    /**
     * Obtiene una página del historial de ventas con soporte de paginación.
     *
     * @param pagina Número de página a solicitar (base 0). Por defecto es `0`.
     * @param tamano Cantidad de elementos por página. Por defecto es `20`.
     * @return [AdminPageResponse] de [Venta] con las ventas y metadatos de paginación.
     */
    @GET("ventas")
    suspend fun getVentas(
        @Query("pagina") pagina: Int = 0,
        @Query("tamano") tamano: Int = 20
    ): AdminPageResponse<Venta>

    /**
     * Obtiene el detalle de una venta específica por su identificador.
     *
     * @param id Identificador numérico de la venta.
     * @return El objeto [Venta] con toda la información de la transacción.
     */
    @GET("ventas/{id}")
    suspend fun getVenta(@Path("id") id: Long): Venta

    // --- Terceros ---

    /**
     * Realiza una búsqueda paginada de terceros (clientes/proveedores) por texto libre.
     *
     * @param q Texto de búsqueda utilizado para filtrar los terceros.
     * @param pagina Número de página a solicitar (base 0). Por defecto es `0`.
     * @param tamano Cantidad de elementos por página. Por defecto es `10`.
     * @return [AdminPageResponse] de [Tercero] con los resultados de la búsqueda.
     */
    @GET("terceros/buscar")
    suspend fun buscarTerceros(
        @Query("q") q: String,
        @Query("pagina") pagina: Int = 0,
        @Query("tamano") tamano: Int = 10
    ): AdminPageResponse<Tercero>

    /**
     * Obtiene una página de todos los terceros registrados en el sistema.
     *
     * @param pagina Número de página a solicitar (base 0). Por defecto es `0`.
     * @param tamano Cantidad de elementos por página. Por defecto es `20`.
     * @return [AdminPageResponse] de [Tercero] con los terceros y metadatos de paginación.
     */
    @GET("terceros")
    suspend fun getTerceros(
        @Query("pagina") pagina: Int = 0,
        @Query("tamano") tamano: Int = 20
    ): AdminPageResponse<Tercero>
}
