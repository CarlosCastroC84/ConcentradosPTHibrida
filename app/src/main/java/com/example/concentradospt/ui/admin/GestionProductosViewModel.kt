package com.example.concentradospt.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.admin.AdminProducto
import com.example.concentradospt.data.network.admin.AdminApiService
import com.example.concentradospt.data.network.admin.AdminRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class GestionProductosState {
    object Loading : GestionProductosState()
    data class Success(val productos: List<AdminProducto>) : GestionProductosState()
    data class Error(val message: String) : GestionProductosState()
}

class GestionProductosViewModel : ViewModel() {

    private val api = AdminRetrofitClient.create<AdminApiService>()

    private val _state = MutableStateFlow<GestionProductosState>(GestionProductosState.Loading)
    val state: StateFlow<GestionProductosState> = _state

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult: StateFlow<String?> = _actionResult

    init { loadProductos() }

    fun loadProductos() {
        viewModelScope.launch {
            _state.value = GestionProductosState.Loading
            try {
                val page = api.getAdminProductos(pagina = 0, tamano = 50)
                _state.value = GestionProductosState.Success(page.content)
            } catch (e: Exception) {
                _state.value = GestionProductosState.Error("No se pudo cargar los productos")
            }
        }
    }

    fun crearProducto(producto: AdminProducto) {
        viewModelScope.launch {
            try {
                api.crearAdminProducto(producto)
                _actionResult.value = "Producto creado exitosamente"
                loadProductos()
            } catch (e: Exception) {
                _actionResult.value = "Error al crear el producto"
            }
        }
    }

    fun actualizarProducto(producto: AdminProducto) {
        viewModelScope.launch {
            try {
                api.actualizarAdminProducto(producto.id, producto)
                _actionResult.value = "Producto actualizado"
                loadProductos()
            } catch (e: Exception) {
                _actionResult.value = "Error al actualizar el producto"
            }
        }
    }

    fun clearActionResult() { _actionResult.value = null }
}
