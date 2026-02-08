package com.heartless.experimentproduct.data.repository

import com.heartless.experimentproduct.data.database.LocationPinDao
import com.heartless.experimentproduct.data.database.LocationPinEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for location pins.
 * Provides abstraction over data sources.
 * Injected by Hilt.
 */
@Singleton
class LocationPinRepository @Inject constructor(
    private val locationPinDao: LocationPinDao
) {
    fun getAllPins(): Flow<List<LocationPinEntity>> = locationPinDao.getAllPins()
    
    suspend fun getPinById(pinId: Long): LocationPinEntity? = 
        locationPinDao.getPinById(pinId)
    
    suspend fun savePin(pin: LocationPinEntity): Long = 
        locationPinDao.insertPin(pin)
    
    suspend fun deletePin(pinId: Long) = 
        locationPinDao.deletePin(pinId)
}
