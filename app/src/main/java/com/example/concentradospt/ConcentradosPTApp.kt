package com.example.concentradospt

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.example.concentradospt.data.network.admin.TokenManager

/**
 * Clase de aplicación personalizada de ConcentradosPT.
 *
 * Punto de entrada global del proceso de la aplicación Android. Se ejecuta antes
 * que cualquier actividad, servicio o receptor. Realiza las inicializaciones
 * necesarias a nivel de aplicación:
 *
 * 1. Inicializa el [TokenManager] con el contexto de la aplicación para gestionar
 *    los tokens de autenticación administrativa.
 * 2. Configura y registra el plugin de autenticación de AWS Amplify ([AWSCognitoAuthPlugin])
 *    para conectar la aplicación con el pool de usuarios de Amazon Cognito.
 *
 * Debe declararse en el `AndroidManifest.xml` mediante el atributo `android:name`.
 */
class ConcentradosPTApp : Application() {

    /**
     * Llamado cuando la aplicación se inicia por primera vez.
     *
     * Inicializa el [TokenManager] para la gestión de tokens administrativos y
     * configura AWS Amplify con el plugin de autenticación Cognito. Si la
     * inicialización de Amplify falla, registra el error en el log sin interrumpir
     * el arranque de la aplicación.
     */
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(applicationContext)
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("Amplify", "Initialized successfully")
        } catch (e: AmplifyException) {
            Log.e("Amplify", "Failed to initialize", e)
        }
    }
}
