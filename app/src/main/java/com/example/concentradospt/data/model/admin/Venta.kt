package com.example.concentradospt.data.model.admin

data class Venta(
    val id: Long = 0,
    val numeroComprobante: String = "",
    val documentoTipo: String = "",
    val estado: String = "",    // "BORRADOR" | "GENERADA" | "ANULADA"
    val tercero: Tercero? = null,
    val vendedor: VentaVendedor? = null,
    val fecha: String = "",
    val subtotal: Double = 0.0,
    val ivaValor: Double = 0.0,
    val total: Double = 0.0,
    val detalles: List<VentaDetalle> = emptyList()
)

data class VentaVendedor(
    val id: Long = 0,
    val nombre: String = "",
    val apellido: String = ""
)

data class VentaDetalle(
    val id: Long = 0,
    val productoId: Long = 0,
    val productoCodigo: String = "",
    val productoDescripcion: String = "",
    val cantidad: Double = 0.0,
    val unidadMedida: String = "",
    val valorUnitario: Double = 0.0,
    val valorTotal: Double = 0.0
)
