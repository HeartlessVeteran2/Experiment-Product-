package com.heartless.experimentproduct.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.heartless.experimentproduct.presentation.ui.MapScreen
import com.heartless.experimentproduct.presentation.ui.theme.ExperimentProductTheme
import com.heartless.experimentproduct.presentation.viewmodel.MapViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for the application.
 * Uses Jetpack Compose for UI.
 * Demonstrates Hilt integration with @AndroidEntryPoint.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExperimentProductTheme {
                // ViewModel injection happens via hiltViewModel()
                val viewModel: MapViewModel = hiltViewModel()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapScreen()
                }
            }
        }
    }
}
