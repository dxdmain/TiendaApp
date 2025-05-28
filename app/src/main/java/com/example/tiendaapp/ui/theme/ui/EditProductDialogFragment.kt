package com.example.tiendaapp.ui.theme.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.tiendaapp.R
import com.example.tiendaapp.data.model.Product

class EditProductDialogFragment : DialogFragment() {

    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments?.getString("id") ?: ""
        val name = arguments?.getString("name") ?: ""
        val price = arguments?.getDouble("price") ?: 0.0
        val stock = arguments?.getInt("stock") ?: 0
        product = Product(id, name, price, stock.toString())
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_edit_product, null)

        val nameInput = view.findViewById<EditText>(R.id.editProductName)
        val priceInput = view.findViewById<EditText>(R.id.editProductPrice)
        val stockInput = view.findViewById<EditText>(R.id.editProductStock)

        nameInput.setText(product.name)
        priceInput.setText(product.price.toString())
        stockInput.setText(product.stock.toString())

        return AlertDialog.Builder(requireContext())
            .setTitle("Editar producto")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val newName = nameInput.text.toString().trim()
                val newPrice = priceInput.text.toString().toDoubleOrNull()
                val newStock = stockInput.text.toString().toIntOrNull()

                if (newName.isNotEmpty() && newPrice != null && newStock != null) {
                    val updatedProduct = product.copy(
                        name = newName,
                        price = newPrice,
                        stock = newStock
                    )
                    (activity as? OnProductEditListener)?.onProductEdited(updatedProduct)
                } else {
                    Toast.makeText(requireContext(), "Datos inválidos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }

    interface OnProductEditListener {
        fun onProductEdited(product: Product)
    }

    companion object {
        fun newInstance(id: String, name: String, price: Double, stock: Int): EditProductDialogFragment {
            val args = Bundle().apply {
                putString("id", id)
                putString("name", name)
                putDouble("price", price)
                putInt("stock", stock)
            }
            return EditProductDialogFragment().apply {
                arguments = args
            }
        }
    }

}
