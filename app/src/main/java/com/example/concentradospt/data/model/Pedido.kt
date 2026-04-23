package com.example.concentradospt.data.model

import com.google.gson.annotations.SerializedName

data class Pedido(
    @SerializedName("pedidoId") val pedidoId: String = "",
    @SerializedName("clienteId") val clienteId: String = "",
    @SerializedName("estado") val estado: String = "",
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("items") val items: List<ItemPedido> = emptyList(),
    @SerializedName("fechaCreacion") val fechaCreacion: String = "",
    @SerializedName("direccionEntrega") val direccionEntrega: String = ""
)

data class ItemPedido(
    @SerializedName("productoId") val productoId: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("cantidad") val cantidad: Int = 0,
    @SerializedName("precioUnitario") val precioUnitario: Double = 0.0,
    @SerializedName("subtotal") val subtotal: Double = 0.0
)

data class CrearPedidoRequest(
    @SerializedName("items") val items: List<ItemPedido>,
    @SerializedName("direccionEntrega") val direccionEntrega: String
)