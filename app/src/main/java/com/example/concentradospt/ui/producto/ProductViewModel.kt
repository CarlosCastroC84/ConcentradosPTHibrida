package com.example.concentradospt.ui.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.CategoriaProducto
import com.example.concentradospt.data.model.Producto
import com.example.concentradospt.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Representa los posibles estados de la pantalla de catálogo de productos.
 * Es una clase sellada que permite manejar de forma exhaustiva cada variante en la UI.
 */
sealed class ProductUiState {
    /** Estado de carga mientras se obtiene el catálogo de productos desde el backend. */
    object Loading : ProductUiState()

    /**
     * Estado de éxito con la lista de productos (posiblemente filtrada).
     * @property productos Lista de productos que se mostrarán en el catálogo.
     */
    data class Success(val productos: List<Producto>) : ProductUiState()

    /**
     * Estado de error con un mensaje descriptivo del problema ocurrido.
     * @property message Mensaje legible por el usuario que describe el error.
     */
    data class Error(val message: String) : ProductUiState()
}

/**
 * ViewModel que gestiona la carga, filtrado y búsqueda del catálogo de productos.
 * Mantiene en memoria la lista completa de productos ([_allProductos]) para aplicar
 * filtros por categoría y búsquedas de texto de forma local sin solicitudes de red adicionales.
 * También expone la lista de categorías disponibles y el producto actualmente seleccionado.
 */
class ProductViewModel : ViewModel() {

    /** Repositorio encargado de las operaciones remotas sobre productos y categorías. */
    private val repository = ProductRepository()

    /** Lista completa de todos los productos cargados desde el backend, sin filtros aplicados. */
    private val _allProductos = MutableStateFlow<List<Producto>>(emptyList())

    /** Lista de categorías disponibles para filtrar el catálogo. */
    private val _categorias = MutableStateFlow<List<CategoriaProducto>>(emptyList())

    /** Categoría actualmente seleccionada para filtrar, o `null` si no hay filtro activo. */
    private val _selectedCategoria = MutableStateFlow<String?>(null)

    /** Texto de búsqueda ingresado por el usuario para filtrar productos por nombre, descripción o categoría. */
    private val _searchQuery = MutableStateFlow("")

    /** Estado mutable interno del catálogo (puede estar filtrado o buscado), modificable solo desde este ViewModel. */
    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)

    /** Estado público e inmutable del catálogo, observado por la UI. */
    val uiState: StateFlow<ProductUiState> = _uiState

    /** Flujo público de categorías disponibles para mostrar en el selector de filtros de la UI. */
    val categorias: StateFlow<List<CategoriaProducto>> = _categorias

    /** Flujo mutable del producto actualmente seleccionado para ver su detalle. */
    private val _selectedProducto = MutableStateFlow<Producto?>(null)

    /** Flujo público del producto seleccionado, observado por la UI de detalle. */
    val selectedProducto: StateFlow<Producto?> = _selectedProducto

    /**
     * Establece el producto que el usuario ha seleccionado para ver su detalle.
     * @param producto Producto seleccionado por el usuario.
     */
    fun selectProducto(producto: Producto) {
        _selectedProducto.value = producto
    }

    /**
     * Refresca el catálogo. Si la lista está vacía, realiza una carga completa desde el backend;
     * de lo contrario, vuelve a aplicar los filtros actuales sobre la lista en memoria.
     */
    fun refresh() {
        if (_allProductos.value.isEmpty()) loadProductos()
        else applyFilter()
    }

    init {
        loadProductos()
    }

    /**
     * Carga la lista completa de productos y categorías desde el backend.
     * Una vez cargados, aplica los filtros activos mediante [applyFilter].
     * En caso de error emite [ProductUiState.Error] con un mensaje descriptivo.
     */
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

    /**
     * Establece la categoría activa como filtro y aplica los filtros combinados.
     * Si [categoria] es `null`, se eliminan todos los filtros por categoría.
     *
     * @param categoria Nombre de la categoría por la que se desea filtrar, o `null` para limpiar el filtro.
     */
    fun filterByCategoria(categoria: String?) {
        _selectedCategoria.value = categoria
        applyFilter()
    }

    /**
     * Actualiza el texto de búsqueda y aplica los filtros combinados sobre la lista en memoria.
     * La búsqueda se aplica sobre el nombre, descripción y categoría del producto.
     *
     * @param query Texto de búsqueda ingresado por el usuario (se recortan espacios en blanco).
     */
    fun search(query: String) {
        _searchQuery.value = query.trim()
        applyFilter()
    }

    /**
     * Retorna los primeros [limit] productos del catálogo completo para mostrar
     * en secciones de productos destacados o recomendados.
     *
     * @param limit Cantidad máxima de productos a retornar. Por defecto es 6.
     * @return Lista con hasta [limit] productos del catálogo completo.
     */
    fun getFeaturedProductos(limit: Int = 6): List<Producto> = _allProductos.value.take(limit)

    /**
     * Aplica en cascada los filtros activos (categoría y búsqueda de texto) sobre [_allProductos]
     * y actualiza [_uiState] con la lista resultante.
     * La comparación de categoría y la búsqueda son insensibles a mayúsculas y minúsculas.
     */
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
