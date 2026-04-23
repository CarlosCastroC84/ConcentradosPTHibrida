package com.example.concentradospt.data.model.admin

data class AdminPageResponse<T>(
    val content: List<T> = emptyList(),
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val last: Boolean = true,
    val number: Int = 0
)
