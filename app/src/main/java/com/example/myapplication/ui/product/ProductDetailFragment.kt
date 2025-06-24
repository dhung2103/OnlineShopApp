package com.example.onlineshopapp.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.onlineshopapp.R
import com.example.onlineshopapp.databinding.FragmentProductDetailBinding
import com.example.onlineshopapp.ui.cart.CartViewModel
import com.example.onlineshopapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val productViewModel: ProductViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    
    // Instead of using Safe Args, we'll get the product ID directly from arguments
    private var productId: Int = 0
    private var quantity = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get product ID from arguments
        arguments?.let {
            productId = it.getInt("productId", 0)
        }
        
        setupQuantityControls()
        setupButtonListeners()
        loadProductDetails()
        observeViewModel()
    }
    
    private fun setupQuantityControls() {
        binding.apply {
            tvQuantity.text = quantity.toString()
            
            btnIncrease.setOnClickListener {
                quantity++
                tvQuantity.text = quantity.toString()
                updateTotalPrice()
            }
            
            btnDecrease.setOnClickListener {
                if (quantity > 1) {
                    quantity--
                    tvQuantity.text = quantity.toString()
                    updateTotalPrice()
                }
            }
        }
    }
    
    private fun setupButtonListeners() {
        binding.fabAddToCart.setOnClickListener {
            productViewModel.productDetails.value?.data?.let { product ->
                cartViewModel.addToCart(product, quantity)
                showAddedToCartMessage()
            }
        }
    }
    
    private fun loadProductDetails() {
        productViewModel.loadProductById(productId)
    }
    
    private fun updateTotalPrice() {
        productViewModel.productDetails.value?.data?.let { product ->
            val totalPrice = product.price * quantity
            val format = NumberFormat.getCurrencyInstance()
            format.currency = Currency.getInstance("USD")
            // The layout doesn't have a tvTotalPrice view, so we'll use the product price TextView
            binding.tvProductPrice.text = format.format(totalPrice)
        }
    }
    
    private fun showAddedToCartMessage() {
        Toast.makeText(requireContext(), "Added to cart successfully", Toast.LENGTH_SHORT).show()
    }
    
    private fun observeViewModel() {
        productViewModel.productDetails.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading<*> -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success<*> -> {
                    binding.progressBar.visibility = View.GONE
                    
                    val product = result.data
                    if (product != null) {
                        binding.apply {
                            tvProductName.text = product.name
                            
                            val format = NumberFormat.getCurrencyInstance()
                            format.currency = Currency.getInstance("USD")
                            tvProductPrice.text = format.format(product.price)
                            
                            tvDescription.text = product.description
                            
                            // Load main image with Glide
                            Glide.with(requireContext())
                                .load(product.imageUrl)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_error)
                                .into(ivProduct)
                            
                            // Display rating if available
                            ratingBar.rating = product.rating
                            tvRatingCount.text = getString(R.string.reviews_count, product.reviewCount)
                            
                            // Initialize total price
                            updateTotalPrice()
                        }
                    }
                }
                is Resource.Error<*> -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message ?: "Error loading product", Toast.LENGTH_LONG).show()
                }
                else -> { /* No action needed */ }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
