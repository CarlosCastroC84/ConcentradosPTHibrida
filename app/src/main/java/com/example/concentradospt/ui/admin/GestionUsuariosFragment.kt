package com.example.concentradospt.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.concentradospt.data.model.admin.AdminUsuarioInfo
import com.example.concentradospt.databinding.FragmentGestionUsuariosBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class GestionUsuariosFragment : Fragment() {

    private var _binding: FragmentGestionUsuariosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GestionUsuariosViewModel by viewModels()

    private val adapter = AdminUsuarioAdapter(
        onToggleEstado = { confirmToggle(it) },
        onResetPin = { confirmRegenerarPin(it) }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
