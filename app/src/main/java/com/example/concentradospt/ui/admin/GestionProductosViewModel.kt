package com.example.concentradospt.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentradospt.data.model.admin.AdminProducto
import com.example.concentradospt.data.network.admin.AdminApiService
import com.example.concentradospt.data.network.admin.AdminRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Representa los posibles estados de la pantalla de gestión de productos del panel administrativo.
 * Es una clase sellada que permite manejar de forma exhaustiva cada variante en la UI.
 */
sealed class GestionProductosState {
    /** Estado de carga mientras se obtiene o procesa la lista de productos. */
    object Loading : GestionProductosState()

    /**
     * Estado de éxito con la lista de productos recuperada del backend.
     * @property productos Lista de productos del catálogo administrativo.
     */
    data class Success(val productos: List<AdminProducto>) : GestionProductosState()

    /**
     * Estado de error con un mensaje descriptivo del problema ocurrido.
     * @property message Mensaje legible por el usuario que describe el error.
     */
    data class Error(val message: String) : GestionProductosState()
}

/**
 * ViewModel que gestiona el CRUD de productos desde el panel administrativo.
 * Utiliza [AdminApiService] para comunicarse con el backend de Lightsail y expone
 * el estado de la lista mediante [state] y el resultado de las acciones CRUD
 * mediante [actionResult], ambos como [StateFlow].
 */
class GestionProductosViewModel : ViewModel() {

    /** Servicio de API administrativo configurado con Retrofit y autenticación de empleado. */
    private val api = AdminRetrofitClient.create<AdminApiService>()

    /** Estado mutable interno de la lista de productos, modificable solo desde este ViewModel. */
    private val _state = MutableStateFlow<GestionProductosState>(GestionProductosState.Loading)

    /** Estado público e inmutable de la lista de productos, observado por la UI. */
    val state: StateFlow<GestionProductosState> = _state

    /**
     * Flujo mutable que transporta mensajes de resultado de operaciones CRUD
     * (crear, actualizar, eliminar). `null` indica que no hay mensaje pendiente.
     */
    private val _actionResult = MutableStateFlow<String?>(null)

    /** Flujo público que expone los mensajes de resultado de operaciones CRUD a la UI. */
    val actionResult: StateFlow<String?> = _actionResult

    init { loadProductos() }

    /**
     * Carga la primera página de productos del catálogo administrativo (hasta 50 elementos).
     * Actualiza [state] con la lista obtenida o con un error si la solicitud falla.
     */
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

    /**
     * Crea un nuevo producto en el backend y recarga la lista de productos.
     * Actualiza [actionResult] con el resultado de la operación.
     *
     * @param producto Objeto [AdminProducto] con los datos del nuevo producto a crear.
     */
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

    /**
     * Actualiza los datos de un producto existente en el backend y recarga la lista.
     * Actualiza [actionResult] con el resultado de la operación.
     *
     * @param producto Objeto [AdminProducto] con los datos actualizados. Su campo `id` determina qué producto se modifica.
     */
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

    /**
     * Elimina un producto del catálogo en el backend y recarga la lista.
     * Actualiza [actionResult] con el resultado de la operación.
     *
     * @param id Identificador único del producto que se desea eliminar.
     */
    fun eliminarProducto(id: Long) {
        viewModelScope.launch {
            try {
                api.eliminarAdminProducto(id)
                _actionResult.value = "Producto eliminado"
                loadProductos()
            } catch (e: Exception) {
                _actionResult.value = "Error al eliminar el producto"
            }
        }
    }

    /**
     * Limpia el mensaje de resultado de la última acción CRUD.
     * Debe llamarse desde la UI después de haber mostrado el mensaje al usuario.
     */
    fun clearActionResult() { _actionResult.value = null }
}
