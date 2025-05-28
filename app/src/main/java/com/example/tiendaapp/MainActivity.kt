package com.example.tiendaapp

import CartFragment
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.activity.viewModels
import com.example.tiendaapp.data.model.CartItem
import com.example.tiendaapp.data.model.Product
import com.example.tiendaapp.databinding.ActivityMainBinding
import com.example.tiendaapp.ui.admin.AdminActivity
import com.example.tiendaapp.ui.theme.CartViewModel
import com.example.tiendaapp.ui.theme.ProductListFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.firestore.SetOptions

class MainActivity : AppCompatActivity(),
    ProductListFragment.OnProductInteractionListener {  // Implementación de la interfaz

    private lateinit var binding: ActivityMainBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { Firebase.firestore }

    // ViewModel para manejar el carrito
    private val cartViewModel: CartViewModel by viewModels()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val GOOGLE_PLAY_SERVICES_REQUEST_CODE = 9000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkGooglePlayServices()
        initializeUI()
        checkUserStatus()
    }

    private fun checkGooglePlayServices() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(
                    this,
                    resultCode,
                    GOOGLE_PLAY_SERVICES_REQUEST_CODE
                )
                    ?.show()
                Snackbar.make(
                    binding.root,
                    "Google Play Services no está disponible",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
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

        binding.btnAdminPanel.setOnClickListener {
            verifyAdminAccess()
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

    // === Gestión ubicación ===
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
        MaterialAlertDialogBuilder(this)
            .setTitle("Permiso de ubicación necesario")
            .setMessage("Esta aplicación necesita acceso a tu ubicación para mostrarte tiendas cercanas y ofertas en tu área.")
            .setPositiveButton("Aceptar") { _, _ -> requestLocationPermissions() }
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
            Snackbar.make(
                binding.root,
                "Error al iniciar servicio de ubicación",
                Snackbar.LENGTH_SHORT
            ).show()
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
                    Snackbar.make(
                        binding.root,
                        "Funciones de ubicación limitadas",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    // ========================

    private fun checkAdminStatus() {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val isAdmin = document.getString("role") == "admin"
                    runOnUiThread {
                        binding.btnAdminPanel.visibility = if (isAdmin) View.VISIBLE else View.GONE
                        binding.btnAdminPanel.isEnabled = isAdmin
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error al verificar estado de administrador", e)
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
                        Snackbar.make(
                            binding.root,
                            "Requiere privilegios de administrador",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        binding.btnAdminPanel.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    Snackbar.make(binding.root, "Error verificando permisos", Snackbar.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun updateWelcomeMessage() {
        binding.tvWelcome.text =
            "¡Bienvenido, ${auth.currentUser?.email?.split("@")?.firstOrNull() ?: "Usuario"}!"
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

    fun showCartFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, CartFragment(), "cart")
            addToBackStack("cart")
            setReorderingAllowed(true)
        }
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

    // --- IMPLEMENTACIÓN DE LA INTERFAZ ---

    override fun onAddToCart(product: Product) {
        if (product.stock <= 0) {
            Snackbar.make(
                binding.root,
                "No hay stock disponible para ${product.name}",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val newStock = product.stock - 1
        val productRef = Firebase.firestore.collection("products").document(product.id)

        productRef.update("stock", newStock)
            .addOnSuccessListener {
                val cartItem = CartItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    quantity = 1,
                    imageUrl = product.imageUrl
                )
                cartViewModel.addToCart(cartItem)
                Snackbar.make(
                    binding.root,
                    "${product.name} agregado al carrito",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Snackbar.make(
                    binding.root,
                    "Error al actualizar stock: ${e.message}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }

    override fun onProductClick(product: Product) {
        Snackbar.make(binding.root, "Producto seleccionado: ${product.name}", Snackbar.LENGTH_SHORT)
            .show()
    }

    // -- Nuevo: editar producto con diálogo --

    override fun onEditProduct(product: Product) {
        val fragment = supportFragmentManager
            .findFragmentById(R.id.editProductPrice) as? ProductListFragment
        fragment?.loadProducts(product)
    }


    override fun onDeleteProduct(productId: String) {
        db.collection("products").document(productId)
            .delete()
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Producto eliminado", Snackbar.LENGTH_SHORT).show()
                loadProductListFragment()
            }
            .addOnFailureListener { e ->
                Snackbar.make(
                    binding.root,
                    "Error al eliminar: ${e.message}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }


    private fun showEditProductDialog(product: Product) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_product, null)

        val etProductName = dialogView.findViewById<EditText>(R.id.editProductName)
        val etProductPrice = dialogView.findViewById<EditText>(R.id.editProductPrice)
        val etProductStock = dialogView.findViewById<EditText>(R.id.editProductStock)

        // Poner valores actuales
        etProductName.setText(product.name)
        etProductPrice.setText(product.price.toString())
        etProductStock.setText(product.stock.toString())

        MaterialAlertDialogBuilder(this)
            .setTitle("Editar Producto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, _ ->
                val newName = etProductName.text.toString().trim()
                val newPriceStr = etProductPrice.text.toString().trim()
                val newStockStr = etProductStock.text.toString().trim()

                if (newName.isEmpty() || newPriceStr.isEmpty() || newStockStr.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "Todos los campos son obligatorios",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    val newPrice = newPriceStr.toDoubleOrNull()
                    val newStock = newStockStr.toIntOrNull()

                    if (newPrice == null || newStock == null) {
                        Snackbar.make(
                            binding.root,
                            "Precio o stock no válidos",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        updateProduct(product, newName, newPrice, newStock)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateProduct(product: Product, newName: String, newPrice: Double, newStock: Int) {
        val productRef = db.collection("products").document(product.id)
        productRef.set(
            mapOf(
                "name" to newName,
                "price" to newPrice,
                "stock" to newStock
            ),
            SetOptions.merge()  // Merge para actualizar sin eliminar otros campos
        )
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Producto actualizado", Snackbar.LENGTH_SHORT).show()
                // Recarga productos para actualizar vista
                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (fragment is ProductListFragment) {
                    fragment.loadProducts(null)




                }
            }
            .addOnFailureListener { e ->
                Snackbar.make(
                    binding.root,
                    "Error al actualizar: ${e.message}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateProductName(product: Product, newName: String, updatedProduct: Product) {
        val productRef = db.collection("products").document(product.id)
        productRef.set(
            mapOf("name" to newName),
            SetOptions.merge()
        )
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Producto actualizado", Snackbar.LENGTH_SHORT).show()
                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (fragment is ProductListFragment) {
                    fragment.loadProducts(updatedProduct)
                }
            }
            .addOnFailureListener { e ->
                Snackbar.make(
                    binding.root,
                    "Error al actualizar: ${e.message}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }
}
