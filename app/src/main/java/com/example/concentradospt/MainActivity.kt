package com.example.concentradospt

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.concentradospt.data.repository.AuthRepository
import com.example.concentradospt.databinding.ActivityMainBinding
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

        binding.bottomNavView.setupWithNavController(navController)

        if (isAdmin) {
            binding.bottomNavView.menu.findItem(R.id.nav_dashboard)?.isVisible = true
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideNavFor = setOf(
                R.id.nav_checkout,
                R.id.nav_detalle_producto,
                R.id.nav_detalle_pedido,
                R.id.nav_gestion_productos,
                R.id.nav_gestion_usuarios
            )
            binding.bottomNavView.visibility =
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