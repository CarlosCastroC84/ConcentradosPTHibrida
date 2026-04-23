package com.example.concentradospt.ui.admin

import android.content.Context
import android.widget.LinearLayout
import com.example.concentradospt.data.model.admin.AdminProducto
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ProductoFormView(context: Context, producto: AdminProducto?) : LinearLayout(context) {

    private val etCodigo: TextInputEditText
    private val etDescripcion: TextInputEditText
    private val etPrecioVenta: TextInputEditText
    private val etStockActual: TextInputEditText
    private val etStockMinimo: TextInputEditText
    private val etCategoria: TextInputEditText

    val codigo get() = etCodigo.text?.toString()?.trim() ?: ""
    val descripcion get() = etDescripcion.text?.toString()?.trim() ?: ""
    val precioVenta get() = etPrecioVenta.text?.toString()?.toDoubleOrNull() ?: 0.0
    val stockActual get() = etStockActual.text?.toString()?.toDoubleOrNull() ?: 0.0
    val stockMinimo get() = etStockMinimo.text?.toString()?.toDoubleOrNull() ?: 0.0
    val categoria get() = etCategoria.text?.toString()?.trim() ?: ""

    init {
        orientation = VERTICAL
        val padding = 48
        setPadding(padding, padding / 2, padding, 0)

        etCodigo = addField("Código", producto?.codigo, android.text.InputType.TYPE_CLASS_TEXT)
        etDescripcion = addField("Descripción / Nombre", producto?.descripcion)
        etPrecioVenta = addField("Precio de venta (COP)", producto?.precioVenta?.toString(),
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        etStockActual = addField("Stock actual", producto?.stockActual?.toString(),
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        etStockMinimo = addField("Stock mínimo", producto?.stockMinimo?.toString(),
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        etCategoria = addField("Categoría", producto?.categoria)
    }

    private fun addField(hint: String, value: String?, inputType: Int = android.text.InputType.TYPE_CLASS_TEXT): TextInputEditText {
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
