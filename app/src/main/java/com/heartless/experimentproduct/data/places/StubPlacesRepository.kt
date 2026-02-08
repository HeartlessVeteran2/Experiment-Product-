package com.heartless.experimentproduct.data.places

import android.content.Context
import com.heartless.experimentproduct.domain.location.UserLocation
import com.heartless.experimentproduct.domain.model.Station
import com.heartless.experimentproduct.domain.repository.PlacesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Stub implementation of PlacesRepository.
 * 
 * This is a placeholder implementation that returns mock data for development and testing.
 * 
 * **Production Implementation TODO:**
 * - Replace with Mapbox Search SDK integration when valid credentials are configured
 * - Alternative: Implement Google Places API as fallback
 * - Requires: Valid Mapbox access token in strings.xml (mapbox_access_token)
 * - Dependency: com.mapbox.search:mapbox-search-android:2.15.0
 * 
 * **Current Behavior:**
 * - Generates realistic mock places around user location
 * - Simulates the 1-mile radius constraint
 * - Filters for "open now" status
 * - Sorts by distance ascending
 * - Returns proper Station models with all required fields
 */
@Singleton
class StubPlacesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : PlacesRepository {

    /**
     * Supported food and drink categories as specified in the requirements.
     */
    private val foodDrinkCategories = listOf(
        "restaurant",
        "cafe",
        "bakery",
        "gas_station",
        "pharmacy",
        "convenience_store",
        "grocery",
        "food_truck"
    )

    /**
     * Color mapping for different establishment categories.
     */
    private val categoryColors = mapOf(
        "restaurant" to "#E63946",
        "cafe" to "#F77F00",
        "bakery" to "#FCBF49",
        "gas_station" to "#06AED5",
        "pharmacy" to "#073B4C",
        "convenience_store" to "#118AB2",
        "grocery" to "#06D6A0",
        "food_truck" to "#EF476F"
    )

    /**
     * Sample place names for mock data generation.
     */
    private val samplePlaceNames = mapOf(
        "restaurant" to listOf("Bistro Central", "The Good Fork", "Spice Kitchen", "Harbor Grill"),
        "cafe" to listOf("Blue Bottle Coffee", "Artisan Cafe", "Morning Brew", "Espresso Bar"),
        "bakery" to listOf("Golden Crust", "Sweet Treats", "Flour & Water", "Daily Bread"),
        "gas_station" to listOf("Shell Station", "Chevron", "76 Gas", "Arco"),
        "pharmacy" to listOf("CVS Pharmacy", "Walgreens", "Rite Aid", "Local Pharmacy"),
        "convenience_store" to listOf("7-Eleven", "Circle K", "Quick Stop", "Corner Store"),
        "grocery" to listOf("Whole Foods", "Safeway", "Trader Joe's", "Local Market"),
        "food_truck" to listOf("Taco Truck", "BBQ Express", "Noodle Cart", "Burger Bus")
    )

    /**
     * Maximum search radius: 1 mile = 1609 meters.
     */
    private val MAX_RADIUS_METERS = 1609.0

    override fun getNearbyPlaces(
        userLocation: UserLocation,
        radiusMeters: Double
    ): Flow<List<Station>> = flow {
        // Simulate network delay
        delay(500)

        // Hard cap at 1 mile
        val cappedRadius = radiusMeters.coerceAtMost(MAX_RADIUS_METERS)

        // Generate mock places
        val places = mutableListOf<Station>()
        
        foodDrinkCategories.forEach { category ->
            val placeNames = samplePlaceNames[category] ?: listOf("Sample Place")
            val numPlaces = Random.nextInt(2, 5) // 2-4 places per category
            
            repeat(numPlaces) { index ->
                // Generate random location within radius
                val distance = Random.nextDouble(50.0, cappedRadius)
                val angle = Random.nextDouble(0.0, 2 * Math.PI)
                
                val location = generateNearbyLocation(userLocation, distance, angle)
                
                // Generate closing time (simulating places open now)
                val closingInfo = generateClosingInfo()
                
                val placeName = if (index < placeNames.size) {
                    placeNames[index]
                } else {
                    "${placeNames[0]} ${index + 1}"
                }
                
                val station = Station(
                    id = "${category}_${placeName.replace(" ", "_").lowercase()}_$index",
                    name = placeName,
                    distance = distance,
                    line = category.replace("_", " ").replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                    },
                    lineColor = categoryColors[category] ?: "#6C757D",
                    openUntil = closingInfo.first,
                    closingSoon = closingInfo.second,
                    location = location
                )
                
                places.add(station)
            }
        }

        // Sort by distance and emit
        val sortedPlaces = places.sortedBy { it.distance }
        emit(sortedPlaces)
    }

    /**
     * Generates a nearby location given distance and angle from origin.
     * Uses simple approximation for small distances.
     */
    private fun generateNearbyLocation(
        origin: UserLocation,
        distanceMeters: Double,
        angleRadians: Double
    ): UserLocation {
        // Earth radius in meters
        val earthRadius = 6371000.0
        
        // Convert distance to radians
        val distanceRad = distanceMeters / earthRadius
        
        // Calculate offset in lat/lon
        val latOffset = distanceRad * cos(angleRadians)
        val lonOffset = distanceRad * sin(angleRadians) / cos(Math.toRadians(origin.latitude))
        
        return UserLocation(
            latitude = origin.latitude + Math.toDegrees(latOffset),
            longitude = origin.longitude + Math.toDegrees(lonOffset)
        )
    }

    /**
     * Generates mock closing time information.
     * Returns (closing time string, is closing soon boolean).
     */
    private fun generateClosingInfo(): Pair<String?, Boolean> {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        
        // Most places close between 8 PM and 11 PM
        val closingHour = Random.nextInt(20, 24)
        val closingMinute = if (Random.nextBoolean()) 0 else 30
        
        // Check if closing within 1 hour
        val minutesUntilClose = (closingHour - currentHour) * 60 + (closingMinute - now.get(Calendar.MINUTE))
        val closingSoon = minutesUntilClose in 0..60
        
        // Format closing time
        val closingCalendar = now.clone() as Calendar
        closingCalendar.set(Calendar.HOUR_OF_DAY, closingHour)
        closingCalendar.set(Calendar.MINUTE, closingMinute)
        
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val closingTimeStr = timeFormat.format(closingCalendar.time)
        
        return Pair(closingTimeStr, closingSoon)
    }
}
