# Places Search Implementation Guide

## Overview

This document describes the implementation of the Places Search feature (Issue #3) which fetches food and drink sources within 1 mile of the user's location.

## Current Status

✅ **Infrastructure Complete:**
- Domain models and interfaces implemented
- Clean Architecture pattern followed
- Use cases properly structured
- ViewModel integration complete
- Dependency injection configured

⚠️ **Using Stub Implementation:**
- Currently using `StubPlacesRepository` with mock data
- Real Mapbox Search SDK ready to integrate when credentials are available

## Architecture

### Domain Layer
```
domain/
├── model/
│   └── Station.kt                 # Domain model for places
├── repository/
│   └── PlacesRepository.kt        # Repository interface
└── places/
    └── GetNearbyPlacesUseCase.kt  # Use case for fetching places
```

### Data Layer
```
data/
└── places/
    └── StubPlacesRepository.kt    # Current stub implementation
    └── MapboxPlacesRepository.kt  # TODO: Real implementation
```

### Presentation Layer
```
presentation/
└── viewmodel/
    └── MapViewModel.kt            # Exposes nearbyPlaces StateFlow
```

## Station Model

The `Station` domain model represents a place that sells food or drinks:

```kotlin
data class Station(
    val id: String,                  // Unique identifier
    val name: String,                // Place name
    val distance: Double,            // Distance in meters
    val line: String,                // Category (e.g., "Restaurant", "Cafe")
    val lineColor: String,           // Hex color for UI (#RRGGBB)
    val openUntil: String?,          // Closing time (e.g., "9:00 PM")
    val closingSoon: Boolean,        // True if closing within 1 hour
    val location: UserLocation       // Coordinates
)
```

## Categories Supported

The following food and drink categories are included:

1. **Restaurant** - `#E63946` (red)
2. **Cafe** - `#F77F00` (orange)
3. **Bakery** - `#FCBF49` (yellow)
4. **Gas Station** - `#06AED5` (blue)
5. **Pharmacy** - `#073B4C` (dark blue)
6. **Convenience Store** - `#118AB2` (teal)
7. **Grocery** - `#06D6A0` (green)
8. **Food Truck** - `#EF476F` (pink)

## Requirements Implemented

✅ **1-Mile Radius:** Hard cap at 1609 meters  
✅ **Open Now Filter:** Stub generates only currently open places; production will use API filter  
✅ **Distance Sort:** Results sorted by distance (ascending)  
✅ **Multiple Categories:** All 8 required categories supported  
✅ **StateFlow Exposure:** Reactive data flow with StateFlow  
✅ **Clean Architecture:** Domain, Data, Presentation separation

## Using the Stub Implementation

The current `StubPlacesRepository` generates realistic mock data:

- **Locations:** Randomly distributed within 1 mile
- **Categories:** 2-4 places per category
- **Distances:** Randomized but within radius
- **Closing Times:** Realistic hours with "closing soon" detection
- **Sorting:** Properly sorted by distance

### Usage in Code

```kotlin
// In ViewModel
val nearbyPlaces: StateFlow<List<Station>> = _nearbyPlaces.asStateFlow()
val isLoadingPlaces: StateFlow<Boolean> = _isLoadingPlaces.asStateFlow()

// Fetch places automatically after location is obtained
fun fetchUserLocation() {
    viewModelScope.launch {
        val location = getUserLocationUseCase()
        _userLocation.value = location
        fetchNearbyPlaces() // Automatically fetch places
    }
}
```

### In Compose UI

```kotlin
val places by viewModel.nearbyPlaces.collectAsState()
val isLoading by viewModel.isLoadingPlaces.collectAsState()

LazyColumn {
    items(places) { station ->
        PlaceItem(station = station)
    }
}
```

## Upgrading to Mapbox Search SDK

### Prerequisites

1. **Mapbox Account:** Create account at https://account.mapbox.com/
2. **Secret Token:** Generate token with `Downloads:Read` scope
3. **Public Token:** Create public access token for runtime

### Step 1: Configure Tokens

**In `gradle.properties`:**
```properties
MAPBOX_DOWNLOADS_TOKEN=sk.YOUR_SECRET_TOKEN_HERE
```

**In `app/src/main/res/values/strings.xml`:**
```xml
<string name="mapbox_access_token">pk.YOUR_PUBLIC_TOKEN_HERE</string>
```

### Step 2: Add Dependency

**In `app/build.gradle.kts`:**
```kotlin
dependencies {
    // Uncomment this line:
    implementation("com.mapbox.search:mapbox-search-android:2.15.0")
}
```

### Step 3: Create MapboxPlacesRepository

Create `app/src/main/java/com/heartless/experimentproduct/data/places/MapboxPlacesRepository.kt`:

```kotlin
@Singleton
class MapboxPlacesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : PlacesRepository {

    private val foodDrinkCategories = listOf(
        "restaurant", "cafe", "bakery", "gas_station",
        "pharmacy", "convenience_store", "grocery", "food_truck"
    )

    override fun getNearbyPlaces(
        userLocation: UserLocation,
        radiusMeters: Double
    ): Flow<List<Station>> = callbackFlow {
        val accessToken = context.getString(R.string.mapbox_access_token)
        val searchEngine = SearchEngine.createSearchEngine(accessToken)
        val searchPoint = Point.fromLngLat(userLocation.longitude, userLocation.latitude)
        
        val searchOptions = SearchOptions.Builder()
            .origin(searchPoint)
            .limit(50)
            .build()

        // Implement multi-category search with callbacks
        // Filter by open_now, distance, and map to Station model
        
        awaitClose { }
    }
}
```

### Step 4: Update Dependency Injection

**In `app/src/main/java/com/heartless/experimentproduct/di/PlacesModule.kt`:**

```kotlin
@Provides
@Singleton
fun providePlacesRepository(
    @ApplicationContext context: Context
): PlacesRepository {
    // Change from StubPlacesRepository to MapboxPlacesRepository
    return MapboxPlacesRepository(context)
}
```

### Step 5: Build and Test

```bash
./gradlew clean assembleDebug
./gradlew installDebug
```

## Alternative: Google Places API

If Mapbox is not suitable, Google Places API can be used as a fallback:

### Prerequisites
1. Google Cloud Platform account
2. Places API enabled
3. API key with Places API permissions

### Implementation
1. Add dependency: `implementation("com.google.android.libraries.places:places:3.3.0")`
2. Configure API key in manifest
3. Create `GooglePlacesRepository` implementing `PlacesRepository`
4. Use Places API with Nearby Search requests
5. Map responses to `Station` model

## Testing

### Unit Tests (TODO)
```kotlin
class GetNearbyPlacesUseCaseTest {
    @Test
    fun `test places within 1 mile radius`() {
        // Mock repository
        // Verify distance constraint
    }
}
```

### Integration Tests (TODO)
```kotlin
class MapViewModelTest {
    @Test
    fun `test nearby places StateFlow emission`() {
        // Mock use case
        // Verify StateFlow updates
    }
}
```

## API Response Mapping

### Mapbox SearchResult → Station

```kotlin
fun mapSearchResultToStation(
    result: SearchResult,
    userLocation: UserLocation,
    category: String
): Station? {
    val coordinates = result.coordinate ?: return null
    val distance = calculateDistance(userLocation, coordinates)
    
    // Filter: within radius
    if (distance > MAX_RADIUS_METERS) return null
    
    // Filter: open now
    val isOpen = result.metadata?.get("opening_hours")
        ?.get("open_now") as? Boolean ?: true
    if (!isOpen) return null
    
    return Station(
        id = result.id ?: generateId(result),
        name = result.name,
        distance = distance,
        line = formatCategory(category),
        lineColor = getCategoryColor(category),
        openUntil = extractClosingTime(result),
        closingSoon = isClosingSoon(result),
        location = UserLocation(coordinates.latitude(), coordinates.longitude())
    )
}
```

## Performance Considerations

- **Caching:** Consider caching results for 5-10 minutes
- **Debouncing:** Debounce location updates to avoid excessive API calls
- **Pagination:** Currently fetches up to 50 results per category
- **Error Handling:** Graceful fallback on network errors

## Security Notes

- ✅ API keys stored in `gradle.properties` (not in version control)
- ✅ Public token in `strings.xml` (obfuscated in release builds)
- ✅ Secret token only used for dependency downloads
- ⚠️ Add ProGuard rules to obfuscate token strings in release

## Known Limitations

1. **Stub Implementation:** Current mock data is for testing only
2. **No Caching:** Places are fetched every time location changes
3. **Open Hours:** Stub uses simplified closing time logic
4. **Categories:** May need adjustment based on actual Mapbox category IDs

## Future Enhancements

- [ ] Add place details view
- [ ] Implement place ratings and reviews
- [ ] Add filtering by category
- [ ] Show places on map with markers
- [ ] Add navigation to selected place
- [ ] Cache results to reduce API calls
- [ ] Add search functionality

## Troubleshooting

### Build Error: "Could not resolve com.mapbox.search"
- **Cause:** Invalid or missing `MAPBOX_DOWNLOADS_TOKEN`
- **Solution:** Configure valid secret token in `gradle.properties`

### Runtime Error: "Unauthorized"
- **Cause:** Invalid public token in `strings.xml`
- **Solution:** Verify token has correct scopes and is not expired

### Empty Results
- **Cause:** No places found in radius, or API quota exceeded
- **Solution:** Check location accuracy, verify API limits

### Distance Calculation Issues
- **Cause:** Haversine formula precision
- **Solution:** Distances are approximate, ±1% accuracy is normal

## References

- [Mapbox Search SDK Documentation](https://docs.mapbox.com/android/search/guides/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Kotlin Flows and StateFlow](https://kotlinlang.org/docs/flow.html)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

## Support

For issues or questions:
1. Check troubleshooting section above
2. Review Mapbox Search SDK documentation
3. Open GitHub issue with details
