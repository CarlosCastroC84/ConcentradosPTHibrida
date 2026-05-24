package com.example.concentradospt.data.network.admin

import com.example.concentradospt.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit singleton exclusivo para el módulo de administración interna.
 *
 * A diferencia de [com.example.concentradospt.data.network.RetrofitClient], este objeto
 * se comunica directamente con el backend Spring Boot en su IP privada de AWS Lightsail,
 * sin pasar por el API Gateway. Incorpora:
 * - [AdminAuthInterceptor]: añade el token de acceso del administrador en las peticiones protegidas.
 * - [AdminTokenAuthenticator]: renueva automáticamente el token de acceso cuando el servidor
 *   responde con HTTP 401.
 * - [HttpLoggingInterceptor]: registra el cuerpo de las peticiones y respuestas para depuración.
 * - Tiempos de espera de 30 segundos para conexión y lectura.
 */
object AdminRetrofitClient {

    /**
     * URL base del backend administrativo Spring Boot, accesible directamente
     * a través de la IP pública de la instancia en AWS Lightsail.
     */
    private const val BASE_URL = "http://100.25.124.3:8080/api/v1/"

    /**
     * Cliente HTTP de OkHttp configurado de forma perezosa (lazy) con los interceptores
     * y el autenticador propios del módulo de administración.
     */
    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(AdminAuthInterceptor())
            .authenticator(AdminTokenAuthenticator())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        builder.build()
    }

    /**
     * Instancia principal de [Retrofit] configurada de forma perezosa (lazy) apuntando
     * a [BASE_URL] con el cliente HTTP administrativo y el convertidor Gson.
     */
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Crea e instancia una implementación de la interfaz de servicio indicada usando Retrofit.
     *
     * @param T Tipo de la interfaz de servicio que se desea crear (p. ej. [AdminApiService]).
     * @return Implementación proxy generada por Retrofit para la interfaz [T].
     */
    inline fun <reified T> create(): T = instance.create(T::class.java)
}
