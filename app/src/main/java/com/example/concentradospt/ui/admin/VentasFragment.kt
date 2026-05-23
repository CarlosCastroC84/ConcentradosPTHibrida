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

/**
 * Fragmento que presenta el listado de ventas registradas en el sistema.
 *
 * Muestra todas las ventas disponibles a través de un RecyclerView con
 * [VentasAdapter]. Al pulsar sobre una venta, navega al [DetalleVentaFragment]
 * pasando el ID de la venta como argumento. Gestiona los estados de
 * carga, éxito (con lista vacía) y error mediante [VentasViewModel].
 */
class VentasFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: FragmentVentasBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel que carga y expone el listado de ventas desde el backend. */
    private val viewModel: VentasViewModel by viewModels()

    /**
     * Adaptador de la lista de ventas.
     * Al pulsar una venta, navega al detalle pasando el ID como argumento de Bundle.
     */
    private val adapter = VentasAdapter(onClick = { venta ->
        findNavController().navigate(
            R.id.action_ventas_to_detalle_venta,
            bundleOf("ventaId" to venta.id)
        )
    })

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVentasBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura el RecyclerView con el adaptador y comienza a observar el estado del ViewModel.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ventasRv.layoutManager = LinearLayoutManager(requireContext())
        binding.ventasRv.adapter = adapter

        observeState()
    }

    /**
     * Observa el [VentasUiState] emitido por el ViewModel y actualiza la UI.
     *
     * - Loading: muestra el indicador de progreso y oculta la lista.
     * - Success: muestra la lista o un mensaje de vacío si no hay ventas registradas.
     * - Error: muestra el mensaje de error en lugar de la lista.
     */
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

    /**
     * Libera el binding al destruir la vista para prevenir pérdidas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
