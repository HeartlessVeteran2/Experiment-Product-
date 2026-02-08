# 🚇 What's Around Me — Master Plan

> A transit-themed "What's around me" food & drink discovery app for Android.
> Scan your surroundings. See what's open. Pin your favorites.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Milestones](#milestones)
- [MVP Features](#mvp-features)
- [V1.1 Features](#v11-features)
- [V1.2 Features](#v12-features)
- [V1.3 Features](#v13-features)
- [V2.0 Features](#v20-features)

---

## Overview

**What's Around Me** uses a transit/subway metaphor to show nearby food and drink options. Places are organized into five color-coded "lines" (like subway lines), displayed on a dark transit-style map with a departure-board bottom sheet UI. Users can scan their surroundings, filter by category, pin favorites, and leave anonymous reviews.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** Clean Architecture (data / domain / presentation)
- **DI:** Hilt
- **Async:** Coroutines + Flow
- **Local Storage:** Room
- **Maps:** Mapbox v11 SDK + maps-compose
- **Search:** Mapbox Search SDK (fallback: Google Places API)
- **Location:** Google Play Services Location

## Milestones

| Milestone | Issues | Description |
|-----------|--------|-------------|
| **MVP**   | #1–#12 | Core scan, map, board, pins, and privacy |
| **V1.1**  | #13–#17 | Vendor mode, UGC (reports, photos, ratings) |
| **V1.2**  | #18–#20 | Badges and ghost stations |
| **V1.3**  | #21–#26 | Smart features (quick pick, wait times, weather, alerts) |
| **V2.0**  | #27–#30 | Vendor tools and power users |

---

## MVP Features

### Issue #1: Project Setup & Architecture Foundation

**Labels:** `MVP`, `setup`

#### Description
Set up the Android project with the full tech stack and clean architecture scaffolding.

#### Tasks
- Initialize Android project with Kotlin DSL Gradle
- Configure Jetpack Compose (Material 3)
- Set up Hilt dependency injection
- Set up Coroutines + Flow
- Set up Room database (on-device persistence for pins)
- Configure Mapbox v11 SDK + maps-compose
- Establish Clean Architecture package structure:
  - `data/` (repositories, data sources, models)
  - `domain/` (use cases, interfaces, domain models)
  - `presentation/` (ViewModels, Compose UI)
- Add all dependencies:
  - `com.google.android.gms:play-services-location`
  - `com.mapbox.maps:android`
  - `com.mapbox.extension:maps-compose`
  - `com.mapbox.search:mapbox-search-android`
  - Room, Hilt, Coroutines, Kotlinx Serialization

#### Definition of Done
- [ ] Project builds and runs
- [ ] Empty Compose screen with Mapbox map loads
- [ ] Hilt injects a sample dependency
- [ ] Room database initializes
- [ ] Package structure matches Clean Architecture layers

---

### Issue #2: Location Permission & Single Scan

**Labels:** `MVP`

#### Description
Implement standard Android location permission ("While using the app") and location fetch. Normal app behavior — uses location while open, stops when closed. No background tracking.

#### Tasks
- Request `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`
- Use `rememberLauncherForActivityResult` for Compose permission flow
- If granted → fetch current location via `FusedLocationProviderClient`
- If denied → show Snackbar ("Location needed to find nearby places") and default to fallback city center
- Location is used while app is open, stops when app is closed
- Expose location as `StateFlow<Location?>` from a `LocationRepository`

#### Definition of Done
- [ ] On first launch, app asks for location permission
- [ ] If granted, app knows user's current position
- [ ] If denied, graceful fallback with message
- [ ] No background location permission requested ever

---

### Issue #3: The Scan — Fetch All Food & Drink Sources Within 1 Mile

**Labels:** `MVP`

**Dependencies:** Issue #2 (Location)

#### Description
When the app opens (or user triggers a scan), fetch every place that sells food or drinks within a 1 mile radius of the user's current location. Only show places that are currently open.

#### Tasks
- Create `PlacesRepository` interface in Domain layer
- Implement using Mapbox Search SDK (fallback: Google Places API)
- Query categories: `restaurant`, `cafe`, `bakery`, `gas_station`, `pharmacy`, `convenience_store`, `grocery`, `food_truck`
- Hard radius: 1 mile
- Filter: `open_now = true` only
- Sort: distance ascending (closest first)
- Map results to `Station` domain model:
  - `id`, `name`, `distance`, `line` (category), `lineColor`, `openUntil`, `closingSoon` (boolean), `location` (lat/lng)
- Expose results as `StateFlow<List<Station>>` from ViewModel

#### Definition of Done
- [ ] Real places populate based on current location
- [ ] Only open places shown
- [ ] Sorted strictly by distance
- [ ] Results include all 5 line categories
- [ ] No results beyond 1 mile

---

### Issue #4: The Five Lines — Category Assignment & Colors

**Labels:** `MVP`

**Dependencies:** Issue #3 (Scan Data)

#### Description
Assign every scan result to one of five "lines" with a distinct color based on what type of place it is.

#### Line Definitions

| Line | Color | Hex | Covers |
|---|---|---|---|
| 🟢 Green | Green | `#4CAF50` | Restaurants, delis, food trucks |
| 🟠 Orange | Orange | `#FF9800` | Coffee shops, bakeries, juice bars |
| 🔴 Red | Red | `#F44336` | Gas stations |
| 🟣 Purple | Purple | `#9C27B0` | Pharmacies |
| ⚪ White | White | `#FFFFFF` | Convenience stores, grocery |

#### Tasks
- Create `Line` enum with color values
- Create `LineMapper` that maps API category/type tags to the correct Line
- Handle ambiguous types (e.g., a gas station with a deli → Red line, food angle noted in detail)

#### Definition of Done
- [ ] Every scan result has a line assignment
- [ ] Colors are correct per category
- [ ] No unassigned results

---

### Issue #5: The Board — Bottom Sheet UI

**Labels:** `MVP`, `UI`

**Dependencies:** Issue #4 (Lines & Colors)

#### Description
Build the main UI component: a persistent bottom sheet that displays the scan results as a transit-style departure board.

#### Tasks
- Implement `ModalBottomSheet` or `BottomSheetScaffold` in Compose
- Half-screen default state, expandable to full-screen
- Display each station as a row:
  - Line color indicator (left bar or dot)
  - Station name
  - Distance (e.g., "0.3 mi")
  - "Closing soon" badge if applicable
  - Open until time
- Group or sort by line, then by distance
- Pull-to-refresh to re-scan
- Empty state: "No open places found nearby"

#### Definition of Done
- [ ] Bottom sheet displays scan results in transit-board style
- [ ] Each row shows line color, name, distance, and hours
- [ ] Closing soon stations are visually distinguished
- [ ] Empty state is handled gracefully
- [ ] Pull-to-refresh triggers a new scan

---

### Issue #6: Filter Chips

**Labels:** `MVP`, `UI`

**Dependencies:** Issue #5 (Board UI)

#### Description
Add filter chips above the board so users can filter visible stations by line color/category.

#### Tasks
- Add a horizontal scrollable row of filter chips at the top of the board
- One chip per line: 🟢 Food, 🟠 Coffee, 🔴 Gas, 🟣 Pharmacy, ⚪ Grocery
- "All" chip selected by default
- Tapping a chip filters the board list to that line only
- Multiple chip selection supported (toggle on/off)
- Chip colors match line colors

#### Definition of Done
- [ ] Filter chips render above the board
- [ ] Tapping a chip filters the list
- [ ] "All" shows everything
- [ ] Multiple filters can be active simultaneously
- [ ] Visual feedback on selected/unselected state

---

### Issue #7: Dark Transit Map Style & Polylines

**Labels:** `MVP`, `map`

**Dependencies:** Issue #4 (Lines & Colors)

#### Description
Apply a dark transit-inspired map style and draw polylines from the user's location to each station, color-coded by line.

#### Tasks
- Apply a custom dark Mapbox style (transit/subway inspired)
- Draw polylines from user location to each visible station
- Polyline color matches the station's line color
- Place colored markers/dots at each station location
- User location shown as a distinct pulsing dot
- Polylines update when filters change (only show filtered stations)

#### Definition of Done
- [ ] Map uses dark transit-style theme
- [ ] Polylines connect user to each station
- [ ] Colors match line assignments
- [ ] Markers appear at station locations
- [ ] Map updates when filters are applied

---

### Issue #8: Station Detail Page

**Labels:** `MVP`, `UI`

**Dependencies:** Issue #5 (Board UI)

#### Description
Tapping a station row on the board (or a map marker) opens a detail page showing full information about that place.

#### Tasks
- Navigate to detail screen on station tap
- Display:
  - Station name and line color badge
  - Address
  - Distance from user
  - Open/closed status with hours
  - "Closing soon" warning if applicable
  - Category/type
  - Phone number (if available)
  - Directions button (opens native maps app)
- "Pin It" button (links to Issue #9)
- Back navigation to board

#### Definition of Done
- [ ] Detail page opens on station tap
- [ ] All available information displayed
- [ ] Directions button works
- [ ] Pin It button is present (functional in Issue #9)
- [ ] Smooth navigation back to board

---

### Issue #9: Pin It — Save Places With Icon Picker

**Labels:** `MVP`

**Dependencies:** Issue #8 (Station Detail)

#### Description
Allow users to save/pin places locally with a custom icon picker. Pins are stored on-device using Room.

#### Tasks
- Add "Pin It" button on station detail page
- On tap, show icon picker dialog with emoji/icon options (🍕 🍔 ☕ ⛽ 💊 🛒 ⭐ ❤️ etc.)
- Save pin to Room database:
  - `id`, `stationId`, `name`, `line`, `icon`, `pinnedAt` (timestamp), `location`
- Toggle: if already pinned, unpin on tap
- Show pin icon on the board for pinned stations
- Show pin markers on the map

#### Definition of Done
- [ ] Users can pin a station with a chosen icon
- [ ] Pinned stations persist across app restarts (Room)
- [ ] Pin icon appears on the board and map
- [ ] Users can unpin a station
- [ ] Icon picker offers at least 8 options

---

### Issue #10: Pins List View

**Labels:** `MVP`, `UI`

**Dependencies:** Issue #9 (Pin It)

#### Description
A dedicated view/tab to see all pinned places, sorted by most recently pinned.

#### Tasks
- Add a "Pins" tab or navigation destination
- List all pinned stations with:
  - Chosen icon
  - Station name
  - Line color indicator
  - Distance from current location
  - Pinned date
- Tap to navigate to station detail
- Swipe-to-delete or long-press to unpin
- Empty state: "No pinned places yet"

#### Definition of Done
- [ ] Pins list view is accessible from main navigation
- [ ] All pinned stations displayed with icons and details
- [ ] Tapping a pin opens station detail
- [ ] Users can remove pins
- [ ] Empty state handled

---

### Issue #11: Time-of-Day Emphasis

**Labels:** `MVP`

**Dependencies:** Issue #5 (Board UI)

#### Description
Adjust the board display based on time of day — emphasize breakfast spots in the morning, lunch places at noon, dinner options in the evening.

#### Tasks
- Determine current time-of-day period:
  - Morning (6 AM – 11 AM): Emphasize coffee, bakeries (Orange line)
  - Lunch (11 AM – 2 PM): Emphasize restaurants, delis (Green line)
  - Afternoon (2 PM – 5 PM): Balanced view
  - Dinner (5 PM – 10 PM): Emphasize restaurants (Green line)
  - Late Night (10 PM – 6 AM): Show only what's open
- Add subtle visual emphasis (slight highlight or "suggested" badge) on relevant lines
- Do NOT hide any results — just emphasize

#### Definition of Done
- [ ] Time-based emphasis is visible on the board
- [ ] Emphasis changes based on actual time of day
- [ ] No results are hidden, only emphasized
- [ ] Works correctly across all time periods

---

### Issue #12: Privacy Onboarding Screen

**Labels:** `MVP`, `UI`

#### Description
Show a clean, reassuring privacy onboarding screen on first launch before requesting any permissions.

#### Tasks
- Display on first app launch only (track with SharedPreferences/DataStore)
- Content:
  - "Your location stays on your device"
  - "No account required"
  - "No tracking. No ads. Just food."
  - Privacy icon/illustration
- Single "Get Started" button proceeds to location permission request
- Skip option available
- Screen does not appear on subsequent launches

#### Definition of Done
- [ ] Privacy screen shows on first launch
- [ ] Clear, reassuring privacy messaging
- [ ] "Get Started" proceeds to permission flow
- [ ] Does not show again after first launch
- [ ] Skip option works

---

## V1.1 Features

### Issue #13: Vendor Mode — Claim & Manage Station

**Labels:** `V1.1`, `vendor`

**Dependencies:** Issue #8 (Station Detail)

#### Description
Allow place owners/managers to "claim" their station and manage basic information like hours, specials, and status.

#### Tasks
- Add "Claim this place" option on station detail (initially just a request flow)
- Vendor dashboard screen:
  - Update hours / special hours
  - Set daily specials or announcements
  - Mark as "temporarily closed"
  - Upload a cover photo
- Vendor data stored locally or via simple backend
- Verification: simple email-based or code-based verification for MVP

#### Definition of Done
- [ ] Vendors can claim a station
- [ ] Vendor dashboard allows editing hours and specials
- [ ] Changes reflect on station detail for all users
- [ ] Basic verification flow exists

---

### Issue #14: Station Reports — Anonymous 140-Character Reviews

**Labels:** `V1.1`, `UGC`

**Dependencies:** Issue #8 (Station Detail)

#### Description
Allow users to leave short, anonymous text reviews ("reports") on any station. Maximum 140 characters, no account required.

#### Tasks
- Add "Leave a Report" button on station detail
- Text input limited to 140 characters
- Reports are anonymous (no user identity stored)
- Display recent reports on station detail (newest first)
- Store reports locally (Room) or via simple backend
- Basic profanity filter
- Rate-limit: max 1 report per station per hour

#### Definition of Done
- [ ] Users can submit 140-char anonymous reports
- [ ] Reports display on station detail page
- [ ] Character limit enforced
- [ ] Basic content filtering in place
- [ ] Rate limiting works

---

### Issue #15: Camera-Only Photos

**Labels:** `V1.1`, `UGC`

**Dependencies:** Issue #8 (Station Detail)

#### Description
Allow users to add photos to a station — camera capture only, no gallery uploads (ensures fresh, authentic content).

#### Tasks
- Add "Add Photo" button on station detail
- Open device camera (no gallery picker)
- Compress and store photo locally
- Display photos in a small gallery on station detail
- Photos are anonymous
- Maximum 3 photos per user per station

#### Definition of Done
- [ ] Camera capture works
- [ ] No gallery/library access
- [ ] Photos display on station detail
- [ ] Storage and compression handled
- [ ] Photo limit enforced

---

### Issue #16: Rider Score — Binary Rating System

**Labels:** `V1.1`, `UGC`

**Dependencies:** Issue #8 (Station Detail)

#### Description
Simple binary rating system: thumbs up (👍) or thumbs down (👎) for each station. Shows aggregate percentage.

#### Tasks
- Add thumbs up/down buttons on station detail
- One vote per station per user (stored locally)
- Display aggregate score: "85% 👍" format
- Minimum 3 votes before showing score
- Users can change their vote
- Score visible on board rows as well

#### Definition of Done
- [ ] Binary voting works
- [ ] Aggregate score displays correctly
- [ ] One vote per user per station
- [ ] Score visible on detail page and board
- [ ] Minimum vote threshold before showing score

---

### Issue #17: Pin It — Optional Note

**Labels:** `V1.1`

**Dependencies:** Issue #9 (Pin It)

#### Description
Extend the Pin It feature to allow users to add an optional short note when pinning a place (e.g., "best breakfast burrito").

#### Tasks
- Add optional text field to pin dialog (max 100 characters)
- Save note alongside pin in Room database
- Display note on pins list view
- Display note on station detail if pinned
- Note is editable after pinning

#### Definition of Done
- [ ] Optional note field appears in pin dialog
- [ ] Note saved and persists
- [ ] Note visible in pins list and station detail
- [ ] Note is editable
- [ ] Character limit enforced

---

## V1.2 Features

### Issue #18: Local Favorite Badge

**Labels:** `V1.2`, `badges`

**Dependencies:** Issue #16 (Rider Score)

#### Description
Automatically award a "Local Favorite" badge to stations that maintain a high rider score over time.

#### Tasks
- Define threshold: 80%+ thumbs up with 10+ total votes
- Display badge icon on station detail and board row
- Badge updates dynamically as votes change
- Visual: star or trophy icon with "Local Favorite" label

#### Definition of Done
- [ ] Badge appears on qualifying stations
- [ ] Threshold logic is correct
- [ ] Badge displays on both detail and board views
- [ ] Badge updates when scores change

---

### Issue #19: Hidden Gem Badge

**Labels:** `V1.2`, `badges`

**Dependencies:** Issue #16 (Rider Score)

#### Description
Award a "Hidden Gem" badge to stations with high scores but low total engagement/visits.

#### Tasks
- Define threshold: 90%+ thumbs up with fewer than 5 total votes
- Display distinct badge icon (gem/diamond)
- "Hidden Gem" label
- Badge removed once vote count exceeds threshold

#### Definition of Done
- [ ] Badge appears on qualifying stations
- [ ] Threshold logic differentiates from Local Favorite
- [ ] Badge is removed when station becomes well-known
- [ ] Visual distinction from Local Favorite badge

---

### Issue #20: Ghost Stations — Closed Place Reminders

**Labels:** `V1.2`

**Dependencies:** Issue #3 (Scan Data)

#### Description
When a previously visited or pinned place is now permanently closed, show it as a "ghost station" with reduced opacity and a closed notice.

#### Tasks
- Track stations the user has interacted with (pinned, rated, reported)
- On scan, cross-reference with known stations
- If a known station is no longer in results (permanently closed), show as ghost
- Ghost station UI: dimmed/faded row with "Permanently Closed" label
- Option to dismiss ghost stations
- Ghost stations appear at bottom of the board

#### Definition of Done
- [ ] Closed stations appear as ghost stations
- [ ] Ghost UI is visually distinct (dimmed)
- [ ] Users can dismiss ghost stations
- [ ] Ghost stations appear at bottom of list
- [ ] Only applies to previously interacted stations

---

## V1.3 Features

### Issue #21: "What's Closest?" Quick Pick

**Labels:** `V1.3`

**Dependencies:** Issue #3 (Scan Data)

#### Description
A one-tap feature that instantly highlights the single closest open place across all categories.

#### Tasks
- Add "What's Closest?" floating action button or quick action
- On tap, highlight the closest station on the board and map
- Zoom map to show user and closest station
- Show mini detail card with name, distance, and directions
- Auto-dismiss after 5 seconds or on tap

#### Definition of Done
- [ ] Quick pick button is accessible
- [ ] Correctly identifies closest open station
- [ ] Map zooms to show the result
- [ ] Mini detail card displays
- [ ] Feature works across all line categories

---

### Issue #22: Wait Time Reporting

**Labels:** `V1.3`, `UGC`

**Dependencies:** Issue #8 (Station Detail)

#### Description
Allow users to report estimated wait times at a station. Shows crowdsourced wait time on station detail and board.

#### Tasks
- Add "Report Wait Time" button on station detail
- Quick picker: "No wait", "5 min", "10 min", "15 min", "20+ min"
- Store reports with timestamp
- Display most recent wait time report (if < 1 hour old)
- Show on board row as secondary info
- Average multiple reports within the same hour

#### Definition of Done
- [ ] Users can report wait times
- [ ] Wait time displays on station detail and board
- [ ] Stale reports (> 1 hour) are not shown
- [ ] Multiple reports are averaged
- [ ] Quick picker is easy to use

---

### Issue #23: Weather Adaptation

**Labels:** `V1.3`

**Dependencies:** Issue #5 (Board UI)

#### Description
Adjust the board emphasis based on current weather — e.g., prioritize indoor dining during rain, highlight coffee shops in cold weather.

#### Tasks
- Integrate a weather API (e.g., OpenWeatherMap free tier)
- Detect current weather conditions at user location
- Weather-based emphasis rules:
  - Rain/Snow: Emphasize indoor restaurants (Green line)
  - Cold (< 40°F): Emphasize coffee/hot drinks (Orange line)
  - Hot (> 90°F): Emphasize places with cold drinks
- Show small weather indicator on the board header
- Emphasis is additive with time-of-day emphasis (Issue #11)

#### Definition of Done
- [ ] Weather data fetched for user location
- [ ] Board emphasis adjusts based on weather
- [ ] Weather indicator visible on board
- [ ] Works alongside time-of-day emphasis
- [ ] Graceful fallback if weather API unavailable

---

### Issue #24: Closing Soon Alerts

**Labels:** `V1.3`

**Dependencies:** Issue #3 (Scan Data)

#### Description
Proactively alert users when nearby pinned or favorited places are closing within 30 minutes.

#### Tasks
- Monitor closing times of pinned/favorited stations
- If a pinned station closes within 30 minutes, show in-app notification
- Alert format: "☕ Café Roma closes in 25 min — 0.4 mi away"
- Tapping the alert opens station detail
- User can mute alerts for specific stations
- Only alert for stations within current scan radius

#### Definition of Done
- [ ] Alerts fire for closing-soon pinned stations
- [ ] Alert format is clear and actionable
- [ ] Tapping alert opens station detail
- [ ] Mute functionality works
- [ ] Only nearby stations trigger alerts

---

### Issue #25: Dead Zone Detection

**Labels:** `V1.3`

**Dependencies:** Issue #3 (Scan Data)

#### Description
Detect and communicate when the user is in a "dead zone" — an area with very few or no open food/drink options.

#### Tasks
- Define dead zone: fewer than 3 open stations within 1 mile
- Show dead zone indicator on map and board
- Suggest expanding radius or trying again later
- If 0 results: "You're in a dead zone — nothing open within 1 mile"
- If 1-2 results: "Limited options nearby" with those results shown

#### Definition of Done
- [ ] Dead zone detected correctly
- [ ] Visual indicator shown on map/board
- [ ] Helpful messaging with suggestions
- [ ] Works with 0, 1, and 2 results
- [ ] Does not interfere with normal operation when many results exist

---

### Issue #26: Night Service Mode

**Labels:** `V1.3`

**Dependencies:** Issue #11 (Time-of-Day Emphasis)

#### Description
After 10 PM, switch to a special "night service" mode that focuses on what's still open — late-night food, 24-hour places, and convenience stores.

#### Tasks
- Activate automatically after 10 PM local time
- Filter to only currently open stations
- Emphasize 24-hour places and late-night friendly spots
- Darker UI theme variant (even darker than default dark mode)
- "Night Service" label/indicator in the board header
- Deactivate at 6 AM

#### Definition of Done
- [ ] Night mode activates automatically after 10 PM
- [ ] Only open stations shown
- [ ] 24-hour places emphasized
- [ ] UI theme adjusts
- [ ] Deactivates at 6 AM

---

## V2.0 Features

### Issue #27: Vendor Shift Change Alerts

**Labels:** `V2.0`, `vendor`

**Dependencies:** Issue #13 (Vendor Mode)

#### Description
Allow vendors to notify nearby users when shifts change (e.g., new chef, fresh batch of food ready).

#### Tasks
- Add "Send Shift Alert" option in vendor dashboard
- Predefined alert templates: "Fresh batch ready!", "New chef on duty", "Happy hour started"
- Custom message option (max 100 characters)
- Alert appears for users within 0.5 miles
- Time-limited: alert expires after 2 hours
- Max 3 alerts per vendor per day

#### Definition of Done
- [ ] Vendors can send shift alerts
- [ ] Alerts reach nearby users
- [ ] Templates and custom messages work
- [ ] Alerts expire after 2 hours
- [ ] Daily limit enforced

---

### Issue #28: Vendor Paid Tiers

**Labels:** `V2.0`, `vendor`, `monetization`

**Dependencies:** Issue #13 (Vendor Mode)

#### Description
Introduce paid tiers for vendors with premium features like highlighted listings, priority placement, and advanced analytics.

#### Tasks
- Define tiers:
  - **Free:** Basic claim, update hours, 1 alert/day
  - **Pro ($9.99/mo):** Highlighted listing, 3 alerts/day, basic analytics
  - **Premium ($24.99/mo):** Top placement, unlimited alerts, full analytics, custom branding
- Implement in-app purchase flow (Google Play Billing)
- Visual distinction for paid listings (subtle — not ad-like)
- Pro/Premium badge on vendor profile

#### Definition of Done
- [ ] Tier system implemented
- [ ] In-app purchase flow works
- [ ] Paid features activate correctly
- [ ] Visual distinction is subtle and non-intrusive
- [ ] Billing integration with Google Play

---

### Issue #29: Vendor Analytics Dashboard

**Labels:** `V2.0`, `vendor`

**Dependencies:** Issue #28 (Vendor Paid Tiers)

#### Description
Provide vendors with analytics about their station's performance — views, pin count, rider score trends, and more.

#### Tasks
- Analytics dashboard screen in vendor mode
- Metrics:
  - Station views (detail page opens)
  - Pin count (how many users pinned this station)
  - Rider score over time (trend chart)
  - Report/review count
  - Wait time reports
  - Photo count
- Time range selector: 7 days, 30 days, 90 days
- Basic charts (line chart for trends, bar chart for comparisons)
- Export data option (CSV)

#### Definition of Done
- [ ] Analytics dashboard accessible to vendors
- [ ] All specified metrics displayed
- [ ] Time range selection works
- [ ] Charts render correctly
- [ ] Export functionality works

---

### Issue #30: Conductor Status — Power Users

**Labels:** `V2.0`, `badges`

**Dependencies:** Issue #16 (Rider Score), Issue #14 (Station Reports)

#### Description
Award "Conductor" status to power users who actively contribute to the community through reports, ratings, photos, and consistent usage.

#### Tasks
- Define Conductor criteria:
  - 50+ stations rated
  - 20+ reports written
  - 10+ photos submitted
  - Active for 30+ days
- Conductor badge on user profile (local)
- Conductor perks:
  - Early access to new features
  - Conductor icon on their reports/ratings
  - Ability to flag inappropriate content
- Progress tracker showing how close user is to Conductor status

#### Definition of Done
- [ ] Conductor status awarded based on criteria
- [ ] Badge displays correctly
- [ ] Progress tracker works
- [ ] Perks activate for Conductors
- [ ] Criteria thresholds are configurable
