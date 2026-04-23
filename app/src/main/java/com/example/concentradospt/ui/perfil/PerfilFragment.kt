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

class PerfilFragment : Fragment() {

    private var _binding: PerfilFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PerfilViewModel by viewModels()

    private val ordersAdapter = PedidosAdapter(onClick = { pedido ->
        findNavController().navigate(
            R.id.action_perfil_to_detalle_pedido,
            bundleOf("pedidoId" to pedido.pedidoId)
        )
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PerfilFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.perfilRvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.perfilRvOrders.adapter = ordersAdapter

        observeState()

        binding.perfilSeeAllOrders.setOnClickListener {
            findNavController().navigate(R.id.action_perfil_to_pedidos)
        }

        binding.perfilSettingEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.perfilBtnLogout.setOnClickListener {
            (requireActivity() as MainActivity).signOut()
        }
    }

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

    private fun showEditProfileDialog() {
        val cliente = viewModel.state.value.cliente

        val spacing = resources.getDimensionPixelSize(R.dimen.spacing_md)

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(spacing * 2, spacing, spacing * 2, 0)
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}