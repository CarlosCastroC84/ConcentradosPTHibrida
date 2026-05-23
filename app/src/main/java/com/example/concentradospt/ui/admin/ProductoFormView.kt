package com.example.concentradospt.ui.admin

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.example.concentradospt.data.model.admin.AdminProducto
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Vista personalizada que representa el formulario de creación y edición de productos.
 *
 * Extiende [LinearLayout] y construye programáticamente todos los campos necesarios:
 * imagen de vista previa, botón de selección de imagen, código, descripción,
 * precio de venta, stock actual, stock mínimo y categoría.
 *
 * Es utilizada como contenido del diálogo en [GestionProductosFragment] y
 * [PanelProductosVendedorFragment]. El fragmento padre es responsable de
 * asignar el listener al botón de imagen mediante [btnSeleccionarImagen].
 *
 * @param context  Contexto de la aplicación necesario para construir las vistas.
 * @param producto Producto existente para prelllenar los campos en modo edición,
 *                 o null para inicializar el formulario vacío en modo creación.
 */
class ProductoFormView(context: Context, producto: AdminProducto?) : LinearLayout(context) {

    /** Campo de texto para el código interno del producto. */
    private val etCodigo: TextInputEditText

    /** Campo de texto para la descripción o nombre del producto. */
    private val etDescripcion: TextInputEditText

    /** Campo de texto numérico para el precio de venta en COP. */
    private val etPrecioVenta: TextInputEditText

    /** Campo de texto numérico para la cantidad actual en inventario. */
    private val etStockActual: TextInputEditText

    /** Campo de texto numérico para el stock mínimo de alerta de inventario. */
    private val etStockMinimo: TextInputEditText

    /** Campo de texto para la categoría a la que pertenece el producto. */
    private val etCategoria: TextInputEditText

    /** ImageView que muestra la vista previa de la imagen del producto seleccionada. */
    val ivImagenPreview: ImageView

    /**
     * Botón que abre el selector de imagen (galería o cámara).
     * El listener debe ser asignado por el fragmento padre después de instanciar esta vista.
     */
    val btnSeleccionarImagen: MaterialButton

    /**
     * URI de la imagen seleccionada por el usuario desde galería o cámara.
     * Es null si no se ha seleccionado ninguna imagen nueva.
     */
    var imagenUri: Uri? = null
        private set

    /** Valor actual del campo código, con espacios eliminados. */
    val codigo get() = etCodigo.text?.toString()?.trim() ?: ""

    /** Valor actual del campo descripción, con espacios eliminados. */
    val descripcion get() = etDescripcion.text?.toString()?.trim() ?: ""

    /** Valor actual del campo precio de venta como Double, o 0.0 si no es válido. */
    val precioVenta get() = etPrecioVenta.text?.toString()?.toDoubleOrNull() ?: 0.0

    /** Valor actual del campo stock actual como Double, o 0.0 si no es válido. */
    val stockActual get() = etStockActual.text?.toString()?.toDoubleOrNull() ?: 0.0

    /** Valor actual del campo stock mínimo como Double, o 0.0 si no es válido. */
    val stockMinimo get() = etStockMinimo.text?.toString()?.toDoubleOrNull() ?: 0.0

    /** Valor actual del campo categoría, con espacios eliminados. */
    val categoria get() = etCategoria.text?.toString()?.trim() ?: ""

    init {
        orientation = VERTICAL
        val padding = 48
        setPadding(padding, padding / 2, padding, 0)

        // Vista previa de la imagen del producto; carga la URL firmada si existe
        ivImagenPreview = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 300).apply {
                bottomMargin = 16
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(0xFFEEEEEE.toInt())
            if (!producto?.imagenProductoUrlFirmada.isNullOrEmpty()) {
                Glide.with(context).load(producto!!.imagenProductoUrlFirmada).into(this)
            }
        }
        addView(ivImagenPreview)

        // Botón de selección de imagen; el fragmento padre asigna el listener
        btnSeleccionarImagen = MaterialButton(context).apply {
            text = "Seleccionar imagen (cámara / galería)"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 32
            }
            isClickable = false // el fragmento pone el listener
        }
        addView(btnSeleccionarImagen)

        // Construcción de los campos del formulario con sus valores iniciales
        etCodigo = addField("Código", producto?.codigo, android.text.InputType.TYPE_CLASS_TEXT)
        etDescripcion = addField("Descripción / Nombre", producto?.descripcion)
        etPrecioVenta = addField(
            "Precio de venta (COP)", producto?.precioVenta?.toString(),
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        )
        etStockActual = addField(
            "Stock actual", producto?.stockActual?.toString(),
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        )
        etStockMinimo = addField(
            "Stock mínimo", producto?.stockMinimo?.toString(),
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        )
        etCategoria = addField("Categoría", producto?.categoria)
    }

    /**
     * Asigna una nueva URI de imagen al formulario y actualiza la vista previa.
     *
     * Llamado por el fragmento padre tras seleccionar o capturar una imagen.
     *
     * @param uri URI de la imagen seleccionada desde galería o cámara.
     */
    fun setImageUri(uri: Uri) {
        imagenUri = uri
        Glide.with(context).load(uri).centerCrop().into(ivImagenPreview)
    }

    /**
     * Crea un [TextInputLayout] con un [TextInputEditText] y los agrega al formulario.
     *
     * @param hint      Texto de sugerencia que describe el campo.
     * @param value     Valor inicial del campo (null muestra el campo vacío).
     * @param inputType Tipo de teclado a mostrar (texto por defecto).
     * @return El [TextInputEditText] creado para leer su valor posteriormente.
     */
    private fun addField(
        hint: String,
        value: String?,
        inputType: Int = android.text.InputType.TYPE_CLASS_TEXT
    ): TextInputEditText {
        val til = TextInputLayout(context).apply {
            this.hint = hint
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 32
            }
        }
        val et = TextInputEditText(context).apply {
            this.inputType = inputType
            setText(value ?: "")
        }
        til.addView(et)
        addView(til)
        return et
    }
}
