package com.example.concentradospt

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.concentradospt.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de bienvenida (splash screen) de la aplicación.
 *
 * Se muestra brevemente al iniciar la aplicación con el logo y la identidad visual
 * de la marca. Tras un retraso de 2200 milisegundos, navega automáticamente hacia
 * [LoginActivity] y se destruye a sí misma para que no aparezca en la pila de retroceso.
 *
 * La anotación [@SuppressLint("CustomSplashScreen")] suprime la advertencia del
 * sistema que recomienda usar la API de SplashScreen de Android 12+, ya que esta
 * implementación personalizada es intencional para mayor compatibilidad.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    /** Referencia al binding de la vista para acceder a los elementos del layout del splash. */
    private lateinit var binding: ActivitySplashBinding

    /**
     * Inicializa la actividad, muestra el layout del splash y programa la navegación
     * automática a [LoginActivity] después de 2200 milisegundos usando una corrutina
     * del ciclo de vida de la actividad.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, o null si es nueva.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(2200L)
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }
    }
}
