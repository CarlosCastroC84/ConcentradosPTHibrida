package com.example.concentradospt.ui.home

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
import com.example.concentradospt.databinding.HomeFragmentBinding
import com.example.concentradospt.ui.carrito.CartViewModel
import com.example.concentradospt.ui.producto.ProductUiState
import com.example.concentradospt.ui.producto.ProductViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()

    private val adapter = HomeProductAdapter(
        onProductClick = { producto ->
            val bundle = Bundle().apply { putString("productoId", producto.productoId) }
            findNavController().navigate(R.id.nav_detalle_producto, bundle)
        },
        onAddToCart = { producto -> cartViewModel.addToCart(producto) }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.homeRvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.homeRvProducts.adapter = adapter

        binding.homeSeeAllProducts.setOnClickListener {
            findNavController().navigate(R.id.nav_catalog)
        }

        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ProductUiState.Loading -> {
                        binding.homeProgress.visibility = View.VISIBLE
                        binding.homeScroll.visibility = View.GONE
                        binding.homeTvEmpty.visibility = View.GONE
                    }
                    is ProductUiState.Success -> {
                        binding.homeProgress.visibility = View.GONE
                        val featured = viewModel.getFeaturedProductos()
                        if (featured.isEmpty()) {
                            binding.homeScroll.visibility = View.GONE
                            binding.homeTvEmpty.visibility = View.VISIBLE
                            binding.homeTvEmpty.text = "No hay productos disponibles"
                        } else {
                            binding.homeScroll.visibility = View.VISIBLE
                            binding.homeTvEmpty.visibility = View.GONE
                            adapter.submitList(featured)
                        }
                    }
                    is ProductUiState.Error -> {
                        binding.homeProgress.visibility = View.GONE
                        binding.homeScroll.visibility = View.GONE
                        binding.homeTvEmpty.visibility = View.VISIBLE
                        binding.homeTvEmpty.text = state.message
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}