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

/**
 * Adaptador para el RecyclerView de gestión de productos en el panel de administración.
 *
 * Muestra cada [AdminProducto] con imagen, nombre, categoría, precio y stock.
 * Expone botones de edición y eliminación para que el administrador pueda
 * gestionar cada producto directamente desde la lista.
 *
 * @param onEdit   Lambda invocada con el [AdminProducto] cuando se pulsa "Editar".
 * @param onDelete Lambda invocada con el [AdminProducto] cuando se pulsa "Eliminar".
 */
class AdminProductoAdapter(
    private val onEdit: (AdminProducto) -> Unit,
    private val onDelete: (AdminProducto) -> Unit
) : ListAdapter<AdminProducto, AdminProductoAdapter.ViewHolder>(DiffCallback) {

    /**
     * ViewHolder que mantiene las referencias a las vistas de un ítem de producto administrativo.
     *
     * @param binding Binding generado a partir del layout [ItemAdminProductoBinding].
     */
    inner class ViewHolder(private val binding: ItemAdminProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de un [AdminProducto] a las vistas del ViewHolder.
         *
         * Muestra nombre (descripción), categoría, precio y stock del producto.
         * Carga la imagen firmada con Glide y asigna los listeners de editar y eliminar.
         *
         * @param p Producto administrativo a renderizar en este ítem.
         */
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

    /**
     * Crea un nuevo [ViewHolder] inflando el layout del ítem de producto administrativo.
     *
     * @param parent ViewGroup padre al que se adjuntará la vista.
     * @param viewType Tipo de vista (no utilizado; todos los ítems son del mismo tipo).
     * @return Nuevo [ViewHolder] listo para ser enlazado con datos.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminProductoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Enlaza el [AdminProducto] en la posición indicada con su [ViewHolder].
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
    companion object DiffCallback : DiffUtil.ItemCallback<AdminProducto>() {
        /** Compara dos productos administrativos por su identificador numérico. */
        override fun areItemsTheSame(a: AdminProducto, b: AdminProducto) = a.id == b.id

        /** Compara el contenido completo de dos productos (comparación estructural). */
        override fun areContentsTheSame(a: AdminProducto, b: AdminProducto) = a == b
    }
}

/**
 * Extensión para formatear un [Double] como moneda colombiana (COP).
 *
 * @return Cadena con el valor formateado, por ejemplo "$1.500,00".
 */
private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
