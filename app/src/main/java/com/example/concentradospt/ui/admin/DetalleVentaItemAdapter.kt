package com.example.concentradospt.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.concentradospt.data.model.admin.VentaDetalle
import com.example.concentradospt.databinding.ItemDetalleVentaItemBinding
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/**
 * Adaptador para el RecyclerView de líneas de detalle dentro de una venta.
 *
 * Muestra cada [VentaDetalle] con la descripción del producto, su código,
 * la cantidad con unidad de medida (eliminando ceros decimales innecesarios)
 * y el valor total de esa línea en formato COP.
 *
 * Es de solo lectura; no expone acciones de edición al usuario.
 */
class DetalleVentaItemAdapter : ListAdapter<VentaDetalle, DetalleVentaItemAdapter.ViewHolder>(Diff) {

    /** Formateador de moneda colombiana reutilizable en todos los ítems del adaptador. */
    private val cop = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    /**
     * ViewHolder que mantiene las referencias a las vistas de una línea de detalle de venta.
     *
     * @param binding Binding generado a partir del layout [ItemDetalleVentaItemBinding].
     */
    inner class ViewHolder(private val binding: ItemDetalleVentaItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de un [VentaDetalle] a las vistas del ViewHolder.
         *
         * La cantidad se formatea con [BigDecimal.stripTrailingZeros] para eliminar
         * ceros decimales innecesarios y se concatena con la unidad de medida.
         *
         * @param item Línea de detalle de venta a renderizar.
         */
        fun bind(item: VentaDetalle) {
            binding.itemDetalleDescripcion.text = item.productoDescripcion
            binding.itemDetalleCodigo.text = item.productoCodigo
            val cantidadStr = BigDecimal(item.cantidad).stripTrailingZeros().toPlainString()
            binding.itemDetalleCantidad.text = "$cantidadStr ${item.unidadMedida}"
            binding.itemDetalleTotal.text = cop.format(item.valorTotal)
        }
    }

    /**
     * Crea un nuevo [ViewHolder] inflando el layout de la línea de detalle de venta.
     *
     * @param parent ViewGroup padre al que se adjuntará la vista.
     * @param viewType Tipo de vista (no utilizado; todos los ítems son del mismo tipo).
     * @return Nuevo [ViewHolder] listo para ser enlazado con datos.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetalleVentaItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Enlaza el [VentaDetalle] en la posición indicada con su [ViewHolder].
     *
     * @param holder ViewHolder que debe ser actualizado.
     * @param position Posición del ítem en la lista.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Callback de diferencias utilizado por [ListAdapter] para detectar
     * cambios en la lista y animar las actualizaciones de forma eficiente.
     */
    companion object Diff : DiffUtil.ItemCallback<VentaDetalle>() {
        /** Compara dos líneas de detalle por su identificador único. */
        override fun areItemsTheSame(a: VentaDetalle, b: VentaDetalle) = a.id == b.id

        /** Compara el contenido completo de dos líneas de detalle (comparación estructural). */
        override fun areContentsTheSame(a: VentaDetalle, b: VentaDetalle) = a == b
    }
}
