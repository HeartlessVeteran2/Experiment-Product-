# Experiment Product - What's Around Me Food App

[![Build & Test](https://github.com/Heartless-Veteran/Experiment-Product-/actions/workflows/ci.yml/badge.svg)](https://github.com/Heartless-Veteran/Experiment-Product-/actions/workflows/ci.yml)
[![Lint](https://github.com/Heartless-Veteran/Experiment-Product-/actions/workflows/lint.yml/badge.svg)](https://github.com/Heartless-Veteran/Experiment-Product-/actions/workflows/lint.yml)
[![CodeQL](https://github.com/Heartless-Veteran/Experiment-Product-/actions/workflows/codeql.yml/badge.svg)](https://github.com/Heartless-Veteran/Experiment-Product-/actions/workflows/codeql.yml)

An Android application built with modern Android development practices, featuring a clean architecture design and Mapbox integration for location-based food discovery.

## Tech Stack

- **Language**: Kotlin 1.9.20
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture (Data, Domain, Presentation layers)
- **Dependency Injection**: Hilt
- **Async Processing**: Kotlin Coroutines & Flow
- **Database**: Room (local persistence)
- **Maps**: Mapbox Maps SDK v11 with maps-compose
- **Build System**: Gradle 8.2 with Kotlin DSL

## Project Structure

```
app/src/main/java/com/heartless/experimentproduct/
├── data/                       # Data Layer
│   ├── database/               # Room database entities and DAOs
│   │   ├── AppDatabase.kt
│   │   ├── LocationPinDao.kt
│   │   └── LocationPinEntity.kt
│   └── repository/             # Repository implementations
│       └── LocationPinRepository.kt
├── domain/                     # Domain Layer (Business Logic)
│   ├── model/                  # Domain models
│   │   └── LocationPin.kt
│   └── usecase/                # Use cases
│       └── GetLocationPinsUseCase.kt
├── presentation/               # Presentation Layer (UI)
│   ├── ui/
│   │   ├── theme/              # Compose theme
│   │   │   ├── Color.kt
│   │   │   ├── Theme.kt
│   │   │   └── Type.kt
│   │   └── MapScreen.kt        # Mapbox map UI
│   ├── viewmodel/
│   │   └── MapViewModel.kt     # ViewModel with StateFlow
│   └── MainActivity.kt         # Main entry point
├── di/                         # Dependency Injection
│   └── DatabaseModule.kt       # Hilt modules
└── ExperimentProductApp.kt     # Application class
```

## Features

✅ **Implemented:**
- Complete Android project setup with Gradle Kotlin DSL
- Clean Architecture with clear separation of concerns
- Hilt dependency injection fully integrated
- Room database configured for location pins persistence
- Jetpack Compose UI with Material 3
- Mapbox integration for map display
- ViewModel with StateFlow for reactive state management
- Coroutines and Flow for async operations
- Location permissions setup

## Setup Instructions

### Prerequisites
1. Android Studio Hedgehog (2023.1.1) or later
2. JDK 17
3. Android SDK with API level 34
4. Mapbox access token

### Configuration

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Heartless-Veteran/Experiment-Product-.git
   cd Experiment-Product-
   ```

2. **Configure Mapbox token:**
   - Open `gradle.properties`
   - Replace `YOUR_MAPBOX_TOKEN_HERE` with your Mapbox secret token:
     ```
     MAPBOX_DOWNLOADS_TOKEN=sk.your_actual_token
     ```
   - Get your token from: https://account.mapbox.com/access-tokens/

3. **Set up Android SDK:**
   - Create `local.properties` in the project root:
     ```
     sdk.dir=/path/to/your/android/sdk
     ```

### Building the Project

```bash
# Build the app
./gradlew build

# Run on connected device/emulator
./gradlew installDebug

# Clean build
./gradlew clean
```

## Architecture Details

### Clean Architecture Layers

**Data Layer** (`data/`):
- Handles data operations and external data sources
- Room database for local persistence
- Repository pattern for data abstraction

**Domain Layer** (`domain/`):
- Contains business logic and models
- Pure Kotlin with no Android dependencies
- Use cases encapsulate specific business operations

**Presentation Layer** (`presentation/`):
- MVVM architecture with ViewModels
- Jetpack Compose for declarative UI
- StateFlow for reactive state management

### Dependency Injection

Hilt is used throughout the app:
- `@HiltAndroidApp` on Application class
- `@AndroidEntryPoint` on Activities
- `@HiltViewModel` on ViewModels
- Modules in `di/` package provide dependencies

### Data Flow

```
UI (Compose) → ViewModel → Use Case → Repository → DAO → Room Database
                  ↓
              StateFlow
                  ↓
            UI Recomposition
```

## Key Components

### Database (Room)
- **AppDatabase**: Main database class
- **LocationPinEntity**: Entity for storing location pins
- **LocationPinDao**: Data access methods with Flow support

### Mapbox Integration
- Integrated in `MapScreen.kt`
- Uses AndroidView for native MapView
- Lifecycle-aware with DisposableEffect

### Hilt Modules
- **DatabaseModule**: Provides Room database and DAOs

## Permissions

Required permissions (already configured in AndroidManifest.xml):
- `INTERNET` - For Mapbox tile loading
- `ACCESS_FINE_LOCATION` - For precise location
- `ACCESS_COARSE_LOCATION` - For approximate location
- `ACCESS_NETWORK_STATE` - For network availability

## CI/CD Workflows

The project includes automated CI/CD workflows for code quality and releases:

- **Build & Test** - Automatically builds and tests on every push/PR
- **Lint** - Runs Android Lint checks for code quality
- **CodeQL** - Security analysis for vulnerability scanning
- **Dependabot** - Automated dependency updates
- **Release** - Automated APK/AAB builds on version tags

See [CI_CD_WORKFLOWS.md](./CI_CD_WORKFLOWS.md) for detailed documentation.

## Next Steps

Future enhancements:
- [ ] Add location tracking functionality
- [ ] Implement food place search with Mapbox Search API
- [ ] Add pin management (add, edit, delete)
- [ ] Implement user preferences screen
- [ ] Add place details and reviews
- [ ] Implement offline mode
- [ ] Add analytics and crash reporting

## License

This project follows the Apache License 2.0.

## Contributing

Contributions are welcome! Please follow the existing code style and architecture patterns.
