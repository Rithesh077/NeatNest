# NeatNest: Intelligent Digital Workspace for Students

## Project Overview
NeatNest is an Android application designed to reduce the cognitive load on students by organizing their digital chaos through on-device intelligence. It focuses on three core pillars: **Privacy**, **Automation**, and **Clarity**. The app intercepts system notifications and scans local storage to classify and organize information into a structured, study-friendly environment.

## Phase 1: Foundational Architecture & Workflow
The current project state establishes the **Foundational Data Pipeline**—the underlying "pipes" that allow data to flow from ingestion to organization without manual effort.

### 1. Ingestion & Onboarding (`OnboardingActivity.kt`)
**Intention**: To establish trust and gain the necessary system-level access before any processing begins.
- **Permission Centralization**: Uses a single-screen toggle system to request Media Storage, Notification Listening, and Battery Optimization Ignore permissions.
- **Background Readiness**: By requesting `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, the app ensures that the background scanner isn't killed by aggressive system power management.
- **Foundational Scan**: Triggers an initial data ingestion simulation to prepare the user for the dashboard experience.

### 2. The Intelligence Engine (`DigitalAssetHub.kt`)
**Intention**: To create a modular classification system that can evolve from basic logic to advanced ML.
- **Classification Logic**: Uses metadata (file extensions and keywords) to categorize files as `STUDY_MATERIAL` (e.g., .pdf, "lecture") or `DIGITAL_CLUTTER` (e.g., .jpg, "whatsapp").
- **Unified Logic**: Merges the previously separate "Smart Study" and "Anti-Clutter" modules into a single processing engine, reducing code redundancy.

### 3. Automated Data Pipeline (`AssetScannerWorker.kt` & `FileMover.kt`)
**Intention**: To implement "Automatic Actions" with zero manual effort from the user.
- **Background Scanning**: Utilizes `WorkManager` and `CoroutineWorker` to query `MediaStore` in the background for new assets.
- **Direct Organization**: Automatically creates subdirectories named after file extensions (e.g., `/pdf/`) within a user-selected root directory.
- **Duplicate Prevention**: Before moving a file, the system checks both the filesystem and the database to ensure no redundant copies are created.
- **Secure Bit-Stream Copying**: `FileMover` handles the physical transfer of data using Scoped Storage APIs, ensuring compatibility with modern Android security.

### 4. Persistent State Management (`AppDatabase.kt`)
**Intention**: To ensure the app has "Memory" and operates efficiently.
- **Room Persistence**: Stores metadata for every processed file and notification.
- **Scan Optimization**: The `isFileProcessed` query prevents the app from re-scanning the same 10,000 images every cycle, significantly saving battery and CPU.
- **Multi-Entity Tracking**: Tracks both `ProcessedFile` (assets) and `ProcessedNotification` (signals) in a unified local database.

### 5. Centralized Analytics Dashboard (`MainActivity.kt`)
**Intention**: To provide the user with a "At-a-glance" status of their digital environment.
- **Reactive UI**: Uses `LiveData` and `Flow` to observe the Room database. The "Assets Organized" and "Signals Cleaned" stats update in real-time as the background worker operates.
- **Modular Entry Points**: Provides direct access to the **Signal Noise Cleaner** and the **Digital Asset Hub**.

### 6. Signal Noise Interception (`NotificationService.kt`)
**Intention**: To capture data at the source of distraction.
- **Notification Listener**: Inherits from `NotificationListenerService` to intercept raw notification streams.
- **Metadata Extraction**: Extracts titles and package names to begin the classification into priority sets (Phase 2 feature).

---

## Technical Stack
- **Language**: Kotlin with Coroutines (for non-blocking IO).
- **Architecture**: MVVM-lite with a Repository-style data pipeline.
- **Database**: Room (SQL-based on-device storage).
- **Background**: WorkManager (for robust, deferrable background tasks).
- **Security**: Scoped Storage & Notification Listener APIs.
- **Future Migration**: Potential tech migration to React Native.

## Future Scope (Phase 2)
- **TinyML Integration**: Migrating from keyword-based logic to on-device neural networks for image and text classification.
- **Advanced Prioritization**: Ranking notifications from "Least Important" to "Most Important" based on student-specific context.
- **Physical Migration**: Finalizing the "Move" logic (Delete original after copy) for fully automated device cleaning.
