package com.example.concentradospt.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.concentradospt.R
import com.example.concentradospt.databinding.FragmentPagoBinding
import com.example.concentradospt.ui.carrito.CartViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PagoFragment : Fragment() {

    private var _binding: FragmentPagoBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()

    private val bancos = listOf(
        "Bancolombia", "Davivienda", "Banco de Bogotá", "BBVA", "Nequi",
        "Banco Popular", "AV Villas", "Colpatria", "Banco de Occidente"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPagoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pedidoId = arguments?.getString("pedidoId") ?: ""
        val total = cartViewModel.total

        binding.pagoPedidoId.text = pedidoId.takeLast(8).uppercase()
        binding.pagoTotal.text = total.formatCOP()

        binding.pagoToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupBancoDropdown()
        setupMetodoPago()

        binding.pagoBtnConfirmar.setOnClickListener { procesarPago(pedidoId) }
    }

    private fun setupBancoDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, bancos)
        binding.pagoActvBanco.setAdapter(adapter)
    }

    private fun setupMetodoPago() {
        binding.pagoRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            binding.pagoFormTarjeta.visibility = View.GONE
            binding.pagoFormPse.visibility = View.GONE
            binding.pagoCardContraentrega.visibility = View.GONE

            when (checkedId) {
                R.id.pago_radio_tarjeta -> binding.pagoFormTarjeta.visibility = View.VISIBLE
                R.id.pago_radio_pse -> binding.pagoFormPse.visibility = View.VISIBLE
                R.id.pago_radio_contraentrega -> binding.pagoCardContraentrega.visibility = View.VISIBLE
            }
        }
    }

    private fun procesarPago(pedidoId: String) {
        val metodo = binding.pagoRadioGroup.checkedRadioButtonId

        if (metodo == R.id.pago_radio_tarjeta) {
            val numero = binding.pagoEtNumero.text?.toString()?.trim() ?: ""
            val nombre = binding.pagoEtNombre.text?.toString()?.trim() ?: ""
            val vence = binding.pagoEtVence.text?.toString()?.trim() ?: ""
            val cvv = binding.pagoEtCvv.text?.toString()?.trim() ?: ""

            if (numero.length < 13) {
                binding.pagoTilNumero.error = "Número de tarjeta inválido"
                return
            }
            if (nombre.isEmpty()) {
                binding.pagoTilNombre.error = "Ingrese el nombre"
                return
            }
            if (!vence.matches(Regex("\\d{2}/\\d{2}"))) {
                binding.pagoTilVence.error = "Formato MM/AA"
                return
            }
            if (cvv.length < 3) {
                binding.pagoTilCvv.error = "CVV inválido"
                return
            }
        } else if (metodo == R.id.pago_radio_pse) {
            val documento = binding.pagoEtDocumentoPse.text?.toString()?.trim() ?: ""
            if (documento.isEmpty()) {
                binding.pagoTilDocumentoPse.error = "Ingrese su número de documento"
                return
            }
        }

        clearErrors()
        setLoading(true)

        lifecycleScope.launch {
            delay(2000L)
            setLoading(false)

            val args = Bundle().apply {
                putString("pedidoId", pedidoId)
            }
            findNavController().navigate(R.id.action_pago_to_exito, args)
        }
    }

    private fun clearErrors() {
        binding.pagoTilNumero.error = null
        binding.pagoTilNombre.error = null
        binding.pagoTilVence.error = null
        binding.pagoTilCvv.error = null
        binding.pagoTilDocumentoPse.error = null
    }

    private fun setLoading(loading: Boolean) {
        binding.pagoProgress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.pagoBtnConfirmar.isEnabled = !loading
        binding.pagoRadioGroup.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
