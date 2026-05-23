package com.example.concentradospt.ui.pedidos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.concentradospt.data.model.ItemPedido
import com.example.concentradospt.databinding.ItemDetallePedidoProductoBinding
import java.text.NumberFormat
import java.util.Locale

/**
 * Adaptador para el RecyclerView de productos dentro del detalle de un pedido.
 *
 * Renderiza cada [ItemPedido] mostrando nombre del producto, cantidad
 * (con el prefijo "×") y subtotal en formato COP. No requiere interacción
 * del usuario; es de solo lectura.
 */
class DetallePedidoItemAdapter :
    ListAdapter<ItemPedido, DetallePedidoItemAdapter.ViewHolder>(DiffCallback) {

    /**
     * ViewHolder que mantiene las referencias a las vistas de un ítem del detalle de pedido.
     *
     * @param binding Binding generado a partir del layout [ItemDetallePedidoProductoBinding].
     */
    inner class ViewHolder(private val binding: ItemDetallePedidoProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de un [ItemPedido] a las vistas del ViewHolder.
         *
         * Muestra el nombre del producto, la cantidad prefijada con "×"
         * y el subtotal formateado en pesos colombianos.
         *
         * @param item Ítem del pedido que se va a renderizar.
         */
        fun bind(item: ItemPedido) {
            binding.detalleItemNombre.text = item.nombre
            binding.detalleItemCantidad.text = "× ${item.cantidad}"
            binding.detalleItemSubtotal.text = item.subtotal.formatCOP()
        }
    }

    /**
     * Crea un nuevo [ViewHolder] inflando el layout del ítem de detalle de pedido.
     *
     * @param parent ViewGroup padre al que se adjuntará la vista.
     * @param viewType Tipo de vista (no utilizado; todos los ítems son del mismo tipo).
     * @return Nuevo [ViewHolder] listo para ser enlazado con datos.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetallePedidoProductoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Enlaza el [ItemPedido] en la posición indicada con su [ViewHolder].
     *
     * @param holder ViewHolder que debe ser actualizado.
     * @param position Posición del ítem en la lista.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    /**
     * Callback de diferencias utilizado por [ListAdapter] para detectar
     * cambios en la lista y animar actualizaciones de forma eficiente.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<ItemPedido>() {
        /** Compara dos ítems de pedido por el ID del producto. */
        override fun areItemsTheSame(a: ItemPedido, b: ItemPedido) =
            a.productoId == b.productoId

        /** Compara el contenido completo de dos ítems (comparación estructural). */
        override fun areContentsTheSame(a: ItemPedido, b: ItemPedido) = a == b
    }
}

/**
 * Extensión para formatear un [Double] como moneda colombiana (COP).
 *
 * @return Cadena con el valor formateado, por ejemplo "$1.500,00".
 */
private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
