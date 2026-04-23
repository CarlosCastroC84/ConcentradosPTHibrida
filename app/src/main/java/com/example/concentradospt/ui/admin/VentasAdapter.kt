package com.example.concentradospt.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.concentradospt.data.model.admin.Venta
import com.example.concentradospt.databinding.ItemVentaBinding
import java.text.NumberFormat
import java.util.Locale

class VentasAdapter(
    private val onClick: (Venta) -> Unit
) : ListAdapter<Venta, VentasAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemVentaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(v: Venta) {
            binding.ventaNumero.text = v.numeroComprobante.ifEmpty { "#${v.id}" }
            binding.ventaCliente.text = v.tercero?.nombreDisplay ?: "Sin cliente"
            binding.ventaEstado.text = v.estado
            binding.ventaTotal.text = v.total.formatCOP()
            binding.ventaFecha.text = v.fecha
            binding.root.setOnClickListener { onClick(v) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVentaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<Venta>() {
        override fun areItemsTheSame(a: Venta, b: Venta) = a.id == b.id
        override fun areContentsTheSame(a: Venta, b: Venta) = a == b
    }
}

private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
