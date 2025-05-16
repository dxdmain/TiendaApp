import com.example.tiendaapp.databinding.FragmentCartBinding
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log;
import com.example.tiendaapp.R
import com.example.tiendaapp.data.model.CartItem
import com.example.tiendaapp.ui.theme.CartAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tiendaapp.data.model.Product

public class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartAdapter: CartAdapter
    private val cartItems = mutableListOf<CartItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCheckoutButton()
        binding.recyclerViewCart.adapter = cartAdapter
    }

    public fun setupRecyclerView() {
        cartAdapter = CartAdapter(cartItems) { updatedItem ->
            updateTotalPrice()

            }
        binding.recyclerViewCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }


        binding.recyclerViewCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun updateTotalPrice() {
        val total = cartItems.sumOf { it.price * it.quantity }
        binding.txtTotal.text = "Total: $${"%.2f".format(total)}"
        Log.d("CartFragment", "Total updated: $total")
    }

    public fun setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener {
            Toast.makeText(context, "Compra realizada!", Toast.LENGTH_SHORT).show()
        }
    }


    public fun addToCart(product: Product) {
        val existingItem = cartItems.find { it.productId == product.id }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(CartItem(
                productId = product.id,
                name = product.name,
                price = product.price,
                imageUrl = product.imageUrl

            ))
        }
        cartAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}