package com.example.concentradospt.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.concentradospt.R
import com.example.concentradospt.data.model.Producto
import com.example.concentradospt.databinding.HomeItemProductCardBinding
import java.text.NumberFormat
import java.util.Locale

class HomeProductAdapter(
    private val onProductClick: (Producto) -> Unit,
    private val onAddToCart: (Producto) -> Unit
) : ListAdapter<Producto, HomeProductAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: HomeItemProductCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(producto: Producto) {
            binding.homeProductName.text = producto.nombre
            binding.homeProductCategory.text = producto.categoria
            binding.homeProductPrice.text = producto.precio.formatCOP()

            Glide.with(binding.root)
                .load(producto.imagenUrl)
                .placeholder(R.drawable.bg_card)
                .centerCrop()
                .into(binding.homeProductImage)

            binding.root.setOnClickListener { onProductClick(producto) }
            binding.homeProductBtnAdd.setOnClickListener { onAddToCart(producto) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeItemProductCardBinding.inflate(
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