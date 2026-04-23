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

class PedidosAdapter(
    private val onClick: (Pedido) -> Unit
) : ListAdapter<Pedido, PedidosAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemPedidoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pedido: Pedido) {
            binding.pedidoItemId.text = "Pedido #${pedido.pedidoId.takeLast(8).uppercase()}"
            binding.pedidoItemEstado.text = pedido.estado.replaceFirstChar { it.uppercase() }
            binding.pedidoItemFecha.text = pedido.fechaCreacion.take(10)
            binding.pedidoItemItems.text = "${pedido.items.sumOf { it.cantidad }} artículos"
            binding.pedidoItemTotal.text = pedido.total.formatCOP()
            binding.root.setOnClickListener { onClick(pedido) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPedidoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<Pedido>() {
        override fun areItemsTheSame(a: Pedido, b: Pedido) = a.pedidoId == b.pedidoId
        override fun areContentsTheSame(a: Pedido, b: Pedido) = a == b
    }
}

private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
