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

class PedidosVendedorFragment : Fragment() {

    private var _binding: FragmentPedidosVendedorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PedidosVendedorViewModel by viewModels()

    private val adapter = VentasAdapter { venta ->
        findNavController().navigate(
            R.id.action_pedidos_vend_to_detalle,
            android.os.Bundle().apply { putLong("ventaId", venta.id) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidosVendedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pedvRv.layoutManager = LinearLayoutManager(requireContext())
        binding.pedvRv.adapter = adapter

        setupChipFilters()
        observeState()
    }

    private fun setupChipFilters() {
        binding.pedvChipTodos.setOnClickListener { viewModel.filtrar(null) }
        binding.pedvChipPendiente.setOnClickListener { viewModel.filtrar("PENDIENTE") }
        binding.pedvChipEnProceso.setOnClickListener { viewModel.filtrar("EN_PROCESO") }
        binding.pedvChipEntregado.setOnClickListener { viewModel.filtrar("ENTREGADA") }
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
