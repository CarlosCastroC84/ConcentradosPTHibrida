package com.example.concentradospt.ui.checkout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.concentradospt.R
import com.example.concentradospt.databinding.FragmentPagoBinding
import com.example.concentradospt.ui.carrito.CartViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Fragmento de selección y procesamiento del método de pago.
 *
 * Presenta al usuario tres opciones de pago:
 * - **Tarjeta de crédito/débito**: solicita número, nombre, fecha de vencimiento y CVV,
 *   y abre [EpaycoCheckoutActivity] para procesar el pago en línea.
 * - **PSE**: solicita el número de documento y abre [EpaycoCheckoutActivity].
 * - **Contraentrega**: simula un breve procesamiento y navega directamente al éxito.
 *
 * Muestra el identificador abreviado del pedido y el total formateado en COP.
 * Al completar el pago exitosamente, navega al [PagoExitoFragment] con el ID del
 * pedido y la referencia de ePayco.
 */
class PagoFragment : Fragment() {

    /**
     * Referencia interna al binding, anulable para evitar fugas de memoria
     * al destruirse la vista del fragmento.
     */
    private var _binding: FragmentPagoBinding? = null

    /**
     * Acceso seguro al binding. Solo debe usarse entre [onCreateView] y [onDestroyView].
     */
    private val binding get() = _binding!!

    /** ViewModel compartido a nivel de actividad que contiene el estado del carrito de compras. */
    private val cartViewModel: CartViewModel by activityViewModels()

    /** Identificador del pedido actualmente en proceso de pago, recibido como argumento de navegación. */
    private var pedidoIdActual: String = ""

    /**
     * Lista de bancos disponibles para el pago por PSE.
     */
    private val bancos = listOf(
        "Bancolombia", "Davivienda", "Banco de Bogotá", "BBVA", "Nequi",
        "Banco Popular", "AV Villas", "Colpatria", "Banco de Occidente"
    )

    /**
     * Lanzador de actividad para [EpaycoCheckoutActivity].
     *
     * Cuando la actividad de pago devuelve [Activity.RESULT_OK], extrae la referencia
     * de ePayco y navega a la pantalla de éxito. Si el resultado es distinto (cancelación
     * o error), muestra un Snackbar indicando que el pago no se completó.
     */
    private val epaycoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        setLoading(false)
        if (result.resultCode == Activity.RESULT_OK) {
            val refPayco = result.data?.getStringExtra(EpaycoCheckoutActivity.RESULT_REF_PAYCO) ?: ""
            navigarAExito(pedidoIdActual, refPayco)
        } else {
            Snackbar.make(
                binding.root,
                "Pago no completado. Intenta de nuevo.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

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
        _binding = FragmentPagoBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura los componentes de la vista una vez que ha sido creada.
     *
     * Recupera el ID del pedido y el total del carrito desde los argumentos y el ViewModel,
     * los muestra en pantalla, y configura el dropdown de bancos, el selector de método de
     * pago y el botón de confirmación.
     *
     * @param view Vista raíz ya creada del fragmento.
     * @param savedInstanceState Estado previamente guardado del fragmento, o null.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pedidoIdActual = arguments?.getString("pedidoId") ?: ""
        val total = cartViewModel.total

        binding.pagoPedidoId.text = pedidoIdActual.takeLast(8).uppercase()
        binding.pagoTotal.text = total.formatCOP()

        binding.pagoToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupBancoDropdown()
        setupMetodoPago()
        setupFechaVencimiento()

        binding.pagoBtnConfirmar.setOnClickListener { procesarPago() }
    }

    /**
     * Configura el campo de fecha de vencimiento de la tarjeta con formato automático MM/AA.
     *
     * Observa los cambios de texto y reformatea automáticamente la entrada del usuario,
     * insertando la barra separadora tras los primeros dos dígitos del mes.
     */
    private fun setupFechaVencimiento() {
        binding.pagoEtVence.addTextChangedListener(object : TextWatcher {
            private var editing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (editing || s == null) return
                editing = true
                val digits = s.filter { it.isDigit() }.take(4)
                val formatted = when {
                    digits.length > 2 -> "${digits.substring(0, 2)}/${digits.substring(2)}"
                    else -> digits.toString()
                }
                s.replace(0, s.length, formatted)
                editing = false
            }
        })
    }

    /**
     * Configura el dropdown de selección de banco para el pago por PSE,
     * enlazando el adaptador con la lista de [bancos] disponibles.
     */
    private fun setupBancoDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, bancos)
        binding.pagoActvBanco.setAdapter(adapter)
    }

    /**
     * Configura el comportamiento del selector de método de pago (RadioGroup).
     *
     * Oculta todos los formularios específicos de pago y muestra únicamente el
     * correspondiente al método seleccionado por el usuario:
     * - Tarjeta: muestra el formulario de datos de tarjeta.
     * - PSE: muestra el formulario de pago por PSE.
     * - Contraentrega: muestra la tarjeta informativa de contraentrega.
     */
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

    /**
     * Procesa el pago según el método seleccionado en el RadioGroup.
     *
     * Para tarjeta valida número (mínimo 13 dígitos), nombre, fecha de vencimiento (MM/AA)
     * y CVV (mínimo 3 dígitos) antes de abrir ePayco. Para PSE valida el número de
     * documento. Para contraentrega simula un retardo de 800 ms y navega directamente
     * al éxito con la referencia "CONTRAENTREGA".
     */
    private fun procesarPago() {
        val metodo = binding.pagoRadioGroup.checkedRadioButtonId
        clearErrors()

        when (metodo) {
            R.id.pago_radio_tarjeta -> {
                val numero = binding.pagoEtNumero.text?.toString()?.trim() ?: ""
                val nombre = binding.pagoEtNombre.text?.toString()?.trim() ?: ""
                val vence = binding.pagoEtVence.text?.toString()?.trim() ?: ""
                val cvv = binding.pagoEtCvv.text?.toString()?.trim() ?: ""

                if (numero.length < 13) { binding.pagoTilNumero.error = "Número de tarjeta inválido"; return }
                if (nombre.isEmpty()) { binding.pagoTilNombre.error = "Ingrese el nombre"; return }
                if (!vence.matches(Regex("\\d{2}/\\d{2}"))) { binding.pagoTilVence.error = "Formato MM/AA"; return }
                if (cvv.length < 3) { binding.pagoTilCvv.error = "CVV inválido"; return }

                setLoading(true)
                abrirEpayco()
            }

            R.id.pago_radio_pse -> {
                val documento = binding.pagoEtDocumentoPse.text?.toString()?.trim() ?: ""
                if (documento.isEmpty()) { binding.pagoTilDocumentoPse.error = "Ingrese su número de documento"; return }

                setLoading(true)
                abrirEpayco()
            }

            R.id.pago_radio_contraentrega -> {
                setLoading(true)
                lifecycleScope.launch {
                    delay(800L)
                    setLoading(false)
                    navigarAExito(pedidoIdActual, "CONTRAENTREGA")
                }
            }
        }
    }

    /**
     * Abre [EpaycoCheckoutActivity] para procesar el pago en línea mediante el WebView de ePayco.
     * Pasa el identificador del pedido y el total como extras del Intent.
     */
    private fun abrirEpayco() {
        val intent = Intent(requireContext(), EpaycoCheckoutActivity::class.java).apply {
            putExtra(EpaycoCheckoutActivity.EXTRA_PEDIDO_ID, pedidoIdActual)
            putExtra(EpaycoCheckoutActivity.EXTRA_TOTAL, cartViewModel.total)
        }
        epaycoLauncher.launch(intent)
    }

    /**
     * Navega hacia [PagoExitoFragment] pasando el identificador del pedido y
     * la referencia de la transacción de ePayco como argumentos de navegación.
     *
     * @param pedidoId Identificador único del pedido procesado.
     * @param refPayco Referencia de la transacción ePayco, o "CONTRAENTREGA" si aplica.
     */
    private fun navigarAExito(pedidoId: String, refPayco: String) {
        val args = Bundle().apply {
            putString("pedidoId", pedidoId)
            putString("refPayco", refPayco)
        }
        findNavController().navigate(R.id.action_pago_to_exito, args)
    }

    /**
     * Limpia los mensajes de error de todos los campos del formulario de pago,
     * preparando la interfaz para una nueva validación.
     */
    private fun clearErrors() {
        binding.pagoTilNumero.error = null
        binding.pagoTilNombre.error = null
        binding.pagoTilVence.error = null
        binding.pagoTilCvv.error = null
        binding.pagoTilDocumentoPse.error = null
    }

    /**
     * Controla la visibilidad del indicador de progreso y habilita o deshabilita
     * el botón de confirmación y el grupo de radio durante una operación en curso.
     *
     * @param loading `true` para mostrar el indicador y bloquear los controles;
     *                `false` para ocultarlo y habilitarlos nuevamente.
     */
    private fun setLoading(loading: Boolean) {
        binding.pagoProgress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.pagoBtnConfirmar.isEnabled = !loading
        binding.pagoRadioGroup.isEnabled = !loading
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

/**
 * Función de extensión que formatea un valor [Double] como moneda colombiana (COP).
 *
 * Utiliza [NumberFormat] con el locale `es_CO` para aplicar el símbolo y el
 * formato numérico estándar de pesos colombianos.
 *
 * @return Cadena de texto con el valor formateado en COP (ej. "$1.500.000").
 */
private fun Double.formatCOP(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(this)
