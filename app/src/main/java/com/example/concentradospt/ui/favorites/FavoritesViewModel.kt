package com.example.concentradospt.ui.favorites

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel que gestiona la lista de productos favoritos del usuario de forma local en memoria.
 * Almacena los identificadores de los productos marcados como favoritos en un [Set] reactivo,
 * lo que garantiza que no haya duplicados y que la UI reaccione automáticamente a cada cambio.
 * Los favoritos se almacenan únicamente en memoria y no persisten entre sesiones.
 */
class FavoritesViewModel : ViewModel() {

    /**
     * Conjunto mutable de identificadores de productos marcados como favoritos.
     * Modificable solo desde este ViewModel.
     */
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())

    /**
     * Conjunto público e inmutable de identificadores de productos favoritos, observado por la UI.
     * La UI puede usar este flujo para reflejar el estado de favorito en cada producto del catálogo.
     */
    val favorites: StateFlow<Set<String>> = _favorites

    /**
     * Agrega o quita un producto de la lista de favoritos según su estado actual.
     * Si [productoId] ya está en favoritos, lo elimina; de lo contrario, lo agrega.
     *
     * @param productoId Identificador único del producto cuyo estado de favorito se desea alternar.
     */
    fun toggle(productoId: String) {
        val current = _favorites.value
        _favorites.value = if (productoId in current) current - productoId else current + productoId
    }

    /**
     * Consulta si un producto específico está actualmente marcado como favorito.
     *
     * @param productoId Identificador único del producto que se desea consultar.
     * @return `true` si el producto está en favoritos, `false` en caso contrario.
     */
    fun isFavorite(productoId: String) = productoId in _favorites.value
}
