package com.example.onlineshopapp.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.onlineshopapp.R
import com.example.onlineshopapp.data.local.AppDatabase
import com.example.onlineshopapp.utils.Constants.SPLASH_DELAY

/**
 * SplashFragment - The entry point of the app
 * Shows a splash screen and checks for login status
 */
class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use a Handler to delay the navigation
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if user is already logged in
            checkLoginStatus()
        }, SPLASH_DELAY)
    }

    private fun checkLoginStatus() {
        // Check if there's a valid token stored in SharedPreferences
        val sharedPrefs = requireActivity().getSharedPreferences(
            "online_shop_prefs",
            android.content.Context.MODE_PRIVATE
        )
        val authToken = sharedPrefs.getString("auth_token", null)
        
        if (authToken != null) {
            // User is logged in, navigate to HomeFragment
            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
        } else {
            // User is not logged in, navigate to LoginFragment
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }
    }
}
