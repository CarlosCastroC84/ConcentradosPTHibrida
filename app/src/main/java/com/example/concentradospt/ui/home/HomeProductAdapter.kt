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

/**
 * Adaptador para el RecyclerView de productos destacados en la pantalla de inicio.
 *
 * Muestra cada [Producto] en formato de tarjeta con imagen, nombre, categoría
 * y precio. Soporta tres acciones del usuario: navegar al detalle, agregar al
 * carrito y alternar el estado de favorito. Optimiza el re-renderizado del ícono
 * de favorito mediante payloads parciales ([PAYLOAD_FAVORITE]).
 *
 * @param onProductClick Lambda invocada al pulsar la tarjeta del producto.
 * @param onAddToCart    Lambda invocada al pulsar el botón de añadir al carrito.
 * @param onFavoriteToggle Lambda invocada al pulsar el ícono de favorito.
 */
class HomeProductAdapter(
    private val onProductClick: (Producto) -> Unit,
    private val onAddToCart: (Producto) -> Unit,
    private val onFavoriteToggle: (Producto) -> Unit
) : ListAdapter<Producto, HomeProductAdapter.ViewHolder>(DiffCallback) {

    /** Conjunto de IDs de productos marcados como favoritos por el usuario. */
    private var favorites: Set<String> = emptySet()

    /**
     * Actualiza el conjunto de favoritos y notifica solo los ítems cuyo estado cambió,
     * usando el payload [PAYLOAD_FAVORITE] para evitar un redibujado completo.
     *
     * @param favSet Nuevo conjunto de IDs de productos favoritos.
     */
    fun updateFavorites(favSet: Set<String>) {
        val previous = favorites
        favorites = favSet
        currentList.forEachIndexed { index, producto ->
            if (producto.productoId in favSet != producto.productoId in previous) {
                notifyItemChanged(index, PAYLOAD_FAVORITE)
            }
        }
    }

    /**
     * ViewHolder que mantiene las referencias a las vistas de una tarjeta de producto.
     *
     * @param binding Binding generado a partir del layout [HomeItemProductCardBinding].
     */
    inner class ViewHolder(private val binding: HomeItemProductCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos completos de un [Producto] a las vistas del ViewHolder.
         *
         * Carga la imagen con Glide, asigna nombre, categoría, precio y configura
         * los listeners de clic en la tarjeta, botón de carrito y botón de favorito.
         *
         * @param producto Producto a renderizar en esta tarjeta.
         */
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

        /**
         * Actualiza únicamente el ícono y el color del botón de favorito
         * según si el producto está en la lista de favoritos actual.
         *
         * Este método se usa tanto en [bind] como en actualizaciones parciales
         * con payload [PAYLOAD_FAVORITE] para mayor eficiencia.
         *
         * @param productoId ID del producto cuyo estado de favorito se va a reflejar.
         */
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

    /**
     * Crea un nuevo [ViewHolder] inflando el layout de la tarjeta de producto.
     *
     * @param parent ViewGroup padre al que se adjuntará la vista.
     * @param viewType Tipo de vista (no utilizado; todos los ítems son del mismo tipo).
     * @return Nuevo [ViewHolder] listo para ser enlazado con datos.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeItemProductCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Enlaza el [Producto] en la posición dada con su [ViewHolder] (enlace completo).
     *
     * @param holder ViewHolder que debe ser actualizado.
     * @param position Posición del producto en la lista.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    /**
     * Enlaza el [Producto] con soporte para payloads parciales.
     *
     * Si el payload contiene [PAYLOAD_FAVORITE], solo actualiza el ícono
     * de favorito sin redibujar toda la tarjeta; de lo contrario, realiza
     * el enlace completo delegando al método padre.
     *
     * @param holder   ViewHolder a actualizar.
     * @param position Posición del producto en la lista.
     * @param payloads Lista de payloads proporcionados por [DiffUtil].
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.contains(PAYLOAD_FAVORITE)) {
            holder.bindFavorite(getItem(position).productoId)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    /**
     * Callback de diferencias y constantes compartidas del adaptador.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Producto>() {
        /** Payload utilizado para actualizaciones parciales del ícono de favorito. */
        const val PAYLOAD_FAVORITE = "payload_favorite"

        /** Compara dos productos por su identificador único. */
        override fun areItemsTheSame(a: Producto, b: Producto) = a.productoId == b.productoId

        /** Compara el contenido completo de dos productos (comparación estructural). */
        override fun areContentsTheSame(a: Producto, b: Producto) = a == b
    }
}

/**
 * Extensión para formatear un [Double] como moneda colombiana (COP) sin decimales.
 *
 * @return Cadena con el valor formateado sin centavos, por ejemplo "$1.500".
 */
private fun Double.formatCOP(): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    fmt.maximumFractionDigits = 0
    fmt.minimumFractionDigits = 0
    return fmt.format(this.toLong())
}
