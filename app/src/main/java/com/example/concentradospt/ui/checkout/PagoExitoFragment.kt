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

/**
 * Fragmento de confirmación de pago exitoso.
 *
 * Muestra al usuario la confirmación de que su pedido fue procesado correctamente,
 * incluyendo el identificador abreviado del pedido y, cuando aplica, la referencia
 * de la transacción de ePayco. Para pagos con método "CONTRAENTREGA", la referencia
 * no se muestra.
 *
 * Al cargarse, limpia automáticamente el carrito de compras para preparar la aplicación
 * para un nuevo ciclo de compra. Ofrece un botón para regresar a la pantalla de inicio.
 */
class PagoExitoFragment : Fragment() {

    /**
     * Referencia interna al binding, anulable para evitar fugas de memoria
     * al destruirse la vista del fragmento.
     */
    private var _binding: FragmentPagoExitoBinding? = null

    /**
     * Acceso seguro al binding. Solo debe usarse entre [onCreateView] y [onDestroyView].
     */
    private val binding get() = _binding!!

    /** ViewModel compartido a nivel de actividad utilizado para limpiar el carrito tras el pago. */
    private val cartViewModel: CartViewModel by activityViewModels()

    /**
     * Infla el layout del fragmento y lo asigna al binding.
     *
     * @param inflater Objeto para inflar vistas en el contexto del fragmento.
     * @param container Contenedor padre al que se adjuntará el fragmento, o null.
     * @param savedInstanceState Estado previamente guardado del fragmento, o null.
     * @return Vista raíz del fragmento inflada.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPagoExitoBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura la interfaz de confirmación una vez que la vista ha sido creada.
     *
     * Muestra el identificador abreviado del pedido (últimos 8 caracteres en mayúsculas).
     * Si la referencia de ePayco está presente y no es "CONTRAENTREGA", la muestra
     * también en pantalla. Limpia el carrito de compras e inicializa el botón de
     * regreso a la pantalla de inicio.
     *
     * @param view Vista raíz ya creada del fragmento.
     * @param savedInstanceState Estado previamente guardado del fragmento, o null.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pedidoId = arguments?.getString("pedidoId") ?: ""
        binding.exitoPedidoId.text = "Pedido #${pedidoId.takeLast(8).uppercase()}"

        val refPayco = arguments?.getString("refPayco") ?: ""
        if (refPayco.isNotEmpty() && refPayco != "CONTRAENTREGA") {
            binding.exitoRefPayco.text = "Ref. ePayco: $refPayco"
            binding.exitoRefPayco.visibility = android.view.View.VISIBLE
        }

        cartViewModel.clear()

        binding.exitoBtnInicio.setOnClickListener {
            findNavController().navigate(R.id.nav_home)
        }
    }

    /**
     * Libera la referencia al binding para evitar fugas de memoria cuando
     * la vista del fragmento es destruida.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
