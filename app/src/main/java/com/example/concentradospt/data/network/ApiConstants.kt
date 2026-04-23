package com.example.concentradospt.data.network

object ApiConstants {
    const val BASE_URL = "https://uswxs1ee2d.execute-api.us-east-2.amazonaws.com/prod/"
    const val S3_BASE_URL = "https://concentrados-app-imagenes-517329182634-us-east-2-an.s3.us-east-2.amazonaws.com"

    // Endpoints públicos que no requieren token
    val PUBLIC_ENDPOINTS = setOf(
        "productos",
        "categorias-producto",
        "marcas-producto",
        "presentaciones-producto"
    )
}