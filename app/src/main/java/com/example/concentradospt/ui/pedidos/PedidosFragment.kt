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
import androidx.core.os.bundleOf
import com.example.concentradospt.R
import com.example.concentradospt.databinding.FragmentPedidosBinding
import kotlinx.coroutines.launch

/**
 * Fragmento que muestra el historial de pedidos del usuario autenticado.
 *
 * Presenta una lista de pedidos ordenados cronológicamente. Al pulsar
 * sobre un pedido, navega al [DetallePedidoFragment] pasando el ID
 * del pedido como argumento. Gestiona los estados de carga, éxito y error
 * mediante [PedidosViewModel].
 */
class PedidosFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: FragmentPedidosBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel que carga y expone el listado de pedidos del usuario. */
    private val viewModel: PedidosViewModel by viewModels()

    /**
     * Adaptador de la lista de pedidos. Al hacer clic en un pedido,
     * navega al detalle pasando el [Pedido.pedidoId] como argumento.
     */
    private val adapter = PedidosAdapter(
        onClick = { pedido ->
            findNavController().navigate(
                R.id.action_pedidos_to_detalle_pedido,
                bundleOf("pedidoId" to pedido.pedidoId)
            )
        }
    )

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidosBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura la barra de herramientas, el RecyclerView y comienza
     * la observación del estado del ViewModel.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pedidosToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.pedidosRv.layoutManager = LinearLayoutManager(requireContext())
        binding.pedidosRv.adapter = adapter

        observeState()
    }

    /**
     * Observa el [PedidosState] emitido por el ViewModel y actualiza la UI.
     *
     * - [PedidosState.Loading]: muestra el indicador de carga y oculta la lista.
     * - [PedidosState.Success]: muestra la lista o un mensaje si está vacía.
     * - [PedidosState.Error]: muestra el mensaje de error en lugar de la lista.
     */
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is PedidosState.Loading -> {
                        binding.pedidosProgress.visibility = View.VISIBLE
                        binding.pedidosRv.visibility = View.GONE
                        binding.pedidosTvEmpty.visibility = View.GONE
                    }
                    is PedidosState.Success -> {
                        binding.pedidosProgress.visibility = View.GONE
                        if (state.pedidos.isEmpty()) {
                            binding.pedidosRv.visibility = View.GONE
                            binding.pedidosTvEmpty.visibility = View.VISIBLE
                            binding.pedidosTvEmpty.text = "Aún no tienes pedidos"
                        } else {
                            binding.pedidosRv.visibility = View.VISIBLE
                            binding.pedidosTvEmpty.visibility = View.GONE
                            adapter.submitList(state.pedidos)
                        }
                    }
                    is PedidosState.Error -> {
                        binding.pedidosProgress.visibility = View.GONE
                        binding.pedidosRv.visibility = View.GONE
                        binding.pedidosTvEmpty.visibility = View.VISIBLE
                        binding.pedidosTvEmpty.text = state.message
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
