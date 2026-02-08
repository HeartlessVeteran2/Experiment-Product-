package com.heartless.experimentproduct.domain.model

/**
 * Domain model for a location pin.
 * Clean domain entity without persistence annotations.
 */
data class LocationPin(
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
