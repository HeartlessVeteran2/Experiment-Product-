package com.heartless.experimentproduct.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

/**
 * Composable function displaying a Mapbox map.
 * Demonstrates Jetpack Compose integration with Mapbox SDK.
 * Uses AndroidView for native view integration.
 */
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
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
        Text(
            text = "Mapbox Map Loaded",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
    }
}
