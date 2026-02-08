package com.heartless.experimentproduct.domain.location

import com.heartless.experimentproduct.data.location.LocationService
import javax.inject.Inject

/**
 * Use case for getting the user's current location.
 * Encapsulates the business logic for location fetching.
 * Returns a default city-center location as fallback if location is unavailable.
 */
class GetUserLocationUseCase @Inject constructor(
    private val locationService: LocationService
) {
    /**
     * City-center fallback location (San Francisco, CA).
     * Used when location permission is denied or location is unavailable.
     */
    private val defaultLocation = UserLocation(
        latitude = 37.7749,
        longitude = -122.4194
    )

    /**
     * Fetches the user's current location.
     * @return UserLocation with current coordinates, or default city-center if unavailable
     */
    suspend operator fun invoke(): UserLocation {
        return try {
            val result = locationService.getCurrentLocation()
            result?.let {
                UserLocation(
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            } ?: defaultLocation
        } catch (e: Exception) {
            // Includes SecurityException, IOException, or any other error
            defaultLocation
        }
    }
}
