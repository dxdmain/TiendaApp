package com.example.tiendaapp.ui.admin

import android.app.AlertDialog
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tiendaapp.data.model.User
import androidx.appcompat.app.AppCompatActivity
import com.example.tiendaapp.databinding.ActivityAdminBinding
import android.os.Bundle
import com.example.tiendaapp.R
import com.example.tiendaapp.ui.theme.UsersAdapter

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var usersAdapter: UsersAdapter
    private val db = Firebase.firestore  // Instancia de Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración del RecyclerView
        usersAdapter = UsersAdapter()
        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(this@AdminActivity)
            adapter = usersAdapter
        }

        // Botón de refrescar (asegúrate de tener este ID en tu XML)
        binding.btnRefresh.setOnClickListener { loadUsers() }

        // Cargar datos iniciales
        loadUsers()
    }

    private fun loadUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val users = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(User::class.java)
                }
                usersAdapter.updateList(users)
            }
            .addOnFailureListener { exception ->
                // Manejar error
            }
    }

    fun assignAdminRole(userId: String) {
        val userRef = Firebase.firestore.collection("users").document(userId)

        userRef.update("role", "admin")
            .addOnSuccessListener {
                Toast.makeText(this, "Rol de admin asignado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun showRoleChangeDialog(userId: String, currentRole: String) {
        val roles = arrayOf("Cliente", "Administrador")
        val checkedItem = if (currentRole == "admin") 1 else 0

        AlertDialog.Builder(this)
            .setTitle("Cambiar rol")
            .setSingleChoiceItems(roles, checkedItem) { dialog, which ->
                val newRole = if (which == 0) "client" else "admin"
                updateUserRole(userId, newRole)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateUserRole(userId: String, newRole: String) {
        db.collection("users")
            .document(userId)
            .update("role", newRole)
            .addOnSuccessListener { loadUsers() }
    }
}