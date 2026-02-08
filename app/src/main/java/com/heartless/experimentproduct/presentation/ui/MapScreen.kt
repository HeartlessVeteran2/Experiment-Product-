package com.heartless.experimentproduct.presentation.ui

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.heartless.experimentproduct.presentation.viewmodel.MapViewModel
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

/**
 * Composable function displaying a Mapbox map.
 * Demonstrates Jetpack Compose integration with Mapbox SDK.
 * Uses AndroidView for native view integration.
 * Handles location permissions with Accompanist permissions library.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var hasRequestedPermission by remember { mutableStateOf(false) }
    var lastPermissionState by remember { mutableStateOf<Boolean?>(null) }
    
    // Location permission state
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    // Collect ViewModel states
    val userLocation by viewModel.userLocation.collectAsState()
    val permissionDenied by viewModel.permissionDenied.collectAsState()
    
    // Show snackbar when permission is denied
    LaunchedEffect(permissionDenied) {
        if (permissionDenied) {
            snackbarHostState.showSnackbar(
                message = "Location permission denied. Using city-center fallback.",
                withDismissAction = true
            )
            viewModel.resetPermissionDenied()
        }
    }
    
    // Handle permission state changes
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        val currentState = locationPermissionsState.allPermissionsGranted
        
        when {
            // First composition - check if already granted or request
            lastPermissionState == null -> {
                if (currentState) {
                    viewModel.onPermissionGranted()
                    hasRequestedPermission = true
                } else {
                    locationPermissionsState.launchMultiplePermissionRequest()
                    hasRequestedPermission = true
                }
            }
            // Permission state changed after request
            hasRequestedPermission && currentState != lastPermissionState -> {
                if (currentState) {
                    viewModel.onPermissionGranted()
                } else {
                    viewModel.onPermissionDenied()
                }
            }
        }
        
        lastPermissionState = currentState
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        val mapView = remember {
            MapView(context).apply {
                getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
            }
        }
        
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
        
        DisposableEffect(Unit) {
            mapView.onStart()
            onDispose {
                mapView.onStop()
                mapView.onDestroy()
            }
        }
        
        // Info text overlay
        val locationText = remember(userLocation) {
            viewModel.getLocationDisplayText()
        }
        
        Text(
            text = locationText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
        
        // Snackbar host for showing permission denied message
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
