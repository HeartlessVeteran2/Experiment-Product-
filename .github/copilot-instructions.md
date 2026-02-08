# Copilot Instructions for Experiment Product - What's Around Me Food App

This is an Android application built with modern Android development practices, featuring a clean architecture design and Mapbox integration for location-based food discovery.

## Project Overview

- **Language**: Kotlin (100%)
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture (Data, Domain, Presentation layers)
- **Dependency Injection**: Hilt
- **Async Processing**: Kotlin Coroutines & Flow
- **Database**: Room (local persistence)
- **Maps**: Mapbox Maps SDK v11
- **Build System**: Gradle 8.2.0 with Kotlin DSL
- **Min SDK**: 26, Target SDK: 34

## Code Standards

### Required Before Each Commit

1. **Build verification**: Ensure the app builds successfully
   ```bash
   ./gradlew build
   ```

2. **Clean build** (if you modified dependencies or build files):
   ```bash
   ./gradlew clean build
   ```

### Development Workflow

- **Build**: `./gradlew build`
- **Install on device/emulator**: `./gradlew installDebug`
- **Clean**: `./gradlew clean`

## Repository Structure

```
app/src/main/java/com/heartless/experimentproduct/
├── data/                       # Data Layer
│   ├── database/               # Room database entities and DAOs
│   ├── location/               # Location services
│   ├── places/                 # Places data sources
│   └── repository/             # Repository implementations
├── domain/                     # Domain Layer (Business Logic)
│   ├── model/                  # Domain models (Station, LocationPin)
│   ├── repository/             # Repository interfaces
│   ├── usecase/                # Use cases
│   ├── location/               # Location domain logic
│   └── places/                 # Places domain logic
├── presentation/               # Presentation Layer (UI)
│   ├── ui/
│   │   └── theme/              # Compose theme
│   ├── viewmodel/              # ViewModels with StateFlow
│   └── MainActivity.kt         # Main entry point
└── di/                         # Dependency Injection (Hilt modules)
```

## Architecture Principles

### Clean Architecture Layers

1. **Data Layer** (`data/`):
   - Handles data operations and external data sources
   - Room database for local persistence
   - Repository implementations
   - Data entities and DAOs

2. **Domain Layer** (`domain/`):
   - Contains business logic and models
   - Written in Kotlin; may depend on selected Android framework services (e.g., location services via `LocationService`) where needed
   - Use cases encapsulate specific business operations
   - Repository interfaces and service abstractions define contracts

3. **Presentation Layer** (`presentation/`):
   - MVVM architecture with ViewModels
   - Jetpack Compose for declarative UI
   - StateFlow for reactive state management

### Dependency Flow

Dependencies point inward toward the Domain layer:
- Presentation → Domain
- Data → Domain
- Domain must not depend on Presentation or Data layers; it defines interfaces that outer layers implement

## Key Guidelines

### 1. Kotlin Best Practices

- **Null Safety**: Use strict null safety; prefer safe calls (`?.`) and Elvis operator (`?:`); avoid `!!` operator
- **Immutability**: Use `val` for immutable properties, `var` only when necessary
- **Data Classes**: Use data classes for models
- **Sealed Classes**: Use sealed classes for representing states and results
- **Extension Functions**: Use extension functions to enhance readability

### 2. Coroutines & Flow

- Use Kotlin Coroutines for all asynchronous operations
- Use `viewModelScope` for ViewModel coroutines
- Expose state with `StateFlow` in ViewModels
- Use `Flow` for reactive data streams from repositories
- Use `stateIn()` with `WhileSubscribed(5000)` for Flow to StateFlow conversion

**Example**:
```kotlin
val uiState: StateFlow<UiState> = repository.getData()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // 5000ms stopTimeout allows brief disconnections without stopping flow
        initialValue = UiState.Loading
    )
```

### 3. Dependency Injection (Hilt)

- Use `@HiltAndroidApp` on Application class
- Use `@AndroidEntryPoint` on Activities and Fragments
- Use `@HiltViewModel` on ViewModels
- Create Hilt modules in `di/` package with `@Module` and `@InstallIn`
- Use constructor injection wherever possible
- Use `@Singleton` for app-level dependencies

### 4. Jetpack Compose UI

- Write stateless composable functions; hoist state to ViewModels
- Collect StateFlow in composables with `collectAsState()`
- Use `remember` for objects that should survive recomposition
- Use `LaunchedEffect` for side effects that should run on composition
- Follow Material 3 design guidelines
- Use existing theme definitions from `presentation/ui/theme/`

### 5. ViewModels

- Expose UI state via `StateFlow`
- Expose one-time events via `SharedFlow` or `Channel`
- Keep ViewModels free of Android framework dependencies (except lifecycle)
- Use use cases from domain layer, not repositories directly
- Handle errors gracefully with sealed classes or Result types

**Example**:
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCase: MyUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
}
```

### 6. Room Database

- Define entities with `@Entity` annotation
- Use `@Dao` interfaces with suspend functions
- Return `Flow` from DAO queries for reactive updates
- Use `@Transaction` for complex operations
- Define database in `data/database/`

### 7. Repository Pattern

- **Preferred pattern (for all new or refactored code):**
  - Define repository interfaces in `domain/repository/`
  - Implement repositories in `data/repository/` or feature-specific folders
  - Data layer implementations should transform data entities (e.g., `LocationPinEntity`) to domain models (e.g., `LocationPin`) internally before returning results
  - Use `Flow` for reactive data streams
- **Legacy/transition code:**
  - Some existing use cases (e.g., `GetLocationPinsUseCase` in `domain/`) currently depend directly on concrete `data.repository.LocationPinRepository` class and perform entity-to-domain mapping within the use case itself, importing `data.database.LocationPinEntity`.
  - Do **not** copy this pattern for new features. When you touch these legacy areas, prefer to:
    - Introduce a domain-level repository interface in `domain/repository/`
    - Move entity-to-domain mapping logic into the data layer implementation
    - Have the use case depend only on the domain interface
  - Until that refactor is completed, keep behavior stable and avoid partial changes that could break the existing flow.

### 8. Use Cases

- One use case per business operation
- Use case classes should have an `operator fun invoke()` method
- Use cases orchestrate business logic and call repositories
- Use cases should **depend on domain repository interfaces**, not concrete data-layer implementations, for all new or refactored features.
- Existing exceptions (such as the current location pins flow mentioned above) are considered legacy and should be migrated toward the preferred pattern when they are next modified.
- Keep use cases simple and focused

### 9. Naming Conventions

- **Entities**: `*Entity` (e.g., `LocationPinEntity`)
- **Domain Models**: Clear names (e.g., `LocationPin`, `Station`)
- **ViewModels**: `*ViewModel` (e.g., `MapViewModel`)
- **Use Cases**: `*UseCase` (e.g., `GetLocationPinsUseCase`)
- **Repositories**: `*Repository` (e.g., `LocationPinRepository`)
- **DAOs**: `*Dao` (e.g., `LocationPinDao`)

### 10. Testing Guidelines

- Write unit tests for ViewModels using fake/mock use cases
- Write unit tests for use cases with mocked repositories
- Test repositories with in-memory Room database when possible
- Use JUnit 4 (configured for compatibility with Android testing framework)
- Place tests in `app/src/test/` for unit tests
- Place instrumented tests in `app/src/androidTest/` for UI tests

## Mapbox Configuration

### Important Notes

- Mapbox requires a **Downloads Token** for dependency resolution
- **Security Warning**: The `gradle.properties` file is tracked in version control (check `git ls-files gradle.properties`) with a placeholder token. To prevent accidentally committing real tokens:
  - **Option 1 (Recommended for local development)**: Set `MAPBOX_DOWNLOADS_TOKEN` as an environment variable instead of in `gradle.properties`
  - **Option 2**: Use a user-level Gradle properties file at `~/.gradle/gradle.properties` to store the token
  - **Option 3**: Add `gradle.properties` to `.gitignore` and create a `gradle.properties.example` file with placeholder values for other developers
- Token must be a **secret** token (starting with `sk.`) with Downloads:Read scope
- Never commit real tokens to version control
- Mapbox Maven repository is configured in `settings.gradle.kts`

### Current Implementation

- **StubPlacesRepository**: Currently uses mock data for places
- **MapboxPlacesRepository**: Ready to implement when token is configured
- Places search integration guide available in `PLACES_SEARCH_GUIDE.md`

## Security Considerations

1. **API Keys**: Store sensitive tokens in `gradle.properties`, never in code
2. **Permissions**: Request runtime permissions for location access
3. **Data**: Consider encryption for sensitive local data
4. **Network**: Use HTTPS for all API calls
5. **Code Security**: Run CodeQL scans (configured in `.github/workflows/codeql.yml`)

## Common Tasks

### Adding a New Feature

1. **Data Layer**:
   - Create entity (if persisting data)
   - Create/update DAO
   - Create/update repository implementation

2. **Domain Layer**:
   - Create domain model
   - Create repository interface
   - Create use case

3. **Presentation Layer**:
   - Create ViewModel with StateFlow
   - Create Composable screen/component
   - Wire up in MainActivity or navigation

### Adding a New Dependency

1. Add to `app/build.gradle.kts`
2. Sync Gradle
3. If it's a Mapbox dependency, ensure `MAPBOX_DOWNLOADS_TOKEN` is configured
4. Update documentation if it's a significant dependency

## Error Handling

- Use sealed classes to represent success, error, and loading states
- Handle errors gracefully in ViewModels
- Show user-friendly error messages in UI
- Log errors appropriately for debugging

**Example**:
```kotlin
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

## Performance Best Practices

1. **StateFlow**: Only triggers recomposition when state changes
2. **WhileSubscribed(5000)**: Stops flow collection when no active collectors
3. **Room Indexing**: Use indexes for frequently queried columns
4. **Lazy Loading**: Load data on demand
5. **Coroutines**: Use structured concurrency for proper cancellation

## Documentation

- Update `README.md` for user-facing changes
- Update `ARCHITECTURE.md` for architectural decisions
- Add KDoc comments for public APIs and complex logic
- Update `SETUP_GUIDE.md` if setup process changes

## Additional Resources

- Architecture details: `ARCHITECTURE.md`
- Setup instructions: `SETUP_GUIDE.md`
- Places search integration: `PLACES_SEARCH_GUIDE.md`
- Security policy: `SECURITY.md`

## Code Review Standards

When reviewing code or making changes:
1. Ensure adherence to Clean Architecture principles
2. Verify proper null safety
3. Check for appropriate use of coroutines and Flow
4. Confirm Hilt is used correctly for DI
5. Validate that UI is stateless with hoisted state
6. Ensure proper error handling
7. Verify naming conventions are followed
8. Check for proper separation of concerns
