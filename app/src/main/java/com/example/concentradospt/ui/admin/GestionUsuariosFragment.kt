package com.example.concentradospt.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.concentradospt.data.model.admin.AdminUsuarioInfo
import com.example.concentradospt.databinding.FragmentGestionUsuariosBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.LinearLayout
import android.widget.Spinner
import kotlinx.coroutines.launch

class GestionUsuariosFragment : Fragment() {

    private var _binding: FragmentGestionUsuariosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GestionUsuariosViewModel by viewModels()

    private val adapter = AdminUsuarioAdapter(
        onToggleEstado = { confirmToggle(it) },
        onResetPin = { confirmRegenerarPin(it) },
        onEditar = { showEditarDialog(it) },
        onEliminar = { confirmEliminar(it) }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestionUsuariosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.guToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.guRv.layoutManager = LinearLayoutManager(requireContext())
        binding.guRv.adapter = adapter
        binding.guFabAdd.setOnClickListener { showCrearDialog() }

        observeState()
        observeActionResult()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is GestionUsuariosState.Loading -> {
                        binding.guProgress.visibility = View.VISIBLE
                        binding.guRv.visibility = View.GONE
                        binding.guTvEmpty.visibility = View.GONE
                    }
                    is GestionUsuariosState.Success -> {
                        binding.guProgress.visibility = View.GONE
                        if (state.usuarios.isEmpty()) {
                            binding.guRv.visibility = View.GONE
                            binding.guTvEmpty.visibility = View.VISIBLE
                            binding.guTvEmpty.text = "No hay usuarios registrados"
                        } else {
                            binding.guRv.visibility = View.VISIBLE
                            binding.guTvEmpty.visibility = View.GONE
                            adapter.submitList(state.usuarios)
                        }
                    }
                    is GestionUsuariosState.Error -> {
                        binding.guProgress.visibility = View.GONE
                        binding.guTvEmpty.visibility = View.VISIBLE
                        binding.guTvEmpty.text = state.message
                    }
                }
            }
        }
    }

    private fun observeActionResult() {
        lifecycleScope.launch {
            viewModel.actionResult.collect { msg ->
                msg?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    viewModel.clearActionResult()
                }
            }
        }
    }

    private fun confirmToggle(usuario: AdminUsuarioInfo) {
        val accion = if (usuario.isActive) "desactivar" else "activar"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar acción")
            .setMessage("¿Deseas $accion a ${usuario.nombreCompleto.ifBlank { usuario.cedula }}?")
            .setPositiveButton("Confirmar") { _, _ -> viewModel.toggleEstado(usuario) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmRegenerarPin(usuario: AdminUsuarioInfo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Regenerar PIN")
            .setMessage("Se enviará un correo a ${usuario.email ?: usuario.cedula} con el nuevo PIN.")
            .setPositiveButton("Enviar") { _, _ -> viewModel.regenerarPin(usuario) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showCrearDialog() {
        val ctx = requireContext()
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            val pad = resources.getDimensionPixelSize(com.example.concentradospt.R.dimen.spacing_md)
            setPadding(pad, pad, pad, 0)
        }
        val etCedula = buildTextInput(layout, "Cédula")
        val etNombre = buildTextInput(layout, "Nombre")
        val etApellido = buildTextInput(layout, "Apellido")
        val etEmail = buildTextInput(layout, "Correo electrónico")
        val etPin = buildTextInput(layout, "PIN (4 dígitos)")
        val spRol = buildRolSpinner(layout)

        MaterialAlertDialogBuilder(ctx)
            .setTitle("Crear Usuario")
            .setView(layout)
            .setPositiveButton("Crear") { _, _ ->
                val nuevo = AdminUsuarioInfo(
                    cedula = etCedula.text?.toString()?.trim() ?: "",
                    nombre = etNombre.text?.toString()?.trim() ?: "",
                    apellido = etApellido.text?.toString()?.trim() ?: "",
                    email = etEmail.text?.toString()?.trim()?.ifEmpty { null },
                    rol = spRol.selectedItem?.toString() ?: "VENDEDOR",
                    estado = "ACTIVO"
                )
                viewModel.crearUsuario(nuevo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditarDialog(usuario: AdminUsuarioInfo) {
        val ctx = requireContext()
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            val pad = resources.getDimensionPixelSize(com.example.concentradospt.R.dimen.spacing_md)
            setPadding(pad, pad, pad, 0)
        }
        val etNombre = buildTextInput(layout, "Nombre", usuario.nombre)
        val etApellido = buildTextInput(layout, "Apellido", usuario.apellido)
        val etEmail = buildTextInput(layout, "Correo electrónico", usuario.email ?: "")
        val spRol = buildRolSpinner(layout, usuario.rol)

        MaterialAlertDialogBuilder(ctx)
            .setTitle("Editar Usuario")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val actualizado = usuario.copy(
                    nombre = etNombre.text?.toString()?.trim() ?: usuario.nombre,
                    apellido = etApellido.text?.toString()?.trim() ?: usuario.apellido,
                    email = etEmail.text?.toString()?.trim()?.ifEmpty { null },
                    rol = spRol.selectedItem?.toString() ?: usuario.rol
                )
                viewModel.editarUsuario(actualizado)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmEliminar(usuario: AdminUsuarioInfo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar usuario")
            .setMessage("¿Eliminar a ${usuario.nombreCompleto.ifBlank { usuario.cedula }}? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> viewModel.eliminarUsuario(usuario) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun buildTextInput(parent: LinearLayout, hint: String, initial: String = ""): TextInputEditText {
        val ctx = parent.context
        val til = TextInputLayout(ctx, null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox).apply {
            this.hint = hint
            val margin = resources.getDimensionPixelSize(com.example.concentradospt.R.dimen.spacing_sm)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, margin) }
        }
        val et = TextInputEditText(til.context).apply {
            setText(initial)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        til.addView(et)
        parent.addView(til)
        return et
    }

    private fun buildRolSpinner(parent: LinearLayout, selected: String = "VENDEDOR"): Spinner {
        val ctx = parent.context
        val roles = listOf("ADMIN", "VENDEDOR", "BODEGA")
        val spinner = Spinner(ctx).apply {
            adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, roles)
            val margin = resources.getDimensionPixelSize(com.example.concentradospt.R.dimen.spacing_sm)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, margin, 0, margin) }
            setSelection(roles.indexOf(selected).coerceAtLeast(0))
        }
        parent.addView(spinner)
        return spinner
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
