package com.example.concentradospt.data.repository

import com.example.concentradospt.data.model.ActualizarClienteRequest
import com.example.concentradospt.data.model.Cliente
import com.example.concentradospt.data.network.ApiService
import com.example.concentradospt.data.network.RetrofitClient

class ClienteRepository {
    private val api = RetrofitClient.create<ApiService>()

    suspend fun getMiPerfil(): Cliente = api.getMiPerfil()

    suspend fun actualizarPerfil(nombre: String, telefono: String, direccion: String): Cliente =
        api.actualizarPerfil(ActualizarClienteRequest(nombre, telefono, direccion))
}
