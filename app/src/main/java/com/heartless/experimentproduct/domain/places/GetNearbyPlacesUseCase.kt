package com.heartless.experimentproduct.domain.places

import com.heartless.experimentproduct.domain.location.UserLocation
import com.heartless.experimentproduct.domain.model.Station
import com.heartless.experimentproduct.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching nearby places that sell food and drinks.
 * Encapsulates the business logic for place searching with the 1-mile radius constraint.
 */
class GetNearbyPlacesUseCase @Inject constructor(
    private val placesRepository: PlacesRepository
) {
    /**
     * Fetches places within 1 mile of the user's location.
     * Results are filtered to only include currently open places,
     * sorted by distance in ascending order.
     *
     * @param userLocation Current location of the user
     * @return Flow emitting list of nearby stations
     */
    operator fun invoke(userLocation: UserLocation): Flow<List<Station>> {
        return placesRepository.getNearbyPlaces(
            userLocation = userLocation,
            radiusMeters = ONE_MILE_IN_METERS
        )
    }

    companion object {
        /**
         * 1 mile in meters (hard cap as per requirements).
         */
        private const val ONE_MILE_IN_METERS = 1609.0
    }
}
