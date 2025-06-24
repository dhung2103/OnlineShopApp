package com.example.onlineshopapp.ui.auth

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.onlineshopapp.R
import com.example.onlineshopapp.databinding.FragmentProfileBinding
import com.example.onlineshopapp.utils.Constants

/**
 * ProfileFragment displays the user's profile information
 * and provides options for logout and editing profile
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var authViewModel: AuthViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel and SharedPreferences
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        sharedPreferences = requireActivity().getSharedPreferences(
            Constants.PREFS_NAME, 
            android.content.Context.MODE_PRIVATE
        )
        
        // Setup UI
        setupUI()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupUI() {
        // Display current user data if available
        val user = authViewModel.currentUser.value
        user?.let {
            binding.tvName.text = it.name
            binding.tvEmail.text = it.email
            binding.tvPhone.text = it.phone ?: getString(R.string.no_phone_available)
            binding.tvAddress.text = it.address ?: getString(R.string.no_address_available)
        }
    }

    private fun observeViewModel() {
        // Observe current user changes
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            binding.progressBar.visibility = View.GONE
            
            if (user != null) {
                binding.tvName.text = user.name
                binding.tvEmail.text = user.email
                binding.tvPhone.text = user.phone ?: getString(R.string.no_phone_available)
                binding.tvAddress.text = user.address ?: getString(R.string.no_address_available)
            }
        }
        
        // Observe login status changes
        authViewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (!isLoggedIn) {
                binding.progressBar.visibility = View.GONE
                findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            }
        }
    }

    private fun setupClickListeners() {
        // Logout button
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        
        // Edit profile button
        binding.btnEditProfile.setOnClickListener {
            // For now, we'll just show a toast message
            // In a real app, you would navigate to an edit profile screen
            Toast.makeText(requireContext(), "Edit Profile functionality not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.confirm_logout))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                binding.progressBar.visibility = View.VISIBLE
                authViewModel.logout()
            }
            .setNegativeButton(getString(R.string.no), null)
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
