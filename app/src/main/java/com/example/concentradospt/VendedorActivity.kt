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

class VendedorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ROL = "extra_vend_rol"
        const val EXTRA_NOMBRE = "extra_vend_nombre"
        const val EXTRA_CEDULA = "extra_vend_cedula"
    }

    private lateinit var binding: ActivityVendedorBinding
    val nombre: String by lazy { intent.getStringExtra(EXTRA_NOMBRE) ?: "Vendedor" }
    val cedula: String by lazy { intent.getStringExtra(EXTRA_CEDULA) ?: "" }

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

    fun vendedorSignOut() {
        AdminAuthRepository().signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}
