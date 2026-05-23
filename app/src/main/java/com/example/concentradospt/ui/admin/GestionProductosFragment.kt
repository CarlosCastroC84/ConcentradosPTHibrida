package com.example.concentradospt.ui.admin

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
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
import java.io.File

/**
 * Fragmento de administración para la gestión completa de productos del catálogo.
 *
 * Muestra la lista de productos registrados y permite al administrador:
 * - Crear nuevos productos mediante un formulario en diálogo ([ProductoFormView]).
 * - Editar productos existentes con sus datos precargados.
 * - Eliminar productos con confirmación previa.
 * - Seleccionar imagen desde la galería o tomarla con la cámara.
 *
 * Las operaciones se delegan a [GestionProductosViewModel] y los resultados
 * se notifican al usuario mediante Snackbar.
 */
class GestionProductosFragment : Fragment() {

    /** Referencia al binding de la vista; se anula en [onDestroyView] para evitar fugas de memoria. */
    private var _binding: FragmentGestionProductosBinding? = null

    /** Acceso seguro al binding mientras la vista está activa. */
    private val binding get() = _binding!!

    /** ViewModel que gestiona las operaciones CRUD sobre productos administrativos. */
    private val viewModel: GestionProductosViewModel by viewModels()

    /** Referencia al formulario actualmente abierto, necesaria para asignar la imagen seleccionada. */
    private var currentFormView: ProductoFormView? = null

    /** URI temporal donde se almacena la foto tomada con la cámara antes de asignarla al formulario. */
    private var cameraImageUri: Uri? = null

    /**
     * Launcher para seleccionar una imagen desde la galería del dispositivo.
     * Cuando el usuario elige una imagen, se asigna su URI al formulario actual.
     */
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { currentFormView?.setImageUri(it) }
    }

    /**
     * Launcher para tomar una foto con la cámara del dispositivo.
     * Si la captura fue exitosa, asigna [cameraImageUri] al formulario actual.
     */
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { currentFormView?.setImageUri(it) }
        }
    }

    /**
     * Adaptador de la lista de productos administrativos.
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
        _binding = FragmentGestionProductosBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura la barra de herramientas, el RecyclerView, el botón FAB
     * y comienza a observar el estado del ViewModel y los resultados de acciones.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gpToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.gpRv.layoutManager = LinearLayoutManager(requireContext())
        binding.gpRv.adapter = adapter
        binding.gpFabAdd.setOnClickListener { showProductoDialog(null) }

        observeState()
        observeActionResult()
    }

    /**
     * Observa el [GestionProductosState] y actualiza la UI según el estado actual.
     *
     * - Loading: muestra el indicador de progreso.
     * - Success: muestra la lista o un mensaje de vacío si no hay productos.
     * - Error: muestra el mensaje de error en lugar de la lista.
     */
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
     * El botón de selección de imagen en el formulario abre [mostrarSelectorImagen].
     *
     * @param producto Producto a editar, o null para crear uno nuevo.
     */
    private fun showProductoDialog(producto: AdminProducto?) {
        val isNew = producto == null
        val fields = ProductoFormView(requireContext(), producto)
        currentFormView = fields

        fields.btnSeleccionarImagen.isClickable = true
        fields.btnSeleccionarImagen.setOnClickListener { mostrarSelectorImagen() }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (isNew) "Nuevo Producto" else "Editar Producto")
            .setView(fields)
            .setPositiveButton(if (isNew) "Crear" else "Guardar") { _, _ ->
                val updated = buildProductoFromForm(fields, producto)
                if (isNew) viewModel.crearProducto(updated)
                else viewModel.actualizarProducto(updated)
            }
            .setNegativeButton("Cancelar", null)
            .setOnDismissListener { currentFormView = null }
            .show()
    }

    /**
     * Muestra un diálogo de selección para elegir entre galería o cámara
     * como fuente de la imagen del producto.
     */
    private fun mostrarSelectorImagen() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Seleccionar imagen")
            .setItems(arrayOf("Desde galería", "Tomar foto")) { _, which ->
                when (which) {
                    0 -> galleryLauncher.launch("image/*")
                    1 -> abrirCamara()
                }
            }
            .show()
    }

    /**
     * Crea un archivo temporal en la caché externa y lanza la cámara apuntando a él.
     *
     * Usa [FileProvider] para generar una URI segura compatible con Android 7+.
     * La URI se guarda en [cameraImageUri] para recuperarla tras la captura.
     */
    private fun abrirCamara() {
        val tmpFile = File(requireContext().externalCacheDir, "producto_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.concentradospt.fileprovider",
            tmpFile
        )
        cameraImageUri = uri
        cameraLauncher.launch(uri)
    }

    /**
     * Construye un [AdminProducto] a partir de los datos ingresados en el formulario.
     *
     * Si se seleccionó una nueva imagen, usa su URI como cadena; de lo contrario
     * conserva la URL firmada original del producto existente.
     *
     * @param form     Vista del formulario con los datos ingresados por el usuario.
     * @param original Producto original en modo edición, o null en modo creación.
     * @return Nuevo objeto [AdminProducto] con los datos del formulario.
     */
    private fun buildProductoFromForm(form: ProductoFormView, original: AdminProducto?): AdminProducto {
        return AdminProducto(
            id = original?.id ?: 0,
            codigo = form.codigo,
            descripcion = form.descripcion,
            precioVenta = form.precioVenta,
            stockActual = form.stockActual,
            stockMinimo = form.stockMinimo,
            categoria = form.categoria.ifEmpty { null },
            imagenProductoUrlFirmada = form.imagenUri?.toString() ?: original?.imagenProductoUrlFirmada,
            activo = true
        )
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
     * Libera el formulario activo y el binding al destruir la vista
     * para prevenir pérdidas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        currentFormView = null
        _binding = null
    }
}
