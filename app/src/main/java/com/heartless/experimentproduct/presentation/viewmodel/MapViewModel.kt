package com.heartless.experimentproduct.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartless.experimentproduct.domain.location.GetUserLocationUseCase
import com.heartless.experimentproduct.domain.location.UserLocation
import com.heartless.experimentproduct.domain.model.LocationPin
import com.heartless.experimentproduct.domain.usecase.GetLocationPinsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the main map screen.
 * Demonstrates Hilt injection and StateFlow for state management.
 * Uses Clean Architecture use case for data access.
 * Handles location permission and user location state.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    getLocationPinsUseCase: GetLocationPinsUseCase,
    private val getUserLocationUseCase: GetUserLocationUseCase
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
     */
    fun fetchUserLocation() {
        viewModelScope.launch {
            val location = getUserLocationUseCase()
            _userLocation.value = location
        }
    }

    /**
     * Called when location permission is granted.
     * Automatically fetches user location.
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
