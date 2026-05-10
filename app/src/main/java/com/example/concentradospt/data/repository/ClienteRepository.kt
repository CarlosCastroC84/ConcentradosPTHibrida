package com.example.concentradospt.data.repository

import android.util.Log
import com.example.concentradospt.data.model.ActualizarClienteRequest
import com.example.concentradospt.data.model.Cliente
import com.example.concentradospt.data.network.ApiService
import com.example.concentradospt.data.network.RetrofitClient
import retrofit2.HttpException

class ClienteRepository {
    private val api = RetrofitClient.create<ApiService>()

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

    suspend fun actualizarPerfil(nombre: String, telefono: String, direccion: String): Cliente =
        api.actualizarPerfil(ActualizarClienteRequest(nombre, telefono, direccion))
}
