package com.example.concentradospt.ui.favorites

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FavoritesViewModel : ViewModel() {

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites

    fun toggle(productoId: String) {
        val current = _favorites.value
        _favorites.value = if (productoId in current) current - productoId else current + productoId
    }

    fun isFavorite(productoId: String) = productoId in _favorites.value
}
