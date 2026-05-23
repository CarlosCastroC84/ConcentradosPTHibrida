package com.example.concentradospt.data.network.admin

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject

/**
 * Gestor singleton de tokens de autenticación para el módulo de administración.
 *
 * Almacena y recupera de forma segura el access token y el refresh token del administrador
 * mediante [EncryptedSharedPreferences], que cifra tanto las claves como los valores usando
 * AES-256. Además, ofrece utilidades para verificar el estado de la sesión y extraer claims
 * del JWT sin depender de librerías externas.
 *
 * Debe inicializarse llamando a [init] con un [Context] de aplicación antes de usar
 * cualquier otro método.
 */
object TokenManager {

    /** Nombre del archivo de SharedPreferences cifrado donde se persisten los tokens. */
    private const val PREFS_FILE = "admin_tokens"

    /** Clave usada para almacenar el access token en las preferencias cifradas. */
    private const val KEY_ACCESS = "access_token"

    /** Clave usada para almacenar el refresh token en las preferencias cifradas. */
    private const val KEY_REFRESH = "refresh_token"

    /** Referencia a las preferencias compartidas cifradas, inicializada en [init]. */
    private lateinit var prefs: android.content.SharedPreferences

    /**
     * Inicializa el [TokenManager] creando las [EncryptedSharedPreferences] respaldadas
     * por una clave maestra AES-256-GCM generada en el Android Keystore.
     *
     * Debe llamarse una única vez desde `Application.onCreate()` antes de cualquier
     * operación de red que requiera autenticación administrativa.
     *
     * @param context Contexto de aplicación utilizado para crear la clave maestra y
     *                las preferencias cifradas.
     */
    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Persiste el par de tokens de autenticación en las preferencias cifradas.
     *
     * @param accessToken Token de acceso JWT emitido por el backend para autorizar peticiones.
     * @param refreshToken Token de refresco JWT utilizado para renovar el access token expirado.
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .apply()
    }

    /**
     * Recupera el access token almacenado actualmente.
     *
     * @return El access token como [String], o `null` si no existe ninguno almacenado.
     */
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS, null)

    /**
     * Recupera el refresh token almacenado actualmente.
     *
     * @return El refresh token como [String], o `null` si no existe ninguno almacenado.
     */
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    /**
     * Elimina ambos tokens de las preferencias cifradas, cerrando efectivamente la sesión
     * del administrador.
     */
    fun clearTokens() {
        prefs.edit().remove(KEY_ACCESS).remove(KEY_REFRESH).apply()
    }

    /**
     * Indica si el administrador tiene una sesión activa comprobando si existe un access token.
     *
     * @return `true` si existe un access token no vacío almacenado; `false` en caso contrario.
     */
    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrEmpty()

    /**
     * Extrae el valor del claim `rol` del access token JWT actual.
     *
     * @return El rol del administrador como [String], o `null` si el token no existe o
     *         no contiene el claim `rol`.
     */
    fun getRolFromToken(): String? = parseClaimFromToken(getAccessToken(), "rol")

    /**
     * Decodifica el payload del JWT y extrae el valor del claim especificado sin utilizar
     * librerías externas.
     *
     * El payload del JWT es el segundo segmento separado por `.`, codificado en Base64 URL-safe.
     * Se decodifica a JSON y se busca el claim por su nombre.
     *
     * @param token Cadena JWT de la que se extraerá el claim. Puede ser `null`.
     * @param claim Nombre del claim a extraer del payload del JWT.
     * @return Valor del claim como [String], o `null` si el token es nulo, el claim no existe
     *         o ocurre un error durante el parseo.
     */
    private fun parseClaimFromToken(token: String?, claim: String): String? {
        token ?: return null
        return try {
            val payload = token.split(".")[1]
            val decoded = String(Base64.decode(payload, Base64.URL_SAFE))
            JSONObject(decoded).optString(claim).ifEmpty { null }
        } catch (e: Exception) {
            null
        }
    }
}
