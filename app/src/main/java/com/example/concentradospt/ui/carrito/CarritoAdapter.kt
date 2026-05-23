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

/**
 * Adaptador para el RecyclerView del carrito de compras.
 *
 * Muestra cada [CartItem] con imagen, nombre, cantidad y subtotal.
 * Expone botones para aumentar o disminuir la cantidad del producto,
 * delegando la lógica al ViewModel a través de las lambdas recibidas.
 *
 * @param onIncrease Lambda invocada con el ID del producto cuando se pulsa "aumentar".
 * @param onDecrease Lambda invocada con el ID del producto cuando se pulsa "disminuir".
 */
class CarritoAdapter(
    private val onIncrease: (String) -> Unit,
    private val onDecrease: (String) -> Unit
) : ListAdapter<CartItem, CarritoAdapter.ViewHolder>(DiffCallback) {

    /**
     * ViewHolder que mantiene las referencias a las vistas de un ítem del carrito.
     *
     * @param binding Binding generado a partir del layout [CarritoItemProductBinding].
     */
    inner class ViewHolder(private val binding: CarritoItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de un [CartItem] a las vistas del ViewHolder.
         *
         * Carga la imagen del producto con Glide, muestra nombre, subtotal y cantidad,
         * y asigna los listeners a los botones de incremento y decremento.
         *
         * @param item Elemento del carrito que se va a renderizar.
         */
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

    /**
     * Crea un nuevo [ViewHolder] inflando el layout del ítem del carrito.
     *
     * @param parent ViewGroup padre al que se adjuntará la vista.
     * @param viewType Tipo de vista (no utilizado; todos los ítems son del mismo tipo).
     * @return Nuevo [ViewHolder] listo para ser enlazado con datos.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CarritoItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Enlaza el [CartItem] en la posición dada con el [ViewHolder] correspondiente.
     *
     * @param holder ViewHolder que debe ser actualizado.
     * @param position Posición del ítem en la lista.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    /**
     * Callback de diferencias utilizado por [ListAdapter] para detectar cambios en la lista
     * y realizar animaciones eficientes sin redibujar todos los elementos.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        /** Determina si dos ítems representan el mismo producto comparando sus IDs. */
        override fun areItemsTheSame(a: CartItem, b: CartItem) =
            a.producto.productoId == b.producto.productoId

        /** Determina si el contenido de dos ítems es idéntico (comparación estructural). */
        override fun areContentsTheSame(a: CartItem, b: CartItem) = a == b
    }
}

/**
 * Extensión para formatear un [Double] como moneda colombiana (COP).
 *
 * @return Cadena con el valor formateado, por ejemplo "$1.500,00".
 */
private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
