package com.example.concentradospt.data.model

import com.google.gson.annotations.SerializedName

/**
 * Representa un producto disponible en el catálogo de la tienda.
 * Contiene toda la información necesaria para mostrarlo al cliente y gestionar su inventario.
 *
 * @property productoId Identificador único del producto en el backend.
 * @property nombre Nombre comercial del producto.
 * @property descripcion Descripción detallada del producto.
 * @property precio Precio de venta al público expresado en la moneda local.
 * @property stock Cantidad de unidades disponibles en inventario.
 * @property categoria Categoría a la que pertenece el producto (p. ej. "Concentrados", "Suplementos").
 * @property marca Marca o fabricante del producto.
 * @property presentacion Presentación comercial del producto (p. ej. "Bulto 40 kg", "Bolsa 5 kg").
 * @property imagenUrl URL pública de la imagen del producto.
 * @property activo Indica si el producto se encuentra habilitado para la venta.
 */
data class Producto(
    @SerializedName("id") val productoId: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("descripcion") val descripcion: String = "",
    @SerializedName("precio") val precio: Double = 0.0,
    @SerializedName("stock") val stock: Int = 0,
    @SerializedName("categoria") val categoria: String = "",
    @SerializedName("marca") val marca: String = "",
    @SerializedName("presentacion") val presentacion: String = "",
    @SerializedName("imagenUrl") val imagenUrl: String = "",
    @SerializedName("activo") val activo: Boolean = true
)

/**
 * Representa una categoría a la que pueden pertenecer los productos del catálogo.
 * Se usa para filtrar y organizar los productos en la interfaz de usuario.
 *
 * @property categoriaId Identificador único de la categoría.
 * @property nombre Nombre descriptivo de la categoría.
 */
data class CategoriaProducto(
    @SerializedName("categoriaId") val categoriaId: String = "",
    @SerializedName("nombre") val nombre: String = ""
)

/**
 * Representa una marca o fabricante asociada a los productos del catálogo.
 * Permite filtrar el catálogo por marca específica.
 *
 * @property marcaId Identificador único de la marca.
 * @property nombre Nombre comercial de la marca.
 */
data class MarcaProducto(
    @SerializedName("marcaId") val marcaId: String = "",
    @SerializedName("nombre") val nombre: String = ""
)

/**
 * Representa una presentación comercial disponible para los productos (p. ej. peso o empaque).
 * Permite filtrar el catálogo según el tamaño o formato de presentación deseado.
 *
 * @property presentacionId Identificador único de la presentación.
 * @property nombre Descripción de la presentación (p. ej. "Bulto 40 kg").
 */
data class PresentacionProducto(
    @SerializedName("presentacionId") val presentacionId: String = "",
    @SerializedName("nombre") val nombre: String = ""
)
