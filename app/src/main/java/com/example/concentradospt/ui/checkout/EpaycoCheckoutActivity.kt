package com.example.concentradospt.ui.checkout

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.concentradospt.R

/**
 * Actividad que presenta el formulario de pago de ePayco dentro de un [WebView].
 *
 * Construye dinámicamente un HTML con el script de checkout de ePayco, cargando
 * la llave pública de la pasarela de pago desde los recursos de la aplicación.
 * Muestra el resumen del pedido (ID y monto total) antes del formulario de pago.
 *
 * Intercepta el esquema de URL personalizado `epayco://` para detectar la respuesta
 * de la transacción: si el estado es "Aceptada", devuelve [Activity.RESULT_OK] con
 * la referencia de ePayco; de lo contrario, devuelve [Activity.RESULT_CANCELED].
 *
 * Permite la navegación interna dentro de los dominios de ePayco sin salir del WebView.
 */
class EpaycoCheckoutActivity : AppCompatActivity() {

    companion object {
        /** Clave del extra que recibe el identificador del pedido desde [PagoFragment]. */
        const val EXTRA_PEDIDO_ID = "pedidoId"

        /** Clave del extra que recibe el monto total del pedido desde [PagoFragment]. */
        const val EXTRA_TOTAL = "total"

        /** Clave del resultado que devuelve la referencia de la transacción ePayco al fragmento llamante. */
        const val RESULT_REF_PAYCO = "refPayco"

        /** Clave del resultado que indica si la transacción fue aprobada. */
        const val RESULT_APROBADO = "aprobado"

        /** Esquema de URL personalizado usado por ePayco para devolver la respuesta de la transacción. */
        private const val RESPONSE_SCHEME = "epayco"
    }

    /**
     * Inicializa la actividad, construye la interfaz con un [ProgressBar] y un [WebView]
     * de forma programática, configura el [WebViewClient] para interceptar las respuestas
     * de ePayco, y carga el HTML del checkout con los datos del pedido.
     *
     * Habilita JavaScript y almacenamiento DOM en el WebView, necesarios para el
     * funcionamiento del widget de pago de ePayco.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, o null si es nueva.
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pedidoId = intent.getStringExtra(EXTRA_PEDIDO_ID) ?: ""
        val total = intent.getDoubleExtra(EXTRA_TOTAL, 0.0)

        val progress = ProgressBar(this).apply {
            visibility = View.VISIBLE
        }
        val webView = WebView(this)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            addView(
                progress,
                android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
            addView(
                webView,
                android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
        setContentView(layout)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        webView.webViewClient = object : WebViewClient() {
            /** Oculta la barra de progreso cuando la página termina de cargar. */
            override fun onPageFinished(view: WebView, url: String) {
                progress.visibility = View.GONE
            }

            /**
             * Intercepta cada solicitud de navegación dentro del WebView.
             *
             * Si la URL usa el esquema `epayco://`, procesa la respuesta de la transacción
             * llamando a [handleEpaycoResponse]. Para URLs de los dominios de ePayco,
             * permite la navegación interna dentro del WebView sin interferir.
             *
             * @param view WebView que generó la solicitud.
             * @param request Objeto con la URL y metadatos de la solicitud de navegación.
             * @return `true` si la actividad maneja la URL (esquema epayco); `false` para
             *         permitir que el WebView la cargue normalmente.
             */
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (request.url.scheme == RESPONSE_SCHEME) {
                    handleEpaycoResponse(request.url)
                    return true
                }
                // Deja que ePayco navegue dentro del WebView (iframes, redirects internos)
                if (url.contains("epayco.co") || url.contains("payco.co") || url.contains("secure.payco")) {
                    return false
                }
                return false
            }
        }

        val publicKey = getString(R.string.epayco_public_key)
        webView.loadDataWithBaseURL(
            "https://checkout.epayco.co",
            buildCheckoutHtml(publicKey, total, pedidoId),
            "text/html",
            "UTF-8",
            null
        )
    }

    /**
     * Procesa la URI de respuesta de ePayco para determinar si la transacción fue aprobada.
     *
     * Extrae el estado de la transacción (`x_transaction_state`) y la referencia de pago
     * (`x_ref_payco`) de los parámetros de la URI. Si el estado es "Aceptada" (insensible
     * a mayúsculas), devuelve [Activity.RESULT_OK]; de lo contrario, [Activity.RESULT_CANCELED].
     * En ambos casos finaliza la actividad para retornar el control a [PagoFragment].
     *
     * @param uri URI con el esquema `epayco://` que contiene los parámetros de la respuesta.
     */
    private fun handleEpaycoResponse(uri: Uri) {
        val estado = uri.getQueryParameter("x_transaction_state") ?: ""
        val refPayco = uri.getQueryParameter("x_ref_payco") ?: ""
        val aprobado = estado.equals("Aceptada", ignoreCase = true)

        val result = Intent().apply {
            putExtra(RESULT_REF_PAYCO, refPayco)
            putExtra(RESULT_APROBADO, aprobado)
        }
        setResult(if (aprobado) Activity.RESULT_OK else Activity.RESULT_CANCELED, result)
        finish()
    }

    /**
     * Construye el HTML completo del checkout de ePayco con los datos del pedido.
     *
     * El HTML incluye una tarjeta de resumen con el ID y el monto del pedido,
     * y el script oficial de ePayco configurado con la llave pública, el monto,
     * descripción, moneda y las URLs de respuesta y confirmación usando el esquema
     * personalizado `epayco://`. El modo de prueba está activo (`data-epayco-test="true"`).
     *
     * @param key Llave pública de ePayco obtenida desde los recursos de la aplicación.
     * @param amount Monto total del pedido en pesos colombianos.
     * @param pedidoId Identificador completo del pedido; se usa solo los últimos 8 caracteres.
     * @return Cadena de texto con el HTML completo listo para cargar en el [WebView].
     */
    private fun buildCheckoutHtml(key: String, amount: Double, pedidoId: String): String {
        val amountInt = amount.toInt()
        val pedidoCorto = pedidoId.takeLast(8).uppercase()
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        margin: 0;
                        padding: 24px;
                        font-family: sans-serif;
                        background: #f5f5f5;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                    }
                    .card {
                        background: white;
                        border-radius: 12px;
                        padding: 24px;
                        width: 100%;
                        max-width: 480px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                        text-align: center;
                        margin-bottom: 24px;
                    }
                    .label { color: #888; font-size: 14px; margin-bottom: 4px; }
                    .pedido { font-size: 18px; font-weight: bold; color: #333; }
                    .total { font-size: 28px; font-weight: bold; color: #2E7D32; margin: 8px 0; }
                    .sub { font-size: 12px; color: #aaa; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="label">Pedido</div>
                    <div class="pedido">#$pedidoCorto</div>
                    <div class="total">$$amountInt COP</div>
                    <div class="sub">Modo prueba — ePayco Checkout</div>
                </div>
                <script
                    src="https://checkout.epayco.co/payment.minified.js"
                    data-epayco-key="$key"
                    data-epayco-amount="$amountInt"
                    data-epayco-name="Concentrados Puente Tierra"
                    data-epayco-description="Pedido #$pedidoCorto"
                    data-epayco-currency="cop"
                    data-epayco-country="co"
                    data-epayco-test="true"
                    data-epayco-response="epayco://response"
                    data-epayco-confirmation="epayco://confirmation"
                    data-epayco-external="false">
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}
