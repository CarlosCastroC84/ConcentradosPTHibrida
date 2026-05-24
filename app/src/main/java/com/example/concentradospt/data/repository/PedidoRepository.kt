package com.example.concentradospt.data.repository

import com.example.concentradospt.data.model.CrearPedidoRequest
import com.example.concentradospt.data.model.DeliverySnapshotRequest
import com.example.concentradospt.data.model.ItemPedidoRequest
import com.example.concentradospt.data.model.Pedido
import com.example.concentradospt.data.model.CartItem
import com.example.concentradospt.data.network.ApiService
import com.example.concentradospt.data.network.RetrofitClient

/**
 * Repositorio que gestiona las operaciones relacionadas con los pedidos del cliente
 * autenticado, actuando como intermediario entre la capa de presentación y [ApiService].
 *
 * Se encarga de construir los objetos de petición necesarios (p. ej. [CrearPedidoRequest])
 * a partir de los datos del carrito y del formulario de entrega, delegando la comunicación
 * HTTP a la instancia de [ApiService] creada por [RetrofitClient].
 */
class PedidoRepository {

    /** Implementación de [ApiService] generada por Retrofit para realizar las peticiones HTTP. */
    private val api = RetrofitClient.create<ApiService>()

    /**
     * Obtiene la lista de todos los pedidos realizados por el cliente autenticado.
     *
     * @return Lista de [Pedido] asociados al usuario cuyo token está activo.
     */
    suspend fun getMisPedidos(): List<Pedido> = api.getMisPedidos()

    /**
     * Obtiene el detalle de un pedido específico del cliente autenticado.
     *
     * @param id Identificador único del pedido a consultar.
     * @return El [Pedido] correspondiente al [id] indicado.
     */
    suspend fun getMiPedido(id: String): Pedido = api.getMiPedido(id)

    /**
     * Crea un nuevo pedido para el cliente autenticado a partir de los ítems del carrito
     * y los datos del formulario de entrega.
     *
     * Construye internamente un [CrearPedidoRequest] mapeando cada [CartItem] a un
     * [ItemPedidoRequest] y envolviendo los datos de entrega en un [DeliverySnapshotRequest].
     * La referencia de dirección se normaliza a `null` si está en blanco.
     *
     * @param items Lista de [CartItem] que representan los productos seleccionados con su cantidad.
     * @param fullName Nombre completo del destinatario del pedido.
     * @param phone Teléfono de contacto para la entrega.
     * @param municipality Municipio de destino del pedido.
     * @param addressReference Referencia adicional de la dirección de entrega (opcional, puede ser nula).
     * @return El [Pedido] creado con su identificador y estado inicial asignados por el backend.
     */
    suspend fun crearPedido(
        items: List<CartItem>,
        fullName: String,
        phone: String,
        municipality: String,
        addressReference: String? = null
    ): Pedido {
        val request = CrearPedidoRequest(
            items = items.map { cartItem ->
                ItemPedidoRequest(
                    productId = cartItem.producto.productoId,
                    quantity = cartItem.cantidad
                )
            },
            deliverySnapshot = DeliverySnapshotRequest(
                fullName = fullName,
                phone = phone,
                municipality = municipality,
                addressReference = addressReference?.ifBlank { null }
            )
        )
        return api.crearPedido(request)
    }
}
