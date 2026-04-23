package com.example.concentradospt.ui.catalogo

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
import com.example.concentradospt.databinding.CatalogoFragmentBinding
import com.example.concentradospt.ui.carrito.CartViewModel
import com.example.concentradospt.ui.producto.ProductUiState
import com.example.concentradospt.ui.producto.ProductViewModel
import com.google.android.material.search.SearchView
import kotlinx.coroutines.launch

class CatalogoFragment : Fragment() {

    private var _binding: CatalogoFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()

    private val adapter = CatalogoAdapter(
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
        _binding = CatalogoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.catalogoRvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.catalogoRvProducts.adapter = adapter

        setupChipFilters()
        setupSearch()
        observeState()
    }

    private fun setupChipFilters() {
        binding.catalogoChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val categoria = when (checkedIds.firstOrNull()) {
                R.id.catalogo_chip_all -> null
                R.id.catalogo_chip_concentrados -> "Concentrados"
                R.id.catalogo_chip_fertilizantes -> "Fertilizantes"
                R.id.catalogo_chip_semillas -> "Semillas"
                R.id.catalogo_chip_herramientas -> "Herramientas"
                else -> null
            }
            viewModel.filterByCategoria(categoria)
        }
    }

    private fun setupSearch() {
        binding.catalogoSearchView.setupWithSearchBar(binding.catalogoSearchBar)
        binding.catalogoSearchView.addTransitionListener { _, _, newState ->
            if (newState == SearchView.TransitionState.HIDDEN) {
                viewModel.search("")
                binding.catalogoSearchBar.setText("")
            }
        }

        binding.catalogoSearchView.editText.setOnEditorActionListener { tv, _, _ ->
            val query = tv.text.toString()
            binding.catalogoSearchBar.setText(query)
            binding.catalogoSearchView.hide()
            viewModel.search(query)
            false
        }

        binding.catalogoSearchView.editText.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    viewModel.search(s?.toString() ?: "")
                }
                override fun afterTextChanged(s: android.text.Editable?) = Unit
            }
        )
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ProductUiState.Loading -> {
                        binding.catalogoProgress.visibility = View.VISIBLE
                        binding.catalogoRvProducts.visibility = View.GONE
                        binding.catalogoTvEmpty.visibility = View.GONE
                    }
                    is ProductUiState.Success -> {
                        binding.catalogoProgress.visibility = View.GONE
                        if (state.productos.isEmpty()) {
                            binding.catalogoRvProducts.visibility = View.GONE
                            binding.catalogoTvEmpty.visibility = View.VISIBLE
                            binding.catalogoTvEmpty.text = getString(R.string.catalog_empty_message)
                        } else {
                            binding.catalogoRvProducts.visibility = View.VISIBLE
                            binding.catalogoTvEmpty.visibility = View.GONE
                            adapter.submitList(state.productos)
                        }
                    }
                    is ProductUiState.Error -> {
                        binding.catalogoProgress.visibility = View.GONE
                        binding.catalogoRvProducts.visibility = View.GONE
                        binding.catalogoTvEmpty.visibility = View.VISIBLE
                        binding.catalogoTvEmpty.text = state.message
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