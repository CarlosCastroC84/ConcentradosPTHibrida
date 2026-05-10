package com.example.concentradospt.ui.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.CategoriaProducto
import com.example.concentradospt.data.model.Producto
import com.example.concentradospt.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val productos: List<Producto>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

class ProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _allProductos = MutableStateFlow<List<Producto>>(emptyList())
    private val _categorias = MutableStateFlow<List<CategoriaProducto>>(emptyList())
    private val _selectedCategoria = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState

    val categorias: StateFlow<List<CategoriaProducto>> = _categorias

    private val _selectedProducto = MutableStateFlow<Producto?>(null)
    val selectedProducto: StateFlow<Producto?> = _selectedProducto

    fun selectProducto(producto: Producto) {
        _selectedProducto.value = producto
    }

    fun refresh() {
        if (_allProductos.value.isEmpty()) loadProductos()
        else applyFilter()
    }

    init {
        loadProductos()
    }

    fun loadProductos() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                _allProductos.value = repository.getProductos()
                _categorias.value = repository.getCategorias()
                applyFilter()
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error("No se pudo cargar el catálogo. Verifica tu conexión.")
            }
        }
    }

    fun filterByCategoria(categoria: String?) {
        _selectedCategoria.value = categoria
        applyFilter()
    }

    fun search(query: String) {
        _searchQuery.value = query.trim()
        applyFilter()
    }

    fun getFeaturedProductos(limit: Int = 6): List<Producto> = _allProductos.value.take(limit)

    private fun applyFilter() {
        val cat = _selectedCategoria.value
        val query = _searchQuery.value

        var result = _allProductos.value

        if (!cat.isNullOrEmpty()) {
            result = result.filter { it.categoria.equals(cat, ignoreCase = true) }
        }

        if (query.isNotEmpty()) {
            result = result.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                it.descripcion.contains(query, ignoreCase = true) ||
                it.categoria.contains(query, ignoreCase = true)
            }
        }

        _uiState.value = ProductUiState.Success(result)
    }
}