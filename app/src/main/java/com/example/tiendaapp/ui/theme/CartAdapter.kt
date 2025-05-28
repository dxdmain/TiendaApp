package com.example.tiendaapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tiendaapp.R
import com.example.tiendaapp.data.model.CartItem

class CartAdapter(
    private val onItemRemoved: (CartItem) -> Unit,
    private val onQuantityChanged: (CartItem, Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val items: MutableList<CartItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    // ✅ Método para actualizar toda la lista del carrito
    fun updateCartItems(newItems: List<CartItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    // ✅ Método para actualizar un solo item del carrito
    fun updateCartItem(updatedItem: CartItem) {
        val index = items.indexOfFirst { it.productId == updatedItem.productId }
        if (index != -1) {
            items[index] = updatedItem
            notifyItemChanged(index)
        }
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.txtCartItemName)
        private val txtPrice: TextView = itemView.findViewById(R.id.txtCartItemPrice)
        private val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
        private val btnIncrease: Button = itemView.findViewById(R.id.btnIncrease)
        private val btnDecrease: Button = itemView.findViewById(R.id.btnDecrease)

        fun bind(cartItem: CartItem) {
            txtName.text = cartItem.name
            txtPrice.text = "$${cartItem.price}"
            txtQuantity.text = cartItem.quantity.toString()

            btnIncrease.setOnClickListener {
                val newQuantity = cartItem.quantity + 1
                onQuantityChanged(cartItem, newQuantity)
            }

            btnDecrease.setOnClickListener {
                if (cartItem.quantity > 1) {
                    val newQuantity = cartItem.quantity - 1
                    onQuantityChanged(cartItem, newQuantity)
                } else {
                    onItemRemoved(cartItem)
                }
            }
        }
    }
}
