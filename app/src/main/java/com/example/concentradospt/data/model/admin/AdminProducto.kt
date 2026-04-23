package com.example.concentradospt.data.model.admin

data class AdminProducto(
    val id: Long = 0,
    val codigo: String = "",
    val descripcion: String = "",   // nombre del producto
    val categoria: String? = null,
    val subcategoria: String? = null,
    val precioVenta: Double = 0.0,
    val precioCosto: Double? = null,
    val stockActual: Double = 0.0,
    val stockMinimo: Double = 0.0,
    val stockMaximo: Double? = null,
    val imagenProductoUrlFirmada: String? = null,
    val activo: Boolean = true
)
