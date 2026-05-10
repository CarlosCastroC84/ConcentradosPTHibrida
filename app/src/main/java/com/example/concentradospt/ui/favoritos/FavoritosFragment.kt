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

class FavoritosFragment : Fragment() {

    private var _binding: FragmentFavoritosBinding? = null
    private val binding get() = _binding!!

    private val favoritesViewModel: FavoritesViewModel by activityViewModels()
    private val productViewModel: ProductViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritosBinding.inflate(inflater, container, false)
        return binding.root
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
