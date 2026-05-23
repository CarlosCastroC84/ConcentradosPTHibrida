package com.example.concentradospt.data.repository

import com.example.concentradospt.data.model.CategoriaProducto
import com.example.concentradospt.data.model.Producto
import com.example.concentradospt.data.network.ApiService
import com.example.concentradospt.data.network.RetrofitClient

/**
 * Repositorio que gestiona las operaciones de consulta del catálogo de productos
 * y las categorías asociadas, actuando como intermediario entre la capa de presentación
 * y [ApiService].
 *
 * Delega directamente las peticiones a la instancia de [ApiService] generada por
 * [RetrofitClient], proporcionando una API suspend-friendly para los ViewModels.
 */
class ProductRepository {

    /** Implementación de [ApiService] generada por Retrofit para realizar las peticiones HTTP. */
    private val api = RetrofitClient.create<ApiService>()

    /**
     * Obtiene la lista completa de productos disponibles en el catálogo público.
     *
     * @return Lista de [Producto] con todos los artículos del catálogo.
     */
    suspend fun getProductos(): List<Producto> = api.getProductos()

    /**
     * Obtiene la lista de todas las categorías de productos del catálogo público.
     *
     * @return Lista de [CategoriaProducto] con todas las categorías registradas.
     */
    suspend fun getCategorias(): List<CategoriaProducto> = api.getCategorias()
}
