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

/**
 * Fragmento del panel del vendedor para gestionar el catálogo de productos.
 *
 * Reutiliza [GestionProductosViewModel] y [AdminProductoAdapter] del módulo admin
 * para ofrecer al vendedor las mismas capacidades de gestión:
 * - Ver la lista de productos registrados.
 * - Crear nuevos productos mediante [ProductoFormView] en un diálogo.
 * - Editar productos existentes con sus datos precargados.
 * - Eliminar productos con confirmación previa.
 *
 * A diferencia de [GestionProductosFragment], este fragmento no incluye la
 * selección de imagen desde cámara/galería en el flujo actual.
 */
class PanelProductosVendedorFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: FragmentPanelProductosVendedorBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel compartido del módulo admin que gestiona las operaciones CRUD de productos. */
    private val viewModel: GestionProductosViewModel by viewModels()

    /**
     * Adaptador reutilizado del módulo admin para la lista de productos.
     * Expone acciones de editar y eliminar por cada ítem.
     */
    private val adapter = AdminProductoAdapter(
        onEdit = { showProductoDialog(it) },
        onDelete = { confirmDelete(it) }
    )

    /**
     * Infla el layout del fragmento y lo enlaza con ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPanelProductosVendedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura el RecyclerView, el botón FAB para crear productos
     * y comienza a observar el estado del ViewModel y los resultados de acciones.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vpRv.layoutManager = LinearLayoutManager(requireContext())
        binding.vpRv.adapter = adapter
        binding.vpFabAdd.setOnClickListener { showProductoDialog(null) }

        observeState()
        observeActionResult()
    }

    /**
     * Observa el [GestionProductosState] y actualiza la UI según el estado actual.
     *
     * - Loading: muestra el indicador de progreso.
     * - Success: muestra la lista o un mensaje de vacío si no hay productos.
     * - Error: muestra el mensaje de error.
     */
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

    /**
     * Observa los mensajes de resultado de acciones (crear, editar, eliminar)
     * y los muestra en un Snackbar, limpiando el resultado tras mostrarlo.
     */
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

    /**
     * Muestra el diálogo con el formulario [ProductoFormView] para crear o editar un producto.
     *
     * Si [producto] es null, el diálogo está en modo creación; si no, en modo edición.
     * Al confirmar, construye el [AdminProducto] con los datos del formulario y lo
     * envía al ViewModel para persistirlo.
     *
     * @param producto Producto a editar, o null para crear uno nuevo.
     */
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

    /**
     * Muestra un diálogo de confirmación antes de eliminar el producto indicado.
     *
     * Al confirmar, delega la eliminación a [GestionProductosViewModel.eliminarProducto].
     *
     * @param producto Producto que se desea eliminar.
     */
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

    /**
     * Libera el binding al destruir la vista para prevenir pérdidas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
