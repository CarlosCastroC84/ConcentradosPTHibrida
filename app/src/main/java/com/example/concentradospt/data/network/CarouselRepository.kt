package com.example.concentradospt.data.network

import android.util.Log
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class CarouselRepository {

    private val client = OkHttpClient()

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
