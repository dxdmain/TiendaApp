package com.example.tiendaapp.ui.theme.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendaapp.data.model.Product
import com.example.tiendaapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    // Estados para la UI
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Cargar productos
    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _products.value = repository.getAllProducts()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Operaciones CRUD
    fun addProduct(product: Product) = viewModelScope.launch {
        try {
            repository.addProduct(product)
            loadProducts() // Refrescar lista
        } catch (e: Exception) {
            _errorMessage.value = "Error al añadir: ${e.message}"
        }
    }

    fun updateProduct(productId: String, updates: Map<String, Any>) = viewModelScope.launch {
        try {
            repository.updateProduct(productId, updates)
            loadProducts()
        } catch (e: Exception) {
            _errorMessage.value = "Error al actualizar: ${e.message}"
        }
    }

    fun deleteProduct(productId: String) = viewModelScope.launch {
        try {
            repository.deleteProduct(productId)
            loadProducts()
        } catch (e: Exception) {
            _errorMessage.value = "Error al eliminar: ${e.message}"
        }
    }
}