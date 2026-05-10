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
    private val onAddToCart: (Producto) -> Unit,
    private val onFavoriteToggle: (Producto) -> Unit
) : ListAdapter<Producto, HomeProductAdapter.ViewHolder>(DiffCallback) {

    private var favorites: Set<String> = emptySet()

    fun updateFavorites(favSet: Set<String>) {
        val previous = favorites
        favorites = favSet
        currentList.forEachIndexed { index, producto ->
            if (producto.productoId in favSet != producto.productoId in previous) {
                notifyItemChanged(index, PAYLOAD_FAVORITE)
            }
        }
    }

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
            binding.homeProductBtnWishlist.setOnClickListener { onFavoriteToggle(producto) }

            bindFavorite(producto.productoId)
        }

        fun bindFavorite(productoId: String) {
            val isFav = productoId in favorites
            binding.homeProductBtnWishlist.setImageResource(
                if (isFav) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
            )
            binding.homeProductBtnWishlist.setColorFilter(
                binding.root.context.getColor(
                    if (isFav) R.color.tertiary else R.color.on_surface_variant
                )
            )
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.contains(PAYLOAD_FAVORITE)) {
            holder.bindFavorite(getItem(position).productoId)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Producto>() {
        const val PAYLOAD_FAVORITE = "payload_favorite"
        override fun areItemsTheSame(a: Producto, b: Producto) = a.productoId == b.productoId
        override fun areContentsTheSame(a: Producto, b: Producto) = a == b
    }
}

private fun Double.formatCOP(): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    fmt.maximumFractionDigits = 0
    fmt.minimumFractionDigits = 0
    return fmt.format(this.toLong())
}
