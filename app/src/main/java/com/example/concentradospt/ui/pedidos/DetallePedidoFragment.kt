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

class DetallePedidoFragment : Fragment() {

    private var _binding: FragmentDetallePedidoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetallePedidoViewModel by viewModels()
    private val itemsAdapter = DetallePedidoItemAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetallePedidoBinding.inflate(inflater, container, false)
        return binding.root
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)