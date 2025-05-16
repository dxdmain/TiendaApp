package com.example.tiendaapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tiendaapp.databinding.ActivityRegisterBinding
import com.example.tiendaapp.ui.admin.AdminActivity
import com.example.tiendaapp.ui.seller.SellerActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

public class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var isAdminCreatingUser = false

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        isAdminCreatingUser = intent.getBooleanExtra("isAdmin", false)

        setupUI()
        setupClickListeners() // Mover la configuración del listener aquí
    }

    private fun setupUI() {
        // Configurar el selector de roles
        val roles = resources.getStringArray(R.array.roles_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)

        (binding.spinnerRole as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            threshold = 1
        }

        binding.tilRole.visibility = if (isAdminCreatingUser) View.VISIBLE else View.GONE
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            Log.d(TAG, "Botón de registro presionado")
            registerUser()
        }
    }

    private fun registerUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val name = binding.etName.text.toString().trim()

        Log.d("RegisterActivity", "Intentando registrar: $email")

        if (!validateInputs(email, password, name)) {
            Log.d("RegisterActivity", "Validación fallida")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterActivity", "Registro Firebase Auth exitoso")
                    saveUserToFirestore(email, name, "client") // Rol por defecto
                } else {
                    Log.e("RegisterActivity", "Error en registro", task.exception)
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message ?: "Error desconocido"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
    private fun validateInputs(email: String, password: String, name: String): Boolean {
        // Limpiar errores previos
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilName.error = null

        var isValid = true

        // Validación de email
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email requerido"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Email inválido"
            isValid = false
        }

        // Validación de contraseña
        if (password.isEmpty()) {
            binding.tilPassword.error = "Contraseña requerida"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Mínimo 6 caracteres"
            isValid = false
        }

        // Validación de nombre
        if (name.isEmpty()) {
            binding.tilName.error = "Nombre requerido"
            isValid = false
        } else if (name.length < 3) {
            binding.tilName.error = "Nombre muy corto"
            isValid = false
        }

        // Mostrar errores en log
        if (!isValid) {
            Log.w("RegisterActivity", "Validación fallida: " +
                    "Email: ${if (email.isEmpty()) "vacío" else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) "inválido" else "OK"}, " +
                    "Password: ${if (password.isEmpty()) "vacía" else if (password.length < 6) "corta" else "OK"}, " +
                    "Name: ${if (name.isEmpty()) "vacío" else if (name.length < 3) "corto" else "OK"}")
        }

        return isValid
    }

    private fun saveUserToFirestore(email: String, name: String, role: String) {
        val user = auth.currentUser ?: run {
            Log.e("RegisterActivity", "Usuario currentUser es null")
            Toast.makeText(this, "Error: No se pudo crear el usuario", Toast.LENGTH_LONG).show()
            return
        }

        Log.d("RegisterActivity", "Guardando usuario en Firestore: ${user.uid}")

        val userData = hashMapOf(
            "uid" to user.uid,
            "email" to email,
            "name" to name,
            "role" to role,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Usuario guardado en Firestore")
                Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                redirectUser(role)
            }
            .addOnFailureListener { e ->
                Log.e("RegisterActivity", "Error al guardar en Firestore", e)
                Toast.makeText(
                    this,
                    "Error al guardar datos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun redirectUser(role: String) {
        Log.d("RegisterActivity", "Redirigiendo usuario con rol: $role")

        val intent = when (role.lowercase()) {
            "admin" -> Intent(this, AdminActivity::class.java)
            "seller" -> Intent(this, SellerActivity::class.java)
            else -> Intent(this, MainActivity::class.java)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()

        Log.d("RegisterActivity", "Redirección completada")
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e(TAG, message)
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.d(TAG, message)
    }
}