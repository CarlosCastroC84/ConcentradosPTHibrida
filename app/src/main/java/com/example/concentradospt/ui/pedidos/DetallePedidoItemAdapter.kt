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

class DetallePedidoItemAdapter :
    ListAdapter<ItemPedido, DetallePedidoItemAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemDetallePedidoProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemPedido) {
            binding.detalleItemNombre.text = item.nombre
            binding.detalleItemCantidad.text = "× ${item.cantidad}"
            binding.detalleItemSubtotal.text = item.subtotal.formatCOP()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetallePedidoProductoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<ItemPedido>() {
        override fun areItemsTheSame(a: ItemPedido, b: ItemPedido) =
            a.productoId == b.productoId
        override fun areContentsTheSame(a: ItemPedido, b: ItemPedido) = a == b
    }
}

private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)