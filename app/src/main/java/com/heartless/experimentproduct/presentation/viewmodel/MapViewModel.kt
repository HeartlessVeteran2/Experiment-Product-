package com.heartless.experimentproduct.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartless.experimentproduct.domain.model.LocationPin
import com.heartless.experimentproduct.domain.usecase.GetLocationPinsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for the main map screen.
 * Demonstrates Hilt injection and StateFlow for state management.
 * Uses Clean Architecture use case for data access.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    getLocationPinsUseCase: GetLocationPinsUseCase
) : ViewModel() {
    
    val locationPins: StateFlow<List<LocationPin>> = getLocationPinsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
