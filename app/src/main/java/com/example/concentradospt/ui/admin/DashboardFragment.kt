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

class DashboardFragment : Fragment() {

    private var _binding: DashboardFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DashboardFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dashboardRvRecentOrders.visibility = View.GONE

        binding.dashboardQuickProducts.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_gestion_productos)
        }
        binding.dashboardQuickUsers.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_gestion_usuarios)
        }
        binding.dashboardBtnNewReport.setOnClickListener {
            findNavController().navigate(R.id.action_admin_dashboard_to_ventas)
        }

        observeState()
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
