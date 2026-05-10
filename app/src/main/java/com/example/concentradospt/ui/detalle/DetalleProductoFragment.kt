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

class DetalleProductoFragment : Fragment() {

    private var _binding: FragmentDetalleProductoBinding? = null
    private val binding get() = _binding!!

    private val productViewModel: ProductViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()

    private var cantidad = 1
    private var producto: Producto? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleProductoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.detalleToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        lifecycleScope.launch {
            productViewModel.selectedProducto.collect { p ->
                p?.let { bindProducto(it) }
            }
        }

        binding.detalleBtnDecrease.setOnClickListener {
            if (cantidad > 1) {
                cantidad--
                binding.detalleCantidad.text = cantidad.toString()
            }
        }

        binding.detalleBtnIncrease.setOnClickListener {
            val stock = producto?.stock ?: Int.MAX_VALUE
            if (cantidad < stock) {
                cantidad++
                binding.detalleCantidad.text = cantidad.toString()
            }
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun Double.formatCOP(): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    fmt.maximumFractionDigits = 0
    fmt.minimumFractionDigits = 0
    return fmt.format(this.toLong())
}