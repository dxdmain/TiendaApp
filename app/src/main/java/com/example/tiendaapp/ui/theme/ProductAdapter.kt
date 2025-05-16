package com.example.tiendaapp.ui.theme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tiendaapp.R
import com.example.tiendaapp.data.model.Product

class ProductAdapter(
    var products: List<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorPlaceholder: View = itemView.findViewById(R.id.colorPlaceholder)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
        val btnAddToCart: Button = itemView.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        // Configuración de datos
        holder.txtName.text = product.name
        holder.txtPrice.text = "$${"%.2f".format(product.price)}"

        // Colores de placeholder
        val colors = listOf(
            holder.itemView.context.getColor(R.color.purple_200),
            holder.itemView.context.getColor(R.color.teal_200),
            holder.itemView.context.getColor(R.color.green_200)
        )
        holder.colorPlaceholder.setBackgroundColor(colors[position % colors.size])

        // Listeners
        holder.itemView.setOnClickListener { onItemClick(product) }
        holder.btnAddToCart.setOnClickListener { onAddToCartClick(product) }
    }

    override fun getItemCount() = products.size

    // Método para actualizar la lista de productos
    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}