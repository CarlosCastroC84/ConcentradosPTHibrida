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

/**
 * Adaptador para el RecyclerView del catálogo de productos.
 *
 * Muestra cada [Producto] en formato de lista con imagen, nombre, categoría,
 * descripción y precio. Expone dos acciones: navegar al detalle del producto
 * o agregar el producto al carrito directamente desde el ítem.
 *
 * @param onProductClick Lambda invocada con el [Producto] cuando se pulsa su tarjeta.
 * @param onAddToCart    Lambda invocada con el [Producto] cuando se pulsa "Agregar".
 */
class CatalogoAdapter(
    private val onProductClick: (Producto) -> Unit,
    private val onAddToCart: (Producto) -> Unit
) : ListAdapter<Producto, CatalogoAdapter.ViewHolder>(DiffCallback) {

    /**
     * ViewHolder que mantiene las referencias a las vistas de un ítem del catálogo.
     *
     * @param binding Binding generado a partir del layout [CatalogoItemProductBinding].
     */
    inner class ViewHolder(private val binding: CatalogoItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de un [Producto] a las vistas del ViewHolder.
         *
         * Carga la imagen con Glide y muestra nombre, categoría, descripción
         * y precio formateado en COP. Asigna los listeners de clic al ítem
         * completo y al botón de agregar al carrito.
         *
         * @param producto Producto a renderizar en este ítem del catálogo.
         */
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

    /**
     * Crea un nuevo [ViewHolder] inflando el layout del ítem del catálogo.
     *
     * @param parent ViewGroup padre al que se adjuntará la vista.
     * @param viewType Tipo de vista (no utilizado; todos los ítems son del mismo tipo).
     * @return Nuevo [ViewHolder] listo para ser enlazado con datos.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CatalogoItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Enlaza el [Producto] en la posición indicada con su [ViewHolder].
     *
     * @param holder ViewHolder que debe ser actualizado.
     * @param position Posición del producto en la lista.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    /**
     * Callback de diferencias utilizado por [ListAdapter] para detectar
     * cambios en la lista y animar las actualizaciones de forma eficiente.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Producto>() {
        /** Compara dos productos por su identificador único. */
        override fun areItemsTheSame(a: Producto, b: Producto) = a.productoId == b.productoId

        /** Compara el contenido completo de dos productos (comparación estructural). */
        override fun areContentsTheSame(a: Producto, b: Producto) = a == b
    }
}

/**
 * Extensión para formatear un [Double] como moneda colombiana (COP).
 *
 * @return Cadena con el valor formateado, por ejemplo "$1.500,00".
 */
private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
