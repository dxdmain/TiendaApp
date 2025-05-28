package com.example.tiendaapp.ui.theme

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tiendaapp.data.model.CartItem
import com.example.tiendaapp.R
import com.example.tiendaapp.data.model.Product
import com.example.tiendaapp.databinding.FragmentProductListBinding

class ProductListFragment : Fragment() {

    interface OnProductInteractionListener {
        fun onAddToCart(product: Product)
        fun onProductClick(product: Product)
        fun onDeleteProduct(productId: String)
        fun onEditProduct(product: Product)
    }

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    lateinit var productAdapter: ProductAdapter
    private val cartViewModel: CartViewModel by activityViewModels()
    private var listener: OnProductInteractionListener? = null

    private var isAdmin: Boolean = false // Cambiar según tu lógica de usuario

    // Lista mutable local de productos para mantener estado
    private var products: MutableList<Product> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnProductInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnProductInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isAdmin = checkIfUserIsAdmin()
        setupRecyclerView()
        loadProducts(null) // Carga inicial sin producto actualizado
    }

    private fun checkIfUserIsAdmin(): Boolean {
        // Aquí tu lógica real de admin
        return true
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = products,
            onItemClick = { product -> listener?.onProductClick(product) },
            onAddToCartClick = { product ->
                listener?.onAddToCart(product)
                addToCartLocal(product)
            },
            onEditClick = { product ->
                if (isAdmin) {
                    showEditProductDialog(product)
                } else {
                    Toast.makeText(requireContext(), "No tienes permiso para editar", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { productId ->
                if (isAdmin) {
                    listener?.onDeleteProduct(productId)
                } else {
                    Toast.makeText(requireContext(), "No tienes permiso para eliminar", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.recyclerViewProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
            setHasFixedSize(true)
            addItemDecoration(GridSpacingItemDecoration(2, 16, true))
        }
    }

    fun showEditProductDialog(product: Product) {
        val context = requireContext()

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_product, null)

        val etName = dialogView.findViewById<EditText>(R.id.editProductName)
        val etPrice = dialogView.findViewById<EditText>(R.id.editProductPrice)
        val etStock = dialogView.findViewById<EditText>(R.id.editProductStock)

        etName.setText(product.name)
        etPrice.setText(product.price.toString())
        etStock.setText(product.stock.toString())

        AlertDialog.Builder(context)
            .setTitle("Editar producto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, _ ->
                val newName = etName.text.toString().trim()
                val newPrice = etPrice.text.toString().toDoubleOrNull() ?: product.price
                val newStock = etStock.text.toString().toIntOrNull() ?: product.stock

                if (newName.isNotEmpty()) {
                    val updatedProduct = product.copy(
                        name = newName,
                        price = newPrice,
                        stock = newStock
                    )
                    loadProducts(updatedProduct)  // Actualiza la lista local y refresca
                    Toast.makeText(context, "Producto actualizado localmente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Carga los productos en la lista local.
     * Si updatedProduct es nulo, carga la lista inicial.
     * Si no, actualiza ese producto en la lista y refresca el adapter.
     */
    fun loadProducts(updatedProduct: Product? = null) {
        if (updatedProduct != null) {
            val index = products.indexOfFirst { it.id == updatedProduct.id }
            if (index != -1) {
                products[index] = updatedProduct
            } else {
                products.add(updatedProduct) // opcional
            }
        } else {
            products = getSampleProducts().toMutableList()
        }
        productAdapter.updateProducts(products)
    }


    private fun getSampleProducts(): List<Product> {
        return listOf(
            Product(id = "1", name = "Camiseta", price = 19.99, imageUrl = "url_imagen_1"),
            Product(id = "2", name = "Pantalón", price = 39.99, imageUrl = "url_imagen_2"),
            Product(id = "3", name = "Zapatos", price = 59.99, imageUrl = "url_imagen_3"),
            Product(id = "4", name = "Gorra", price = 14.99, imageUrl = "url_imagen_4"),
            Product(id = "5", name = "Bufanda", price = 12.99, imageUrl = "url_imagen_5"),
            Product(id = "6", name = "Guantes", price = 9.99, imageUrl = "url_imagen_6")
        )
    }

    private fun addToCartLocal(product: Product) {
        try {
            val cartItem = CartItem(
                productId = product.id,
                name = product.name,
                price = product.price,
                quantity = 1,
                imageUrl = product.imageUrl
            )
            cartViewModel.addToCart(cartItem)
            Toast.makeText(requireContext(), "${product.name} agregado al carrito", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al agregar al carrito", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        listener = null
    }

    class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
        // Aquí puedes implementar si tienes lógica para el espaciado
    }
}
