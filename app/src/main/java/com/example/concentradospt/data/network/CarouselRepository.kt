package com.example.concentradospt.data.network

import android.util.Log
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Repositorio responsable de obtener las URLs de las imágenes del carrusel almacenadas
 * en el bucket de AWS S3 configurado en [ApiConstants.S3_BASE_URL].
 *
 * Realiza una petición HTTP directa al bucket S3 usando la API de listado de objetos (List
 * Objects V2) con un prefijo de carpeta, parsea la respuesta XML y devuelve las URLs
 * completas de cada imagen encontrada.
 */
class CarouselRepository {

    /** Cliente HTTP de OkHttp utilizado para realizar la petición de listado al bucket S3. */
    private val client = OkHttpClient()

    /**
     * Obtiene de forma asíncrona la lista de URLs de las imágenes del carrusel desde S3.
     *
     * Construye la URL de listado con el prefijo `imagenes_carrusel/`, ejecuta la petición
     * en el dispatcher de I/O, parsea el XML de respuesta para extraer las claves (`<Key>`)
     * de cada objeto y devuelve las URLs absolutas descartando el objeto de directorio raíz.
     *
     * @return Lista de [String] con las URLs completas de las imágenes del carrusel.
     *         Devuelve una lista vacía si la petición falla o el cuerpo es nulo.
     */
    suspend fun getCarouselImageUrls(): List<String> = withContext(Dispatchers.IO) {
        val bucket = ApiConstants.S3_BASE_URL
        val url = "$bucket/?prefix=imagenes_carrusel/&list-type=2"

        val request = Request.Builder().url(url).build()
        val body = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("CarouselRepository", "S3 listing failed: HTTP ${response.code} - ${response.body?.string()}")
                return@withContext emptyList()
            }
            response.body?.string() ?: run {
                Log.e("CarouselRepository", "S3 listing returned empty body")
                return@withContext emptyList()
            }
        }

        parseS3Keys(body)
            .filter { it != "imagenes_carrusel/" && it.isNotBlank() }
            .map { "$bucket/$it" }
    }

    /**
     * Parsea el XML de respuesta del listado de objetos S3 y extrae los valores de las
     * etiquetas `<Key>`.
     *
     * Utiliza un [org.xmlpull.v1.XmlPullParser] para recorrer el documento XML de forma
     * eficiente en memoria, acumulando cada clave encontrada en una lista mutable.
     *
     * @param xml Cadena de texto con el contenido XML retornado por S3.
     * @return Lista de [String] con todas las claves de objetos encontradas en el XML.
     */
    private fun parseS3Keys(xml: String): List<String> {
        val keys = mutableListOf<String>()
        val parser = Xml.newPullParser()
        parser.setFeature(org.xmlpull.v1.XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.setInput(xml.reader())
        var inKey = false
        var eventType = parser.eventType
        while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                org.xmlpull.v1.XmlPullParser.START_TAG -> inKey = (parser.name == "Key")
                org.xmlpull.v1.XmlPullParser.TEXT -> if (inKey) keys.add(parser.text)
                org.xmlpull.v1.XmlPullParser.END_TAG -> if (parser.name == "Key") inKey = false
            }
            eventType = parser.next()
        }
        return keys
    }
}
