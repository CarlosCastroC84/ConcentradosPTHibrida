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
import com.example.concentradospt.data.model.admin.AdminProducto
import com.example.concentradospt.databinding.FragmentGestionProductosBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class GestionProductosFragment : Fragment() {

    private var _binding: FragmentGestionProductosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GestionProductosViewModel by viewModels()

    private val adapter = AdminProductoAdapter(
        onEdit = { showProductoDialog(it) },
        onDelete = { confirmDelete(it) }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestionProductosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gpToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.gpRv.layoutManager = LinearLayoutManager(requireContext())
        binding.gpRv.adapter = adapter
        binding.gpFabAdd.setOnClickListener { showProductoDialog(null) }

        observeState()
        observeActionResult()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is GestionProductosState.Loading -> {
                        binding.gpProgress.visibility = View.VISIBLE
                        binding.gpRv.visibility = View.GONE
                        binding.gpTvEmpty.visibility = View.GONE
                    }
                    is GestionProductosState.Success -> {
                        binding.gpProgress.visibility = View.GONE
                        if (state.productos.isEmpty()) {
                            binding.gpRv.visibility = View.GONE
                            binding.gpTvEmpty.visibility = View.VISIBLE
                            binding.gpTvEmpty.text = "No hay productos registrados"
                        } else {
                            binding.gpRv.visibility = View.VISIBLE
                            binding.gpTvEmpty.visibility = View.GONE
                            adapter.submitList(state.productos)
                        }
                    }
                    is GestionProductosState.Error -> {
                        binding.gpProgress.visibility = View.GONE
                        binding.gpTvEmpty.visibility = View.VISIBLE
                        binding.gpTvEmpty.text = state.message
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

    private fun showProductoDialog(producto: AdminProducto?) {
        val isNew = producto == null
        val fields = ProductoFormView(requireContext(), producto)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (isNew) "Nuevo Producto" else "Editar Producto")
            .setView(fields)
            .setPositiveButton(if (isNew) "Crear" else "Guardar") { _, _ ->
                val updated = buildProductoFromForm(fields, producto)
                if (isNew) viewModel.crearProducto(updated)
                else viewModel.actualizarProducto(updated)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun buildProductoFromForm(form: ProductoFormView, original: AdminProducto?): AdminProducto {
        return AdminProducto(
            id = original?.id ?: 0,
            codigo = form.codigo,
            descripcion = form.descripcion,
            precioVenta = form.precioVenta,
            stockActual = form.stockActual,
            stockMinimo = form.stockMinimo,
            categoria = form.categoria.ifEmpty { null },
            activo = true
        )
    }

    private fun confirmDelete(producto: AdminProducto) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Eliminar \"${producto.descripcion}\"? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                Snackbar.make(binding.root, "Eliminación no disponible en esta versión", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
