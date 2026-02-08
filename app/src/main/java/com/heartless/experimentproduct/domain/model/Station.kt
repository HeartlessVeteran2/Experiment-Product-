package com.heartless.experimentproduct.domain.model

import com.heartless.experimentproduct.domain.location.UserLocation

/**
 * Domain model representing a place/station that sells food or drinks.
 * 
 * @param id Unique identifier for the station
 * @param name Name of the establishment
 * @param distance Distance from user's location in meters
 * @param line Line assignment for this station (e.g., "Green", "Orange", "Red", "Purple", "White")
 * @param lineColor Color hex code for UI visualization of the line
 * @param openUntil Closing time as formatted string (e.g., "9:00 PM"), null if not available
 * @param closingSoon True if the place is closing within 1 hour
 * @param location Coordinates of the station
 */
data class Station(
    val id: String,
    val name: String,
    val distance: Double,
    val line: String,
    val lineColor: String,
    val openUntil: String?,
    val closingSoon: Boolean,
    val location: UserLocation
)
