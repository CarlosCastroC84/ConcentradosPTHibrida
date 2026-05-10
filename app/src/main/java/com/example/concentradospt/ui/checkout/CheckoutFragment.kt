package com.example.concentradospt.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.concentradospt.R
import com.example.concentradospt.databinding.FragmentCheckoutBinding
import com.example.concentradospt.ui.carrito.CartViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()
    private val checkoutViewModel: CheckoutViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.checkoutToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        updateSummary()
        observeState()

        binding.checkoutBtnConfirm.setOnClickListener {
            clearFieldErrors()
            val nombre = binding.checkoutEtNombre.text?.toString()?.trim() ?: ""
            val telefono = binding.checkoutEtTelefono.text?.toString()?.trim() ?: ""
            val direccion = binding.checkoutEtDireccion.text?.toString()?.trim() ?: ""

            checkoutViewModel.confirmarPedido(
                items = cartViewModel.items.value,
                nombre = nombre,
                telefono = telefono,
                direccion = direccion
            )
        }
    }

    private fun updateSummary() {
        val items = cartViewModel.items.value
        binding.checkoutTotal.text = cartViewModel.total.formatCOP()
        binding.checkoutItemsCount.text =
            "${items.sumOf { it.cantidad }} ${getString(R.string.checkout_label_items_in_cart)}"
    }

    private fun observeState() {
        lifecycleScope.launch {
            checkoutViewModel.state.collect { state ->
                when (state) {
                    is CheckoutState.Idle -> setLoading(false)

                    is CheckoutState.Loading -> setLoading(true)

                    is CheckoutState.Success -> {
                        setLoading(false)
                        navigateToPago(state.pedidoId)
                    }

                    is CheckoutState.Error -> {
                        setLoading(false)
                        // Si el error es de validación de campo, resaltar el campo
                        when {
                            state.message.contains("nombre") ->
                                binding.checkoutTilNombre.error = state.message
                            state.message.contains("teléfono") || state.message.contains("Número") ->
                                binding.checkoutTilTelefono.error = state.message
                            state.message.contains("dirección") ->
                                binding.checkoutTilDireccion.error = state.message
                            else ->
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                        checkoutViewModel.resetState()
                    }
                }
            }
        }
    }

    private fun navigateToPago(pedidoId: String) {
        checkoutViewModel.resetState()
        val args = android.os.Bundle().apply {
            putString("pedidoId", pedidoId)
        }
        findNavController().navigate(R.id.action_checkout_to_pago, args)
    }

    private fun setLoading(loading: Boolean) {
        binding.checkoutProgress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.checkoutBtnConfirm.isEnabled = !loading
        binding.checkoutEtNombre.isEnabled = !loading
        binding.checkoutEtTelefono.isEnabled = !loading
        binding.checkoutEtDireccion.isEnabled = !loading
    }

    private fun clearFieldErrors() {
        binding.checkoutTilNombre.error = null
        binding.checkoutTilTelefono.error = null
        binding.checkoutTilDireccion.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
