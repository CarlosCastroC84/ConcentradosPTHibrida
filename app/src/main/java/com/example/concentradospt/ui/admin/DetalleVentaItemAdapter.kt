package com.example.concentradospt.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.concentradospt.data.model.admin.VentaDetalle
import com.example.concentradospt.databinding.ItemDetalleVentaItemBinding
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class DetalleVentaItemAdapter : ListAdapter<VentaDetalle, DetalleVentaItemAdapter.ViewHolder>(Diff) {

    private val cop = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    inner class ViewHolder(private val binding: ItemDetalleVentaItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VentaDetalle) {
            binding.itemDetalleDescripcion.text = item.productoDescripcion
            binding.itemDetalleCodigo.text = item.productoCodigo
            val cantidadStr = BigDecimal(item.cantidad).stripTrailingZeros().toPlainString()
            binding.itemDetalleCantidad.text = "$cantidadStr ${item.unidadMedida}"
            binding.itemDetalleTotal.text = cop.format(item.valorTotal)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetalleVentaItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object Diff : DiffUtil.ItemCallback<VentaDetalle>() {
        override fun areItemsTheSame(a: VentaDetalle, b: VentaDetalle) = a.id == b.id
        override fun areContentsTheSame(a: VentaDetalle, b: VentaDetalle) = a == b
    }
}
