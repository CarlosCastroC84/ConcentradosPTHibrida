package com.example.concentradospt.data.network

/**
 * Constantes globales de configuración de red para la aplicación ConcentradosPT.
 *
 * Centraliza las URLs base y los identificadores de endpoints públicos que se utilizan
 * en todo el módulo de red, evitando la duplicación de cadenas de texto en el código.
 */
object ApiConstants {

    /**
     * URL base del API Gateway de AWS que actúa como proxy inverso hacia el backend
     * Spring Boot desplegado en AWS Lightsail.
     * Todas las peticiones Retrofit se construyen a partir de esta URL.
     */
    const val BASE_URL = "https://uswxs1ee2d.execute-api.us-east-2.amazonaws.com/prod/"

    /**
     * URL base del bucket de AWS S3 donde se almacenan las imágenes de la aplicación,
     * incluyendo las imágenes del carrusel de la pantalla principal.
     */
    const val S3_BASE_URL = "https://concentrados-app-imagenes-517329182634-us-east-2-an.s3.us-east-2.amazonaws.com"

    /**
     * Conjunto de segmentos de ruta que corresponden a endpoints públicos.
     * Las peticiones GET cuyas rutas contengan alguno de estos valores no requieren
     * un token de autorización y son dejadas pasar directamente por [AuthInterceptor].
     */
    val PUBLIC_ENDPOINTS = setOf(
        "productos",
        "categorias-producto",
        "marcas-producto",
        "presentaciones-producto"
    )
}
