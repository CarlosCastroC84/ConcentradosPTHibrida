package com.example.concentradospt

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.concentradospt.data.repository.CartManager
import com.example.concentradospt.data.repository.AuthRepository
import com.example.concentradospt.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Actividad principal de la aplicación para usuarios clientes autenticados.
 *
 * Actúa como contenedor del grafo de navegación de la aplicación, alojando el
 * [NavHostFragment] y la barra de navegación inferior. Gestiona la visibilidad
 * del badge del carrito en tiempo real y oculta la barra de navegación en
 * determinadas pantallas (checkout, pago, detalle de producto, etc.).
 * También expone la función de cierre de sesión.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        /**
         * Clave del extra que indica si el usuario autenticado tiene permisos de administrador.
         * Se recibe desde [LoginActivity] al navegar hacia esta actividad.
         */
        const val EXTRA_IS_ADMIN = "extra_is_admin"
    }

    /** Referencia al binding de la vista para acceder a los elementos del layout. */
    private lateinit var binding: ActivityMainBinding

    /**
     * Indica si el usuario autenticado tiene rol de administrador.
     * Se inicializa de forma diferida leyendo el extra del Intent.
     */
    val isAdmin: Boolean by lazy { intent.getBooleanExtra(EXTRA_IS_ADMIN, false) }

    /**
     * Inicializa la actividad, configura el sistema de navegación con la barra inferior
     * y comienza a observar los cambios en el carrito para actualizar el badge.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, o null si es nueva.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navView = binding.root.findViewById<NavigationBarView>(R.id.bottom_nav_view)
        if (navView != null) {
            setupNav(navController, navView)
        }
        observeCartBadge()
    }

    /**
     * Enlaza el [NavController] con la barra de navegación inferior y configura
     * la visibilidad dinámica de la barra según el destino activo.
     *
     * La barra se oculta en pantallas que tienen su propia barra de herramientas
     * o requieren pantalla completa (checkout, pago, detalle de producto, etc.).
     * Si el usuario es administrador, hace visible la pestaña del dashboard.
     *
     * @param navController Controlador de navegación del grafo principal.
     * @param navView Vista de la barra de navegación inferior a configurar.
     */
    private fun setupNav(navController: NavController, navView: NavigationBarView) {
        navView.setupWithNavController(navController)
        if (isAdmin) navView.menu.findItem(R.id.nav_dashboard)?.isVisible = true

        val hideNavFor = setOf(
            R.id.nav_checkout,
            R.id.nav_pago,
            R.id.nav_pago_exito,
            R.id.nav_detalle_producto,
            R.id.nav_detalle_pedido,
            R.id.nav_gestion_productos,
            R.id.nav_gestion_usuarios
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
            navView.visibility =
                if (destination.id in hideNavFor) View.GONE else View.VISIBLE
        }
    }

    /**
     * Observa el flujo de ítems del [CartManager] y actualiza en tiempo real
     * el badge numérico del ícono del carrito en la barra de navegación inferior.
     *
     * Muestra el badge con la cantidad total de unidades cuando hay ítems en el
     * carrito, y lo oculta cuando el carrito está vacío.
     */
    private fun observeCartBadge() {
        lifecycleScope.launch {
            CartManager.items.collectLatest { items ->
                val total = items.sumOf { it.cantidad }
                val navView = binding.root.findViewById<NavigationBarView>(R.id.bottom_nav_view)
                navView?.let {
                    val badge = it.getOrCreateBadge(R.id.nav_cart)
                    if (total > 0) {
                        badge.isVisible = true
                        badge.number = total
                    } else {
                        badge.isVisible = false
                    }
                }
            }
        }
    }

    /**
     * Cierra la sesión del usuario autenticado a través de [AuthRepository] y
     * redirige a [LoginActivity], limpiando la pila de actividades para evitar
     * que el usuario pueda regresar con el botón de retroceso.
     */
    fun signOut() {
        lifecycleScope.launch {
            AuthRepository().signOut()
            val intent = Intent(this@MainActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}
