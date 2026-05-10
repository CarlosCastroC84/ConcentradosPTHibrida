package com.example.concentradospt.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.concentradospt.data.model.admin.AdminUsuarioInfo
import com.example.concentradospt.databinding.ItemAdminUsuarioBinding

class AdminUsuarioAdapter(
    private val onToggleEstado: (AdminUsuarioInfo) -> Unit,
    private val onResetPin: (AdminUsuarioInfo) -> Unit,
    private val onEditar: (AdminUsuarioInfo) -> Unit = {},
    private val onEliminar: (AdminUsuarioInfo) -> Unit = {}
) : ListAdapter<AdminUsuarioInfo, AdminUsuarioAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemAdminUsuarioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(u: AdminUsuarioInfo) {
            binding.adminUserName.text = u.nombreCompleto.ifBlank { u.cedula }
            binding.adminUserEmail.text = u.email ?: u.cedula
            binding.adminUserRol.text = u.rol
            binding.adminUserEstado.text = if (u.isActive) "ACTIVO" else "INACTIVO"

            val ctx = binding.root.context
            binding.adminUserEstado.setBackgroundColor(
                ctx.getColor(
                    if (u.isActive) com.example.concentradospt.R.color.primary
                    else com.example.concentradospt.R.color.error
                )
            )

            binding.adminUserBtnToggle.text = if (u.isActive) "Desactivar" else "Activar"
            binding.adminUserBtnToggle.setOnClickListener { onToggleEstado(u) }
            binding.adminUserBtnResetPass.setOnClickListener { onResetPin(u) }
            binding.adminUserBtnEditar.setOnClickListener { onEditar(u) }
            binding.adminUserBtnEliminar.setOnClickListener { onEliminar(u) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminUsuarioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<AdminUsuarioInfo>() {
        override fun areItemsTheSame(a: AdminUsuarioInfo, b: AdminUsuarioInfo) = a.id == b.id
        override fun areContentsTheSame(a: AdminUsuarioInfo, b: AdminUsuarioInfo) = a == b
    }
}
