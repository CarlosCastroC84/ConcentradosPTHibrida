package com.example.concentradospt.ui.catalogo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.concentradospt.R
import com.example.concentradospt.data.model.Producto
import com.example.concentradospt.databinding.CatalogoItemProductBinding
import java.text.NumberFormat
import java.util.Locale

class CatalogoAdapter(
    private val onProductClick: (Producto) -> Unit,
    private val onAddToCart: (Producto) -> Unit
) : ListAdapter<Producto, CatalogoAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: CatalogoItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(producto: Producto) {
            binding.catalogoProductName.text = producto.nombre
            binding.catalogoProductCategory.text = producto.categoria
            binding.catalogoProductDesc.text = producto.descripcion
            binding.catalogoProductPrice.text = producto.precio.formatCOP()

            Glide.with(binding.root)
                .load(producto.imagenUrl)
                .placeholder(R.drawable.bg_card)
                .centerCrop()
                .into(binding.catalogoProductImage)

            binding.root.setOnClickListener { onProductClick(producto) }
            binding.catalogoProductBtnAdd.setOnClickListener { onAddToCart(producto) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CatalogoItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(a: Producto, b: Producto) = a.productoId == b.productoId
        override fun areContentsTheSame(a: Producto, b: Producto) = a == b
    }
}

private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)