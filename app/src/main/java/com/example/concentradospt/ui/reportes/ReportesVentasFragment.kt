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

/**
 * Fragmento que presenta el dashboard de reportes de ventas para el administrador.
 *
 * Muestra los siguientes indicadores calculados a partir de la lista completa de ventas:
 * - KPIs: ingresos totales, número de pedidos y ticket promedio.
 * - Gráfico de barras horizontal con el conteo de ventas por estado
 *   (Generadas, Borradores, Anuladas), con anchura proporcional al máximo.
 * - Ranking de los 5 productos más vendidos (por unidades) derivado
 *   de los detalles de cada venta.
 *
 * Incluye un selector de período (Spinner) y un botón de exportación
 * (función disponible en versión Pro, muestra Snackbar informativo).
 *
 * Reutiliza [VentasViewModel] del módulo admin para obtener los datos.
 */
class ReportesVentasFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: FragmentReportesVentasBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel del módulo admin reutilizado para cargar la lista completa de ventas. */
    private val viewModel: VentasViewModel by viewModels()

    /** Copia local de todas las ventas cargadas, usada para los cálculos del dashboard. */
    private var allVentas: List<Venta> = emptyList()

    /** Opciones del selector de período disponibles para el usuario. */
    private val periodos = listOf("Esta semana", "Este mes", "Este año", "Todo")

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportesVentasBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura la barra de herramientas, el Spinner de período, el botón de exportación
     * y comienza a observar el estado de ventas del ViewModel.
     *
     * El Spinner se inicializa preseleccionando "Este mes" (índice 1).
     */
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

    /**
     * Observa el [VentasUiState] del ViewModel.
     *
     * Cuando el estado es [VentasUiState.Success], almacena las ventas localmente
     * y actualiza el dashboard completo. En caso de error, actualiza el dashboard
     * con una lista vacía para mostrar ceros en los indicadores.
     */
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

    /**
     * Actualiza todos los indicadores y gráficos del dashboard con la lista de ventas proporcionada.
     *
     * Calcula ingresos totales, número de pedidos, ticket promedio,
     * conteo por estado y delega la construcción del ranking de productos a [updateTopProductos].
     *
     * @param ventas Lista de ventas sobre la que se calculan los indicadores.
     */
    private fun updateDashboard(ventas: List<Venta>) {
        val totalIngresos = ventas.sumOf { it.total }
        val totalPedidos = ventas.size
        val promedio = if (totalPedidos > 0) totalIngresos / totalPedidos else 0.0

        binding.rvKpiIngresos.text = totalIngresos.formatCOP()
        binding.rvKpiPedidos.text = totalPedidos.toString()
        binding.rvKpiPromedio.text = promedio.formatCOP()

        // Conteo de ventas por estado para el gráfico de barras
        val generadas = ventas.count { it.estado.equals("GENERADA", true) }
        val borradores = ventas.count { it.estado.equals("BORRADOR", true) }
        val anuladas = ventas.count { it.estado.equals("ANULADA", true) }
        val maxCount = maxOf(generadas, borradores, anuladas, 1)

        updateBar(binding.rvBarGenerada, binding.rvBarValGenerada, generadas, maxCount)
        updateBar(binding.rvBarBorrador, binding.rvBarValBorrador, borradores, maxCount)
        updateBar(binding.rvBarAnulada, binding.rvBarValAnulada, anuladas, maxCount)

        updateTopProductos(ventas)
    }

    /**
     * Actualiza visualmente la barra de un estado de venta en el gráfico horizontal.
     *
     * La anchura de la barra se calcula proporcionalmente al máximo valor del conjunto
     * (máximo 240dp). Si el conteo es mayor que cero, se asegura un ancho mínimo de 8dp.
     *
     * @param barView   Vista de la barra cuya anchura se va a ajustar.
     * @param labelView TextView donde se muestra el conteo numérico.
     * @param count     Número de ventas en este estado.
     * @param max       Valor máximo entre todos los estados, usado como referencia de escala.
     */
    private fun updateBar(barView: View, labelView: TextView, count: Int, max: Int) {
        val maxWidthDp = 240
        val widthDp = if (max > 0) (maxWidthDp * count / max).coerceAtLeast(if (count > 0) 8 else 0) else 0
        val density = resources.displayMetrics.density
        barView.layoutParams = (barView.layoutParams as ViewGroup.LayoutParams).also {
            (it as? android.widget.LinearLayout.LayoutParams)?.width = (widthDp * density).toInt()
        }
        labelView.text = count.toString()
    }

    /**
     * Construye el ranking de los 5 productos más vendidos en el período indicado.
     *
     * Agrega las cantidades vendidas por nombre de producto a través de todos los
     * detalles de las ventas, ordena descendentemente y muestra las primeras 5 entradas
     * como filas de texto en el contenedor del ranking.
     *
     * Si no hay datos de productos, muestra un mensaje informativo.
     *
     * @param ventas Lista de ventas de cuya que se extraen los detalles de productos.
     */
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
