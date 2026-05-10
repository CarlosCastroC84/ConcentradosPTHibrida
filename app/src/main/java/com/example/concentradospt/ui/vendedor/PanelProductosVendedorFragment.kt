package com.example.concentradospt.ui.vendedor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.concentradospt.data.model.admin.AdminProducto
import com.example.concentradospt.databinding.FragmentPanelProductosVendedorBinding
import com.example.concentradospt.ui.admin.AdminProductoAdapter
import com.example.concentradospt.ui.admin.GestionProductosState
import com.example.concentradospt.ui.admin.GestionProductosViewModel
import com.example.concentradospt.ui.admin.ProductoFormView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PanelProductosVendedorFragment : Fragment() {

    private var _binding: FragmentPanelProductosVendedorBinding? = null
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
        _binding = FragmentPanelProductosVendedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vpRv.layoutManager = LinearLayoutManager(requireContext())
        binding.vpRv.adapter = adapter
        binding.vpFabAdd.setOnClickListener { showProductoDialog(null) }

        observeState()
        observeActionResult()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is GestionProductosState.Loading -> {
                        binding.vpProgress.visibility = View.VISIBLE
                        binding.vpRv.visibility = View.GONE
                        binding.vpTvEmpty.visibility = View.GONE
                    }
                    is GestionProductosState.Success -> {
                        binding.vpProgress.visibility = View.GONE
                        if (state.productos.isEmpty()) {
                            binding.vpRv.visibility = View.GONE
                            binding.vpTvEmpty.visibility = View.VISIBLE
                            binding.vpTvEmpty.text = "No hay productos registrados"
                        } else {
                            binding.vpRv.visibility = View.VISIBLE
                            binding.vpTvEmpty.visibility = View.GONE
                            adapter.submitList(state.productos)
                        }
                    }
                    is GestionProductosState.Error -> {
                        binding.vpProgress.visibility = View.GONE
                        binding.vpTvEmpty.visibility = View.VISIBLE
                        binding.vpTvEmpty.text = state.message
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
                val updated = AdminProducto(
                    id = producto?.id ?: 0,
                    codigo = fields.codigo,
                    descripcion = fields.descripcion,
                    precioVenta = fields.precioVenta,
                    stockActual = fields.stockActual,
                    stockMinimo = fields.stockMinimo,
                    categoria = fields.categoria.ifEmpty { null },
                    activo = true
                )
                if (isNew) viewModel.crearProducto(updated)
                else viewModel.actualizarProducto(updated)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(producto: AdminProducto) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Eliminar \"${producto.descripcion}\"? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarProducto(producto.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
