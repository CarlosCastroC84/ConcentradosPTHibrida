package com.example.concentradospt.ui.carrito

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.concentradospt.R
import com.example.concentradospt.data.model.CartItem
import com.example.concentradospt.databinding.CarritoItemProductBinding
import java.text.NumberFormat
import java.util.Locale

class CarritoAdapter(
    private val onIncrease: (String) -> Unit,
    private val onDecrease: (String) -> Unit
) : ListAdapter<CartItem, CarritoAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: CarritoItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.carritoItemName.text = item.producto.nombre
            binding.carritoItemPrice.text = item.subtotal.formatCOP()
            binding.carritoItemQuantity.text = item.cantidad.toString()

            Glide.with(binding.root)
                .load(item.producto.imagenUrl)
                .placeholder(R.drawable.bg_card)
                .centerCrop()
                .into(binding.carritoItemImage)

            binding.carritoItemBtnIncrease.setOnClickListener {
                onIncrease(item.producto.productoId)
            }
            binding.carritoItemBtnDecrease.setOnClickListener {
                onDecrease(item.producto.productoId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CarritoItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(a: CartItem, b: CartItem) =
            a.producto.productoId == b.producto.productoId
        override fun areContentsTheSame(a: CartItem, b: CartItem) = a == b
    }
}

private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)