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

class DetalleVentaFragment : Fragment() {

    private var _binding: FragmentDetalleVentaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetalleVentaViewModel by viewModels()
    private val itemAdapter = DetalleVentaItemAdapter()
    private val cop = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleVentaBinding.inflate(inflater, container, false)
        return binding.root
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
