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

interface AdminApiService {

    // --- Auth ---
    @POST("auth/login")
    suspend fun login(@Body req: AdminLoginRequest): AdminLoginResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body req: AdminRefreshRequest): AdminLoginResponse

    @GET("auth/me")
    suspend fun getMe(): AdminUsuarioInfo

    // --- Dashboard ---
    @GET("dashboard/kpis")
    suspend fun getKpis(): Map<String, Any>

    // --- Usuarios ---
    @GET("usuarios")
    suspend fun getUsuarios(): List<AdminUsuarioInfo>

    @PATCH("usuarios/{id}/estado")
    suspend fun cambiarEstadoUsuario(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    )

    @POST("usuarios/{id}/regenerar-pin")
    suspend fun regenerarPin(@Path("id") id: Long)

    // --- Productos (admin) ---
    @GET("admin/productos")
    suspend fun getAdminProductos(
        @Query("pagina") pagina: Int = 0,
        @Query("tamano") tamano: Int = 50
    ): AdminPageResponse<AdminProducto>

    @POST("admin/productos")
    suspend fun crearAdminProducto(@Body body: AdminProducto): AdminProducto

    @PUT("admin/productos/{id}")
    suspend fun actualizarAdminProducto(
        @Path("id") id: Long,
        @Body body: AdminProducto
    ): AdminProducto

    @DELETE("admin/productos/{id}")
    suspend fun eliminarAdminProducto(@Path("id") id: Long)

    // --- Ventas ---
    @GET("ventas")
    suspend fun getVentas(
        @Query("pagina") pagina: Int = 0,
        @Query("tamano") tamano: Int = 20
    ): AdminPageResponse<Venta>

    @GET("ventas/{id}")
    suspend fun getVenta(@Path("id") id: Long): Venta

    // --- Terceros ---
    @GET("terceros/buscar")
    suspend fun buscarTerceros(
        @Query("q") q: String,
        @Query("pagina") pagina: Int = 0,
        @Query("tamano") tamano: Int = 10
    ): AdminPageResponse<Tercero>

    @GET("terceros")
    suspend fun getTerceros(
        @Query("pagina") pagina: Int = 0,
        @Query("tamano") tamano: Int = 20
    ): AdminPageResponse<Tercero>
}
