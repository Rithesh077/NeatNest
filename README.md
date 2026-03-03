# NeatNest

NeatNest is an Android file organizer and notification tracker that helps users declutter their digital lives. It automatically scans, categorizes, and organizes files from selected source folders into a structured root directory, while simultaneously capturing and classifying device notifications by importance.

## Features

### Digital Asset Hub

- **File Scanning:** Scans user-selected folders (SAF) or the entire device (MediaStore) for images, videos, and audio files.
- **Smart Classification:** Uses `DigitalAssetHub` to categorize files as Study Material, Clutter, or by extension (jpg, pdf, mp3, etc.).
- **Move or Copy:** Users choose whether to move files (deleting originals) or copy them safely.
- **Background Sync:** Optional periodic re-scans every 4 hours via WorkManager.
- **Reset & Restore:** One-tap reset that restores all organized files back to Downloads and clears app state.

### Signal Noise Cleaner

- **Notification Capture:** A `NotificationListenerService` intercepts all device notifications in real-time.
- **Priority Classification:** Automatically classifies notifications as Most Important, Normal, Low, or Least Important.
- **Clear All Noise:** One-tap button to permanently wipe all captured notifications and reset counts to 0.

### Dev Mode

- **Fragment Lifecycle Viewer:** Live display of Android Fragment lifecycle callbacks.
- **Menu Demonstrations:** Options Menu (toolbar icons), Context Menu (long-press), and Pop-up Menu with forced icon display.
- **AlertDialog Showcase:** Confirmation dialogs for delete and archive actions.

### Utility Hub

- **Rescan Files:** Trigger a manual file organization scan.
- **Upcoming Tools:** Placeholders for Video Editor, File Editor, Data Extractor, and Price Tracker.

## Architecture

```
com.example.neatnest/
├── MainActivity.kt              # Dashboard (launcher)
├── OnboardingActivity.kt        # Folder setup and scan configuration
├── DigitalAssetHubActivity.kt   # Organized files viewer
├── SignalNoiseCleanerActivity.kt # Notification viewer
├── FileMoverActivity.kt         # Dev Mode (menus + fragments)
├── UtilityHubActivity.kt        # Utility tools
├── AssetScannerWorker.kt        # Background file scanner (WorkManager)
├── ResetWorker.kt               # Background reset/restore (WorkManager)
├── NotificationService.kt       # Notification listener service
├── FileMover.kt                 # File copy utility
├── DigitalAssetHub.kt           # Smart file classifier
├── PermissionManager.kt         # Centralized permission checks
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt       # Room database (singleton)
│   │   ├── ProcessedFileDao.kt
│   │   ├── ProcessedNotificationDao.kt
│   │   ├── TrackedFolderDao.kt
│   │   └── NeatNestPreferences.kt
│   ├── model/
│   │   ├── ProcessedFile.kt
│   │   ├── ProcessedNotification.kt
│   │   ├── TrackedFolder.kt
│   │   └── RecentActivityItem.kt
│   └── repository/
│       ├── FileRepository.kt
│       └── NotificationRepository.kt
├── ui/
│   ├── main/DashboardViewModel.kt
│   ├── onboarding/OnboardingViewModel.kt
│   ├── assethub/AssetHubViewModel.kt
│   ├── signalcleaner/SignalCleanerViewModel.kt
│   └── common/UiState.kt
└── di/AppModule.kt              # Koin dependency injection
```

## Tech Stack

| Component       | Technology                                  |
| --------------- | ------------------------------------------- |
| Language        | Kotlin                                      |
| UI              | XML Layouts + Material Design 3             |
| Database        | Room (SQLite)                               |
| Background Work | WorkManager                                 |
| DI              | Koin                                        |
| Architecture    | MVVM (ViewModel + StateFlow/LiveData)       |
| File Access     | Storage Access Framework (SAF) + MediaStore |
| Min SDK         | 24 (Android 7.0)                            |
| Target SDK      | 36                                          |

## Permissions

| Permission                           | Purpose                        |
| ------------------------------------ | ------------------------------ |
| `READ_EXTERNAL_STORAGE`              | Legacy file access (API < 33)  |
| `READ_MEDIA_IMAGES`                  | Image scanning (API 33+)       |
| `READ_MEDIA_VIDEO`                   | Video scanning (API 33+)       |
| `READ_MEDIA_AUDIO`                   | Audio scanning (API 33+)       |
| `POST_NOTIFICATIONS`                 | Notification display (API 33+) |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Capture device notifications   |

## Building

1. Open the project in Android Studio (Ladybug or later recommended).
2. Sync Gradle (File → Sync Project with Gradle Files).
3. Run on a device or emulator (API 24+).

> **Note:** The `kotlinOptions.jvmTarget` is set to `"11"` to match `compileOptions`. If you see JVM target mismatch errors, ensure your JDK version is 11+.

## App Flow

```
┌─────────────────┐
│   MainActivity   │  ← App opens here (Launcher)
│   (Dashboard)    │
└──────┬──────────┘
       │
       ├── Asset Hub card ──→ Onboarding (if first time) ──→ DigitalAssetHubActivity
       │                  └─→ DigitalAssetHubActivity (if setup done)
       ├── Signal Cleaner ──→ SignalNoiseCleanerActivity
       ├── Re-Sync card ───→ Confirmation dialog → AssetScannerWorker
       ├── Utility Hub ────→ UtilityHubActivity
       └── Dev Mode ───────→ FileMoverActivity
```

## Production Status (Version 1.0.0)

NeatNest is currently in its initial production release (v1.0.0). The core orchestration engine, which includes background file categorization via the Digital Asset Hub, and real-time noise suppression via the Signal Noise Cleaner, is fully stable and operational. Additional tools in the Utility Hub are slated for future releases.

## Documentation

See [docs/](docs/) for detailed documentation:

- [Architecture Guide](docs/architecture.md) — Component design and data flow
- [Features Guide — Version 1.0.0](docs/features.md) — Feature descriptions and user flows
- [Feature Tracking Logs](docs/feature_tracker.md) — Current development and production status of all features

## License

This project is for educational purposes.
