package com.example.concentradospt.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit singleton utilizado para realizar peticiones HTTP a la API del backend.
 *
 * Configura [OkHttpClient] con interceptores de autenticación y de logging, tiempos de
 * espera de conexión y lectura, y un convertidor Gson para la serialización/deserialización
 * automática de JSON. Expone una instancia única de [Retrofit] basada en [ApiConstants.BASE_URL].
 */
object RetrofitClient {

    /**
     * Cliente HTTP configurado de forma perezosa (lazy).
     *
     * Incluye:
     * - [AuthInterceptor]: añade el token de autorización a las peticiones protegidas.
     * - [HttpLoggingInterceptor]: registra el cuerpo completo de las peticiones y respuestas
     *   para facilitar la depuración.
     * - Tiempos de espera de 30 segundos tanto para la conexión como para la lectura.
     */
    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Instancia principal de [Retrofit] configurada de forma perezosa (lazy).
     *
     * Usa [ApiConstants.BASE_URL] como URL base y [GsonConverterFactory] para convertir
     * automáticamente los cuerpos JSON en objetos Kotlin y viceversa.
     */
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Crea e instancia una implementación de la interfaz de servicio indicada usando Retrofit.
     *
     * @param T Tipo de la interfaz de servicio que se desea crear (p. ej. [ApiService]).
     * @return Implementación proxy generada por Retrofit para la interfaz [T].
     */
    inline fun <reified T> create(): T = instance.create(T::class.java)
}
