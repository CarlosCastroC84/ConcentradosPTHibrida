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

/**
 * Fragmento que muestra el carrito de compras del usuario.
 *
 * Presenta la lista de productos agregados, sus cantidades, subtotales,
 * IVA y total. Permite aumentar o disminuir la cantidad de cada ítem
 * y navega al flujo de pago (checkout) cuando el usuario confirma.
 */
class CarritoFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: CarritoFragmentBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel compartido con otras pantallas que gestiona el estado del carrito. */
    private val cartViewModel: CartViewModel by activityViewModels()

    /**
     * Adaptador del RecyclerView del carrito.
     * Recibe lambdas para incrementar o decrementar la cantidad de un producto.
     */
    private val adapter = CarritoAdapter(
        onIncrease = { id -> cartViewModel.increase(id) },
        onDecrease = { id -> cartViewModel.decrease(id) }
    )

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CarritoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura el RecyclerView, el adaptador y los listeners de UI
     * una vez que la vista ya ha sido creada.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.carritoRvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.carritoRvItems.adapter = adapter

        observeCart()

        binding.carritoBtnCheckout.setOnClickListener {
            findNavController().navigate(R.id.action_cart_to_checkout)
        }
    }

    /**
     * Observa el flujo de ítems del carrito y actualiza la UI en consecuencia.
     *
     * Cuando el carrito está vacío, oculta el resumen y desactiva el botón de pago.
     * Cuando hay ítems, muestra subtotal, envío (gratis), IVA y total en formato COP.
     */
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
