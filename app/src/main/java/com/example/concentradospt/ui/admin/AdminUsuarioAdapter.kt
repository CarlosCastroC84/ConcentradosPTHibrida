package com.example.concentradospt.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.concentradospt.data.model.admin.AdminUsuarioInfo
import com.example.concentradospt.databinding.ItemAdminUsuarioBinding

/**
 * Adaptador para el RecyclerView de gestión de usuarios en el panel de administración.
 *
 * Muestra cada [AdminUsuarioInfo] con nombre, correo, rol y estado (ACTIVO/INACTIVO).
 * El color del chip de estado varía según si el usuario está activo (primario) o inactivo (error).
 * Expone cuatro acciones por usuario: activar/desactivar, regenerar PIN, editar y eliminar.
 *
 * @param onToggleEstado Lambda invocada al pulsar el botón de activar/desactivar.
 * @param onResetPin     Lambda invocada al pulsar el botón de regenerar PIN.
 * @param onEditar       Lambda invocada al pulsar el botón de editar (opcional, por defecto vacío).
 * @param onEliminar     Lambda invocada al pulsar el botón de eliminar (opcional, por defecto vacío).
 */
class AdminUsuarioAdapter(
    private val onToggleEstado: (AdminUsuarioInfo) -> Unit,
    private val onResetPin: (AdminUsuarioInfo) -> Unit,
    private val onEditar: (AdminUsuarioInfo) -> Unit = {},
    private val onEliminar: (AdminUsuarioInfo) -> Unit = {}
) : ListAdapter<AdminUsuarioInfo, AdminUsuarioAdapter.ViewHolder>(DiffCallback) {

    /**
     * ViewHolder que mantiene las referencias a las vistas de un ítem de usuario administrativo.
     *
     * @param binding Binding generado a partir del layout [ItemAdminUsuarioBinding].
     */
    inner class ViewHolder(private val binding: ItemAdminUsuarioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de un [AdminUsuarioInfo] a las vistas del ViewHolder.
         *
         * Muestra nombre completo (o cédula como fallback), correo (o cédula), rol y estado.
         * Aplica color diferenciado al chip de estado y configura todos los botones de acción.
         *
         * @param u Información del usuario administrativo a renderizar.
         */
        fun bind(u: AdminUsuarioInfo) {
            binding.adminUserName.text = u.nombreCompleto.ifBlank { u.cedula }
            binding.adminUserEmail.text = u.email ?: u.cedula
            binding.adminUserRol.text = u.rol
            binding.adminUserEstado.text = if (u.isActive) "ACTIVO" else "INACTIVO"

            // El color del badge de estado indica visualmente si el usuario está activo o no
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

    /**
     * Crea un nuevo [ViewHolder] inflando el layout del ítem de usuario administrativo.
     *
     * @param parent ViewGroup padre al que se adjuntará la vista.
     * @param viewType Tipo de vista (no utilizado; todos los ítems son del mismo tipo).
     * @return Nuevo [ViewHolder] listo para ser enlazado con datos.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminUsuarioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Enlaza el [AdminUsuarioInfo] en la posición indicada con su [ViewHolder].
     *
     * @param holder ViewHolder que debe ser actualizado.
     * @param position Posición del usuario en la lista.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    /**
     * Callback de diferencias utilizado por [ListAdapter] para detectar
     * cambios en la lista y animar las actualizaciones de forma eficiente.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<AdminUsuarioInfo>() {
        /** Compara dos usuarios por su identificador único. */
        override fun areItemsTheSame(a: AdminUsuarioInfo, b: AdminUsuarioInfo) = a.id == b.id

        /** Compara el contenido completo de dos usuarios (comparación estructural). */
        override fun areContentsTheSame(a: AdminUsuarioInfo, b: AdminUsuarioInfo) = a == b
    }
}
