package com.example.concentradospt

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.concentradospt.data.repository.AdminAuthRepository
import com.example.concentradospt.databinding.ActivityAdminBinding
import com.google.android.material.navigation.NavigationBarView

/**
 * Actividad principal para usuarios con rol administrativo (ADMIN o BODEGA).
 *
 * Actúa como contenedor del grafo de navegación del panel de administración,
 * alojando el [NavHostFragment] y la barra de navegación inferior exclusiva
 * para administradores. Muestra u oculta opciones del menú según el rol del
 * usuario: solo el rol ADMIN puede acceder a la gestión de usuarios.
 * También oculta la barra de navegación en pantallas con toolbar propio.
 */
class AdminActivity : AppCompatActivity() {

    companion object {
        /**
         * Clave del extra que transporta el rol del usuario administrativo
         * (por ejemplo "ADMIN" o "BODEGA") desde [LoginActivity].
         */
        const val EXTRA_ROL = "extra_admin_rol"

        /**
         * Clave del extra que transporta el nombre completo del usuario administrativo
         * desde [LoginActivity].
         */
        const val EXTRA_NOMBRE = "extra_admin_nombre"
    }

    /** Referencia al binding de la vista para acceder a los elementos del layout. */
    private lateinit var binding: ActivityAdminBinding

    /**
     * Rol del usuario administrativo, inicializado de forma diferida desde el Intent.
     * Valor por defecto: "ADMIN".
     */
    val rol: String by lazy { intent.getStringExtra(EXTRA_ROL) ?: "ADMIN" }

    /**
     * Inicializa la actividad, configura el sistema de navegación con la barra inferior
     * del panel de administración y aplica la visibilidad de ítems de menú según el rol.
     *
     * Oculta la barra de navegación en pantallas que disponen de toolbar propio
     * (gestión de productos, gestión de usuarios, detalle de venta y reportes).
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, o null si es nueva.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.admin_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navBarView = binding.adminBottomNavView as NavigationBarView
        navBarView.setupWithNavController(navController)

        // Mostrar pestaña Usuarios solo para ADMIN
        if (rol == "ADMIN") {
            navBarView.menu.findItem(R.id.nav_admin_gestion_usuarios)?.isVisible = true
        }

        // Ocultar nav en pantallas con toolbar propio
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideNavFor = setOf(
                R.id.nav_admin_gestion_productos,
                R.id.nav_admin_gestion_usuarios,
                R.id.nav_admin_detalle_venta,
                R.id.nav_admin_reportes_ventas
            )
            navBarView.visibility =
                if (destination.id in hideNavFor) android.view.View.GONE else android.view.View.VISIBLE
        }
    }

    /**
     * Cierra la sesión del usuario administrativo a través de [AdminAuthRepository]
     * y redirige a [LoginActivity], limpiando la pila de actividades para evitar
     * que el usuario pueda regresar con el botón de retroceso.
     */
    fun adminSignOut() {
        AdminAuthRepository().signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}
