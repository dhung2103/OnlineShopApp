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

    // LiveData for store locations
    private val _storeLocations = MutableLiveData<Resource<List<StoreLocation>>>()
    val storeLocations: LiveData<Resource<List<StoreLocation>>> = _storeLocations
    
    init {
        // Load store locations when ViewModel is created
        loadStoreLocations()
    }
    
    /**
     * Load store locations data
     */
    fun loadStoreLocations() {
        _storeLocations.value = Resource.Loading()
        
        viewModelScope.launch {
            val result = storeRepository.getStoreLocations()
            _storeLocations.postValue(result)
        }
    }
}
