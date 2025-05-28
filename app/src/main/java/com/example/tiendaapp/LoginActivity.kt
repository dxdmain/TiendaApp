package com.example.tiendaapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tiendaapp.ui.CameraDialogFragment
import com.example.tiendaapp.databinding.ActivityLoginBinding
import com.example.tiendaapp.ui.admin.AdminActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val TAG = "LoginActivity"
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Configuración de listeners
        setupClickListeners()

        // Verificar usuario existente
        checkCurrentUser()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            signInWithEmail()
        }

        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnAdminPanel.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }
        binding.btnOpenCameraDialog.setOnClickListener {
            val cameraDialog = CameraDialogFragment()
            cameraDialog.show(supportFragmentManager, "CameraDialog")
        }
    }

    private fun checkCurrentUser() {
        auth.currentUser?.let { user ->
            checkAdminStatus(user.uid)
            updateUI(user)
        }
    }

    private fun signInWithEmail() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingresa email y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        checkAdminStatus(it.uid)
                        updateUI(it)
                    }
                } else {
                    showAuthError(task.exception?.message ?: "Error desconocido")
                }
            }
    }

    private fun signInWithGoogle() {
        try {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar Google Sign-In: ${e.message}")
            showAuthError("No se pudo iniciar el inicio de sesión con Google.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (data == null) {
                showAuthError("No se recibió información del intento de inicio de sesión.")
                return
            }

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val token = account?.idToken

                if (token != null && token.isNotEmpty()) {
                    firebaseAuthWithGoogle(token)
                } else {
                    showAuthError("No se pudo obtener el token de autenticación de Google.")
                }

            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)

                // Manejamos los códigos de error más comunes
                val message = when (e.statusCode) {
                    7 -> "No se pudo conectar con los servicios de Google. Revisa tu conexión a internet."
                    8 -> "Error interno de los servicios de Google. Intenta más tarde."
                    10 -> "Configuración incorrecta de la app (SHA-1 o ID de cliente OAuth)."
                    else -> "Error de autenticación con Google. Código: ${e.statusCode}"
                }

                showAuthError(message)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    handleSuccessfulLogin(task.result.user, isNewUser = task.result.additionalUserInfo?.isNewUser ?: false)
                } else {
                    showAuthError(task.exception?.message ?: "Autenticación fallida")
                }
            }
    }

    private fun handleSuccessfulLogin(user: FirebaseUser?, isNewUser: Boolean) {
        user?.let {
            if (isNewUser) {
                saveNewUserToFirestore(it)
            }
            checkAdminStatus(it.uid)
            updateUI(it)
        }
    }

    private fun saveNewUserToFirestore(user: FirebaseUser) {
        val userData = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "name" to (user.displayName ?: ""),
            "role" to "user",
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "Usuario guardado en Firestore")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al guardar usuario", e)
            }
    }

    private fun checkAdminStatus(uid: String) {
        // Verifica primero si el usuario está autenticado
        if (Firebase.auth.currentUser == null) return

        Firebase.firestore.collection("users").document(uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val isAdmin = document.getString("role") == "admin"
                        binding.btnAdminPanel.visibility = if (isAdmin) View.VISIBLE else View.GONE
                    } else {
                        // Crea el documento si no existe
                        createInitialUserDocument(uid)
                    }
                } else {
                    Log.e(TAG, "Firestore error: ", task.exception)
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun createInitialUserDocument(uid: String) {
        val userData = hashMapOf(
            "uid" to uid,
            "role" to "user", // Rol por defecto
            "createdAt" to FieldValue.serverTimestamp()
        )

        Firebase.firestore.collection("users").document(uid)
            .set(userData)
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al crear documento", e)
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    val isAdmin = document.getString("role") == "admin"
                    if (isAdmin) {
                        Toast.makeText(this, "Bienvenido Administrador", Toast.LENGTH_SHORT).show()
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
        }
    }

    private fun showAuthError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e(TAG, message)
    }

    companion object {
        private const val TAG = "LoginActivity"
    }

}