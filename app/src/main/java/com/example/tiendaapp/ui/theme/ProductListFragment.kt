package com.example.tiendaapp.ui.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tiendaapp.data.model.Product
import com.example.tiendaapp.databinding.FragmentProductListBinding

class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter

    interface OnProductInteractionListener {
        fun onAddToCart(product: Product)
    }

    private var interactionListener: OnProductInteractionListener? = null

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

        activity?.let {
            if (it is OnProductInteractionListener) {
                interactionListener = it
            }
        }

        setupRecyclerView()
        // Eliminada la llamada a loadProducts() ya que ahora usamos getSampleProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = getSampleProducts(), // Reemplazamos loadProducts() por getSampleProducts()
            onItemClick = { product ->
                showProductDetails(product)
            },
            onAddToCartClick = { product ->
                addToCart(product)
            }
        )

        binding.recyclerViewProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun getSampleProducts(): List<Product> {
        return listOf(
            Product(id = "1", name = "Camiseta", price = 19.99, imageUrl = "url_imagen_1"),
            Product(id = "2", name = "Pantalón", price = 39.99, imageUrl = "url_imagen_2"),
            Product(id = "3", name = "Zapatos", price = 59.99, imageUrl = "url_imagen_3"),
            Product(id = "4", name = "Gorra", price = 14.99, imageUrl = "url_imagen_4")
        )
    }

    private fun showProductDetails(product: Product) {
        Toast.makeText(requireContext(), "Mostrando detalles de ${product.name}", Toast.LENGTH_SHORT).show()
    }

    private fun addToCart(product: Product) {
        try {
            interactionListener?.onAddToCart(product)
            Toast.makeText(
                requireContext(),
                "${product.name} agregado al carrito",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al agregar al carrito", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        interactionListener = null
    }
}