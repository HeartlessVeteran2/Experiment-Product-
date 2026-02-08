package com.heartless.experimentproduct.data.places

import android.content.Context
import com.heartless.experimentproduct.R
import com.heartless.experimentproduct.domain.location.UserLocation
import com.heartless.experimentproduct.domain.model.Station
import com.heartless.experimentproduct.domain.repository.PlacesRepository
import com.mapbox.geojson.Point
import com.mapbox.search.CategorySearchEngine
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchOptions
import com.mapbox.search.category.CategorySearchOptions
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Implementation of PlacesRepository using Mapbox Search SDK.
 * Fetches nearby food and drink establishments within 1 mile radius,
 * filtering for places that are currently open.
 */
@Singleton
class MapboxPlacesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : PlacesRepository {

    /**
     * Supported food and drink categories as specified in the requirements.
     * Maps to Mapbox Search category identifiers.
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
     * Used for UI visualization.
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
     * Maximum search radius: 1 mile = 1609 meters (hard cap as per requirements).
     */
    private val MAX_RADIUS_METERS = 1609.0

    override fun getNearbyPlaces(
        userLocation: UserLocation,
        radiusMeters: Double
    ): Flow<List<Station>> = callbackFlow {
        // Hard cap at 1 mile as per requirements
        val cappedRadius = radiusMeters.coerceAtMost(MAX_RADIUS_METERS)

        try {
            // Get Mapbox access token from resources
            val accessToken = context.getString(R.string.mapbox_access_token)
            
            // Initialize search engine with access token
            val searchEngine = SearchEngine.createSearchEngine(accessToken)
            
            // Create search origin point
            val searchPoint = Point.fromLngLat(userLocation.longitude, userLocation.latitude)
            
            // Configure search options
            val searchOptions = SearchOptions.Builder()
                .origin(searchPoint)
                .limit(50) // Fetch up to 50 results
                .build()

            // Collect all results from multiple category searches
            val allStations = mutableListOf<Station>()
            var completedSearches = 0
            val totalSearches = foodDrinkCategories.size

            // Search each category
            foodDrinkCategories.forEach { category ->
                val categoryQuery = category.replace("_", " ")
                
                searchEngine.search(
                    categoryQuery,
                    searchOptions,
                    object : SearchCallback {
                        override fun onResults(
                            results: List<SearchResult>,
                            responseInfo: ResponseInfo
                        ) {
                            // Filter and map results
                            val stations = results.mapNotNull { result ->
                                try {
                                    mapResultToStation(result, userLocation, category, cappedRadius)
                                } catch (e: Exception) {
                                    null // Skip invalid results
                                }
                            }
                            
                            synchronized(allStations) {
                                allStations.addAll(stations)
                                completedSearches++
                                
                                // When all searches complete, emit results
                                if (completedSearches == totalSearches) {
                                    // Remove duplicates by ID, sort by distance
                                    val uniqueStations = allStations
                                        .distinctBy { it.id }
                                        .sortedBy { it.distance }
                                    
                                    trySend(uniqueStations)
                                    close()
                                }
                            }
                        }

                        override fun onError(e: Exception) {
                            synchronized(allStations) {
                                completedSearches++
                                
                                if (completedSearches == totalSearches) {
                                    // Still emit whatever we have
                                    val uniqueStations = allStations
                                        .distinctBy { it.id }
                                        .sortedBy { it.distance }
                                    
                                    trySend(uniqueStations)
                                    close()
                                }
                            }
                        }
                    }
                )
            }

            // Handle case where no categories exist
            if (foodDrinkCategories.isEmpty()) {
                trySend(emptyList())
                close()
            }

        } catch (e: Exception) {
            // On any initialization error, emit empty list
            trySend(emptyList())
            close(e)
        }

        awaitClose {
            // Cleanup if needed
        }
    }

    /**
     * Maps a Mapbox SearchResult to our Station domain model.
     * Filters out places outside the radius or that are closed.
     *
     * @return Station if valid and within criteria, null otherwise
     */
    private fun mapResultToStation(
        result: SearchResult,
        userLocation: UserLocation,
        category: String,
        radiusMeters: Double
    ): Station? {
        // Get coordinates
        val coordinates = result.coordinate ?: return null
        val resultLocation = UserLocation(
            latitude = coordinates.latitude(),
            longitude = coordinates.longitude()
        )

        // Calculate distance
        val distance = calculateDistance(userLocation, resultLocation)
        
        // Filter: must be within radius
        if (distance > radiusMeters) {
            return null
        }

        // Check if place is open (if metadata available)
        val metadata = result.metadata
        val openHours = metadata?.get("opening_hours") as? Map<*, *>
        val isOpen = openHours?.get("open_now") as? Boolean ?: true // Assume open if unknown
        
        // Filter: must be currently open as per requirements
        if (!isOpen) {
            return null
        }

        // Determine closing time and if closing soon
        val (closingTime, closingSoon) = calculateClosingInfo(openHours)

        // Get place name
        val name = result.name

        // Get or generate ID
        val id = result.id ?: "${name}_${coordinates.latitude()}_${coordinates.longitude()}"

        // Get category color
        val lineColor = categoryColors[category] ?: "#6C757D"

        return Station(
            id = id,
            name = name,
            distance = distance,
            line = category.replace("_", " ").replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            },
            lineColor = lineColor,
            openUntil = closingTime,
            closingSoon = closingSoon,
            location = resultLocation
        )
    }

    /**
     * Calculates distance between two points using Haversine formula.
     * Returns distance in meters.
     */
    private fun calculateDistance(from: UserLocation, to: UserLocation): Double {
        val earthRadiusMeters = 6371000.0
        
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val deltaLat = Math.toRadians(to.latitude - from.latitude)
        val deltaLon = Math.toRadians(to.longitude - from.longitude)

        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadiusMeters * c
    }

    /**
     * Extracts closing time information from opening hours metadata.
     * Returns pair of (closing time string, is closing within 1 hour).
     */
    private fun calculateClosingInfo(openHours: Map<*, *>?): Pair<String?, Boolean> {
        if (openHours == null) {
            return Pair(null, false)
        }

        try {
            // Try to parse closing time (format varies by Mapbox API response)
            val closingHourStr = openHours["close_time"] as? String
            if (closingHourStr != null) {
                // Check if closing within next hour
                val now = Calendar.getInstance()
                val closingTime = parseTime(closingHourStr, now)
                
                if (closingTime != null) {
                    val millisUntilClose = closingTime.timeInMillis - now.timeInMillis
                    val closingSoon = millisUntilClose in 0..3600000 // Within 1 hour
                    
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val closingTimeStr = timeFormat.format(closingTime.time)
                    
                    return Pair(closingTimeStr, closingSoon)
                }
            }
        } catch (e: Exception) {
            // If parsing fails, return defaults
        }

        return Pair(null, false)
    }

    /**
     * Parses time string and returns Calendar instance.
     * Handles common time formats.
     */
    private fun parseTime(timeStr: String, baseCalendar: Calendar): Calendar? {
        try {
            // Try HH:mm format
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull()
                val minute = parts[1].toIntOrNull()
                
                if (hour != null && minute != null) {
                    val calendar = baseCalendar.clone() as Calendar
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    return calendar
                }
            }
        } catch (e: Exception) {
            // Return null if parsing fails
        }
        return null
    }
}
