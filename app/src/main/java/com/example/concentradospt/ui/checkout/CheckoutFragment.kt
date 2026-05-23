package com.example.concentradospt.ui.checkout

import android.Manifest
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.concentradospt.R
import com.example.concentradospt.databinding.FragmentCheckoutBinding
import com.example.concentradospt.ui.carrito.CartViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Fragmento de confirmación del pedido (checkout).
 *
 * Permite al usuario ingresar los datos de entrega: nombre, teléfono y dirección.
 * Ofrece la opción de obtener la ubicación actual del dispositivo mediante GPS
 * para autocompletar la dirección usando geocodificación inversa. Muestra un
 * resumen del total del carrito y la cantidad de ítems antes de confirmar el pedido.
 *
 * Al confirmar, delega la creación del pedido al [CheckoutViewModel] y, en caso
 * de éxito, navega al fragmento de pago pasando el identificador del pedido creado.
 */
class CheckoutFragment : Fragment() {

    /**
     * Referencia interna al binding, anulable para evitar fugas de memoria
     * al destruirse la vista del fragmento.
     */
    private var _binding: FragmentCheckoutBinding? = null

    /**
     * Acceso seguro al binding. Solo debe usarse entre [onCreateView] y [onDestroyView].
     */
    private val binding get() = _binding!!

    /** ViewModel compartido a nivel de actividad que contiene el estado del carrito de compras. */
    private val cartViewModel: CartViewModel by activityViewModels()

    /** ViewModel local que gestiona la lógica de negocio del proceso de checkout. */
    private val checkoutViewModel: CheckoutViewModel by viewModels()

    /**
     * Lanzador de solicitud de permisos de ubicación (fina y aproximada).
     *
     * Si el usuario concede al menos uno de los permisos, invoca [fetchLocation]
     * para obtener la posición actual. Si los deniega, muestra un Snackbar informativo.
     */
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) fetchLocation()
        else Snackbar.make(binding.root, "Permiso de ubicación denegado", Snackbar.LENGTH_SHORT).show()
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
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura los componentes de la vista una vez que ha sido creada.
     *
     * Inicializa el resumen del carrito, observa el estado del checkout,
     * y configura los listeners para el botón de ubicación (solicita permisos GPS)
     * y el botón de confirmación del pedido (valida campos y llama al ViewModel).
     *
     * @param view Vista raíz ya creada del fragmento.
     * @param savedInstanceState Estado previamente guardado del fragmento, o null.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.checkoutToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        updateSummary()
        observeState()

        binding.checkoutBtnLocation.setOnClickListener {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

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

    /**
     * Obtiene la ubicación actual del dispositivo con alta precisión usando el
     * proveedor de ubicación fusionado de Google Play Services.
     *
     * Utiliza geocodificación inversa con localización en español (Colombia) para
     * construir una dirección legible a partir de las coordenadas. Si la geocodificación
     * no devuelve resultados, coloca directamente las coordenadas en el campo de dirección.
     * Maneja errores de permisos y fallos del proveedor mostrando un Snackbar al usuario.
     */
    private fun fetchLocation() {
        val client = LocationServices.getFusedLocationProviderClient(requireContext())
        try {
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location == null) {
                        Snackbar.make(binding.root, getString(R.string.checkout_location_error), Snackbar.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                    @Suppress("DEPRECATION")
                    val addresses = Geocoder(requireContext(), Locale("es", "CO"))
                        .getFromLocation(location.latitude, location.longitude, 1)
                    val addr = addresses?.firstOrNull()
                    if (addr != null) {
                        val text = buildString {
                            if (!addr.thoroughfare.isNullOrEmpty()) append(addr.thoroughfare)
                            if (!addr.subThoroughfare.isNullOrEmpty()) append(" ${addr.subThoroughfare}")
                            if (!addr.subLocality.isNullOrEmpty()) append(", ${addr.subLocality}")
                            if (!addr.locality.isNullOrEmpty()) append(", ${addr.locality}")
                            if (!addr.adminArea.isNullOrEmpty()) append(", ${addr.adminArea}")
                        }
                        binding.checkoutEtDireccion.setText(text)
                    } else {
                        binding.checkoutEtDireccion.setText(
                            "${location.latitude}, ${location.longitude}"
                        )
                    }
                }
                .addOnFailureListener {
                    Snackbar.make(binding.root, getString(R.string.checkout_location_error), Snackbar.LENGTH_SHORT).show()
                }
        } catch (e: SecurityException) {
            Snackbar.make(binding.root, "Permiso de ubicación requerido", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Actualiza el resumen del pedido en la interfaz mostrando el total formateado
     * en pesos colombianos (COP) y la cantidad total de unidades en el carrito.
     */
    private fun updateSummary() {
        val items = cartViewModel.items.value
        binding.checkoutTotal.text = cartViewModel.total.formatCOP()
        binding.checkoutItemsCount.text =
            "${items.sumOf { it.cantidad }} ${getString(R.string.checkout_label_items_in_cart)}"
    }

    /**
     * Observa el estado del [CheckoutViewModel] y actualiza la interfaz en consecuencia.
     *
     * Reacciona a los estados del flujo de checkout:
     * - [CheckoutState.Idle]: oculta el indicador de carga.
     * - [CheckoutState.Loading]: muestra el indicador de carga.
     * - [CheckoutState.Success]: navega al fragmento de pago con el ID del pedido.
     * - [CheckoutState.Error]: muestra errores de campo en el input correspondiente
     *   (nombre, teléfono, dirección) o en un Snackbar para errores generales.
     */
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

    /**
     * Navega al [PagoFragment] pasando el identificador del pedido recién creado,
     * y resetea el estado del ViewModel antes de la navegación.
     *
     * @param pedidoId Identificador único del pedido creado en el backend.
     */
    private fun navigateToPago(pedidoId: String) {
        checkoutViewModel.resetState()
        val args = android.os.Bundle().apply {
            putString("pedidoId", pedidoId)
        }
        findNavController().navigate(R.id.action_checkout_to_pago, args)
    }

    /**
     * Controla la visibilidad del indicador de progreso y habilita o deshabilita
     * los controles del formulario durante una operación en curso.
     *
     * @param loading `true` para mostrar el indicador y bloquear los controles;
     *                `false` para ocultarlo y habilitarlos nuevamente.
     */
    private fun setLoading(loading: Boolean) {
        binding.checkoutProgress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.checkoutBtnConfirm.isEnabled = !loading
        binding.checkoutBtnLocation.isEnabled = !loading
        binding.checkoutEtNombre.isEnabled = !loading
        binding.checkoutEtTelefono.isEnabled = !loading
        binding.checkoutEtDireccion.isEnabled = !loading
    }

    /**
     * Limpia los mensajes de error de los campos del formulario de checkout,
     * preparando la interfaz para una nueva validación.
     */
    private fun clearFieldErrors() {
        binding.checkoutTilNombre.error = null
        binding.checkoutTilTelefono.error = null
        binding.checkoutTilDireccion.error = null
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
