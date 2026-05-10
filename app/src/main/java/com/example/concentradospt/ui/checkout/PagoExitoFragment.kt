package com.example.concentradospt.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.concentradospt.R
import com.example.concentradospt.databinding.FragmentPagoExitoBinding
import com.example.concentradospt.ui.carrito.CartViewModel

class PagoExitoFragment : Fragment() {

    private var _binding: FragmentPagoExitoBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPagoExitoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pedidoId = arguments?.getString("pedidoId") ?: ""
        binding.exitoPedidoId.text = "Pedido #${pedidoId.takeLast(8).uppercase()}"

        cartViewModel.clear()

        binding.exitoBtnInicio.setOnClickListener {
            findNavController().navigate(R.id.nav_home)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
