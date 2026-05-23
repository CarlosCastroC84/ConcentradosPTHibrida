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

/**
 * Actividad de registro de nuevos usuarios en la aplicación.
 *
 * Presenta un formulario donde el usuario ingresa su nombre completo, correo electrónico
 * y contraseña para crear una cuenta a través de AWS Cognito. Tras el registro exitoso,
 * muestra un diálogo para ingresar el código de confirmación enviado al correo y
 * finaliza el proceso de verificación de la cuenta.
 */
class RegisterActivity : AppCompatActivity() {

    /** Referencia al binding de la vista para acceder a los elementos del layout. */
    private lateinit var binding: ActivityRegisterBinding

    /** ViewModel que gestiona la lógica de negocio y el estado del proceso de registro. */
    private val viewModel: RegisterViewModel by viewModels()

    /**
     * Inicializa la actividad, infla el layout, configura los listeners de los botones
     * y comienza a observar el estado del registro.
     *
     * Valida los campos del formulario antes de delegar la operación al ViewModel.
     * Las validaciones incluyen: nombre no vacío, correo con formato válido,
     * contraseña de al menos 8 caracteres y coincidencia de contraseñas.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, o null si es nueva.
     */
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

    /**
     * Observa el estado del [RegisterViewModel] y actualiza la interfaz en consecuencia.
     *
     * Reacciona a cada estado del flujo de registro:
     * - [RegisterState.Idle]: oculta el indicador de carga.
     * - [RegisterState.Loading]: muestra el indicador de carga.
     * - [RegisterState.NeedsConfirmation]: muestra el diálogo de código de confirmación.
     * - [RegisterState.Success]: muestra un diálogo de éxito y finaliza la actividad.
     * - [RegisterState.Error]: muestra el mensaje de error en un Snackbar.
     */
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

    /**
     * Controla la visibilidad del indicador de progreso y habilita o deshabilita
     * los campos del formulario durante una operación en curso.
     *
     * @param loading `true` para mostrar el indicador y bloquear los controles;
     *                `false` para ocultarlo y habilitarlos nuevamente.
     */
    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnCreateAccount.isEnabled = !loading
        binding.etName.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.etConfirmPassword.isEnabled = !loading
    }

    /**
     * Limpia los mensajes de error de todos los campos del formulario de registro,
     * preparando la interfaz para un nuevo intento de validación.
     */
    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    /**
     * Muestra el diálogo de confirmación de cuenta que solicita el código enviado por correo.
     *
     * El usuario debe ingresar el código de 6 dígitos recibido en su correo electrónico
     * para completar la verificación de la cuenta en AWS Cognito. Valida que el código
     * no esté vacío antes de enviarlo al ViewModel.
     *
     * @param email Correo electrónico del usuario recién registrado, necesario para confirmar la cuenta.
     */
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
