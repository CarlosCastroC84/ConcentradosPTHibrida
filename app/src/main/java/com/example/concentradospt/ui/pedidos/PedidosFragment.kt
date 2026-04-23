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

class PedidosFragment : Fragment() {

    private var _binding: FragmentPedidosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PedidosViewModel by viewModels()

    private val adapter = PedidosAdapter(
        onClick = { pedido ->
            findNavController().navigate(
                R.id.action_pedidos_to_detalle_pedido,
                bundleOf("pedidoId" to pedido.pedidoId)
            )
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pedidosToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.pedidosRv.layoutManager = LinearLayoutManager(requireContext())
        binding.pedidosRv.adapter = adapter

        observeState()
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
