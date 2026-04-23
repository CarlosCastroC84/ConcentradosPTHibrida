package com.example.concentradospt.ui.carrito

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.concentradospt.R
import com.example.concentradospt.databinding.CarritoFragmentBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CarritoFragment : Fragment() {

    private var _binding: CarritoFragmentBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()

    private val adapter = CarritoAdapter(
        onIncrease = { id -> cartViewModel.increase(id) },
        onDecrease = { id -> cartViewModel.decrease(id) }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CarritoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.carritoRvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.carritoRvItems.adapter = adapter

        observeCart()

        binding.carritoBtnCheckout.setOnClickListener {
            findNavController().navigate(R.id.action_cart_to_checkout)
        }
    }

    private fun observeCart() {
        lifecycleScope.launch {
            cartViewModel.items.collect { items ->
                adapter.submitList(items.toList())

                val isEmpty = items.isEmpty()
                binding.carritoSummaryCard.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.carritoBtnCheckout.isEnabled = !isEmpty

                val subtotal = cartViewModel.subtotal
                val iva = cartViewModel.iva
                val total = cartViewModel.total

                binding.carritoSubtotal.text = subtotal.formatCOP()
                binding.carritoShipping.text = "Gratis"
                binding.carritoTaxes.text = iva.formatCOP()
                binding.carritoTotal.text = total.formatCOP()
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