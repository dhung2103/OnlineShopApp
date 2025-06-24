package com.example.onlineshopapp.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineshopapp.R
import com.example.onlineshopapp.databinding.FragmentCartBinding
import com.example.onlineshopapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupCheckoutButton()
          // Cart items will be automatically loaded from LiveData
    }    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onItemClick = { /* No action needed for item click */ },
            onIncreaseClick = { cartItem ->
                viewModel.increaseQuantity(cartItem.productId)
            },
            onDecreaseClick = { cartItem ->
                if (cartItem.quantity > 1) {
                    viewModel.decreaseQuantity(cartItem.productId)
                }
            },
            onRemoveClick = { cartItem ->
                viewModel.removeFromCart(cartItem.productId)
            }
        )
        
        binding.rvCartItems.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener {
            if (cartAdapter.currentList.isNotEmpty()) {
                findNavController().navigate(R.id.action_cartFragment_to_checkoutFragment)
            } else {
                Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }    private fun observeViewModel() {
        viewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            binding.progressBar.visibility = View.GONE
            
            if (cartItems.isEmpty()) {
                binding.emptyCartContainer.visibility = View.VISIBLE
                binding.checkoutPanel.visibility = View.GONE
            } else {
                binding.emptyCartContainer.visibility = View.GONE
                binding.checkoutPanel.visibility = View.VISIBLE
                cartAdapter.submitList(cartItems)
                
                // Calculate and display total price
                val totalPrice = cartItems.sumOf { it.productPrice * it.quantity }
                updateTotalPrice(totalPrice)
            }
        }
    }
      private fun updateTotalPrice(total: Double) {
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance("USD")
        binding.tvTotal.text = format.format(total)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
