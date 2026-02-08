package com.heartless.experimentproduct.domain.usecase

import com.heartless.experimentproduct.data.database.LocationPinEntity
import com.heartless.experimentproduct.data.repository.LocationPinRepository
import com.heartless.experimentproduct.domain.model.LocationPin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for getting all location pins.
 * Demonstrates Clean Architecture with domain layer.
 * Converts data entities to domain models.
 */
class GetLocationPinsUseCase @Inject constructor(
    private val repository: LocationPinRepository
) {
    operator fun invoke(): Flow<List<LocationPin>> {
        return repository.getAllPins().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    private fun LocationPinEntity.toDomainModel() = LocationPin(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        description = description,
        createdAt = createdAt
    )
}
