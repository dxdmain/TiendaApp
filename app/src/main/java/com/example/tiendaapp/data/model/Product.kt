package com.example.tiendaapp.data.model

import com.google.firebase.firestore.DocumentId

data class Product(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val stock: Int = 99999,
    val imageUrl: String = "",
    val description: String? = null
) {
    constructor() : this("", "", 0.0, "", 0, "")
}
