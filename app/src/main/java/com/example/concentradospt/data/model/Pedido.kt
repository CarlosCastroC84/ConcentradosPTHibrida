package com.example.concentradospt.data.model

import com.google.gson.annotations.SerializedName

/**
 * Representa un pedido realizado por un cliente en la aplicación.
 * Agrupa los ítems comprados, el estado de la orden, el total y la información de entrega.
 *
 * @property pedidoId Identificador único del pedido generado por el backend.
 * @property clienteId Identificador del cliente que realizó el pedido.
 * @property estado Estado actual del pedido (p. ej. "PENDIENTE", "ENVIADO", "ENTREGADO").
 * @property total Valor total del pedido incluyendo todos los ítems.
 * @property items Lista de productos incluidos en el pedido con sus cantidades y precios.
 * @property fechaCreacion Fecha y hora en que se registró el pedido (formato ISO 8601).
 * @property direccionEntrega Dirección física donde se debe entregar el pedido.
 */
data class Pedido(
    @SerializedName("pedidoId") val pedidoId: String = "",
    @SerializedName("clienteId") val clienteId: String = "",
    @SerializedName("estado") val estado: String = "",
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("items") val items: List<ItemPedido> = emptyList(),
    @SerializedName("fechaCreacion") val fechaCreacion: String = "",
    @SerializedName("direccionEntrega") val direccionEntrega: String = ""
)

/**
 * Representa un ítem individual dentro de un pedido existente.
 * Contiene el detalle del producto, la cantidad solicitada y los valores calculados.
 *
 * @property productoId Identificador del producto asociado a este ítem.
 * @property nombre Nombre del producto en el momento de realizar el pedido.
 * @property cantidad Número de unidades solicitadas del producto.
 * @property precioUnitario Precio por unidad del producto al momento del pedido.
 * @property subtotal Valor total de este ítem (precioUnitario × cantidad).
 */
data class ItemPedido(
    @SerializedName("productoId") val productoId: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("cantidad") val cantidad: Int = 0,
    @SerializedName("precioUnitario") val precioUnitario: Double = 0.0,
    @SerializedName("subtotal") val subtotal: Double = 0.0
)

/**
 * DTO de request para crear un nuevo pedido.
 * Estructura exacta que espera la función Lambda en el backend.
 *
 * @property items Lista de productos con sus cantidades que conforman el pedido.
 * @property deliverySnapshot Datos de entrega capturados al momento de la compra.
 */
data class CrearPedidoRequest(
    @SerializedName("items") val items: List<ItemPedidoRequest>,
    @SerializedName("deliverySnapshot") val deliverySnapshot: DeliverySnapshotRequest
)

/**
 * Contiene el resumen de la información de entrega al momento de generar un pedido.
 * Se persiste como una instantánea para preservar los datos aunque el cliente los modifique después.
 *
 * @property fullName Nombre completo de la persona que recibirá el pedido.
 * @property phone Número de teléfono de contacto para la entrega.
 * @property municipality Municipio o ciudad de destino del pedido.
 * @property addressReference Referencia adicional de la dirección de entrega (opcional).
 */
data class DeliverySnapshotRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("municipality") val municipality: String,
    @SerializedName("addressReference") val addressReference: String? = null
)

/**
 * Representa un ítem individual en la solicitud de creación de un pedido.
 * Contiene únicamente el identificador del producto y la cantidad requerida.
 *
 * @property productId Identificador único del producto que se desea ordenar.
 * @property quantity Número de unidades del producto solicitadas.
 */
data class ItemPedidoRequest(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Int
)
