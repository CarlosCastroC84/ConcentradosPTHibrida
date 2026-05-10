package com.example.concentradospt

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
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

        // fab_cart existe solo en el layout de teléfono; en tablet se usa NavigationRailView
        if (binding.fabCart != null) {
            setupPhoneNav(navController)
            observeCartBadge()
        } else {
            val navRail = binding.root.findViewById<NavigationBarView>(R.id.bottom_nav_view)
            setupTabletNav(navController, navRail)
        }
    }

    private fun observeCartBadge() {
        lifecycleScope.launch {
            CartManager.items.collectLatest { items ->
                val total = items.sumOf { it.cantidad }
                binding.cartBadge?.let { badge ->
                    if (total > 0) {
                        badge.visibility = View.VISIBLE
                        badge.text = if (total > 99) "99+" else total.toString()
                    } else {
                        badge.visibility = View.GONE
                    }
                }

                // Si también queremos badge en el NavigationRailView (tablet)
                val navRail = binding.root.findViewById<NavigationBarView>(R.id.bottom_nav_view)
                navRail?.let { rail ->
                    val badge = rail.getOrCreateBadge(R.id.nav_cart)
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

    private fun setupTabletNav(navController: NavController, navView: NavigationBarView) {
        navView.setupWithNavController(navController)
        if (isAdmin) navView.menu.findItem(R.id.nav_dashboard)?.isVisible = true

        val hideNavFor = setOf(
            R.id.nav_checkout,
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

    private fun setupPhoneNav(navController: NavController) {
        if (isAdmin) binding.navItemDashboard?.visibility = View.VISIBLE

        fun navigate(destinationId: Int) {
            val options = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(navController.graph.startDestinationId, inclusive = false, saveState = true)
                .build()
            try { navController.navigate(destinationId, null, options) } catch (_: Exception) { }
        }

        binding.navItemHome?.setOnClickListener { navigate(R.id.nav_home) }
        binding.navItemCatalog?.setOnClickListener { navigate(R.id.nav_catalog) }
        binding.fabCart?.setOnClickListener { navigate(R.id.nav_cart) }
        binding.navItemProfile?.setOnClickListener { navigate(R.id.nav_profile) }
        binding.navItemDashboard?.setOnClickListener { navigate(R.id.nav_dashboard) }

        val hideNavFor = setOf(
            R.id.nav_checkout,
            R.id.nav_detalle_producto,
            R.id.nav_detalle_pedido,
            R.id.nav_gestion_productos,
            R.id.nav_gestion_usuarios
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.navItemHome?.isSelected = destination.id == R.id.nav_home
            binding.navItemCatalog?.isSelected = destination.id == R.id.nav_catalog
            binding.navItemProfile?.isSelected = destination.id == R.id.nav_profile
            binding.navItemDashboard?.isSelected = destination.id == R.id.nav_dashboard

            binding.bottomNavContainer?.visibility =
                if (destination.id in hideNavFor) View.GONE else View.VISIBLE
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
