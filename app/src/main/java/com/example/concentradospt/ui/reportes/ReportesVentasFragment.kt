package com.example.concentradospt.ui.reportes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.concentradospt.data.model.admin.Venta
import com.example.concentradospt.databinding.FragmentReportesVentasBinding
import com.example.concentradospt.ui.admin.VentasUiState
import com.example.concentradospt.ui.admin.VentasViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ReportesVentasFragment : Fragment() {

    private var _binding: FragmentReportesVentasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VentasViewModel by viewModels()
    private var allVentas: List<Venta> = emptyList()

    private val periodos = listOf("Esta semana", "Este mes", "Este año", "Todo")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportesVentasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvToolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val periodosAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            periodos
        )
        binding.rvSpinnerPeriod.adapter = periodosAdapter
        binding.rvSpinnerPeriod.setSelection(1)

        binding.rvBtnExportar.setOnClickListener {
            Snackbar.make(binding.root, "Función disponible en versión Pro", Snackbar.LENGTH_SHORT).show()
        }

        observeVentas()
    }

    private fun observeVentas() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is VentasUiState.Loading -> Unit
                    is VentasUiState.Success -> {
                        allVentas = state.ventas
                        updateDashboard(allVentas)
                    }
                    is VentasUiState.Error -> {
                        updateDashboard(emptyList())
                    }
                }
            }
        }
    }

    private fun updateDashboard(ventas: List<Venta>) {
        val totalIngresos = ventas.sumOf { it.total }
        val totalPedidos = ventas.size
        val promedio = if (totalPedidos > 0) totalIngresos / totalPedidos else 0.0

        binding.rvKpiIngresos.text = totalIngresos.formatCOP()
        binding.rvKpiPedidos.text = totalPedidos.toString()
        binding.rvKpiPromedio.text = promedio.formatCOP()

        val generadas = ventas.count { it.estado.equals("GENERADA", true) }
        val borradores = ventas.count { it.estado.equals("BORRADOR", true) }
        val anuladas = ventas.count { it.estado.equals("ANULADA", true) }
        val maxCount = maxOf(generadas, borradores, anuladas, 1)

        updateBar(binding.rvBarGenerada, binding.rvBarValGenerada, generadas, maxCount)
        updateBar(binding.rvBarBorrador, binding.rvBarValBorrador, borradores, maxCount)
        updateBar(binding.rvBarAnulada, binding.rvBarValAnulada, anuladas, maxCount)

        updateTopProductos(ventas)
    }

    private fun updateBar(barView: View, labelView: TextView, count: Int, max: Int) {
        val maxWidthDp = 240
        val widthDp = if (max > 0) (maxWidthDp * count / max).coerceAtLeast(if (count > 0) 8 else 0) else 0
        val density = resources.displayMetrics.density
        barView.layoutParams = (barView.layoutParams as ViewGroup.LayoutParams).also {
            (it as? android.widget.LinearLayout.LayoutParams)?.width = (widthDp * density).toInt()
        }
        labelView.text = count.toString()
    }

    private fun updateTopProductos(ventas: List<Venta>) {
        val container = binding.rvTopProductosContainer
        container.removeAllViews()

        val productoCounts = mutableMapOf<String, Int>()
        ventas.forEach { venta ->
            venta.detalles.forEach { detalle ->
                val nombre = detalle.productoDescripcion.ifEmpty { "Producto #${detalle.productoId}" }
                productoCounts[nombre] = (productoCounts[nombre] ?: 0) + detalle.cantidad.toInt()
            }
        }

        if (productoCounts.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text = "Sin datos de productos disponibles"
                textSize = 12f
            }
            container.addView(empty)
            return
        }

        productoCounts.entries
            .sortedByDescending { it.value }
            .take(5)
            .forEachIndexed { idx, entry ->
                val row = TextView(requireContext()).apply {
                    text = "${idx + 1}. ${entry.key}  —  ${entry.value} uds."
                    textSize = 13f
                    setPadding(0, 4, 0, 4)
                }
                container.addView(row)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
