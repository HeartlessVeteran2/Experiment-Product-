package com.heartless.experimentproduct.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.heartless.experimentproduct.presentation.ui.MapScreen
import com.heartless.experimentproduct.presentation.ui.theme.ExperimentProductTheme
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
