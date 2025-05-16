package com.example.tiendaapp.ui.seller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tiendaapp.databinding.ActivitySellerBinding

class SellerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySellerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViews()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Panel de Vendedor"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupViews() {
        // Aquí configuraremos las vistas específicas para vendedores
        // Ejemplo: Lista de clientes, productos, ventas, etc.
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}