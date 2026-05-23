package com.example.concentradospt.data.repository

import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.options.AuthSignInOptions
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repositorio que encapsula todas las operaciones de autenticación de usuarios
 * mediante Amazon Cognito a través de AWS Amplify.
 *
 * Convierte las APIs basadas en callbacks de Amplify en funciones `suspend` mediante
 * [suspendCancellableCoroutine], permitiendo su uso directo desde corrutinas y ViewModels.
 * También decodifica manualmente los tokens JWT para extraer claims sin depender de
 * librerías externas de parseo de JWT.
 */
class AuthRepository {

    /**
     * Inicia sesión en Cognito con correo electrónico y contraseña.
     *
     * @param email Dirección de correo electrónico del usuario.
     * @param password Contraseña del usuario.
     * @return [AuthSignInResult] con el resultado del intento de inicio de sesión,
     *         que puede indicar éxito o requerir pasos adicionales (p. ej. MFA).
     * @throws [AuthException] si las credenciales son incorrectas o el usuario no existe.
     */
    suspend fun signIn(email: String, password: String): AuthSignInResult =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.signIn(email, password,
                { result -> cont.resume(result) },
                { error -> cont.resumeWithException(error) }
            )
        }

    /**
     * Cierra la sesión del usuario actualmente autenticado en Cognito.
     * La operación siempre completa sin lanzar excepción, independientemente del resultado.
     */
    suspend fun signOut(): Unit =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.signOut { cont.resume(Unit) }
        }

    /**
     * Obtiene el access token de la sesión activa de Cognito.
     *
     * @return El access token como [String] si el usuario está autenticado, o `null`
     *         si no hay sesión activa o ocurre un error al obtenerla.
     */
    suspend fun fetchAccessToken(): String? =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.fetchAuthSession(
                { session ->
                    val cognitoSession = session as? AWSCognitoAuthSession
                    val token = cognitoSession?.userPoolTokensResult?.value?.accessToken
                    cont.resume(token)
                },
                { error ->
                    android.util.Log.e("AuthRepository", "Error fetching access token", error)
                    cont.resume(null)
                }
            )
        }

    /**
     * Verifica si hay un usuario con sesión activa en Cognito.
     *
     * @return `true` si existe una sesión autenticada válida; `false` en caso contrario
     *         o si ocurre un error al consultar la sesión.
     */
    suspend fun isSignedIn(): Boolean =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.fetchAuthSession(
                { session -> cont.resume(session.isSignedIn) },
                { cont.resume(false) }
            )
        }

    /**
     * Obtiene el correo electrónico del usuario actualmente autenticado decodificando
     * el claim `email` del ID Token de Cognito.
     *
     * @return El correo electrónico como [String] si está disponible en el token,
     *         o `null` si no hay sesión activa o el claim no existe.
     */
    suspend fun getCurrentUserEmail(): String? =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.fetchAuthSession(
                { session ->
                    val cognitoSession = session as? AWSCognitoAuthSession
                    val idToken = cognitoSession?.userPoolTokensResult?.value?.idToken
                    cont.resume(parseEmailFromToken(idToken))
                },
                { cont.resume(null) }
            )
        }

    /**
     * Obtiene los grupos de Cognito a los que pertenece el usuario autenticado, decodificando
     * el claim `cognito:groups` del ID Token.
     *
     * @return Lista de [String] con los nombres de los grupos a los que pertenece el usuario.
     *         Devuelve una lista vacía si no hay sesión activa, el usuario no pertenece a
     *         ningún grupo, o ocurre un error.
     */
    suspend fun getUserGroups(): List<String> =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.fetchAuthSession(
                { session ->
                    val cognitoSession = session as? AWSCognitoAuthSession
                    val idToken = cognitoSession?.userPoolTokensResult?.value?.idToken
                    cont.resume(parseGroupsFromToken(idToken))
                },
                { cont.resume(emptyList()) }
            )
        }

    /**
     * Decodifica el claim `email` del payload de un ID Token JWT sin utilizar librerías externas.
     *
     * El payload es el segundo segmento del JWT separado por `.`, codificado en Base64 URL-safe.
     *
     * @param idToken Cadena JWT del ID Token de Cognito. Puede ser `null`.
     * @return El correo electrónico extraído del token, o `null` si el token es nulo,
     *         el claim no existe o ocurre un error de parseo.
     */
    private fun parseEmailFromToken(idToken: String?): String? {
        idToken ?: return null
        return try {
            val payload = idToken.split(".")[1]
            val decoded = String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE))
            val json = org.json.JSONObject(decoded)
            json.optString("email").ifEmpty { null }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decodifica el claim `cognito:groups` del payload de un ID Token JWT sin utilizar
     * librerías externas.
     *
     * @param idToken Cadena JWT del ID Token de Cognito. Puede ser `null`.
     * @return Lista de [String] con los nombres de los grupos Cognito. Devuelve una lista
     *         vacía si el token es nulo, el claim no existe o ocurre un error de parseo.
     */
    private fun parseGroupsFromToken(idToken: String?): List<String> {
        idToken ?: return emptyList()
        return try {
            val payload = idToken.split(".")[1]
            val decoded = String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE))
            val json = org.json.JSONObject(decoded)
            val groups = json.optJSONArray("cognito:groups") ?: return emptyList()
            (0 until groups.length()).map { groups.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Inicia el flujo de recuperación de contraseña enviando un código de verificación
     * al correo electrónico del usuario.
     *
     * @param email Dirección de correo electrónico del usuario que desea recuperar su contraseña.
     * @throws [AuthException] si el correo no corresponde a ningún usuario registrado.
     */
    suspend fun forgotPassword(email: String): Unit =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.resetPassword(email,
                { cont.resume(Unit) },
                { error -> cont.resumeWithException(error) }
            )
        }

    /**
     * Confirma el restablecimiento de contraseña usando el código recibido por correo
     * y establece la nueva contraseña.
     *
     * @param email Dirección de correo electrónico del usuario.
     * @param code Código de verificación enviado al correo del usuario.
     * @param newPassword Nueva contraseña que se asignará al usuario.
     * @throws [AuthException] si el código es inválido, ha expirado, o la nueva contraseña
     *         no cumple la política de Cognito.
     */
    suspend fun confirmForgotPassword(email: String, code: String, newPassword: String): Unit =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.confirmResetPassword(email, newPassword, code,
                { cont.resume(Unit) },
                { error -> cont.resumeWithException(error) }
            )
        }

    /**
     * Registra un nuevo usuario en el User Pool de Cognito con correo, contraseña y nombre completo.
     *
     * El nombre completo se agrega como atributo de usuario `name` de Cognito. Tras el registro
     * exitoso, el usuario recibirá un correo de verificación con un código de confirmación.
     *
     * @param email Dirección de correo electrónico que será el identificador del usuario.
     * @param password Contraseña inicial del usuario (debe cumplir la política de Cognito).
     * @param fullName Nombre completo del usuario que se almacenará como atributo `name`.
     * @throws [AuthException] si el correo ya está registrado o la contraseña no cumple la política.
     */
    suspend fun signUp(email: String, password: String, fullName: String): Unit =
        suspendCancellableCoroutine { cont ->
            val options = AuthSignUpOptions.builder()
                .userAttribute(AuthUserAttributeKey.name(), fullName)
                .build()
            Amplify.Auth.signUp(email, password, options,
                { cont.resume(Unit) },
                { error -> cont.resumeWithException(error) }
            )
        }

    /**
     * Confirma el registro de un usuario usando el código de verificación enviado por Cognito
     * al correo electrónico proporcionado durante el registro.
     *
     * @param email Dirección de correo electrónico del usuario a confirmar.
     * @param code Código de verificación de 6 dígitos recibido por correo.
     * @throws [AuthException] si el código es inválido o ha expirado.
     */
    suspend fun confirmSignUp(email: String, code: String): Unit =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.confirmSignUp(email, code,
                { cont.resume(Unit) },
                { error -> cont.resumeWithException(error) }
            )
        }
}
