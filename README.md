# Poultry Management Android Application

## Overview
This Android application is designed for poultry farm management, focusing on data collection, reporting, on-ground support, and data visualization. It targets farmers with basic tech literacy, providing a simple, functional interface to track flock data, generate reports, and monitor key metrics. The app supports multiple poultry types (Broiler, Layer, Breeder) with tailored data fields, supports data export to Excel, and includes dynamic charts and alerts for real-time insights.

## Features
- **Data Collection**: Collects poultry data via a form with common and type-specific fields:
  - Common fields: Date & Time (auto-filled), Flock ID, Feed Given Today, Water Consumed, Mortality, Notes, Photo Upload (optional).
  - Broiler-specific: Average Bird Weight, FCR Value.
  - Layer-specific: Eggs Collected, Hen-Day Egg Production.
  - Breeder-specific: Fertile Eggs, Hatchability.
- **Database Storage**: Uses SQLite with Room for local storage, with a `poultry_data` table for entries and a `reference_values` table for alert thresholds.
- **Reporting and Export**: Exports data to Excel files via MediaStore, with a fixed export button and toast notification on success.
- **Charts and Visualization**: Displays trends for feed consumption, mortality rate, and egg production in the `ChartsSection` (MainActivity) and detailed views in `ChartsActivity`, using Jetpack Compose `Canvas`.
- **Alerts and Notifications**: Triggers alerts for metrics like Fertile Eggs < Expected Average, Hen-Day Egg Production < 75%, and Broiler weight < 1.5kg at 7 days, displayed in `StatusSection` with NotificationManager support (API 26+).
- **UI Design**: Minimal, modern interface using Jetpack Compose and Material 3 components, with a dark theme and SVG-based VectorDrawable icons.

## Screenshots
https://github.com/harshalbondre101/Poultry-APP/blob/main/screenshots/1000137262.jpg

https://github.com/harshalbondre101/Poultry-APP/blob/main/screenshots/1000137263.jpg

https://github.com/harshalbondre101/Poultry-APP/blob/main/screenshots/1000137264.jpg


## Technical Stack
- **Language**: Primarily Java, with optional Kotlin for Jetpack Compose UI components.
- **UI Framework**: Jetpack Compose for modern, scrollable layouts; XML layouts for legacy components.
- **Database**: SQLite with Room for structured data storage.
- **Libraries**:
  - Material 3 for UI components.
  - Material Icons Extended for icons.
  - Coil for potential image loading (photo upload).
- **Icons**: SVG-based VectorDrawable XML files stored individually in `res/drawable` for performance.
- **Export**: MediaStore for Excel file exports, ensuring compatibility across Android versions.
- **Notifications**: NotificationManager with channels for API 26+ compatibility.
- **Target API**: 26 (minimum), ensuring broad device support.

## Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/poultryapp/
│   │   │   ├── MainActivity.java       # Dashboard with ChartsSection, StatusSection
│   │   │   ├── FormActivity.java       # Form for data entry
│   │   │   ├── ChartsActivity.java     # Detailed chart views
│   │   │   ├── DatabaseHelper.java     # SQLite database management
│   │   │   ├── FormActivity.kt         # Optional Kotlin for Compose-based form
│   │   │   ├── DatabaseHelper.kt       # Optional Kotlin for database
│   │   ├── res/
│   │   │   ├── drawable/              # VectorDrawable XMLs for icons
│   │   │   ├── layout/
│   │   │   │   ├── form.xml           # Legacy XML layout for FormActivity
│   │   │   ├── values/
│   │   │   │   ├── arrays.xml         # Spinner arrays for poultry types
│   │   ├── AndroidManifest.xml         # Permissions (e.g., WRITE_EXTERNAL_STORAGE)
├── build.gradle                       # Gradle config with Kotlin and Compose support
```

## Setup and Installation
1. **Prerequisites**:
   - Android Studio (latest stable version).
   - Minimum SDK: API 26.
   - Gradle with Kotlin plugin for mixed Java/Kotlin support.
2. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   ```
3. **Build the Project**:
   - Open in Android Studio.
   - Sync Gradle with dependencies (Material 3, Room, Coil).
   - Ensure Kotlin plugin is enabled in `build.gradle`:
     ```gradle
     plugins {
         id 'com.android.application'
         id 'kotlin-android'
     }
     dependencies {
         implementation 'androidx.core:core-ktx:1.12.0'
         implementation 'androidx.activity:activity-compose:1.8.2'
         implementation 'androidx.compose.material3:material3:1.2.1'
         implementation 'androidx.room:room-ktx:2.6.1'
         implementation 'io.coil-kt:coil-compose:2.4.0'
     }
     ```
4. **Run**:
   - Build and run on an emulator or device (API 26+).
   - Grant storage permissions for Excel export.

## Usage
1. **Dashboard (MainActivity)**:
   - View `ChartsSection` with clickable chart previews (feed, mortality, egg production).
   - View `StatusSection` with dynamic alerts for flocks.
   - Click the plus icon to navigate to the data entry form.
2. **Data Entry (FormActivity)**:
   - Select poultry type (Broiler, Layer, Breeder) via spinner.
   - Enter common fields and type-specific fields (e.g., FCR for Broiler).
   - Submit to save data to SQLite database; toast confirms submission.
3. **Charts (ChartsActivity)**:
   - View detailed charts with filters for poultry type, flock ID, and date range.
   - Uses `Canvas` for rendering trends based on `poultry_data` queries.
4. **Export**:
   - Export data to Excel from the history activity.
   - Fixed export button at the bottom shows a toast on success.
5. **Notifications**:
   - Alerts trigger on data submission if metrics fall below thresholds (e.g., Hen-Day < 75%).
   - Notifications use channels for API 26+ compatibility.

## Database Schema
- **poultry_data**:
  - Columns: `id` (INTEGER PRIMARY KEY), `date_time` (TEXT), `flock_id` (TEXT), `poultry_type` (TEXT), `feed_given` (REAL), `water_consumed` (REAL), `mortality` (INTEGER), `notes` (TEXT), `photo_path` (TEXT), `avg_bird_weight` (REAL), `fcr_value` (REAL), `eggs_collected` (INTEGER), `hen_day_production` (REAL), `fertile_eggs` (INTEGER), `hatchability` (REAL).
- **reference_values**:
  - Columns: `id` (INTEGER PRIMARY KEY), `poultry_type` (TEXT), `metric` (TEXT), `threshold` (REAL).
  - Example: `metric: fertile_eggs, threshold: <expected_average>`, `metric: hen_day_production, threshold: 0.75`.

## Known Issues
- **Photo Upload**: Placeholder logic exists; requires clarification on storage or cloud integration (e.g., Firebase).
- **Error Handling**: Basic validation for numeric inputs; needs expansion for edge cases.
- **Package Not Found**: Rare issue due to package name mismatch; verify `AndroidManifest.xml` and rebuild.
- **Performance**: Large datasets may slow chart rendering; consider pagination for `ChartsActivity`.

## Future Improvements
- Add Firebase for cloud syncing and backup.
- Enhance photo upload with compression and cloud storage.
- Implement advanced input validation (e.g., regex for Flock ID).
- Optimize database queries for large datasets.
- Add user authentication for multi-user support.

## Debugging Tips
- **Logs Not Visible**: Add `Log.d` statements in `DatabaseHelper` and `FormActivity` to trace data flow.
- **Data Not Saving**: Check `DatabaseHelper` initialization and ensure Room database is created.
- **Export Fails**: Verify storage permissions in `AndroidManifest.xml` and runtime checks.
- **UI Issues**: Test on multiple screen sizes; ensure Compose layouts are scrollable.

## Notes
- The app assumes manual data entry without sensor integration for simplicity.
- Alerts rely on `reference_values` table; ensure thresholds are populated.
- Charts use `Canvas` for minimal dependencies; external libraries like MPAndroidChart can be added if needed.
- For mixed Java/Kotlin, ensure Gradle is configured correctly to avoid build errors.
