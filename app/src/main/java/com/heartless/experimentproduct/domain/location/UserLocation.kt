package com.heartless.experimentproduct.domain.location

/**
 * Domain model representing a user's location.
 * Pure Kotlin data class with no Android dependencies.
 *
 * @param latitude The latitude coordinate
 * @param longitude The longitude coordinate
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double
)
