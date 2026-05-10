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

class AuthRepository {

    suspend fun signIn(email: String, password: String): AuthSignInResult =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.signIn(email, password,
                { result -> cont.resume(result) },
                { error -> cont.resumeWithException(error) }
            )
        }

    suspend fun signOut(): Unit =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.signOut { cont.resume(Unit) }
        }

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

    suspend fun isSignedIn(): Boolean =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.fetchAuthSession(
                { session -> cont.resume(session.isSignedIn) },
                { cont.resume(false) }
            )
        }

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

    // Decodifica el email del JWT sin librería externa
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

    suspend fun forgotPassword(email: String): Unit =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.resetPassword(email,
                { cont.resume(Unit) },
                { error -> cont.resumeWithException(error) }
            )
        }

    suspend fun confirmForgotPassword(email: String, code: String, newPassword: String): Unit =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.confirmResetPassword(email, newPassword, code,
                { cont.resume(Unit) },
                { error -> cont.resumeWithException(error) }
            )
        }

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

    suspend fun confirmSignUp(email: String, code: String): Unit =
        suspendCancellableCoroutine { cont ->
            Amplify.Auth.confirmSignUp(email, code,
                { cont.resume(Unit) },
                { error -> cont.resumeWithException(error) }
            )
        }
}