package com.example.concentradospt.ui.favoritos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.concentradospt.R
import com.example.concentradospt.databinding.FragmentFavoritosBinding
import com.example.concentradospt.ui.carrito.CartViewModel
import com.example.concentradospt.ui.favorites.FavoritesViewModel
import com.example.concentradospt.ui.home.HomeProductAdapter
import com.example.concentradospt.ui.producto.ProductViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Fragmento que presenta la lista de productos marcados como favoritos por el usuario.
 *
 * Filtra los productos del catálogo general utilizando los IDs almacenados en
 * [FavoritesViewModel] y los muestra en una cuadrícula de dos columnas mediante
 * [HomeProductAdapter] (reutilizado desde la pantalla de inicio).
 *
 * Permite al usuario:
 * - Navegar al detalle de un producto favorito.
 * - Agregar un producto al carrito con Snackbar de confirmación.
 * - Quitar un producto de favoritos pulsando el ícono de estrella.
 *
 * Si la lista de favoritos está vacía, muestra un mensaje informativo.
 */
class FavoritosFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: FragmentFavoritosBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel compartido que gestiona el conjunto de IDs de productos favoritos. */
    private val favoritesViewModel: FavoritesViewModel by activityViewModels()

    /** ViewModel compartido que provee el catálogo completo de productos. */
    private val productViewModel: ProductViewModel by activityViewModels()

    /** ViewModel compartido que gestiona el carrito de compras. */
    private val cartViewModel: CartViewModel by activityViewModels()

    /**
     * Adaptador reutilizado del home para mostrar los productos favoritos en cuadrícula.
     * Maneja clic en producto, agregar al carrito y alternar favorito.
     */
    private val adapter = HomeProductAdapter(
        onProductClick = { producto ->
            val bundle = Bundle().apply { putString("productoId", producto.productoId) }
            findNavController().navigate(R.id.action_favoritos_to_detalle_producto, bundle)
        },
        onAddToCart = { producto ->
            cartViewModel.addToCart(producto)
            Snackbar.make(requireView(), "\"${producto.nombre}\" agregado al carrito", Snackbar.LENGTH_SHORT).show()
        },
        onFavoriteToggle = { producto ->
            favoritesViewModel.toggle(producto.productoId)
        }
    )

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritosBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura la barra de herramientas, el RecyclerView en cuadrícula de 2 columnas
     * y observa los cambios en la lista de favoritos para actualizar la UI.
     *
     * Cuando el conjunto de favoritos cambia:
     * 1. Actualiza el estado visual del ícono de favorito en el adaptador.
     * 2. Filtra todos los productos del catálogo para obtener solo los favoritos.
     * 3. Muestra la lista filtrada o el mensaje de vacío según corresponda.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.favoritosToolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.favoritosRv.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.favoritosRv.adapter = adapter

        lifecycleScope.launch {
            favoritesViewModel.favorites.collect { favIds ->
                adapter.updateFavorites(favIds)
                val allProductos = productViewModel.getFeaturedProductos(Int.MAX_VALUE)
                val favoritos = allProductos.filter { it.productoId in favIds }
                if (favoritos.isEmpty()) {
                    binding.favoritosRv.visibility = View.GONE
                    binding.favoritosTvEmpty.visibility = View.VISIBLE
                } else {
                    binding.favoritosRv.visibility = View.VISIBLE
                    binding.favoritosTvEmpty.visibility = View.GONE
                    adapter.submitList(favoritos)
                }
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
