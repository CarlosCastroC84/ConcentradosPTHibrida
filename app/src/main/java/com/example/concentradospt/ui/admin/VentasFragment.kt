package com.example.concentradospt.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.concentradospt.R
import com.example.concentradospt.databinding.FragmentVentasBinding
import kotlinx.coroutines.launch

class VentasFragment : Fragment() {

    private var _binding: FragmentVentasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VentasViewModel by viewModels()

    private val adapter = VentasAdapter(onClick = { venta ->
        findNavController().navigate(
            R.id.action_ventas_to_detalle_venta,
            bundleOf("ventaId" to venta.id)
        )
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVentasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ventasRv.layoutManager = LinearLayoutManager(requireContext())
        binding.ventasRv.adapter = adapter

        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is VentasUiState.Loading -> {
                        binding.ventasProgress.visibility = View.VISIBLE
                        binding.ventasRv.visibility = View.GONE
                        binding.ventasTvEmpty.visibility = View.GONE
                    }
                    is VentasUiState.Success -> {
                        binding.ventasProgress.visibility = View.GONE
                        if (state.ventas.isEmpty()) {
                            binding.ventasRv.visibility = View.GONE
                            binding.ventasTvEmpty.visibility = View.VISIBLE
                            binding.ventasTvEmpty.text = "No hay ventas registradas"
                        } else {
                            binding.ventasRv.visibility = View.VISIBLE
                            binding.ventasTvEmpty.visibility = View.GONE
                            adapter.submitList(state.ventas)
                        }
                    }
                    is VentasUiState.Error -> {
                        binding.ventasProgress.visibility = View.GONE
                        binding.ventasRv.visibility = View.GONE
                        binding.ventasTvEmpty.visibility = View.VISIBLE
                        binding.ventasTvEmpty.text = state.message
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
