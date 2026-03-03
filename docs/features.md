# Features Guide

**Current Version: 2.1.4.1**

## 1. Dashboard (Main Screen)

Clean hub launcher on a light grey (#F0F0F0) background with branding header and 4 color-coded navigation cards:

- 🟢 **Digital Asset Hub** — File organization with ML classification (green tint)
- 🩵 **Signal Noise Cleaner** — Notification analytics and management (teal tint)
- 🟣 **Developer Mode** — Menus, fragments, and dialog demos (purple tint)
- 🔵 **Utility Hub** — Placeholder tools (blue tint)

Each card has a 4dp colored accent bar, section icon, and subtitle. Staggered entrance animations on load. Logo fade-in animation.

---

## 2. Splash Screen

Animated splash with NeatNest logo, fade-in effect, and material progress indicator. Auto-navigates to Main Dashboard after 2 seconds.

---

## 3. Digital Asset Hub

### Onboarding (First Launch)

1. **Scan mode** — Pick Folders (SAF) or Complete Scan (MediaStore)
2. **Root directory** — Where organized files are stored
3. **Move or Copy** — Delete originals or keep them
4. **Classification engine** — Naive Bayes (default) or TFLite (advanced)
5. **Optional** — Enable notification capture and background scans

### ML-Powered Classification

| Category        | Subdirectory      |
| --------------- | ----------------- |
| Study Material  | `Study Material/` |
| Work Documents  | `Work Documents/` |
| Media           | `Media/`          |
| Digital Clutter | `Clutter/`        |
| Uncategorized   | `{extension}/`    |

- Dual engine: **NaiveBayesClassifier** (pure Kotlin, pre-trained word priors) or **TFLiteClassifier** (character-level neural net from `assets/file_classifier.tflite`)
- Each file records: `engineUsed`, `category`, `originalUri`, `targetPath`

### Folder Card View

Category folders displayed as green-tinted cards (#E8F5E9) with file counts. Tap to drill down into files within that category. Back button to return to folder view.

### File Info

Tap the info button on any file to see: filename, extension, engine used, category, original path, current path.

### Re-Sync (Full Reset)

Restores all files to their original locations (creates directories if they no longer exist), empties root directory, clears all database records and SharedPreferences, and resets to onboarding for a fresh start.

---

## 4. Signal Noise Cleaner

### Analytics Dashboard

Dark teal (#00695C) analytics card showing:

- Total notification count (white text)
- High priority count (light red)
- Normal priority count (light green)
- Low priority count (light yellow)
- Top 5 applications by notification volume

### Notification List

Teal-tinted cards (#E0F2F1) with app icon, notification title, app package name, and priority badge.

### Actions

- **Grant Access** → System notification listener settings
- **Clear All Noise** → Delete all captured notifications with confirmation dialog

---

## 5. Developer Mode

Demonstrates Android UI concepts:

- **Toolbar** with options menu (search, settings)
- **Context menu** on long-press (delete with confirmation dialog, share)
- **Popup menu** on button click (archive with dialog, move)
- **Fragment lifecycle** (`LifecycleFragment` with lifecycle callbacks)
- **AlertDialogs** for delete and archive confirmations

> _Planned replacement: Device Analyser (v2.1.5.1) with real-time monitoring, charts, and reports_

---

## 6. Utility Hub

Placeholder cards for upcoming tools (50% opacity, COMING SOON badges):

- Video Editor
- File Editor
- Data Extractor (OCR)
- Price Tracker

---

## 7. ML Classification Engine

### Naive Bayes (Default)

Pure Kotlin classifier with pre-trained word frequency priors across 4 categories. Uses Laplace smoothing and extension-based fallback. Zero external dependencies.

### TFLite (Advanced)

Character-level dense neural network loaded from `assets/file_classifier.tflite`. Trained via Python script:

```bash
cd scripts && python train_classifier.py
```

Falls back to Naive Bayes if model file is missing or inference fails.

---

## 8. Background Services

### AssetScannerWorker (WorkManager)

Two-pass CoroutineWorker: (1) ingest files from source, (2) classify via ML engine into subdirectories.

### ResetWorker (WorkManager)

Restores files to original locations, empties root, clears DB + prefs, resets onboarding.

### NotificationService (NotificationListenerService)

Real-time notification capture, priority classification (High/Normal/Low), storage in Room database.

---

## Future Scope

- **Device Analyser** (v2.1.5.1) — Real-time RAM, storage, battery, CPU, network monitoring
- **Content-based classification** — File headers, metadata, EXIF for better accuracy
- **Offline model training** — Train custom models on user's own data
- **Utility tools** — Video editor, file editor, OCR extractor, price tracker
