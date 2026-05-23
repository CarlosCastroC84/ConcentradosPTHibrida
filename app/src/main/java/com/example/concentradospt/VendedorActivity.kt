package com.example.concentradospt

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.concentradospt.data.repository.AdminAuthRepository
import com.example.concentradospt.databinding.ActivityVendedorBinding
import com.google.android.material.navigation.NavigationBarView

/**
 * Actividad principal para usuarios con rol de Vendedor.
 *
 * Actúa como contenedor del grafo de navegación exclusivo para vendedores,
 * alojando el [NavHostFragment] y la barra de navegación inferior del panel
 * de ventas. Recibe los datos del vendedor (nombre y cédula) mediante extras
 * del Intent para que los fragmentos hijos puedan acceder a ellos a través
 * de la actividad padre.
 *
 * La barra de navegación inferior se oculta automáticamente cuando el usuario
 * navega al detalle de un pedido, que dispone de su propia barra de herramientas.
 */
class VendedorActivity : AppCompatActivity() {

    companion object {
        /**
         * Clave del extra que transporta el rol del vendedor desde [LoginActivity].
         */
        const val EXTRA_ROL = "extra_vend_rol"

        /**
         * Clave del extra que transporta el nombre completo del vendedor desde [LoginActivity].
         */
        const val EXTRA_NOMBRE = "extra_vend_nombre"

        /**
         * Clave del extra que transporta la cédula del vendedor desde [LoginActivity].
         */
        const val EXTRA_CEDULA = "extra_vend_cedula"
    }

    /** Referencia al binding de la vista para acceder a los elementos del layout. */
    private lateinit var binding: ActivityVendedorBinding

    /**
     * Nombre completo del vendedor, inicializado de forma diferida desde el Intent.
     * Valor por defecto: "Vendedor".
     */
    val nombre: String by lazy { intent.getStringExtra(EXTRA_NOMBRE) ?: "Vendedor" }

    /**
     * Cédula de identidad del vendedor, inicializada de forma diferida desde el Intent.
     * Valor por defecto: cadena vacía.
     */
    val cedula: String by lazy { intent.getStringExtra(EXTRA_CEDULA) ?: "" }

    /**
     * Inicializa la actividad, configura el sistema de navegación con la barra inferior
     * del panel de ventas y registra un listener para ocultar la barra en el detalle de pedido.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, o null si es nueva.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.vendedor_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navBarView = binding.vendedorBottomNavView as NavigationBarView
        navBarView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideNavFor = setOf(R.id.nav_vend_detalle_pedido)
            navBarView.visibility =
                if (destination.id in hideNavFor) View.GONE else View.VISIBLE
        }
    }

    /**
     * Cierra la sesión del vendedor a través de [AdminAuthRepository] y
     * redirige a [LoginActivity], limpiando la pila de actividades para evitar
     * que el usuario pueda regresar con el botón de retroceso.
     */
    fun vendedorSignOut() {
        AdminAuthRepository().signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}
