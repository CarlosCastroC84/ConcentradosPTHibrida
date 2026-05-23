package com.example.concentradospt.ui.pedidos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.concentradospt.data.model.Pedido
import com.example.concentradospt.databinding.ItemPedidoBinding
import java.text.NumberFormat
import java.util.Locale

/**
 * Adaptador para el RecyclerView del historial de pedidos del usuario.
 *
 * Muestra cada [Pedido] con su identificador abreviado, estado, fecha,
 * número de artículos y total. Al pulsar sobre un ítem, invoca [onClick]
 * para que el fragmento pueda navegar al detalle correspondiente.
 *
 * @param onClick Lambda invocada con el [Pedido] seleccionado por el usuario.
 */
class PedidosAdapter(
    private val onClick: (Pedido) -> Unit
) : ListAdapter<Pedido, PedidosAdapter.ViewHolder>(DiffCallback) {

    /**
     * ViewHolder que mantiene las referencias a las vistas de un ítem de pedido.
     *
     * @param binding Binding generado a partir del layout [ItemPedidoBinding].
     */
    inner class ViewHolder(private val binding: ItemPedidoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de un [Pedido] a las vistas del ViewHolder.
         *
         * Muestra los últimos 8 caracteres del ID en mayúsculas, el estado
         * capitalizado, la fecha (primeros 10 caracteres), el conteo total
         * de artículos y el precio total formateado en COP.
         *
         * @param pedido Pedido a renderizar en este ítem.
         */
        fun bind(pedido: Pedido) {
            binding.pedidoItemId.text = "Pedido #${pedido.pedidoId.takeLast(8).uppercase()}"
            binding.pedidoItemEstado.text = pedido.estado.replaceFirstChar { it.uppercase() }
            binding.pedidoItemFecha.text = pedido.fechaCreacion.take(10)
            binding.pedidoItemItems.text = "${pedido.items.sumOf { it.cantidad }} artículos"
            binding.pedidoItemTotal.text = pedido.total.formatCOP()
            binding.root.setOnClickListener { onClick(pedido) }
        }
    }

    /**
     * Crea un nuevo [ViewHolder] inflando el layout del ítem de pedido.
     *
     * @param parent ViewGroup padre al que se adjuntará la vista.
     * @param viewType Tipo de vista (no utilizado; todos los ítems son del mismo tipo).
     * @return Nuevo [ViewHolder] listo para ser enlazado con datos.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPedidoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Enlaza el [Pedido] en la posición indicada con su [ViewHolder].
     *
     * @param holder ViewHolder que debe ser actualizado.
     * @param position Posición del pedido en la lista.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    /**
     * Callback de diferencias utilizado por [ListAdapter] para detectar
     * cambios en la lista y animar las actualizaciones de forma eficiente.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Pedido>() {
        /** Compara dos pedidos por su identificador único. */
        override fun areItemsTheSame(a: Pedido, b: Pedido) = a.pedidoId == b.pedidoId

        /** Compara el contenido completo de dos pedidos (comparación estructural). */
        override fun areContentsTheSame(a: Pedido, b: Pedido) = a == b
    }
}

/**
 * Extensión para formatear un [Double] como moneda colombiana (COP).
 *
 * @return Cadena con el valor formateado, por ejemplo "$1.500,00".
 */
private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
