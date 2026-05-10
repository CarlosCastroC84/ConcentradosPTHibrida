package com.example.concentradospt.ui.home

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.concentradospt.R
import com.example.concentradospt.data.model.CategoriaProducto
import com.example.concentradospt.data.network.CarouselRepository
import com.example.concentradospt.databinding.HomeFragmentBinding
import com.example.concentradospt.ui.carrito.CartViewModel
import com.example.concentradospt.ui.favorites.FavoritesViewModel
import com.example.concentradospt.ui.producto.ProductUiState
import com.example.concentradospt.ui.producto.ProductViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private val bannerAdapter = BannerAdapter(emptyList())
    private val carouselRepository = CarouselRepository()
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val pager = _binding?.homeBannerPager ?: return
            val count = bannerAdapter.itemCount
            if (count > 1) {
                pager.currentItem = (pager.currentItem + 1) % count
            }
            autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY_MS)
        }
    }

    companion object {
        private const val AUTO_SCROLL_DELAY_MS = 60_000L
    }

    private val viewModel: ProductViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    private val favoritesViewModel: FavoritesViewModel by activityViewModels()

    private val adapter = HomeProductAdapter(
        onProductClick = { producto ->
            viewModel.selectProducto(producto)
            findNavController().navigate(R.id.action_home_to_detalle_producto)
        },
        onAddToCart = { producto ->
            cartViewModel.addToCart(producto)
            Snackbar.make(
                requireView(),
                "\"${producto.nombre}\" agregado al carrito",
                Snackbar.LENGTH_SHORT
            ).show()
        },
        onFavoriteToggle = { producto ->
            favoritesViewModel.toggle(producto.productoId)
        }
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

        setupBanner()

        binding.homeSeeAllProducts.setOnClickListener {
            findNavController().navigate(R.id.nav_catalog)
        }

        binding.homeSeeAllCategories.setOnClickListener {
            findNavController().navigate(R.id.nav_catalog)
        }

        binding.btnSearch.setOnClickListener {
            findNavController().navigate(R.id.nav_catalog)
        }

        binding.btnNotifications.setOnClickListener {
            Snackbar.make(
                requireView(),
                "Las notificaciones estarán disponibles próximamente",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        observeState()
        observeCategorias()
        observeFavorites()
    }

    override fun onStart() {
        super.onStart()
        viewModel.refresh()
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

    private fun observeCategorias() {
        lifecycleScope.launch {
            viewModel.categorias.collect { categorias ->
                buildCategoryChips(categorias)
            }
        }
    }

    private fun observeFavorites() {
        lifecycleScope.launch {
            favoritesViewModel.favorites.collect { favSet ->
                adapter.updateFavorites(favSet)
            }
        }
    }

    private fun buildCategoryChips(categorias: List<CategoriaProducto>) {
        val container = binding.homeCategoriesContainer
        container.removeAllViews()
        categorias.forEach { categoria ->
            val chip = Chip(requireContext()).apply {
                text = categoria.nombre
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelLarge)
                isCheckable = false
                chipStrokeWidth = 0f
                setChipBackgroundColorResource(R.color.surface_container_low)
                chipIcon = getCategoryIcon(categoria.nombre)
                isChipIconVisible = chipIcon != null
                setOnClickListener {
                    viewModel.filterByCategoria(categoria.nombre)
                    findNavController().navigate(R.id.nav_catalog)
                }
            }
            container.addView(chip)
        }
    }

    private fun getCategoryIcon(nombre: String): Drawable? {
        val resId = when {
            nombre.contains("ganad", ignoreCase = true) -> R.drawable.ic_cat_ganaderia
            nombre.contains("pollo", ignoreCase = true) ||
                nombre.contains("avicul", ignoreCase = true) -> R.drawable.ic_cat_pollo
            nombre.contains("postura", ignoreCase = true) -> R.drawable.ic_cat_posturas
            nombre.contains("equino", ignoreCase = true) ||
                nombre.contains("caballo", ignoreCase = true) -> R.drawable.ic_cat_equinos
            nombre.contains("porcicul", ignoreCase = true) ||
                nombre.contains("cerdo", ignoreCase = true) -> R.drawable.ic_cat_porcicultura
            nombre.contains("concentrado", ignoreCase = true) -> R.drawable.ic_cat_concentrados
            nombre.contains("fertilizante", ignoreCase = true) -> R.drawable.ic_cat_fertilizantes
            nombre.contains("semilla", ignoreCase = true) -> R.drawable.ic_cat_semillas
            nombre.contains("herramienta", ignoreCase = true) -> R.drawable.ic_cat_herramientas
            nombre.contains("sal", ignoreCase = true) -> R.drawable.ic_cat_sales
            nombre.contains("alimento", ignoreCase = true) ||
                nombre.contains("animal", ignoreCase = true) -> R.drawable.ic_cat_alimento_animal
            else -> R.drawable.ic_cat_default
        }
        return ContextCompat.getDrawable(requireContext(), resId)
    }

    private fun setupBanner() {
        binding.homeBannerPager.adapter = bannerAdapter
        TabLayoutMediator(binding.homeBannerIndicator, binding.homeBannerPager) { _, _ -> }.attach()

        lifecycleScope.launch {
            val urls = try {
                carouselRepository.getCarouselImageUrls()
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error loading banner URLs", e)
                emptyList()
            }
            if (urls.isNotEmpty()) {
                bannerAdapter.updateUrls(urls)
                binding.homeBannerIndicator.visibility = if (urls.size > 1) View.VISIBLE else View.GONE
                if (urls.size > 1) autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY_MS)
            } else {
                binding.homeBannerPager.visibility = View.GONE
                binding.homeBannerIndicator.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
        super.onDestroyView()
        _binding = null
    }
}
