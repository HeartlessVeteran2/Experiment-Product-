package com.heartless.experimentproduct.domain.places

import com.heartless.experimentproduct.domain.model.Line
import java.util.Locale

/**
 * Utility for mapping place types/categories to Lines.
 * 
 * Handles the mapping logic for assigning establishments to one of the five lines
 * based on their category or type tags from the places API.
 * 
 * **Line Assignment Rules:**
 * - Green Line: Restaurants, delis, food trucks
 * - Orange Line: Coffee shops, bakeries, juice bars  
 * - Red Line: Gas stations (always Red, even if they have food)
 * - Purple Line: Pharmacies
 * - White Line: Convenience stores, grocery stores
 * 
 * **Priority Rules for Ambiguous Types:**
 * - Gas stations with food services → Red Line (gas station takes precedence)
 * - Pharmacies with convenience items → Purple Line (pharmacy takes precedence)
 */
object LineMapper {
    
    /**
     * Maps a place type/category string to its corresponding Line.
     * 
     * @param placeType The type or category of the place (e.g., "restaurant", "cafe", "gas_station")
     * @param allTypes Optional list of all types if place has multiple categories (for handling ambiguous cases)
     * @return The Line this place belongs to
     */
    fun mapToLine(placeType: String, allTypes: List<String> = emptyList()): Line {
        val normalizedType = placeType.lowercase(Locale.ROOT).replace(" ", "_")
        val allNormalizedTypes = allTypes.map { it.lowercase(Locale.ROOT).replace(" ", "_") }
        
        // Priority 1: Gas stations always go to Red Line (even if they have food)
        if (matchesAny(normalizedType, allNormalizedTypes, redLineTypes)) {
            return Line.RED
        }
        
        // Priority 2: Pharmacies always go to Purple Line
        if (matchesAny(normalizedType, allNormalizedTypes, purpleLineTypes)) {
            return Line.PURPLE
        }
        
        // Priority 3: Coffee/bakery/juice establishments go to Orange Line
        if (matchesAny(normalizedType, allNormalizedTypes, orangeLineTypes)) {
            return Line.ORANGE
        }
        
        // Priority 4: Convenience stores and grocery go to White Line
        if (matchesAny(normalizedType, allNormalizedTypes, whiteLineTypes)) {
            return Line.WHITE
        }
        
        // Default: Restaurants, delis, food trucks, and other food places go to Green Line
        return Line.GREEN
    }
    
    /**
     * Helper function to check if a type or any of its additional types match a given type set.
     */
    private fun matchesAny(
        normalizedType: String,
        allNormalizedTypes: List<String>,
        typeSet: Set<String>
    ): Boolean {
        return normalizedType in typeSet || allNormalizedTypes.any { it in typeSet }
    }
    
    /**
     * Orange Line types: Coffee shops, bakeries, juice bars
     */
    private val orangeLineTypes = setOf(
        "cafe",
        "coffee",
        "coffee_shop",
        "bakery",
        "juice_bar",
        "juice",
        "tea_house",
        "dessert"
    )
    
    /**
     * Red Line types: Gas stations
     */
    private val redLineTypes = setOf(
        "gas_station",
        "gas",
        "fuel",
        "petrol_station",
        "service_station"
    )
    
    /**
     * Purple Line types: Pharmacies
     */
    private val purpleLineTypes = setOf(
        "pharmacy",
        "drugstore",
        "chemist"
    )
    
    /**
     * White Line types: Convenience stores, grocery stores
     */
    private val whiteLineTypes = setOf(
        "convenience_store",
        "grocery",
        "grocery_store",
        "supermarket",
        "market",
        "convenience"
    )
}
