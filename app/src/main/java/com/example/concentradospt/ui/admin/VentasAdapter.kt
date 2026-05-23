package com.example.concentradospt.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.concentradospt.data.model.admin.Venta
import com.example.concentradospt.databinding.ItemVentaBinding
import java.text.NumberFormat
import java.util.Locale

/**
 * Adaptador para el RecyclerView del listado de ventas en el panel administrativo.
 *
 * Muestra cada [Venta] con su número de comprobante (o ID como fallback),
 * nombre del cliente, estado, total en COP y fecha. Al pulsar sobre un ítem
 * invoca [onClick] para navegar al detalle de la venta.
 *
 * Es compartido tanto por [VentasFragment] (admin) como por [PedidosVendedorFragment].
 *
 * @param onClick Lambda invocada con la [Venta] seleccionada por el usuario.
 */
class VentasAdapter(
    private val onClick: (Venta) -> Unit
) : ListAdapter<Venta, VentasAdapter.ViewHolder>(DiffCallback) {

    /**
     * ViewHolder que mantiene las referencias a las vistas de un ítem de venta.
     *
     * @param binding Binding generado a partir del layout [ItemVentaBinding].
     */
    inner class ViewHolder(private val binding: ItemVentaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de una [Venta] a las vistas del ViewHolder.
         *
         * Muestra el número de comprobante (o "#id" si está vacío), el nombre
         * del cliente (o "Sin cliente"), el estado, el total en COP y la fecha.
         * Asigna el listener de clic al ítem completo.
         *
         * @param v Venta a renderizar en este ítem.
         */
        fun bind(v: Venta) {
            binding.ventaNumero.text = v.numeroComprobante.ifEmpty { "#${v.id}" }
            binding.ventaCliente.text = v.tercero?.nombreDisplay ?: "Sin cliente"
            binding.ventaEstado.text = v.estado
            binding.ventaTotal.text = v.total.formatCOP()
            binding.ventaFecha.text = v.fecha
            binding.root.setOnClickListener { onClick(v) }
        }
    }

    /**
     * Crea un nuevo [ViewHolder] inflando el layout del ítem de venta.
     *
     * @param parent ViewGroup padre al que se adjuntará la vista.
     * @param viewType Tipo de vista (no utilizado; todos los ítems son del mismo tipo).
     * @return Nuevo [ViewHolder] listo para ser enlazado con datos.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVentaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * Enlaza la [Venta] en la posición indicada con su [ViewHolder].
     *
     * @param holder ViewHolder que debe ser actualizado.
     * @param position Posición de la venta en la lista.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    /**
     * Callback de diferencias utilizado por [ListAdapter] para detectar
     * cambios en la lista y animar las actualizaciones de forma eficiente.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Venta>() {
        /** Compara dos ventas por su identificador numérico único. */
        override fun areItemsTheSame(a: Venta, b: Venta) = a.id == b.id

        /** Compara el contenido completo de dos ventas (comparación estructural). */
        override fun areContentsTheSame(a: Venta, b: Venta) = a == b
    }
}

/**
 * Extensión para formatear un [Double] como moneda colombiana (COP).
 *
 * @return Cadena con el valor formateado, por ejemplo "$1.500,00".
 */
private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
