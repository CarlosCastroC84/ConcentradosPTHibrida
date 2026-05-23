package com.example.concentradospt.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.concentradospt.MainActivity
import com.example.concentradospt.R
import com.example.concentradospt.databinding.PerfilFragmentBinding
import com.example.concentradospt.ui.pedidos.PedidosAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/**
 * Fragmento que presenta el perfil del usuario autenticado.
 *
 * Muestra el nombre, correo electrónico y avatar del usuario,
 * junto con un resumen de sus pedidos recientes. Permite al usuario:
 * - Navegar al historial completo de pedidos.
 * - Acceder a sus productos favoritos.
 * - Editar sus datos de perfil (nombre, teléfono, dirección) mediante un diálogo.
 * - Cerrar sesión en la aplicación.
 *
 * Los datos se cargan y administran a través de [PerfilViewModel].
 */
class PerfilFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: PerfilFragmentBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel que gestiona los datos del perfil y los pedidos recientes del usuario. */
    private val viewModel: PerfilViewModel by viewModels()

    /**
     * Adaptador para la lista de pedidos recientes mostrada en el perfil.
     * Al pulsar un pedido, navega al [DetallePedidoFragment] con su ID.
     */
    private val ordersAdapter = PedidosAdapter(onClick = { pedido ->
        findNavController().navigate(
            R.id.action_perfil_to_detalle_pedido,
            bundleOf("pedidoId" to pedido.pedidoId)
        )
    })

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PerfilFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura el RecyclerView de pedidos recientes, los botones de navegación
     * y acciones del perfil, y comienza a observar el estado del ViewModel.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.perfilRvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.perfilRvOrders.adapter = ordersAdapter

        observeState()

        binding.perfilSeeAllOrders.setOnClickListener {
            findNavController().navigate(R.id.action_perfil_to_pedidos)
        }

        binding.perfilSettingFavoritos.setOnClickListener {
            findNavController().navigate(R.id.action_perfil_to_favoritos)
        }

        binding.perfilSettingEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.perfilBtnLogout.setOnClickListener {
            (requireActivity() as MainActivity).signOut()
        }
    }

    /**
     * Observa el estado del [PerfilViewModel] y actualiza la UI con los datos del usuario.
     *
     * Mientras carga, muestra el texto de carga en el nombre y vacía el correo.
     * En éxito, muestra el nombre y correo (con fallback al email de Cognito),
     * el avatar con Glide, la lista de pedidos recientes y cualquier mensaje de error/éxito.
     */
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if (state.isLoading) {
                    binding.perfilName.text = getString(R.string.loading)
                    binding.perfilEmail.text = ""
                    return@collect
                }

                val cliente = state.cliente
                val displayName = cliente?.nombre?.takeIf { it.isNotBlank() }
                    ?: state.emailFallback?.substringBefore("@")
                    ?: "Usuario"
                val displayEmail = cliente?.email?.takeIf { it.isNotBlank() }
                    ?: state.emailFallback
                    ?: ""

                binding.perfilName.text = displayName
                binding.perfilEmail.text = displayEmail

                Glide.with(this@PerfilFragment)
                    .load(null as String?)
                    .placeholder(R.mipmap.ic_launcher)
                    .circleCrop()
                    .into(binding.perfilAvatar)

                ordersAdapter.submitList(state.recentOrders)
                binding.perfilRvOrders.visibility =
                    if (state.recentOrders.isEmpty()) View.GONE else View.VISIBLE

                if (state.saveSuccess) {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.profile_edit_success),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    viewModel.consumeSaveSuccess()
                }

                state.error?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    viewModel.consumeError()
                }
            }
        }
    }

    /**
     * Muestra el diálogo de edición de perfil con campos para nombre, teléfono y dirección.
     *
     * Los campos se pre-rellenan con los datos actuales del cliente.
     * Al confirmar, delega la actualización a [PerfilViewModel.actualizarPerfil].
     */
    private fun showEditProfileDialog() {
        val cliente = viewModel.state.value.cliente

        val spacing = resources.getDimensionPixelSize(R.dimen.spacing_md)

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(spacing * 2, spacing, spacing * 2, 0)
        }

        /**
         * Función local que crea un campo de texto con estilo outlined y lo agrega al contenedor.
         *
         * @param hint        Texto de sugerencia del campo.
         * @param initialText Valor inicial a mostrar en el campo.
         * @return [TextInputEditText] del campo creado.
         */
        fun makeField(hint: String, initialText: String?): TextInputEditText {
            val layout = TextInputLayout(requireContext(), null,
                com.google.android.material.R.attr.textInputOutlinedStyle).apply {
                this.hint = hint
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = spacing }
            }
            val edit = TextInputEditText(layout.context).apply {
                setText(initialText.orEmpty())
            }
            layout.addView(edit)
            container.addView(layout)
            return edit
        }

        val etNombre = makeField(getString(R.string.profile_edit_hint_name), cliente?.nombre)
        val etTelefono = makeField(getString(R.string.profile_edit_hint_phone), cliente?.telefono)
        val etDireccion = makeField(getString(R.string.profile_edit_hint_address), cliente?.direccion)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_edit_dialog_title)
            .setView(container)
            .setPositiveButton(R.string.action_save) { _, _ ->
                viewModel.actualizarPerfil(
                    nombre = etNombre.text.toString(),
                    telefono = etTelefono.text.toString(),
                    direccion = etDireccion.text.toString()
                )
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    /**
     * Libera el binding al destruir la vista para prevenir pérdidas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
