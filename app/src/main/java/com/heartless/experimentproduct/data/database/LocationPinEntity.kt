package com.heartless.experimentproduct.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a saved location pin.
 * Part of Room database for local persistence.
 */
@Entity(tableName = "location_pins")
data class LocationPinEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
