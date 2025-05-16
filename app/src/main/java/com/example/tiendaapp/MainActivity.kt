package com.example.tiendaapp

import CartFragment
import android.Manifest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore // Extensión KTX
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.example.tiendaapp.data.model.Product
import com.example.tiendaapp.databinding.ActivityMainBinding
import com.example.tiendaapp.ui.admin.AdminActivity
import com.example.tiendaapp.ui.theme.ProductListFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), ProductListFragment.OnProductInteractionListener {

    private lateinit var binding: ActivityMainBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { Firebase.firestore }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        initializeUI()
        checkUserStatus()
    }

    private fun initializeUI() {
        setSupportActionBar(binding.toolbar)
        setupClickListeners()
        setupRegisterButton()
    }

    private fun checkUserStatus() {
        if (auth.currentUser == null) {
            showRegisterButton()
            redirectToLogin()
            return
        }

        hideRegisterButton()
        loadProductListFragment()
        checkAdminStatus()
        updateWelcomeMessage()

        // Verificar permisos de ubicación al iniciar
        checkLocationPermissions()
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            redirectToLogin()
        }

        binding.fabCart.setOnClickListener {
            showCartFragment()
        }
    }

    private fun setupRegisterButton() {
        binding.btnRegister.setOnClickListener {
            Log.d("MainActivity", "Botón Registrarse clickeado")
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showRegisterButton() {
        binding.btnRegister.visibility = View.VISIBLE
        binding.btnLogout.visibility = View.GONE
        binding.tvWelcome.text = "Por favor inicia sesión o regístrate"
    }

    private fun hideRegisterButton() {
        binding.btnRegister.visibility = View.GONE
        binding.btnLogout.visibility = View.VISIBLE
    }

    // ======================== [GESTIÓN DE UBICACIÓN] ========================
    private fun checkLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationService()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showLocationPermissionExplanation()
            }
            else -> {
                requestLocationPermissions()
            }
        }
    }

    private fun showLocationPermissionExplanation() {
        AlertDialog.Builder(this)
            .setTitle("Permiso de ubicación necesario")
            .setMessage("Esta aplicación necesita acceso a tu ubicación para mostrarte tiendas cercanas y ofertas en tu área.")
            .setPositiveButton("Aceptar") { _, _ ->
                requestLocationPermissions()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun startLocationService() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, LocationService::class.java))
            } else {
                startService(Intent(this, LocationService::class.java))
            }
            Log.d("MainActivity", "Servicio de ubicación iniciado")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al iniciar servicio de ubicación", e)
            Toast.makeText(this, "Error al iniciar servicio de ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationService()
                } else {
                    Toast.makeText(this, "Funciones de ubicación limitadas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // ========================================================================

    private fun checkAdminStatus() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val isAdmin = document.getString("role") == "admin"

                    runOnUiThread {
                        // Mostrar SIEMPRE el botón (pero deshabilitarlo si no es admin)
                        binding.btnAdminPanel.visibility = View.VISIBLE
                        binding.btnAdminPanel.isEnabled = isAdmin // Solo clickeable si es admin


                    }
                }
        }
    }
    private fun toggleAdminOptions(show: Boolean) {
        binding.btnAdminPanel.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.btnAdminPanel.setOnClickListener {
                verifyAdminAccess()
            }
        }
    }

    private fun verifyAdminAccess() {
        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.getString("role") == "admin") {
                        startActivity(Intent(this, AdminActivity::class.java))
                    } else {
                        Toast.makeText(this, "Requiere privilegios de administrador", Toast.LENGTH_SHORT).show()
                        toggleAdminOptions(false)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error verificando permisos", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun updateWelcomeMessage() {
        binding.tvWelcome.text = "¡Bienvenido, ${auth.currentUser?.email?.split("@")?.firstOrNull() ?: "Usuario"}!"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val isAdmin = document.getString("role") == "admin"
                    Log.d("MenuPrep", "Preparando menú - Admin: $isAdmin")
                    menu.findItem(R.id.action_admin)?.isVisible = isAdmin
                    binding.btnAdminPanel.visibility = if (isAdmin) View.VISIBLE else View.GONE
                }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                showCartFragment()
                true
            }
            R.id.action_admin -> {
                verifyAdminAccess()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadProductListFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, ProductListFragment())
            setReorderingAllowed(true)
        }
    }

    override fun onAddToCart(product: Product) {
        if (isCartFragmentVisible()) {
            addProductToCart(product)
        } else {
            showCartWithProduct(product)
        }
        showProductAddedMessage(product)
    }

    private fun isCartFragmentVisible(): Boolean {
        return supportFragmentManager.findFragmentByTag("cart") != null
    }

    private fun addProductToCart(product: Product) {
        (supportFragmentManager.findFragmentByTag("cart") as? CartFragment)?.addToCart(product)
    }

    fun showCartFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, CartFragment(), "cart")
            addToBackStack("cart")
            setReorderingAllowed(true)
        }
    }


    private fun showCartWithProduct(product: Product) {
        showCartFragment()
        supportFragmentManager.executePendingTransactions()
        addProductToCart(product)
    }

    private fun showProductAddedMessage(product: Product) {
        Toast.makeText(this, "${product.name} agregado al carrito", Toast.LENGTH_SHORT).show()
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}