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

/**
 * Fragmento que presenta el catálogo completo de productos de la tienda.
 *
 * Permite al usuario explorar todos los productos disponibles con soporte para:
 * - Filtrado por categoría mediante chips (Concentrados, Fertilizantes, Semillas, Herramientas).
 * - Búsqueda en tiempo real por nombre a través de un SearchBar/SearchView.
 * - Navegación al detalle de un producto al pulsarlo.
 * - Adición directa de productos al carrito.
 *
 * Usa [ProductViewModel] para filtrar y buscar, y [CartViewModel] para añadir ítems.
 */
class CatalogoFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: CatalogoFragmentBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel compartido que gestiona el catálogo, filtros y búsqueda de productos. */
    private val viewModel: ProductViewModel by activityViewModels()

    /** ViewModel compartido que gestiona el carrito de compras. */
    private val cartViewModel: CartViewModel by activityViewModels()

    /**
     * Adaptador de la lista de productos del catálogo.
     * Permite navegar al detalle o agregar directamente al carrito.
     */
    private val adapter = CatalogoAdapter(
        onProductClick = { producto ->
            viewModel.selectProducto(producto)
            findNavController().navigate(R.id.nav_detalle_producto)
        },
        onAddToCart = { producto -> cartViewModel.addToCart(producto) }
    )

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CatalogoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura el RecyclerView, los filtros de categoría por chips,
     * la barra de búsqueda y comienza a observar el estado del ViewModel.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.catalogoRvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.catalogoRvProducts.adapter = adapter

        setupChipFilters()
        setupSearch()
        observeState()
    }

    /**
     * Configura los chips de filtro por categoría.
     *
     * Al seleccionar un chip, invoca [ProductViewModel.filterByCategoria] con
     * la categoría correspondiente. El chip "Todos" limpia el filtro (pasa null).
     */
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

    /**
     * Configura el comportamiento del SearchBar y el SearchView para búsqueda de productos.
     *
     * - Al ocultar el SearchView, limpia la consulta de búsqueda.
     * - Al confirmar una búsqueda (teclado), actualiza el SearchBar y filtra.
     * - Mientras se escribe, filtra los productos en tiempo real con [ProductViewModel.search].
     */
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

    /**
     * Observa el [ProductUiState] del ViewModel y actualiza la visibilidad de las vistas.
     *
     * - Loading: muestra el indicador de progreso y oculta la lista.
     * - Success: muestra la lista de productos o un mensaje de vacío si no hay resultados.
     * - Error: muestra el mensaje de error en lugar de la lista.
     */
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

    /**
     * Libera el binding al destruir la vista para prevenir pérdidas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
