package com.example.concentradospt

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.concentradospt.databinding.ActivityRegisterBinding
import com.example.concentradospt.databinding.DialogRegisterConfirmBinding
import com.example.concentradospt.ui.register.RegisterState
import com.example.concentradospt.ui.register.RegisterViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeState()

        binding.btnCreateAccount.setOnClickListener {
            clearErrors()
            val fullName = binding.etName.text?.toString()?.trim() ?: ""
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""
            val confirmPassword = binding.etConfirmPassword.text?.toString() ?: ""

            when {
                fullName.isEmpty() ->
                    binding.tilName.error = getString(R.string.register_error_empty_name)
                email.isEmpty() ->
                    binding.tilEmail.error = getString(R.string.register_error_empty_email)
                !email.contains("@") ->
                    binding.tilEmail.error = getString(R.string.register_error_invalid_email)
                password.isEmpty() ->
                    binding.tilPassword.error = getString(R.string.register_error_empty_password)
                password.length < 8 ->
                    binding.tilPassword.error = getString(R.string.register_error_short_password)
                password != confirmPassword ->
                    binding.tilConfirmPassword.error = getString(R.string.register_error_password_mismatch)
                else -> viewModel.signUp(fullName, email, password)
            }
        }

        binding.tvSignIn.setOnClickListener { finish() }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is RegisterState.Idle -> setLoading(false)
                    is RegisterState.Loading -> setLoading(true)
                    is RegisterState.NeedsConfirmation -> {
                        setLoading(false)
                        showConfirmationDialog(state.email)
                    }
                    is RegisterState.Success -> {
                        setLoading(false)
                        MaterialAlertDialogBuilder(this@RegisterActivity)
                            .setTitle(R.string.register_success_title)
                            .setMessage(R.string.register_success_message)
                            .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
                            .setCancelable(false)
                            .show()
                    }
                    is RegisterState.Error -> {
                        setLoading(false)
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnCreateAccount.isEnabled = !loading
        binding.etName.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.etConfirmPassword.isEnabled = !loading
    }

    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    private fun showConfirmationDialog(email: String) {
        val dialogBinding = DialogRegisterConfirmBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.register_confirm_title)
            .setMessage(R.string.register_confirm_message)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_confirm) { dialog, _ ->
                val code = dialogBinding.etConfirmCode.text?.toString()?.trim() ?: ""
                if (code.isEmpty()) {
                    dialogBinding.tilConfirmCode.error = getString(R.string.register_confirm_error_empty_code)
                    return@setPositiveButton
                }
                dialog.dismiss()
                viewModel.confirmSignUp(email, code)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}
