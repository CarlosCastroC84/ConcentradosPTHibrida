package com.example.concentradospt.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.concentradospt.R
import com.example.concentradospt.databinding.ItemBannerSlideBinding

/**
 * Adaptador para el ViewPager2 del banner publicitario en la pantalla de inicio.
 *
 * Carga cada imagen del carrusel desde una URL remota usando Glide.
 * Soporta actualización dinámica de la lista de URLs mediante [updateUrls].
 *
 * @param urls Lista inicial de URLs de imágenes para los slides del banner.
 */
class BannerAdapter(private var urls: List<String>) :
    RecyclerView.Adapter<BannerAdapter.SlideViewHolder>() {

    /**
     * ViewHolder que representa un slide individual del banner.
     *
     * @param binding Binding generado a partir del layout [ItemBannerSlideBinding].
     */
    inner class SlideViewHolder(private val binding: ItemBannerSlideBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Carga la imagen del slide desde la URL proporcionada usando Glide.
         *
         * Muestra un color de fondo como placeholder mientras la imagen se descarga
         * y aplica recorte centrado para adaptarse al contenedor.
         *
         * @param url URL remota de la imagen que se va a mostrar en este slide.
         */
        fun bind(url: String) {
            Glide.with(binding.root)
                .load(url)
                .placeholder(R.color.surface_container)
                .centerCrop()
                .into(binding.bannerSlideImage)
        }
    }

    /**
     * Crea un nuevo [SlideViewHolder] inflando el layout de un slide del banner.
     *
     * @param parent ViewGroup padre al que se adjuntará la vista.
     * @param viewType Tipo de vista (no utilizado; todos los slides son iguales).
     * @return Nuevo [SlideViewHolder] listo para ser enlazado con una URL.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val binding = ItemBannerSlideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SlideViewHolder(binding)
    }

    /**
     * Enlaza la URL en la posición dada con su [SlideViewHolder].
     *
     * @param holder   SlideViewHolder a actualizar.
     * @param position Índice del slide en la lista de URLs.
     */
    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) =
        holder.bind(urls[position])

    /**
     * Retorna el número total de slides en el banner.
     *
     * @return Tamaño de la lista de URLs actual.
     */
    override fun getItemCount() = urls.size

    /**
     * Reemplaza la lista de URLs y notifica al adaptador para que
     * recargue todos los slides del banner.
     *
     * @param newUrls Nueva lista de URLs de imágenes del carrusel.
     */
    fun updateUrls(newUrls: List<String>) {
        urls = newUrls
        notifyDataSetChanged()
    }
}
