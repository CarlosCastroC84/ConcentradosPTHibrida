package com.example.concentradospt.data.model

import com.google.gson.annotations.SerializedName

data class Producto(
    @SerializedName("productoId") val productoId: String = "",
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

data class CategoriaProducto(
    @SerializedName("categoriaId") val categoriaId: String = "",
    @SerializedName("nombre") val nombre: String = ""
)

data class MarcaProducto(
    @SerializedName("marcaId") val marcaId: String = "",
    @SerializedName("nombre") val nombre: String = ""
)

data class PresentacionProducto(
    @SerializedName("presentacionId") val presentacionId: String = "",
    @SerializedName("nombre") val nombre: String = ""
)