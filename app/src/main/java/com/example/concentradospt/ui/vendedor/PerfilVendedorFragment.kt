package com.example.concentradospt.ui.vendedor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.concentradospt.VendedorActivity
import com.example.concentradospt.databinding.FragmentPerfilVendedorBinding
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PerfilVendedorFragment : Fragment() {

    private var _binding: FragmentPerfilVendedorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PedidosVendedorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilVendedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as VendedorActivity
        val nombre = activity.nombre
        val cedula = activity.cedula

        binding.pvNombre.text = nombre.ifBlank { "Vendedor" }
        binding.pvCedula.text = "Cédula: ${cedula.ifBlank { "—" }}"
        binding.pvAvatar.text = nombre.firstOrNull()?.uppercase() ?: "V"

        binding.pvBtnLogout.setOnClickListener { activity.vendedorSignOut() }

        observeStats()
    }

    private fun observeStats() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if (state is PedidosVendedorState.Success) {
                    val pedidos = state.pedidos
                    val total = pedidos.sumOf { it.total }
                    binding.pvStatPedidos.text = pedidos.size.toString()
                    binding.pvStatTotal.text = total.formatCOP()
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
