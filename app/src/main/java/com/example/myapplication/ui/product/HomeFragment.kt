package com.example.onlineshopapp.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.onlineshopapp.R
import com.example.onlineshopapp.databinding.FragmentHomeBinding
import com.example.onlineshopapp.ui.cart.CartViewModel
import com.example.onlineshopapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        
        // Load products when fragment is created
        viewModel.loadProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onItemClick = { product ->
                // Navigate to product detail using direct navigation with bundle
                val bundle = Bundle().apply {
                    putInt("productId", product.id)
                }
                findNavController().navigate(R.id.action_homeFragment_to_productDetailFragment, bundle)
            },
            onAddToCartClick = { product ->
                // Log for debugging
                android.util.Log.d("HomeFragment", "Adding product ${product.id} to cart")
                try {
                    // Add product to cart
                    cartViewModel.addToCart(product)
                    Toast.makeText(requireContext(), getString(R.string.added_to_cart), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    android.util.Log.e("HomeFragment", "Error adding to cart", e)
                    Toast.makeText(
                        requireContext(),
                        "Error adding to cart: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        
        binding.rvProducts.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading<*> -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                }
                is Resource.Success<*> -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                    
                    val products = result.data as? List<*>
                    if (products.isNullOrEmpty()) {
                        // Show empty state message
                        binding.tvError.visibility = View.VISIBLE
                        binding.tvError.text = getString(R.string.no_products_available)
                    } else {
                        productAdapter.submitList(products as List<com.example.onlineshopapp.data.model.Product>)
                    }
                }
                is Resource.Error<*> -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = result.message ?: getString(R.string.error_loading_products)
                    Toast.makeText(requireContext(), result.message ?: getString(R.string.error_loading_products), Toast.LENGTH_LONG).show()
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
