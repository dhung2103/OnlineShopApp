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
import com.example.onlineshopapp.data.model.CartItem
import com.example.onlineshopapp.databinding.FragmentCheckoutBinding
import com.example.onlineshopapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CartViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupPlaceOrderButton()
        
        // Load cart items for checkout
        // No need to call a separate method as we're observing LiveData directly
    }

    private fun setupPlaceOrderButton() {
        binding.btnPlaceOrder.setOnClickListener {
            if (validateForm()) {
                // Create the shipping address string from form fields
                val shippingAddress = buildShippingAddressString()
                viewModel.checkout(shippingAddress)
            }
        }
    }
    
    private fun buildShippingAddressString(): String {
        val name = binding.etName.text.toString()
        val address = binding.etAddress.text.toString()
        val city = binding.etCity.text.toString()
        val state = binding.etState?.text.toString() ?: ""
        val zipCode = binding.etZip.text.toString()
        
        return "$name\n$address\n$city, $state $zipCode"
    }
    
    private fun getSelectedPaymentMethod(): String {
        return when {
            binding.rbCreditCard.isChecked -> "Credit Card"
            binding.rbPaypal.isChecked -> "PayPal"
            binding.rbCash.isChecked -> "Cash on Delivery"
            else -> "Unknown"
        }
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        binding.apply {
            // Check name
            if (etName.text.toString().trim().isEmpty()) {
                etName.error = "Name is required"
                isValid = false
            }
            
            // Check address
            if (etAddress.text.toString().trim().isEmpty()) {
                etAddress.error = "Address is required"
                isValid = false
            }
            
            // Check city
            if (etCity.text.toString().trim().isEmpty()) {
                etCity.error = "City is required"
                isValid = false
            }
            
            // Check zip code
            if (etZip.text.toString().trim().isEmpty()) {
                etZip.error = "Postal/ZIP code is required"
                isValid = false
            }
            
            // Check phone
            if (etPhone.text.toString().trim().isEmpty()) {
                etPhone.error = "Phone number is required"
                isValid = false
            }
            
            // Check if payment method is selected
            if (!rbCreditCard.isChecked && !rbPaypal.isChecked && !rbCash.isChecked) {
                Toast.makeText(requireContext(), "Please select a payment method", Toast.LENGTH_SHORT).show()
                isValid = false
            }
        }
        
        return isValid
    }

    private fun observeViewModel() {
        viewModel.cartItems.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading<*> -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success<*> -> {
                    binding.progressBar.visibility = View.GONE
                    
                    val cartItems = result.data as? List<CartItem>
                    if (cartItems != null && cartItems.isEmpty()) {
                        Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else if (cartItems != null) {
                        // Calculate and display subtotal, shipping, and total
                        val subtotal = cartItems.sumOf { it.productPrice * it.quantity }
                        val shipping = 5.99 // Fixed shipping cost
                        val total = subtotal + shipping
                        
                        updatePriceSummary(subtotal, shipping, total)
                    }
                }
                is Resource.Error<*> -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message ?: "Error loading cart", Toast.LENGTH_LONG).show()
                }
                else -> { /* No action needed */ }
            }
        }
        
        viewModel.checkoutStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading<*> -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnPlaceOrder.isEnabled = false
                }
                is Resource.Success<*> -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnPlaceOrder.isEnabled = true
                    findNavController().navigate(R.id.action_checkoutFragment_to_orderSuccessFragment)
                }
                is Resource.Error<*> -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnPlaceOrder.isEnabled = true
                    Toast.makeText(requireContext(), result.message ?: "Error processing order", Toast.LENGTH_LONG).show()
                }
                else -> { /* No action needed */ }
            }
        }
    }
    
    private fun updatePriceSummary(subtotal: Double, shipping: Double, total: Double) {
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance("USD")
        
        binding.apply {
            tvSubtotal.text = format.format(subtotal)
            tvShipping.text = format.format(shipping)
            tvTotal.text = format.format(total)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
