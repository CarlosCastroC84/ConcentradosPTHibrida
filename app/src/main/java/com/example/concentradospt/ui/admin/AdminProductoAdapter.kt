package com.example.concentradospt.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.concentradospt.R
import com.example.concentradospt.data.model.admin.AdminProducto
import com.example.concentradospt.databinding.ItemAdminProductoBinding
import java.text.NumberFormat
import java.util.Locale

class AdminProductoAdapter(
    private val onEdit: (AdminProducto) -> Unit,
    private val onDelete: (AdminProducto) -> Unit
) : ListAdapter<AdminProducto, AdminProductoAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemAdminProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(p: AdminProducto) {
            binding.adminProdNombre.text = p.descripcion
            binding.adminProdCategoria.text = p.categoria ?: "Sin categoría"
            binding.adminProdPrecio.text = p.precioVenta.formatCOP()
            binding.adminProdStock.text = "Stock: ${p.stockActual.toBigDecimal().stripTrailingZeros().toPlainString()}"

            Glide.with(binding.root)
                .load(p.imagenProductoUrlFirmada)
                .placeholder(R.drawable.bg_card)
                .centerCrop()
                .into(binding.adminProdImage)

            binding.adminProdBtnEdit.setOnClickListener { onEdit(p) }
            binding.adminProdBtnDelete.setOnClickListener { onDelete(p) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminProductoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<AdminProducto>() {
        override fun areItemsTheSame(a: AdminProducto, b: AdminProducto) = a.id == b.id
        override fun areContentsTheSame(a: AdminProducto, b: AdminProducto) = a == b
    }
}

private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
