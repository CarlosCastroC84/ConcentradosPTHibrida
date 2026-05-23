package com.example.concentradospt.data.model.admin

/**
 * Respuesta paginada genérica utilizada por los endpoints del panel de administración.
 * Encapsula una página de resultados junto con la metadata de paginación del servidor.
 *
 * @param T Tipo de los elementos que contiene la página de resultados.
 * @property content Lista de elementos del tipo [T] correspondientes a la página actual.
 * @property totalPages Número total de páginas disponibles para la consulta realizada.
 * @property totalElements Número total de registros existentes en el servidor para la consulta.
 * @property last Indica si la página actual es la última disponible en la paginación.
 * @property number Índice de la página actual (base 0).
 */
data class AdminPageResponse<T>(
    val content: List<T> = emptyList(),
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val last: Boolean = true,
    val number: Int = 0
)
