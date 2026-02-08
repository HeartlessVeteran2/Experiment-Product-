package com.heartless.experimentproduct.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for LocationPin operations.
 * Exposes Flow for reactive data observation.
 */
@Dao
interface LocationPinDao {
    @Query("SELECT * FROM location_pins ORDER BY createdAt DESC")
    fun getAllPins(): Flow<List<LocationPinEntity>>
    
    @Query("SELECT * FROM location_pins WHERE id = :pinId")
    suspend fun getPinById(pinId: Long): LocationPinEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPin(pin: LocationPinEntity): Long
    
    @Query("DELETE FROM location_pins WHERE id = :pinId")
    suspend fun deletePin(pinId: Long)
}
