package com.example.tiendaapp.ui.theme
import android.util.Log;
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tiendaapp.data.model.Product
import com.example.tiendaapp.R
import com.example.tiendaapp.data.model.CartItem
import com.example.tiendaapp.databinding.ItemCartBinding

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onQuantityChanged: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder( val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.txtCartItemName.text = item.name
            binding.txtCartItemPrice.text = "$${"%.2f".format(item.totalPrice())}"
            binding.txtQuantity.text = item.quantity.toString()

            binding.btnIncrease.setOnClickListener {
                // Listener para disminuir cantidad
                binding.btnDecrease.setOnClickListener {
                    if (item.quantity > 1) {
                        item.quantity--
                        binding.txtQuantity.text = item.quantity.toString()
                        binding.txtCartItemPrice.text = "$${"%.2f".format(item.price * item.quantity)}"
                        onQuantityChanged(item)
                        Log.d("CartAdapter", "Decreased: ${item.name} to ${item.quantity}")
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        holder.bind(item)

        holder.binding.btnIncrease.setOnClickListener {
            item.quantity++
            notifyItemChanged(position)
            onQuantityChanged(item) // Esto reemplaza la llamada a updateTotalPrice
        }

        holder.binding.btnDecrease.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(position)
                onQuantityChanged(item) // Esto reemplaza la llamada a updateTotalPrice
            }
        }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateItems(newItems: List<CartItem>) {
        cartItems.clear()
        cartItems.addAll(newItems)
        notifyDataSetChanged()
    }
}