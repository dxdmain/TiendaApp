
import com.example.tiendaapp.databinding.FragmentCartBinding
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tiendaapp.data.model.CartItem
import com.example.tiendaapp.data.model.Product
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.activityViewModels
import com.example.tiendaapp.ui.theme.CartViewModel
import com.example.tiendaapp.R
import com.example.tiendaapp.ui.adapter.CartAdapter


class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartAdapter: CartAdapter
    private val cartViewModel: CartViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCheckoutButton()
        setupObservers()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onItemRemoved = { cartItem ->
                cartViewModel.removeFromCart(cartItem)
            },
            onQuantityChanged = { cartItem, newQuantity ->
                cartViewModel.updateCartItemQuantity(cartItem.productId, newQuantity)
            }
        )

        binding.recyclerViewCart.adapter = cartAdapter


        // Observador para actualizar la lista del adaptador
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartAdapter.updateCartItems(items)
        }
    }


    private fun setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener {
            cartViewModel.cartItems.value?.let { items ->
                if (items.isEmpty()) {
                    Snackbar.make(binding.root, "El carrito está vacío", Snackbar.LENGTH_SHORT).show()
                } else {
                    processCheckout(items)
                }
            }
        }
    }

    private fun setupObservers() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartAdapter.updateCartItems(items)   // Aquí cambias cartItems por items
            updateTotalPrice(items)
            updateEmptyState(items.isEmpty())
        }



    cartViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                cartViewModel.clearErrorMessage()
            }
        }
    }

    private fun updateTotalPrice(items: List<CartItem>) {
        val total = items.sumOf { it.price * it.quantity }
        binding.txtTotal.text = "Total: $${"%.2f".format(total)}"
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.recyclerViewCart.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyCartView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.btnCheckout.isEnabled = !isEmpty
    }

    private fun processCheckout(items: List<CartItem>) {
        Toast.makeText(requireContext(), "Compra realizada!", Toast.LENGTH_SHORT).show()
        cartViewModel.clearCart()
    }

    fun removeFromCart(cartItem: CartItem) {
        cartViewModel.removeFromCart(cartItem)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
