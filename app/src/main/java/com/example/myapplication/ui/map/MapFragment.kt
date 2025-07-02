package com.example.onlineshopapp.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.onlineshopapp.R
import com.example.onlineshopapp.data.model.StoreLocation
import com.example.onlineshopapp.databinding.FragmentMapBinding
import com.example.onlineshopapp.utils.Resource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MapViewModel by viewModels()
    private var googleMap: GoogleMap? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission is required to show your location on the map", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        observeViewModel()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        requestLocationPermission()
        viewModel.loadStoreLocations()
    }
    
    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted, enable location
                enableMyLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show an explanation to the user why we need the permission
                Toast.makeText(
                    requireContext(),
                    "Location permission is needed to show your location on the map",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                // No explanation needed, request the permission
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    
    private fun enableMyLocation() {
        try {
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        } catch (e: SecurityException) {
            // Handle permission denial
        }
    }
    
    private fun updateStoreInfoCard(storeLocation: StoreLocation) {
        binding.tvStoreName.text = storeLocation.name
        binding.tvStoreAddress.text = storeLocation.address
        binding.tvStoreHours.text = storeLocation.openingHours
        
        // Set up button click listeners
        binding.btnCallStore.setOnClickListener {
            // TODO: Implement phone call functionality
            Toast.makeText(requireContext(), "Calling ${storeLocation.phoneNumber}", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnDirections.setOnClickListener {
            // TODO: Implement directions functionality
            Toast.makeText(requireContext(), "Getting directions to ${storeLocation.name}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeViewModel() {
        viewModel.storeLocations.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading<*> -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success<*> -> {
                    binding.progressBar.visibility = View.GONE
                    
                    val locations = result.data
                    if (locations != null && locations.isNotEmpty()) {
                        // Add markers for all store locations
                        locations.forEach { location ->
                            val storePosition = LatLng(location.latitude, location.longitude)
                            googleMap?.addMarker(
                                MarkerOptions()
                                    .position(storePosition)
                                    .title(location.name)
                                    .snippet(location.address)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            )
                        }
                        
                        // Move camera to the first store location
                        val firstLocation = locations[0]
                        val firstStorePosition = LatLng(firstLocation.latitude, firstLocation.longitude)
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(firstStorePosition, 12f))
                        
                        // Update store info card with first location
                        updateStoreInfoCard(firstLocation)
                    }
                }
                is Resource.Error<*> -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message ?: "Error loading store location", Toast.LENGTH_LONG).show()
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
