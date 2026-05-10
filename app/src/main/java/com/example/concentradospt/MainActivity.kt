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

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IS_ADMIN = "extra_is_admin"
    }

    private lateinit var binding: ActivityMainBinding
    val isAdmin: Boolean by lazy { intent.getBooleanExtra(EXTRA_IS_ADMIN, false) }

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
