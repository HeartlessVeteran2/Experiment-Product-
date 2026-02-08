package com.heartless.experimentproduct.data.places

import com.heartless.experimentproduct.domain.location.UserLocation
import com.heartless.experimentproduct.domain.model.Station
import com.heartless.experimentproduct.domain.places.LineMapper
import com.heartless.experimentproduct.domain.repository.PlacesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.min
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
 * - Filters for "open now" status (only generates future closing times)
 * - Sorts by distance ascending
 * - Returns proper Station models with all required fields
 */
@Singleton
class StubPlacesRepository @Inject constructor() : PlacesRepository {

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
                
                // Generate closing time (only future times to simulate "open now")
                val closingInfo = generateClosingInfo()
                
                // Only add if the place is open (closing time is in the future)
                if (closingInfo != null) {
                    val placeName = if (index < placeNames.size) {
                        placeNames[index]
                    } else {
                        "${placeNames[0]} ${index + 1}"
                    }
                    
                    // Use LineMapper to determine the line and color
                    val line = LineMapper.mapToLine(category)
                    
                    val station = Station(
                        id = "${category}_${placeName.replace(" ", "_").lowercase(Locale.ROOT)}_$index",
                        name = placeName,
                        distance = distance,
                        line = line.displayName,
                        lineColor = line.colorHex,
                        openUntil = closingInfo.first,
                        closingSoon = closingInfo.second,
                        location = location
                    )
                    
                    places.add(station)
                }
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
     * Generates mock closing time information for places that are currently open.
     * Returns (closing time string, is closing soon boolean) or null if the place would be closed.
     * Only generates future closing times to simulate "open now" filter.
     */
    private fun generateClosingInfo(): Pair<String?, Boolean>? {
        val now = Calendar.getInstance()
        
        // Calculate minimum closing time (at least 30 minutes from now)
        val minClosingCalendar = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            add(Calendar.MINUTE, 30)
        }
        
        val minClosingHour = minClosingCalendar.get(Calendar.HOUR_OF_DAY)
        val minClosingMinute = minClosingCalendar.get(Calendar.MINUTE)
        
        // Maximum closing time is 11:30 PM
        val maxClosingHour = 23
        val maxClosingMinute = 30
        
        // If minimum closing time is already past max, place would be closed
        if (minClosingHour > maxClosingHour || 
            (minClosingHour == maxClosingHour && minClosingMinute > maxClosingMinute)) {
            return null
        }
        
        // Generate a random closing time between min and max
        val closingHour = Random.nextInt(minClosingHour, maxClosingHour + 1)
        val closingMinute = if (closingHour == minClosingHour) {
            // If same hour as minimum, must be at or after minimum minute
            if (minClosingMinute <= 30) {
                if (Random.nextBoolean()) minClosingMinute else 30
            } else {
                // Round up to next hour
                return generateClosingInfoForHour(minClosingHour + 1, maxClosingHour, maxClosingMinute, now)
            }
        } else if (closingHour == maxClosingHour) {
            // If max hour, must not exceed max minute
            if (Random.nextBoolean()) 0 else min(30, maxClosingMinute)
        } else {
            // Middle hours can be 0 or 30
            if (Random.nextBoolean()) 0 else 30
        }
        
        // Calculate minutes until closing
        val closingCalendar = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            set(Calendar.HOUR_OF_DAY, closingHour)
            set(Calendar.MINUTE, closingMinute)
            set(Calendar.SECOND, 0)
        }
        
        val minutesUntilClose = ((closingCalendar.timeInMillis - now.timeInMillis) / (60 * 1000)).toInt()
        
        // Double-check we generated a valid future time
        if (minutesUntilClose <= 0) {
            return null
        }
        
        val closingSoon = minutesUntilClose in 1..60
        
        // Format closing time
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val closingTimeStr = timeFormat.format(closingCalendar.time)
        
        return Pair(closingTimeStr, closingSoon)
    }
    
    /**
     * Helper function to generate closing info for a specific hour range.
     */
    private fun generateClosingInfoForHour(
        hour: Int,
        maxHour: Int, 
        maxMinute: Int,
        now: Calendar
    ): Pair<String?, Boolean>? {
        if (hour > maxHour) return null
        
        val closingMinute = if (hour == maxHour) {
            if (Random.nextBoolean()) 0 else min(30, maxMinute)
        } else {
            if (Random.nextBoolean()) 0 else 30
        }
        
        val closingCalendar = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, closingMinute)
            set(Calendar.SECOND, 0)
        }
        
        val minutesUntilClose = ((closingCalendar.timeInMillis - now.timeInMillis) / (60 * 1000)).toInt()
        
        if (minutesUntilClose <= 0) {
            return null
        }
        
        val closingSoon = minutesUntilClose in 1..60
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val closingTimeStr = timeFormat.format(closingCalendar.time)
        
        return Pair(closingTimeStr, closingSoon)
    }
}
