# NeatNest

**Unified Digital Workspace & Intelligence Engine**

NeatNest is an Android application that combines ML-powered file organization, real-time notification analytics, and device tools into a single unified workspace.

**Current Version: 2.1.4.1**

## Features

### Digital Asset Hub 🟢

- **ML-Powered Classification** — Dual-model engine: Naive Bayes (Kotlin) or TFLite neural network
- **File Scanning** — SAF (pick folders) or MediaStore (device-wide)
- **Folder Card Browsing** — Category folders with file counts, tap to drill down
- **Move or Copy** — Delete originals or keep them safely
- **Re-Sync** — Restore all files to their original locations, reset app state

### Signal Noise Cleaner 🩵

- **Real-time Notification Capture** — NotificationListenerService intercepts all notifications
- **Priority Classification** — High, Normal, Low priority with analytics
- **Analytics Dashboard** — Priority breakdown, top 5 apps by notification volume
- **Bulk Clear** — One-tap to wipe all captured notifications

### Developer Mode 🟣

- Toolbar menus, context menus, popup menus, fragment lifecycle, AlertDialogs
- _Planned replacement: Device Analyser (v2.1.5.1)_

### Utility Hub 🔵

- Placeholder tools: Video Editor, File Editor, Data Extractor, Price Tracker

## Architecture

```
com.example.neatnest/
├── SplashActivity.kt              # Animated splash screen
├── MainActivity.kt                # Hub launcher (4 nav cards)
├── OnboardingActivity.kt          # Setup: folders, root, scan mode, ML model
├── DigitalAssetHubActivity.kt     # Folder cards → file view, re-sync
├── SignalNoiseCleanerActivity.kt   # Analytics + notification list
├── FileMoverActivity.kt           # Dev Mode (menus + fragments)
├── UtilityHubActivity.kt          # Placeholder tools
├── AssetScannerWorker.kt          # Background scan + classify (WorkManager)
├── ResetWorker.kt                 # Restore files + reset (WorkManager)
├── NotificationService.kt         # Notification listener service
├── FileMover.kt                   # File copy/move utility
├── DigitalAssetHub.kt             # Legacy file classifier
├── PermissionManager.kt           # Centralized permission handling
├── FolderAdapter.kt               # Folder card RecyclerView adapter
├── ml/
│   ├── FileClassificationEngine.kt  # ML engine interface + factory
│   ├── NaiveBayesClassifier.kt      # Pure Kotlin classifier
│   └── TFLiteClassifier.kt          # TFLite/LiteRT wrapper
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room v5, 3 entities, singleton
│   │   ├── ProcessedFileDao.kt
│   │   ├── ProcessedNotificationDao.kt
│   │   ├── TrackedFolderDao.kt
│   │   └── NeatNestPreferences.kt
│   ├── model/
│   │   ├── ProcessedFile.kt        # engineUsed + category fields
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
└── di/AppModule.kt                 # Koin DI graph
```

## Tech Stack

| Component       | Technology                                |
| --------------- | ----------------------------------------- |
| Language        | Kotlin                                    |
| UI              | XML Layouts + Material Design 3           |
| Database        | Room v5 (SQLite) with explicit migrations |
| Background Work | WorkManager (CoroutineWorker)             |
| DI              | Koin 3.x                                  |
| Architecture    | MVVM (ViewModel + StateFlow)              |
| File Access     | SAF + MediaStore                          |
| ML              | Naive Bayes (Kotlin) + TFLite (LiteRT)    |
| Images          | Coil                                      |
| Animations      | Lottie + XML animations                   |
| Min SDK         | 24 (Android 7.0)                          |
| Target SDK      | 36                                        |

## UI Design System

| Section        | Accent    | Card Tint | Text      |
| -------------- | --------- | --------- | --------- |
| Asset Hub      | `#2E7D32` | `#E8F5E9` | `#1B5E20` |
| Signal Cleaner | `#00897B` | `#E0F2F1` | `#00695C` |
| Developer Mode | `#7B1FA2` | `#F3E5F5` | `#6A1B9A` |
| Utility Hub    | `#1976D2` | `#E3F2FD` | `#1565C0` |

Background: `#F0F0F0` • Status bar: `#0F3814` • Nav bar: `#F0F0F0`

## Permissions

| Permission                           | Purpose                        |
| ------------------------------------ | ------------------------------ |
| `READ_EXTERNAL_STORAGE`              | Legacy file access (API < 33)  |
| `READ_MEDIA_IMAGES/VIDEO/AUDIO`      | Scoped media access (API 33+)  |
| `POST_NOTIFICATIONS`                 | Notification display (API 33+) |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Capture device notifications   |

## Building

1. Open in Android Studio Ladybug (2024.2+)
2. Sync Gradle (File → Sync Project with Gradle Files)
3. Run on device or emulator (API 24+)

## Versioning (x.y.z.w)

| Segment | Meaning                                     |
| ------- | ------------------------------------------- |
| x       | Version number                              |
| y       | 1 = Testing, 2 = Production                 |
| z       | Feature number                              |
| w       | 1 = Feature testing, 2 = Feature production |

## Documentation

- [Architecture Guide](docs/architecture.md) — MVVM layers, DB schema, navigation flow
- [Features Guide](docs/features.md) — Feature descriptions and user flows
- [Feature Tracker](docs/feature_tracker.md) — Version history and roadmap

## License

This project is for educational purposes.
