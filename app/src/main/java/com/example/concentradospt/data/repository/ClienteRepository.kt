package com.example.concentradospt.data.repository

import android.util.Log
import com.example.concentradospt.data.model.ActualizarClienteRequest
import com.example.concentradospt.data.model.Cliente
import com.example.concentradospt.data.network.ApiService
import com.example.concentradospt.data.network.RetrofitClient
import retrofit2.HttpException

/**
 * Repositorio que gestiona las operaciones sobre el perfil del cliente autenticado,
 * actuando como intermediario entre la capa de presentación y [ApiService].
 *
 * Encapsula el manejo de errores HTTP y generales alrededor de las llamadas de red,
 * registrando trazas de depuración mediante [Log] antes de relanzar la excepción para
 * que la capa superior (ViewModel) pueda manejarla apropiadamente.
 */
class ClienteRepository {

    /** Implementación de [ApiService] generada por Retrofit para realizar las peticiones HTTP. */
    private val api = RetrofitClient.create<ApiService>()

    /**
     * Obtiene el perfil del cliente actualmente autenticado desde el backend.
     *
     * Registra en el logcat el código de error HTTP y el cuerpo de respuesta en caso de
     * fallo HTTP, o la excepción completa para cualquier otro tipo de error.
     *
     * @return El objeto [Cliente] con los datos del perfil del usuario autenticado.
     * @throws [HttpException] si el servidor responde con un código de error HTTP
     *         (p. ej. 401 no autorizado, 404 no encontrado).
     * @throws [Exception] si ocurre cualquier otro error de red o de parseo.
     */
    suspend fun getMiPerfil(): Cliente {
        return try {
            api.getMiPerfil()
        } catch (e: HttpException) {
            Log.e("ClienteRepository", "getMiPerfil HTTP ${e.code()}: ${e.response()?.errorBody()?.string()}")
            throw e
        } catch (e: Exception) {
            Log.e("ClienteRepository", "getMiPerfil failed", e)
            throw e
        }
    }

    /**
     * Actualiza los datos del perfil del cliente autenticado con el nombre, teléfono y dirección
     * proporcionados.
     *
     * Construye internamente un [ActualizarClienteRequest] con los parámetros recibidos y
     * delega la petición a [ApiService.actualizarPerfil].
     *
     * @param nombre Nombre completo actualizado del cliente.
     * @param telefono Número de teléfono actualizado del cliente.
     * @param direccion Dirección de entrega actualizada del cliente.
     * @return El objeto [Cliente] con los datos ya actualizados confirmados por el backend.
     * @throws [Exception] si ocurre un error de red o el servidor responde con error.
     */
    suspend fun actualizarPerfil(nombre: String, telefono: String, direccion: String): Cliente =
        api.actualizarPerfil(ActualizarClienteRequest(nombre, telefono, direccion))
}
