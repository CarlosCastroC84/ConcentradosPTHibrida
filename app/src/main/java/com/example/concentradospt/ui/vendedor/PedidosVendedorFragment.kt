package com.example.concentradospt.ui.vendedor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.concentradospt.R
import com.example.concentradospt.databinding.FragmentPedidosVendedorBinding
import com.example.concentradospt.ui.admin.VentasAdapter
import kotlinx.coroutines.launch

/**
 * Fragmento del panel del vendedor que muestra los pedidos (ventas) asignados a su usuario.
 *
 * Presenta la lista de pedidos mediante [VentasAdapter] (reutilizado del módulo admin)
 * y ofrece chips de filtro para segmentar los pedidos por estado:
 * Todos, Pendiente, En Proceso y Entregado.
 *
 * Al pulsar sobre un pedido, navega al [DetalleVentaFragment] pasando el ID de la venta.
 * Gestiona los estados de carga, éxito y error a través de [PedidosVendedorViewModel].
 */
class PedidosVendedorFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: FragmentPedidosVendedorBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel que carga y filtra los pedidos del vendedor autenticado. */
    private val viewModel: PedidosVendedorViewModel by viewModels()

    /**
     * Adaptador reutilizado del módulo admin para mostrar ventas.
     * Al pulsar una venta, navega al detalle pasando el ID en el Bundle.
     */
    private val adapter = VentasAdapter { venta ->
        findNavController().navigate(
            R.id.action_pedidos_vend_to_detalle,
            android.os.Bundle().apply { putLong("ventaId", venta.id) }
        )
    }

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidosVendedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura el RecyclerView, los chips de filtro y comienza
     * a observar el estado del ViewModel.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pedvRv.layoutManager = LinearLayoutManager(requireContext())
        binding.pedvRv.adapter = adapter

        setupChipFilters()
        observeState()
    }

    /**
     * Configura los chips de filtro que permiten al vendedor segmentar
     * sus pedidos por estado (null = todos los estados).
     *
     * Cada chip invoca [PedidosVendedorViewModel.filtrar] con el estado correspondiente.
     */
    private fun setupChipFilters() {
        binding.pedvChipTodos.setOnClickListener { viewModel.filtrar(null) }
        binding.pedvChipPendiente.setOnClickListener { viewModel.filtrar("PENDIENTE") }
        binding.pedvChipEnProceso.setOnClickListener { viewModel.filtrar("EN_PROCESO") }
        binding.pedvChipEntregado.setOnClickListener { viewModel.filtrar("ENTREGADA") }
    }

    /**
     * Observa el [PedidosVendedorState] emitido por el ViewModel y actualiza la UI.
     *
     * - Loading: muestra el indicador de progreso y oculta la lista.
     * - Success: muestra la lista de pedidos o un mensaje de vacío si no hay resultados.
     * - Error: muestra el mensaje de error.
     */
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is PedidosVendedorState.Loading -> {
                        binding.pedvProgress.visibility = View.VISIBLE
                        binding.pedvRv.visibility = View.GONE
                        binding.pedvTvEmpty.visibility = View.GONE
                    }
                    is PedidosVendedorState.Success -> {
                        binding.pedvProgress.visibility = View.GONE
                        if (state.pedidos.isEmpty()) {
                            binding.pedvRv.visibility = View.GONE
                            binding.pedvTvEmpty.visibility = View.VISIBLE
                            binding.pedvTvEmpty.text = "No hay pedidos disponibles"
                        } else {
                            binding.pedvRv.visibility = View.VISIBLE
                            binding.pedvTvEmpty.visibility = View.GONE
                            adapter.submitList(state.pedidos)
                        }
                    }
                    is PedidosVendedorState.Error -> {
                        binding.pedvProgress.visibility = View.GONE
                        binding.pedvTvEmpty.visibility = View.VISIBLE
                        binding.pedvTvEmpty.text = state.message
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
