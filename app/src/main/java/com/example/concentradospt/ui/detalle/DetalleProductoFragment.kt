package com.example.concentradospt.ui.detalle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.concentradospt.R
import com.example.concentradospt.data.model.Producto
import com.example.concentradospt.databinding.FragmentDetalleProductoBinding
import com.example.concentradospt.ui.carrito.CartViewModel
import com.example.concentradospt.ui.producto.ProductViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Fragmento que presenta la información completa de un producto seleccionado.
 *
 * Muestra imagen, nombre, categoría, marca, presentación, precio y descripción
 * del producto. Permite al usuario:
 * - Seleccionar la cantidad deseada (respetando el stock disponible).
 * - Agregar la cantidad indicada al carrito con un solo botón.
 * - Acceder directamente al carrito desde el Snackbar de confirmación.
 *
 * El producto se obtiene del estado compartido de [ProductViewModel].
 */
class DetalleProductoFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: FragmentDetalleProductoBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel compartido que provee el producto seleccionado actualmente. */
    private val productViewModel: ProductViewModel by activityViewModels()

    /** ViewModel compartido que gestiona el carrito de compras. */
    private val cartViewModel: CartViewModel by activityViewModels()

    /** Cantidad de unidades que el usuario desea agregar al carrito. Valor mínimo: 1. */
    private var cantidad = 1

    /** Referencia al producto actualmente mostrado, necesaria para operar con el carrito. */
    private var producto: Producto? = null

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleProductoBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura la barra de herramientas, los botones de control de cantidad,
     * el botón de agregar al carrito y observa el producto seleccionado.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.detalleToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Observa el producto seleccionado en el ViewModel compartido y lo renderiza
        lifecycleScope.launch {
            productViewModel.selectedProducto.collect { p ->
                p?.let { bindProducto(it) }
            }
        }

        // Botón para disminuir la cantidad (mínimo 1)
        binding.detalleBtnDecrease.setOnClickListener {
            if (cantidad > 1) {
                cantidad--
                binding.detalleCantidad.text = cantidad.toString()
            }
        }

        // Botón para aumentar la cantidad (máximo: stock disponible)
        binding.detalleBtnIncrease.setOnClickListener {
            val stock = producto?.stock ?: Int.MAX_VALUE
            if (cantidad < stock) {
                cantidad++
                binding.detalleCantidad.text = cantidad.toString()
            }
        }

        // Agrega la cantidad seleccionada al carrito y muestra confirmación con acceso rápido
        binding.detalleBtnAddCart.setOnClickListener {
            producto?.let { p ->
                repeat(cantidad) { cartViewModel.addToCart(p) }
                Snackbar.make(
                    binding.root,
                    "\"${p.nombre}\" agregado al carrito",
                    Snackbar.LENGTH_SHORT
                ).setAction("Ver carrito") {
                    findNavController().navigate(R.id.nav_cart)
                }.show()
            }
        }
    }

    /**
     * Rellena todas las vistas con los datos del [Producto] proporcionado.
     *
     * Construye la cadena de marca y presentación con un separador "·" cuando
     * ambos campos están disponibles. Carga la imagen con Glide usando ajuste centrado.
     *
     * @param p Producto cuyos datos se van a mostrar en pantalla.
     */
    private fun bindProducto(p: Producto) {
        producto = p
        binding.detalleNombre.text = p.nombre
        binding.detalleCategoria.text = p.categoria
        binding.detalleMarca.text = buildString {
            if (p.marca.isNotEmpty()) append(p.marca)
            if (p.presentacion.isNotEmpty()) {
                if (isNotEmpty()) append(" · ")
                append(p.presentacion)
            }
        }
        binding.detallePrecio.text = p.precio.formatCOP()
        binding.detalleDescripcion.text = p.descripcion

        Glide.with(this)
            .load(p.imagenUrl)
            .placeholder(R.drawable.bg_card)
            .fitCenter()
            .into(binding.detalleImage)
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
 * Extensión para formatear un [Double] como moneda colombiana (COP) sin decimales.
 *
 * @return Cadena con el valor formateado sin centavos, por ejemplo "$1.500".
 */
private fun Double.formatCOP(): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    fmt.maximumFractionDigits = 0
    fmt.minimumFractionDigits = 0
    return fmt.format(this.toLong())
}
