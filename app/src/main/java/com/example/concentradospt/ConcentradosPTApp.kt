package com.example.concentradospt

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.example.concentradospt.data.network.admin.TokenManager

class ConcentradosPTApp : Application() {

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