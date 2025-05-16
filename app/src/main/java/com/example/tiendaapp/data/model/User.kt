package com.example.tiendaapp.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "client", // Valores posibles: "client" o "admin"
    val createdAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)