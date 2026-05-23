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

/**
 * Fragmento que presenta el perfil del vendedor autenticado.
 *
 * Muestra el nombre, cédula y avatar (inicial del nombre) del vendedor,
 * así como estadísticas de su actividad: número total de pedidos gestionados
 * y el importe acumulado de ventas en formato COP.
 *
 * Los datos del vendedor se obtienen directamente de [VendedorActivity],
 * mientras que las estadísticas se derivan del estado de [PedidosVendedorViewModel].
 * También incluye el botón de cierre de sesión del panel del vendedor.
 */
class PerfilVendedorFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: FragmentPerfilVendedorBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /**
     * ViewModel que provee el estado de los pedidos del vendedor,
     * utilizado para calcular las estadísticas del perfil.
     */
    private val viewModel: PedidosVendedorViewModel by viewModels()

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilVendedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Obtiene los datos del vendedor desde la actividad contenedora y los muestra.
     * Configura el botón de cierre de sesión y comienza a observar las estadísticas.
     *
     * El avatar muestra la primera letra del nombre del vendedor en mayúscula.
     * Si el nombre está en blanco, se muestra "V" como valor por defecto.
     */
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

    /**
     * Observa el estado [PedidosVendedorState.Success] del ViewModel
     * para calcular y mostrar las estadísticas del vendedor.
     *
     * Calcula el número total de pedidos gestionados y la suma acumulada
     * de sus totales para mostrarlos en los indicadores del perfil.
     */
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
