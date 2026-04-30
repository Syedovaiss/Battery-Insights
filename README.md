# Battery Insights

Battery Monitorer is a native Android application that collects battery telemetry on-device, builds battery behavior insights, and presents health-focused analytics with configurable alerts, retention, and export.

## Repository Purpose

This repository contains the complete Android client implementation, including:

- battery data collection (foreground broadcast + background WorkManager polling)
- local persistence and report aggregation
- insight generation and notifications
- settings, export, and visualization flows
- release telemetry integration (Crashlytics + analytics breadcrumbs)

## Key Features

- Live battery analytics (level, temperature, charging state, usage trends)
- Insight engine for abnormal drain, thermal behavior, and low-battery coaching
- Charge-limit notifications and charging session metrics
- Configurable retention policy (auto-delete history, default 30 days)
- Export in `CSV`, `JSON`, and `PDF`
- Theme customization (dark mode, dynamic colors, accent color)

## Architecture

The project follows a feature-oriented, layered architecture:

- **Presentation**
  - Jetpack Compose UI
  - State-driven screens (`BatteryScreen`, `SettingsScreen`)
  - ViewModels for screen orchestration

- **Domain/Application**
  - `BatteryTracker` for ingestion, alerts, cleanup triggers
  - `InsightEngine` for battery intelligence rules
  - `WorkManagerScheduler` for periodic polling

- **Data**
  - Repository abstraction (`BatteryRepository`, `SettingsRepository`)
  - Local persistence via Room + DataStore
  - Export manager for file generation/share workflows

- **Infrastructure**
  - Hilt dependency injection
  - WorkManager + Hilt Worker factory
  - Firebase Crashlytics/Analytics telemetry hooks

## Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- Hilt (DI)
- Room (local DB)
- DataStore (preferences/settings)
- WorkManager (background jobs)
- Firebase Crashlytics + Analytics
- AndroidX Navigation 3

## Data Model

- `BatteryEntity`: raw battery samples with timestamp and device context
- `DailyReportEntity`: aggregated daily summaries (drain/temp/signal/screen-time)
- `AppSettings`: user-configurable app behavior and retention parameters

## Project Structure

- `core/` app bootstrap, navigation, telemetry, theme
- `feature/home/` battery dashboard, insights, history, reports
- `feature/settings/` preferences, export configuration/actions
- `storage/` Room database, DAO, entities, DI modules
- `worker/` periodic background polling
- `receiver/` battery broadcast handling

## Build and Run

### Requirements

- Android Studio (latest stable)
- JDK 11+
- Android SDK matching project `compileSdk`

### Setup

1. Clone the repository.
2. Add Firebase config file:
   - `app/google-services.json`
3. Sync Gradle and run the `app` module.

## Operational Docs

- Production checklist: `PRODUCTION_CHECKLIST.md`
- QA matrix: `QA_TEST_MATRIX.md`
- Firebase setup: `FIREBASE_SETUP.md`
- Roadmap: `ROADMAP.md`
- Privacy policy page: `privacy-policy.html`
