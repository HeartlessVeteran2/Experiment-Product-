package com.heartless.experimentproduct.domain.model

/**
 * Enum representing the five lines for categorizing food and drink establishments.
 * Each line has a distinct color and covers specific types of places.
 */
enum class Line(
    val displayName: String,
    val colorHex: String
) {
    /**
     * Green Line: Restaurants, delis, food trucks
     */
    GREEN("Green", "#4CAF50"),
    
    /**
     * Orange Line: Coffee shops, bakeries, juice bars
     */
    ORANGE("Orange", "#FF9800"),
    
    /**
     * Red Line: Gas stations
     */
    RED("Red", "#F44336"),
    
    /**
     * Purple Line: Pharmacies
     */
    PURPLE("Purple", "#9C27B0"),
    
    /**
     * White Line: Convenience stores, grocery
     */
    WHITE("White", "#FFFFFF")
}
