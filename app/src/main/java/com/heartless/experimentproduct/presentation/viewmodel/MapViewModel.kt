package com.heartless.experimentproduct.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartless.experimentproduct.domain.location.GetUserLocationUseCase
import com.heartless.experimentproduct.domain.location.UserLocation
import com.heartless.experimentproduct.domain.model.LocationPin
import com.heartless.experimentproduct.domain.model.Station
import com.heartless.experimentproduct.domain.places.GetNearbyPlacesUseCase
import com.heartless.experimentproduct.domain.usecase.GetLocationPinsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the main map screen.
 * Demonstrates Hilt injection and StateFlow for state management.
 * Uses Clean Architecture use cases for data access.
 * Handles location permission, user location state, and nearby places.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    getLocationPinsUseCase: GetLocationPinsUseCase,
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val getNearbyPlacesUseCase: GetNearbyPlacesUseCase
) : ViewModel() {
    
    val locationPins: StateFlow<List<LocationPin>> = getLocationPinsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _userLocation = MutableStateFlow<UserLocation?>(null)
    val userLocation: StateFlow<UserLocation?> = _userLocation.asStateFlow()

    private val _permissionDenied = MutableStateFlow(false)
    val permissionDenied: StateFlow<Boolean> = _permissionDenied.asStateFlow()

    private val _nearbyPlaces = MutableStateFlow<List<Station>>(emptyList())
    val nearbyPlaces: StateFlow<List<Station>> = _nearbyPlaces.asStateFlow()

    private val _isLoadingPlaces = MutableStateFlow(false)
    val isLoadingPlaces: StateFlow<Boolean> = _isLoadingPlaces.asStateFlow()

    // Track the current places fetch job to cancel on new requests
    private var fetchPlacesJob: Job? = null

    /**
     * Formats user location for display.
     * @return Formatted location string or loading message
     */
    fun getLocationDisplayText(): String {
        return _userLocation.value?.let {
            "Location: ${String.format("%.4f", it.latitude)}, ${String.format("%.4f", it.longitude)}"
        } ?: "Fetching location..."
    }

    /**
     * Fetches the user's current location.
     * Uses city-center fallback if permission is denied or location is unavailable.
     * Automatically fetches nearby places after location is obtained.
     */
    fun fetchUserLocation() {
        viewModelScope.launch {
            val location = getUserLocationUseCase()
            _userLocation.value = location
            
            // Automatically fetch nearby places when location is available
            fetchNearbyPlaces()
        }
    }

    /**
     * Fetches nearby places within 1 mile of the user's current location.
     * Results are filtered to only show places that are currently open,
     * sorted by distance in ascending order.
     * Cancels any previous fetch operation to prevent race conditions.
     */
    fun fetchNearbyPlaces() {
        val location = _userLocation.value ?: return
        
        // Cancel any previous fetch operation
        fetchPlacesJob?.cancel()
        
        fetchPlacesJob = viewModelScope.launch {
            getNearbyPlacesUseCase(location)
                .onStart {
                    _isLoadingPlaces.value = true
                }
                .onCompletion {
                    _isLoadingPlaces.value = false
                }
                .catch { _ ->
                    // On error, clear places
                    _nearbyPlaces.value = emptyList()
                }
                .collect { places ->
                    _nearbyPlaces.value = places
                }
        }
    }

    /**
     * Called when location permission is granted.
     * Automatically fetches user location and nearby places.
     */
    fun onPermissionGranted() {
        _permissionDenied.value = false
        fetchUserLocation()
    }

    /**
     * Called when location permission is denied.
     * Sets flag to show snackbar and uses city-center fallback.
     */
    fun onPermissionDenied() {
        _permissionDenied.value = true
        fetchUserLocation() // Will use default city-center location
    }

    /**
     * Resets the permission denied flag after showing snackbar.
     */
    fun resetPermissionDenied() {
        _permissionDenied.value = false
    }
}
