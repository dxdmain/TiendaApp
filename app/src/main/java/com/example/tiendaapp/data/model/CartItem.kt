package com.example.tiendaapp.data.model

data class CartItem(
    val productId: String,
    val name: String,
    val price: Double,
    var quantity: Int = 1,
    val imageUrl: String = ""
) {
    fun totalPrice(): Double = price * quantity
}