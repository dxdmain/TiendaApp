package com.example.tiendaapp.ui.theme

import android.view.LayoutInflater
import com.example.tiendaapp.ui.admin.AdminActivity
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tiendaapp.databinding.ItemUserBinding
import com.example.tiendaapp.data.model.User

class UsersAdapter : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    private val users = mutableListOf<User>()

    // Clase ViewHolder correctamente definida
    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    // Método para actualizar la lista
    fun updateList(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()  // Notifica los cambios
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.binding.apply {
            tvUserName.text = user.name
            tvUserEmail.text = user.email
            tvUserRole.text = user.role

            // Manejo de clics
            root.setOnClickListener {
                (holder.itemView.context as? AdminActivity)?.showRoleChangeDialog(
                    user.uid,
                    user.role
                )
            }
        }
    }

    override fun getItemCount(): Int = users.size

    // Interface para callback de clics (opcional)
    interface OnUserClickListener {
        fun onUserClick(user: User)
    }
}