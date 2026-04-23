package com.example.concentradospt.data.repository

import com.example.concentradospt.data.model.CategoriaProducto
import com.example.concentradospt.data.model.Producto
import com.example.concentradospt.data.network.ApiService
import com.example.concentradospt.data.network.RetrofitClient

class ProductRepository {
    private val api = RetrofitClient.create<ApiService>()

    suspend fun getProductos(): List<Producto> = api.getProductos()
    suspend fun getCategorias(): List<CategoriaProducto> = api.getCategorias()
}