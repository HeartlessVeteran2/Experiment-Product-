package com.heartless.experimentproduct.domain.repository

import com.heartless.experimentproduct.domain.location.UserLocation
import com.heartless.experimentproduct.domain.model.Station
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for fetching nearby places that sell food and drinks.
 * Following Clean Architecture, this interface resides in the domain layer
 * and is implemented in the data layer.
 */
interface PlacesRepository {
    /**
     * Fetches nearby places within the specified radius.
     * Results are filtered to only include places that are currently open
     * and sorted by distance in ascending order.
     *
     * @param userLocation Current location of the user
     * @param radiusMeters Search radius in meters (hard capped at 1 mile = 1609 meters)
     * @return Flow emitting list of nearby stations, sorted by distance
     */
    fun getNearbyPlaces(
        userLocation: UserLocation,
        radiusMeters: Double = 1609.0 // 1 mile default
    ): Flow<List<Station>>
}
