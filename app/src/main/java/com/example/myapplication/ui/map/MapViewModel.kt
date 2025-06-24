package com.example.onlineshopapp.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlineshopapp.data.model.StoreLocation
import com.example.onlineshopapp.data.repository.StoreRepository
import com.example.onlineshopapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for store location map screen
 */
@HiltViewModel
class MapViewModel @Inject constructor(private val storeRepository: StoreRepository) : ViewModel() {

    // LiveData for store location
    private val _storeLocation = MutableLiveData<Resource<StoreLocation>>()
    val storeLocation: LiveData<Resource<StoreLocation>> = _storeLocation
    
    init {
        // Load store location when ViewModel is created
        loadStoreLocation()
    }
    
    /**
     * Load store location data
     */
    fun loadStoreLocation() {
        _storeLocation.value = Resource.Loading()
        
        viewModelScope.launch {
            val result = storeRepository.getStoreLocation()
            _storeLocation.postValue(result)
        }
    }
}
