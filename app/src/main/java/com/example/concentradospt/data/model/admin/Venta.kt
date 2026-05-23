package com.example.concentradospt.data.model.admin

/**
 * Representa una venta o transacción comercial registrada en el sistema administrativo.
 * Agrupa el comprobante, el cliente (tercero), el vendedor, los totales y el detalle de productos.
 *
 * @property id Identificador único de la venta en la base de datos.
 * @property numeroComprobante Número o código del comprobante fiscal asociado a la venta.
 * @property documentoTipo Tipo de documento comercial emitido (p. ej. "FACTURA", "REMISIÓN").
 * @property estado Estado actual de la venta: "BORRADOR", "GENERADA" o "ANULADA".
 * @property tercero Cliente o contraparte asociada a la venta (nullable si aún no fue asignado).
 * @property vendedor Usuario vendedor que registró la venta (nullable si no aplica).
 * @property fecha Fecha en que se realizó la venta (formato ISO 8601).
 * @property subtotal Valor bruto de la venta antes de impuestos.
 * @property ivaValor Valor del IVA calculado sobre el subtotal de la venta.
 * @property total Valor total de la venta incluyendo impuestos (subtotal + ivaValor).
 * @property detalles Lista de líneas de detalle que describen los productos vendidos.
 */
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

/**
 * Representa la información resumida del vendedor asociado a una venta.
 * Se almacena como instantánea para preservar el nombre aunque el usuario sea modificado después.
 *
 * @property id Identificador único del vendedor en el sistema.
 * @property nombre Nombre del vendedor en el momento de registrar la venta.
 * @property apellido Apellido del vendedor en el momento de registrar la venta.
 */
data class VentaVendedor(
    val id: Long = 0,
    val nombre: String = "",
    val apellido: String = ""
)

/**
 * Representa una línea de detalle dentro de una venta, correspondiente a un producto específico.
 * Contiene la cantidad, la unidad de medida y los valores unitario y total del ítem.
 *
 * @property id Identificador único de la línea de detalle.
 * @property productoId Identificador del producto vendido.
 * @property productoCodigo Código o SKU del producto al momento de la venta.
 * @property productoDescripcion Nombre o descripción del producto al momento de la venta.
 * @property cantidad Cantidad de unidades del producto incluidas en esta línea.
 * @property unidadMedida Unidad de medida utilizada (p. ej. "KG", "UND", "BULTO").
 * @property valorUnitario Precio por unidad del producto aplicado en la venta.
 * @property valorTotal Valor total de la línea (valorUnitario × cantidad).
 */
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
