package com.heartless.experimentproduct.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service for accessing device location using FusedLocationProviderClient.
 * Handles location permission checks and provides single location fetch.
 */
@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Checks if location permissions are granted.
     * @return true if either fine or coarse location permission is granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Fetches current device location.
     * Requires location permissions to be granted before calling.
     * @return LocationResult with latitude and longitude on success, or null if location unavailable
     * @throws SecurityException if permissions are not granted
     */
    suspend fun getCurrentLocation(): LocationResult? {
        if (!hasLocationPermission()) {
            throw SecurityException("Location permissions not granted")
        }

        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }

            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(
                            LocationResult(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    } else {
                        continuation.resume(null)
                    }
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * Data class representing a location result.
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     */
    data class LocationResult(
        val latitude: Double,
        val longitude: Double
    )
}
