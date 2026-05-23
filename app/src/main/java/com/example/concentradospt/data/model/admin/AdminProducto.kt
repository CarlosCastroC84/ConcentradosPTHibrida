package com.example.concentradospt.data.model.admin

/**
 * Representa un producto tal como lo gestiona el panel de administración del backend.
 * Incluye información de inventario, precios de costo y venta, y datos de categorización interna.
 *
 * @property id Identificador único del producto en la base de datos del sistema administrativo.
 * @property codigo Código interno o SKU que identifica el producto en el inventario.
 * @property descripcion Nombre o descripción del producto visible en el panel administrativo.
 * @property categoria Categoría principal a la que pertenece el producto (nullable si no está asignada).
 * @property subcategoria Subcategoría más específica dentro de la categoría principal (nullable).
 * @property precioVenta Precio al que el producto se ofrece al cliente final.
 * @property precioCosto Precio de adquisición o costo del producto (nullable si no se registró).
 * @property stockActual Cantidad de unidades disponibles actualmente en el inventario.
 * @property stockMinimo Cantidad mínima de unidades que debe haber en inventario antes de reabastecer.
 * @property stockMaximo Cantidad máxima de unidades que se puede almacenar (nullable si no hay límite).
 * @property imagenProductoUrlFirmada URL firmada para acceder temporalmente a la imagen del producto (nullable).
 * @property activo Indica si el producto está habilitado para operaciones de venta e inventario.
 */
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
