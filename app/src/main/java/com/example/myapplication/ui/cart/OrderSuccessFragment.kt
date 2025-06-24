package com.example.onlineshopapp.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.onlineshopapp.R
import com.example.onlineshopapp.databinding.FragmentOrderSuccessBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * OrderSuccessFragment - Shows confirmation of a successful order
 * Displays order details and provides navigation to continue shopping
 */
@AndroidEntryPoint
class OrderSuccessFragment : Fragment() {

    private var _binding: FragmentOrderSuccessBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CartViewModel by activityViewModels()
    
    // For the navigation argument issue, we'll use a simple approach without SafeArgs
    private var orderId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get orderId from arguments
        arguments?.let {
            orderId = it.getString("orderId") ?: ""
        }

        // Display order details
        setupOrderDetails()
        
        // Set up button click listeners
        setupClickListeners()
    }
    
    private fun setupOrderDetails() {
        // Use the order ID from arguments if available, otherwise generate one
        val orderNumber = if (orderId.isNotEmpty()) orderId else generateOrderNumber()
        
        // Format current date
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        // Get total amount from ViewModel
        val total = viewModel.getCartTotal()
        val formattedTotal = NumberFormat.getCurrencyInstance().format(total)
        
        // Get payment method (could be saved in ViewModel or SharedPreferences from checkout)
        val paymentMethod = getPaymentMethod()
        
        // Set values in the UI
        binding.tvOrderNumber.text = orderNumber
        binding.tvDate.text = currentDate
        binding.tvPaymentMethod.text = paymentMethod
        binding.tvTotal.text = formattedTotal
    }
    
    private fun getPaymentMethod(): String {
        // In a real app, this would come from the checkout flow
        // For this demo, we'll return a static value
        return getString(R.string.credit_card)
    }
    
    private fun setupClickListeners() {
        // Continue shopping button
        binding.btnContinueShopping.setOnClickListener {
            // Navigate back to home screen
            findNavController().navigate(R.id.action_orderSuccessFragment_to_homeFragment)
            
            // Clear the cart
            viewModel.clearCart()
        }
        
        // View orders button
        binding.btnViewOrders.setOnClickListener {
            // In a real app, this would navigate to an orders list screen
            Toast.makeText(requireContext(), "View orders feature not implemented", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun generateOrderNumber(): String {
        // Generate a random order number with prefix
        val randomPart = (10000..99999).random()
        val date = SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())
        return "OS-$date-$randomPart"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
