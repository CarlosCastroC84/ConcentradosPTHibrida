package com.example.concentradospt.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.concentradospt.databinding.FragmentDetalleVentaBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Fragmento que presenta el detalle completo de una venta seleccionada.
 *
 * Recibe el ID de la venta como argumento de navegación ("ventaId"),
 * la carga mediante [DetalleVentaViewModel] y muestra:
 * - Número de comprobante, estado y fecha de la venta.
 * - Datos del cliente y del vendedor responsable.
 * - Lista de productos vendidos con cantidad y valor total por línea.
 * - Subtotal, IVA y total de la venta en formato COP.
 *
 * Gestiona los estados de carga, éxito y error de la operación.
 */
class DetalleVentaFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: FragmentDetalleVentaBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel que carga los datos de la venta desde el backend. */
    private val viewModel: DetalleVentaViewModel by viewModels()

    /** Adaptador para la lista de ítems (líneas de detalle) de la venta. */
    private val itemAdapter = DetalleVentaItemAdapter()

    /** Formateador de moneda colombiana reutilizable en esta pantalla. */
    private val cop = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleVentaBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura la barra de herramientas, el RecyclerView de ítems,
     * solicita la carga de la venta y comienza a observar el estado.
     *
     * El ID de la venta se recupera del Bundle de argumentos de navegación.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.detalleVentaToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.detalleVentaRvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.detalleVentaRvItems.adapter = itemAdapter

        val ventaId = arguments?.getLong("ventaId") ?: 0L
        viewModel.loadVenta(ventaId)

        observeState()
    }

    /**
     * Observa el [DetalleVentaState] emitido por el ViewModel y actualiza la UI.
     *
     * - Loading: muestra el indicador de progreso y oculta el contenido.
     * - Error: muestra el mensaje de error.
     * - Success: llama a [populateVenta] para rellenar todos los campos.
     */
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is DetalleVentaState.Loading -> {
                        binding.detalleVentaProgress.visibility = View.VISIBLE
                        binding.detalleVentaTvError.visibility = View.GONE
                        binding.detalleVentaScroll.visibility = View.GONE
                    }
                    is DetalleVentaState.Error -> {
                        binding.detalleVentaProgress.visibility = View.GONE
                        binding.detalleVentaTvError.visibility = View.VISIBLE
                        binding.detalleVentaTvError.text = state.message
                        binding.detalleVentaScroll.visibility = View.GONE
                    }
                    is DetalleVentaState.Success -> {
                        binding.detalleVentaProgress.visibility = View.GONE
                        binding.detalleVentaTvError.visibility = View.GONE
                        binding.detalleVentaScroll.visibility = View.VISIBLE
                        populateVenta(state.venta)
                    }
                }
            }
        }
    }

    /**
     * Rellena todas las vistas del detalle con los datos de la [Venta] proporcionada.
     *
     * Construye el nombre del cliente y el vendedor concatenando sus campos,
     * carga los ítems en el adaptador y formatea subtotal, IVA y total.
     *
     * @param venta Venta cuyos datos se van a mostrar en pantalla.
     */
    private fun populateVenta(venta: com.example.concentradospt.data.model.admin.Venta) {
        binding.detalleVentaNumero.text = venta.numeroComprobante
        binding.detalleVentaEstado.text = venta.estado
        binding.detalleVentaFecha.text = venta.fecha

        val cliente = venta.tercero?.nombreDisplay
            ?: venta.tercero?.let { "${it.primerNombre} ${it.primerApellido}".trim() }
            ?: "—"
        binding.detalleVentaCliente.text = "Cliente: $cliente"

        val vendedor = venta.vendedor?.let { "${it.nombre} ${it.apellido}".trim() } ?: "—"
        binding.detalleVentaVendedor.text = "Vendedor: $vendedor"

        itemAdapter.submitList(venta.detalles)

        binding.detalleVentaSubtotal.text = cop.format(venta.subtotal)
        binding.detalleVentaIva.text = cop.format(venta.ivaValor)
        binding.detalleVentaTotal.text = cop.format(venta.total)
    }

    /**
     * Libera el binding al destruir la vista para prevenir pérdidas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
