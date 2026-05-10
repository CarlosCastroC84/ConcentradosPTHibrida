package com.example.concentradospt

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.concentradospt.data.repository.AdminAuthRepository
import com.example.concentradospt.databinding.ActivityAdminBinding
import com.google.android.material.navigation.NavigationBarView

class AdminActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ROL = "extra_admin_rol"
        const val EXTRA_NOMBRE = "extra_admin_nombre"
    }

    private lateinit var binding: ActivityAdminBinding
    val rol: String by lazy { intent.getStringExtra(EXTRA_ROL) ?: "ADMIN" }

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

    fun adminSignOut() {
        AdminAuthRepository().signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}
