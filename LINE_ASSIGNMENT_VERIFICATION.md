# Line Category Assignment Verification

This document verifies the implementation of the five-line category assignment system.

## Line Definitions

| Line | Color | Hex | Covers |
|---|---|---|---|
| 🟢 Green | Green | `#4CAF50` | Restaurants, delis, food trucks |
| 🟠 Orange | Orange | `#FF9800` | Coffee shops, bakeries, juice bars |
| 🔴 Red | Red | `#F44336` | Gas stations |
| 🟣 Purple | Purple | `#9C27B0` | Pharmacies |
| ⚪ White | White | `#FFFFFF` | Convenience stores, grocery |

## Category Mappings

### Current StubPlacesRepository Categories

The following categories are used in the mock data generator:

| Category | Assigned Line | Color | Reason |
|----------|--------------|-------|--------|
| `restaurant` | Green | #4CAF50 | Primary food establishment |
| `cafe` | Orange | #FF9800 | Coffee shop |
| `bakery` | Orange | #FF9800 | Bakery |
| `gas_station` | Red | #F44336 | Gas station |
| `pharmacy` | Purple | #9C27B0 | Pharmacy |
| `convenience_store` | White | #FFFFFF | Convenience store |
| `grocery` | White | #FFFFFF | Grocery store |
| `food_truck` | Green | #4CAF50 | Mobile food establishment |

## Ambiguous Type Handling

The LineMapper implements a priority system for handling places with multiple categories:

1. **Priority 1: Gas Stations (Red Line)**
   - If a place is tagged as a gas station, it always goes to Red Line
   - Example: A gas station with a deli → Red Line

2. **Priority 2: Pharmacies (Purple Line)**
   - If a place is tagged as a pharmacy, it always goes to Purple Line
   - Example: A pharmacy with convenience items → Purple Line

3. **Priority 3: Coffee/Bakery/Juice (Orange Line)**
   - Coffee shops, bakeries, and juice bars go to Orange Line

4. **Priority 4: Convenience/Grocery (White Line)**
   - Convenience stores and grocery stores go to White Line

5. **Default: Restaurants/Food (Green Line)**
   - All other food establishments default to Green Line
   - Includes: restaurants, delis, food trucks, bars, etc.

## Implementation Details

### Line Enum
- Location: `app/src/main/java/com/heartless/experimentproduct/domain/model/Line.kt`
- Contains 5 enum values (GREEN, ORANGE, RED, PURPLE, WHITE)
- Each has a displayName and colorHex property

### LineMapper
- Location: `app/src/main/java/com/heartless/experimentproduct/domain/places/LineMapper.kt`
- Object class with `mapToLine()` function
- Handles single and multiple category assignments
- Implements priority-based assignment for ambiguous types

### StubPlacesRepository Integration
- Location: `app/src/main/java/com/heartless/experimentproduct/data/places/StubPlacesRepository.kt`
- Updated to use `LineMapper.mapToLine(category)` instead of hardcoded color map
- Every generated Station now has:
  - `line` field populated with the Line's displayName
  - `lineColor` field populated with the Line's colorHex

## Verification Checklist

- [x] Line enum created with all 5 lines and correct colors
- [x] LineMapper created with priority-based mapping logic
- [x] StubPlacesRepository updated to use LineMapper
- [x] All 8 categories in StubPlacesRepository have line assignments
- [x] Colors match the specification exactly
- [x] Ambiguous type handling implemented with priority rules
- [x] No category can result in an unassigned line (default is Green)

## Future Considerations

When integrating with real place APIs (Mapbox, Google Places):
- The LineMapper can handle multiple place types via the `allTypes` parameter
- Priority system ensures consistent assignment for ambiguous places
- Additional place types can be easily added to the type sets in LineMapper
- Default behavior (Green Line) ensures no place is left unassigned
