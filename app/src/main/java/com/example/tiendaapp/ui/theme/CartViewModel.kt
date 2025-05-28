package com.example.tiendaapp.ui.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tiendaapp.data.model.CartItem

class CartViewModel : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun addToCart(cartItem: CartItem) {
        val currentList = _cartItems.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.productId == cartItem.productId }
        if (index != -1) {
            val updatedItem = currentList[index].copy(quantity = currentList[index].quantity + cartItem.quantity)
            currentList[index] = updatedItem
        } else {
            currentList.add(cartItem)
        }
        _cartItems.value = currentList
    }

    fun removeFromCart(cartItem: CartItem) {
        val updatedList = _cartItems.value?.filter { it.productId != cartItem.productId } ?: emptyList()
        _cartItems.value = updatedList
    }

    fun updateCartItemQuantity(productId: String, newQuantity: Int) {
        val updatedList = _cartItems.value?.map {
            if (it.productId == productId) it.copy(quantity = newQuantity) else it
        } ?: emptyList()
        _cartItems.value = updatedList
    }


    fun clearCart() {
        _cartItems.value = mutableListOf()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun showError(message: String) {
        _errorMessage.value = message
    }
}
