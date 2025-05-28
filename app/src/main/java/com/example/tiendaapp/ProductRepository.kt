package com.example.tiendaapp.data.repository

import com.example.tiendaapp.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class ProductRepository {

    // Instancia de Firestore
    private val db: FirebaseFirestore = Firebase.firestore
    private val productsCollection = db.collection("products")

    /** ------------------------- OPERACIONES CRUD ------------------------- */

    // CREATE: Añadir nuevo producto
    suspend fun addProduct(product: Product): String {
        return try {
            val documentRef = productsCollection.add(product).await()
            documentRef.id // Retornamos el ID del nuevo documento
        } catch (e: Exception) {
            throw Exception("Error al añadir producto: ${e.message}")
        }
    }

    // READ: Obtener todos los productos
    suspend fun getAllProducts(): List<Product> {
        return try {
            val querySnapshot = productsCollection.get().await()
            querySnapshot.documents.map { document ->
                document.toObject(Product::class.java)!!.copy(id = document.id)
            }
        } catch (e: Exception) {
            throw Exception("Error al cargar productos: ${e.message}")
        }
    }

    // READ: Obtener un producto por ID
    suspend fun getProductById(productId: String): Product? {
        return try {
            val document = productsCollection.document(productId).get().await()
            document.toObject(Product::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            throw Exception("Error al obtener producto: ${e.message}")
        }
    }

    // UPDATE: Actualizar producto
    suspend fun updateProduct(productId: String, updates: Map<String, Any>) {
        try {
            productsCollection.document(productId).update(updates).await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar producto: ${e.message}")
        }
    }

    // DELETE: Eliminar producto
    suspend fun deleteProduct(productId: String) {
        try {
            productsCollection.document(productId).delete().await()
        } catch (e: Exception) {
            throw Exception("Error al eliminar producto: ${e.message}")
        }
    }

    /** ------------------------- FUNCIONES ESPECÍFICAS ------------------------- */

    // Buscar productos por nombre
    suspend fun searchProductsByName(query: String): List<Product> {
        return try {
            val querySnapshot = productsCollection
                .orderBy("name")
                .startAt(query)
                .endAt("$query\uf8ff")
                .get()
                .await()

            querySnapshot.documents.map { document ->
                document.toObject(Product::class.java)!!.copy(id = document.id)
            }
        } catch (e: Exception) {
            throw Exception("Error en búsqueda: ${e.message}")
        }
    }

    // Actualizar solo el stock
    suspend fun updateStock(productId: String, newStock: Int) {
        try {
            productsCollection.document(productId)
                .update("stock", newStock)
                .await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar stock: ${e.message}")
        }
    }
}