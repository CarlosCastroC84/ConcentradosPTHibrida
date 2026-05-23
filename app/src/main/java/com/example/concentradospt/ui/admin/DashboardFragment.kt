package com.example.concentradospt.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.concentradospt.AdminActivity
import com.example.concentradospt.R
import com.example.concentradospt.databinding.DashboardFragmentBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Fragmento del panel de control principal del administrador (Dashboard).
 *
 * Presenta un resumen de los indicadores clave del negocio:
 * - Total de ventas acumuladas en formato COP.
 * - Número de pedidos pendientes.
 * - Cantidad de usuarios activos en el sistema.
 *
 * Desde este fragmento el administrador puede navegar rápidamente a:
 * gestión de productos, gestión de usuarios, módulo de reportes,
 * o cerrar sesión en la aplicación.
 */
class DashboardFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: DashboardFragmentBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel que carga y provee los indicadores del dashboard. */
    private val viewModel: DashboardViewModel by viewModels()

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DashboardFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura los botones de acceso rápido, el botón de cierre de sesión
     * y comienza a observar los indicadores del ViewModel.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ocultar la lista de pedidos recientes (pendiente de implementación)
        binding.dashboardRvRecentOrders.visibility = View.GONE

        binding.dashboardQuickProducts.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_gestion_productos)
        }
        binding.dashboardQuickUsers.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_gestion_usuarios)
        }
        binding.dashboardBtnNewReport.setOnClickListener {
            findNavController().navigate(R.id.action_admin_dashboard_to_reportes)
        }
        binding.dashboardBtnLogout.setOnClickListener {
            (activity as? AdminActivity)?.adminSignOut()
        }

        observeState()
    }

    /**
     * Observa el estado del [DashboardViewModel] y actualiza los indicadores de la UI.
     *
     * Mientras carga, muestra el texto de carga y guiones en los contadores.
     * Si hay error, indica "Error" en el total de ventas.
     * En caso de éxito, muestra total de ventas en COP, pedidos pendientes y usuarios activos.
     */
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if (state.isLoading) {
                    binding.dashboardTotalSales.text = getString(R.string.loading)
                    binding.dashboardNewOrders.text = "—"
                    binding.dashboardActiveUsers.text = "—"
                    return@collect
                }

                state.error?.let {
                    binding.dashboardTotalSales.text = "Error"
                    return@collect
                }

                binding.dashboardTotalSales.text = state.totalVentas.formatCOP()
                binding.dashboardNewOrders.text = state.ventasPendientes.toString()
                binding.dashboardActiveUsers.text = state.usuariosActivos.toString()
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

/**
 * Extensión para formatear un [Double] como moneda colombiana (COP).
 *
 * @return Cadena con el valor formateado, por ejemplo "$1.500,00".
 */
private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
