package com.example.concentradospt

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.concentradospt.data.model.admin.AdminUsuarioInfo
import com.example.concentradospt.databinding.ActivityLoginBinding
import com.example.concentradospt.databinding.DialogForgotStep1Binding
import com.example.concentradospt.databinding.DialogForgotStep2Binding
import com.example.concentradospt.ui.login.LoginState
import com.example.concentradospt.ui.login.LoginViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Actividad principal de inicio de sesión de la aplicación.
 *
 * Permite a los usuarios autenticarse mediante correo electrónico y contraseña,
 * o mediante cédula y PIN para usuarios administrativos (Admin, Vendedor, Bodega).
 * También soporta autenticación biométrica (huella dactilar/reconocimiento facial)
 * para usuarios con credenciales previamente almacenadas, y flujo de recuperación
 * de contraseña a través de AWS Cognito.
 */
class LoginActivity : AppCompatActivity() {

    /** Referencia al binding de la vista para acceder a los elementos del layout. */
    private lateinit var binding: ActivityLoginBinding

    /** ViewModel que gestiona la lógica de negocio y el estado del inicio de sesión. */
    private val viewModel: LoginViewModel by viewModels()

    /** Lista de roles disponibles para el inicio de sesión administrativo. */
    private val roles = listOf("ADMIN", "VENDEDOR", "BODEGA")

    /**
     * Constantes utilizadas para el manejo de preferencias cifradas de biometría.
     */
    private companion object {
        /** Nombre del archivo de preferencias cifradas para biometría. */
        const val BIOMETRIC_PREFS_FILE = "biometric_prefs"

        /** Clave para almacenar el correo electrónico del usuario en las preferencias biométricas. */
        const val KEY_BIOMETRIC_EMAIL = "pref_biometric_email"

        /** Clave para almacenar la contraseña del usuario en las preferencias biométricas. */
        const val KEY_BIOMETRIC_PASSWORD = "pref_biometric_password"
    }

    /**
     * Instancia de [EncryptedSharedPreferences] inicializada de forma diferida.
     * Utiliza AES-256-GCM para cifrar claves y valores, garantizando la seguridad
     * de las credenciales guardadas para la autenticación biométrica.
     */
    private val biometricPrefs by lazy {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            this,
            BIOMETRIC_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Inicializa la actividad, configura los componentes de la interfaz y registra
     * los observadores y los listeners de interacción del usuario.
     *
     * Verifica si existe una sesión activa al arrancar, y configura el comportamiento
     * del formulario según el tipo de identificador ingresado (cédula vs. correo).
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, o null si es nueva.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRolDropdown()
        setupIdentifierDetection()
        observeState()
        setupBiometricButton()
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

        binding.tvForgotPassword.setOnClickListener { showForgotPasswordStep1Dialog() }
        binding.tvRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        binding.btnBiometric.setOnClickListener { launchBiometricPrompt() }
    }

    /**
     * Configura el desplegable (dropdown) para la selección del rol administrativo.
     * Enlaza el adaptador con la lista de roles disponibles al campo de texto autocompletable.
     */
    private fun setupRolDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.actvRol.setAdapter(adapter)
    }

    /**
     * Configura la detección automática del tipo de identificador ingresado.
     *
     * Observa los cambios en el campo de identificador: si el texto es una secuencia
     * numérica de 4 a 20 dígitos, se trata como una cédula y se ajusta la interfaz
     * para el flujo de administrador (muestra selector de rol, oculta opciones de cliente).
     * De lo contrario, se muestra la interfaz estándar de correo electrónico.
     */
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
                    binding.tvForgotPassword.visibility = View.GONE
                    binding.btnBiometric.visibility = View.GONE
                    binding.tvOr.visibility = View.GONE
                    binding.llRegister.visibility = View.GONE
                } else {
                    binding.tilRol.visibility = View.GONE
                    binding.actvRol.text = null
                    binding.tilEmail.hint = getString(R.string.login_label_email)
                    binding.tilPassword.hint = getString(R.string.login_label_password)
                    binding.etPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.etPassword.filters = arrayOf()
                    binding.tvForgotPassword.visibility = View.VISIBLE
                    binding.llRegister.visibility = View.VISIBLE
                    setupBiometricButton()
                }
            }
        })
    }

    /**
     * Configura la visibilidad y el estado del botón de autenticación biométrica.
     *
     * Verifica si el dispositivo soporta biometría fuerte o débil. Si la biometría
     * está disponible y existen credenciales guardadas, habilita el botón; de lo
     * contrario, lo muestra deshabilitado con un texto orientativo o lo oculta
     * si el dispositivo no tiene soporte biométrico.
     */
    private fun setupBiometricButton() {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            binding.btnBiometric.visibility = View.VISIBLE
            binding.tvOr.visibility = View.VISIBLE
            if (hasBiometricCredentials()) {
                binding.btnBiometric.isEnabled = true
                binding.btnBiometric.text = getString(R.string.login_btn_biometric)
            } else {
                binding.btnBiometric.isEnabled = false
                binding.btnBiometric.text = getString(R.string.login_biometric_setup_hint)
            }
        } else {
            binding.btnBiometric.visibility = View.GONE
            binding.tvOr.visibility = View.GONE
        }
    }

    /**
     * Lanza el diálogo de autenticación biométrica del sistema.
     *
     * Al autenticarse exitosamente, recupera las credenciales guardadas y llama
     * al ViewModel para iniciar sesión automáticamente. Los errores distintos a
     * cancelación por el usuario se muestran en un Snackbar.
     */
    private fun launchBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.login_biometric_prompt_title))
            .setSubtitle(getString(R.string.login_biometric_prompt_subtitle))
            .setNegativeButtonText(getString(R.string.action_cancel))
            .build()

        BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (getBiometricEmail() == null) return
                viewModel.checkExistingSession()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Snackbar.make(binding.root, errString, Snackbar.LENGTH_SHORT).show()
                }
            }
            override fun onAuthenticationFailed() = Unit
        }).authenticate(promptInfo)
    }

    /**
     * Muestra el primer paso del diálogo de recuperación de contraseña.
     *
     * Solicita el correo electrónico del usuario para enviar el código de verificación.
     * Si el campo de identificador ya contiene un correo válido, lo pre-rellena
     * automáticamente en el diálogo. Al confirmar, delega el envío al ViewModel.
     */
    private fun showForgotPasswordStep1Dialog() {
        val dialogBinding = DialogForgotStep1Binding.inflate(LayoutInflater.from(this))
        val prefillEmail = binding.etEmail.text?.toString()?.trim()
            ?.takeIf { !it.matches(Regex("\\d{4,20}")) } ?: ""
        dialogBinding.etForgotEmail.setText(prefillEmail)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.forgot_password_dialog_title)
            .setMessage(R.string.forgot_password_step1_message)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.forgot_password_btn_send) { dialog, _ ->
                val email = dialogBinding.etForgotEmail.text?.toString()?.trim() ?: ""
                if (email.isEmpty()) {
                    dialogBinding.tilForgotEmail.error = getString(R.string.forgot_password_error_empty_email)
                    return@setPositiveButton
                }
                dialog.dismiss()
                viewModel.forgotPassword(email)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    /**
     * Muestra el segundo paso del diálogo de recuperación de contraseña.
     *
     * Permite al usuario ingresar el código de verificación recibido por correo
     * y establecer una nueva contraseña. Valida que el código no esté vacío,
     * que la contraseña tenga al menos 8 caracteres y que ambas contraseñas coincidan.
     *
     * @param email Correo electrónico del usuario al que se envió el código de verificación.
     */
    private fun showForgotPasswordStep2Dialog(email: String) {
        val dialogBinding = DialogForgotStep2Binding.inflate(LayoutInflater.from(this))

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.forgot_password_dialog_title)
            .setMessage(R.string.forgot_password_step2_message)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.forgot_password_btn_confirm) { dialog, _ ->
                val code = dialogBinding.etResetCode.text?.toString()?.trim() ?: ""
                val newPwd = dialogBinding.etNewPassword.text?.toString() ?: ""
                val confirmPwd = dialogBinding.etConfirmPassword.text?.toString() ?: ""

                dialogBinding.tilResetCode.error = null
                dialogBinding.tilNewPassword.error = null
                dialogBinding.tilConfirmPassword.error = null

                when {
                    code.isEmpty() ->
                        dialogBinding.tilResetCode.error = getString(R.string.forgot_password_error_empty_code)
                    newPwd.length < 8 ->
                        dialogBinding.tilNewPassword.error = getString(R.string.forgot_password_error_short_password)
                    newPwd != confirmPwd ->
                        dialogBinding.tilConfirmPassword.error = getString(R.string.forgot_password_error_password_mismatch)
                    else -> {
                        dialog.dismiss()
                        viewModel.confirmForgotPassword(email, code, newPwd)
                    }
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    /**
     * Observa el estado del [LoginViewModel] y actualiza la interfaz en consecuencia.
     *
     * Reacciona a cada estado posible del flujo de inicio de sesión:
     * - [LoginState.Idle]: oculta el indicador de carga.
     * - [LoginState.Loading]: muestra el indicador de carga.
     * - [LoginState.Success]: guarda credenciales biométricas y navega a la pantalla principal.
     * - [LoginState.AdminSuccess]: navega a la pantalla de administración o vendedor.
     * - [LoginState.Error]: muestra el mensaje de error en un Snackbar.
     * - [LoginState.ForgotPasswordCodeSent]: muestra el paso 2 del diálogo de recuperación.
     * - [LoginState.ForgotPasswordSuccess]: muestra confirmación de restablecimiento exitoso.
     * - [LoginState.ForgotPasswordError]: muestra el error del restablecimiento.
     */
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is LoginState.Idle -> setLoading(false)
                    is LoginState.Loading -> setLoading(true)
                    is LoginState.Success -> {
                        setLoading(false)
                        val identifier = binding.etEmail.text?.toString()?.trim() ?: ""
                        val password = binding.etPassword.text?.toString()?.trim() ?: ""
                        if (identifier.isNotEmpty() && !identifier.matches(Regex("\\d{4,20}"))) {
                            saveBiometricCredentials(identifier)
                        }
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
                    is LoginState.ForgotPasswordCodeSent -> {
                        setLoading(false)
                        showForgotPasswordStep2Dialog(state.email)
                    }
                    is LoginState.ForgotPasswordSuccess -> {
                        setLoading(false)
                        Snackbar.make(binding.root, R.string.forgot_password_success, Snackbar.LENGTH_LONG).show()
                        viewModel.resetToIdle()
                    }
                    is LoginState.ForgotPasswordError -> {
                        setLoading(false)
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        viewModel.resetToIdle()
                    }
                }
            }
        }
    }

    /**
     * Controla la visibilidad del indicador de progreso y habilita o deshabilita
     * los controles del formulario durante una operación en curso.
     *
     * @param loading `true` para mostrar el indicador y deshabilitar controles;
     *                `false` para ocultarlo y habilitarlos nuevamente.
     */
    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.actvRol.isEnabled = !loading
    }

    /**
     * Limpia los mensajes de error de los campos de identificador y contraseña,
     * eliminando cualquier texto de validación previo antes de un nuevo intento.
     */
    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
    }

    /**
     * Almacena de forma cifrada el correo y la contraseña del usuario
     * en [EncryptedSharedPreferences] para permitir futuros inicios de sesión biométricos.
     *
     * @param email Correo electrónico del usuario a guardar.
     * @param password Contraseña del usuario a guardar.
     */
    private fun saveBiometricCredentials(email: String) {
        biometricPrefs.edit()
            .putString(KEY_BIOMETRIC_EMAIL, email)
            .remove(KEY_BIOMETRIC_PASSWORD)
            .apply()
    }

    /**
     * Verifica si existen credenciales biométricas previamente guardadas.
     *
     * @return `true` si hay un correo electrónico almacenado para biometría; `false` en caso contrario.
     */
    private fun hasBiometricCredentials() =
        biometricPrefs.getString(KEY_BIOMETRIC_EMAIL, null) != null

    /**
     * Recupera el correo electrónico almacenado en las preferencias biométricas cifradas.
     *
     * @return El correo electrónico guardado, o `null` si no existe.
     */
    private fun getBiometricEmail() =
        biometricPrefs.getString(KEY_BIOMETRIC_EMAIL, null)

    /**
     * Recupera la contraseña almacenada en las preferencias biométricas cifradas.
     *
     * @return La contraseña guardada, o `null` si no existe.
     */
/**
     * Navega hacia la [MainActivity] finalizando la actividad actual.
     * Pasa el indicador de si el usuario tiene rol de administrador.
     *
     * @param isAdmin `true` si el usuario autenticado tiene permisos de administrador.
     */
    private fun navigateToMain(isAdmin: Boolean) {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_IS_ADMIN, isAdmin)
        })
        finish()
    }

    /**
     * Navega a la pantalla correspondiente según el rol del usuario administrativo.
     *
     * Si el rol es "VENDEDOR", redirige a [VendedorActivity] con los datos del vendedor.
     * Para cualquier otro rol administrativo (ej. ADMIN, BODEGA), redirige a [AdminActivity].
     * En ambos casos finaliza la actividad actual para limpiar la pila de retroceso.
     *
     * @param usuario Información del usuario administrativo autenticado, incluyendo rol, nombre y cédula.
     */
    private fun navigateToAdmin(usuario: AdminUsuarioInfo) {
        if (usuario.rol.equals("VENDEDOR", ignoreCase = true)) {
            startActivity(Intent(this, VendedorActivity::class.java).apply {
                putExtra(VendedorActivity.EXTRA_ROL, usuario.rol)
                putExtra(VendedorActivity.EXTRA_NOMBRE, usuario.nombreCompleto)
                putExtra(VendedorActivity.EXTRA_CEDULA, usuario.cedula)
            })
        } else {
            startActivity(Intent(this, AdminActivity::class.java).apply {
                putExtra(AdminActivity.EXTRA_ROL, usuario.rol)
                putExtra(AdminActivity.EXTRA_NOMBRE, usuario.nombreCompleto)
            })
        }
        finish()
    }
}
