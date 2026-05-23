package com.example.concentradospt.ui.pedidos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.concentradospt.databinding.FragmentDetallePedidoBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Fragmento que presenta el detalle completo de un pedido específico.
 *
 * Recibe el ID del pedido como argumento de navegación ("pedidoId"),
 * lo carga mediante [DetallePedidoViewModel] y muestra la información:
 * número de pedido, estado, fecha, dirección de entrega, lista de productos,
 * subtotal, IVA y total. Gestiona los estados de carga, éxito y error.
 */
class DetallePedidoFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: FragmentDetallePedidoBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel que carga los datos del pedido desde la fuente de datos. */
    private val viewModel: DetallePedidoViewModel by viewModels()

    /** Adaptador para la lista de productos incluidos en el pedido. */
    private val itemsAdapter = DetallePedidoItemAdapter()

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetallePedidoBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura la barra de herramientas, el RecyclerView de ítems,
     * solicita la carga del pedido y comienza a observar el estado.
     *
     * El ID del pedido se recupera de los argumentos de navegación.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.detallePedidoToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.detallePedidoRvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.detallePedidoRvItems.adapter = itemsAdapter

        val pedidoId = arguments?.getString("pedidoId").orEmpty()
        if (pedidoId.isNotBlank()) viewModel.loadPedido(pedidoId)

        observeState()
    }

    /**
     * Observa el [DetallePedidoState] emitido por el ViewModel y actualiza la UI.
     *
     * - [DetallePedidoState.Loading]: muestra el indicador de carga.
     * - [DetallePedidoState.Success]: rellena todos los campos del pedido y calcula el IVA.
     * - [DetallePedidoState.Error]: muestra el mensaje de error.
     */
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is DetallePedidoState.Loading -> {
                        binding.detallePedidoProgress.visibility = View.VISIBLE
                        binding.detallePedidoScroll.visibility = View.GONE
                        binding.detallePedidoTvError.visibility = View.GONE
                    }
                    is DetallePedidoState.Success -> {
                        binding.detallePedidoProgress.visibility = View.GONE
                        binding.detallePedidoTvError.visibility = View.GONE
                        binding.detallePedidoScroll.visibility = View.VISIBLE

                        val p = state.pedido
                        binding.detallePedidoId.text = "Pedido #${p.pedidoId.takeLast(8).uppercase()}"
                        binding.detallePedidoEstado.text = p.estado.replaceFirstChar { it.uppercase() }
                        binding.detallePedidoFecha.text = p.fechaCreacion.take(10)
                        binding.detallePedidoDireccion.text = p.direccionEntrega

                        itemsAdapter.submitList(p.items)

                        // El IVA se calcula como la diferencia entre el total y la suma de subtotales
                        val subtotal = p.items.sumOf { it.subtotal }
                        val iva = p.total - subtotal
                        binding.detallePedidoSubtotal.text = subtotal.formatCOP()
                        binding.detallePedidoIva.text = iva.formatCOP()
                        binding.detallePedidoTotal.text = p.total.formatCOP()
                    }
                    is DetallePedidoState.Error -> {
                        binding.detallePedidoProgress.visibility = View.GONE
                        binding.detallePedidoScroll.visibility = View.GONE
                        binding.detallePedidoTvError.visibility = View.VISIBLE
                        binding.detallePedidoTvError.text = state.message
                    }
                }
            }
        }
    }

    /**
     * Libera el binding al destruir la vista para prevenir pérdidas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Extensión para formatear un [Double] como moneda colombiana (COP).
 *
 * @return Cadena con el valor formateado, por ejemplo "$1.500,00".
 */
private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
