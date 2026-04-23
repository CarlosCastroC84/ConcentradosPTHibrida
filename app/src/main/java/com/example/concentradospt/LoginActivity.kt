package com.example.concentradospt

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.concentradospt.data.model.admin.AdminUsuarioInfo
import com.example.concentradospt.databinding.ActivityLoginBinding
import com.example.concentradospt.ui.login.LoginState
import com.example.concentradospt.ui.login.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    private val roles = listOf("ADMIN", "VENDEDOR", "BODEGA")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRolDropdown()
        setupIdentifierDetection()
        observeState()
        viewModel.checkExistingSession()

        binding.btnLogin.setOnClickListener {
            clearErrors()
            val identifier = binding.etEmail.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString()?.trim() ?: ""
            val rolSelected = binding.actvRol.text?.toString()?.trim() ?: ""
            val isCedula = identifier.matches(Regex("\\d{4,20}"))

            if (identifier.isEmpty()) {
                binding.tilEmail.error = if (isCedula) "Ingrese su cédula" else "Ingrese su correo"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.tilPassword.error = if (isCedula) "Ingrese su PIN" else "Ingrese su contraseña"
                return@setOnClickListener
            }

            if (isCedula && rolSelected.isNotEmpty()) {
                if (password.length != 4) {
                    binding.tilPassword.error = "El PIN debe tener 4 dígitos"
                    return@setOnClickListener
                }
                viewModel.signInAdmin(cedula = identifier, rol = rolSelected, pin = password)
            } else {
                viewModel.signIn(email = identifier, password = password)
            }
        }

        binding.tvForgotPassword.setOnClickListener { /* TODO: recuperar contraseña */ }
        binding.tvRegister.setOnClickListener { /* TODO: registro */ }
        binding.btnBiometric.setOnClickListener { /* TODO: biometría */ }
    }

    private fun setupRolDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.actvRol.setAdapter(adapter)
    }

    private fun setupIdentifierDetection() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                val isCedula = text.matches(Regex("\\d{4,20}"))
                clearErrors()
                if (isCedula) {
                    binding.tilRol.visibility = View.VISIBLE
                    binding.tilEmail.hint = "Cédula de Identidad"
                    binding.tilPassword.hint = "PIN de Seguridad (4 dígitos)"
                    binding.etPassword.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                    binding.etPassword.filters = arrayOf(android.text.InputFilter.LengthFilter(4))
                } else {
                    binding.tilRol.visibility = View.GONE
                    binding.actvRol.text = null
                    binding.tilEmail.hint = getString(R.string.login_label_email)
                    binding.tilPassword.hint = getString(R.string.login_label_password)
                    binding.etPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.etPassword.filters = arrayOf()
                }
            }
        })
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is LoginState.Idle -> setLoading(false)
                    is LoginState.Loading -> setLoading(true)
                    is LoginState.Success -> {
                        setLoading(false)
                        navigateToMain(state.isAdmin)
                    }
                    is LoginState.AdminSuccess -> {
                        setLoading(false)
                        navigateToAdmin(state.usuario)
                    }
                    is LoginState.Error -> {
                        setLoading(false)
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.actvRol.isEnabled = !loading
    }

    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
    }

    private fun navigateToMain(isAdmin: Boolean) {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_IS_ADMIN, isAdmin)
        })
        finish()
    }

    private fun navigateToAdmin(usuario: AdminUsuarioInfo) {
        startActivity(Intent(this, AdminActivity::class.java).apply {
            putExtra(AdminActivity.EXTRA_ROL, usuario.rol)
            putExtra(AdminActivity.EXTRA_NOMBRE, usuario.nombreCompleto)
        })
        finish()
    }
}
