package com.example.tiendaapp.ui.theme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tiendaapp.R
import com.example.tiendaapp.data.model.Product

class ProductAdapter(
    private var products: MutableList<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    fun updateProducts(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    fun updateProductLocal(updatedProduct: Product) {
        val index = products.indexOfFirst { it.id == updatedProduct.id }
        if (index != -1) {
            products[index] = updatedProduct
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.txtName)
        private val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
        private val btnAddToCart: Button = itemView.findViewById(R.id.btnAddToCart)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(product: Product) {
            txtName.text = product.name
            txtPrice.text = "$${product.price}"

            itemView.setOnClickListener { onItemClick(product) }
            btnAddToCart.setOnClickListener { onAddToCartClick(product) }
            btnEdit.setOnClickListener { onEditClick(product) }
            btnDelete.setOnClickListener { onDeleteClick(product.id) }


        }
    }
}
