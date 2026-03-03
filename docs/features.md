# Features Guide

## Version 1.0.0 Features

### 1. Digital Asset Hub

### Overview

The Digital Asset Hub scans, categorizes, and organizes files from user-selected source folders into a structured root directory. Files are classified using smart keyword matching and file extension analysis.

### Setup (Onboarding)

1. **Select scan mode:**
   - **Pick Folders** — Choose specific folders to scan (uses SAF, no extra permissions needed).
   - **Complete Scan** — Scan all images, videos, and audio on the device (requires MediaStore permissions).
2. **Select root directory** — Choose where organized files will be stored.
3. **Move or Copy** — Toggle whether to delete originals after organizing (move) or keep them (copy).
4. **Optional:** Enable notification capture and background periodic scans.

### Classification Logic

Files are classified by `DigitalAssetHub.classifyByName()`:

| Category        | Keywords / Rules                                       | Target Folder                         |
| --------------- | ------------------------------------------------------ | ------------------------------------- |
| Study Material  | lecture, assignment, exam, notes, quiz, textbook, .pdf | `Study Material/`                     |
| Digital Clutter | meme, whatsapp, temp, junk, screenshot                 | `Clutter/`                            |
| Uncategorized   | Everything else                                        | `{extension}/` (e.g., `jpg/`, `mp3/`) |

### Re-Sync

Trigger a new scan from the Dashboard (card or FAB). Only unprocessed files are picked up — already-organized files are skipped.

### Reset

From the Digital Asset Hub screen, tap "Reset and Start Over" to:

1. Restore all organized files to `Downloads/NeatNest_Restored/`.
2. Clear the database and app preferences.
3. Return to the onboarding setup screen.

---

### 2. Signal Noise Cleaner

### Overview

Captures all device notifications in real-time, classifies them by importance, and displays them in a scrollable list.

### Priority Levels

| Level           | Source                                                    |
| --------------- | --------------------------------------------------------- |
| Most Important  | Channel importance HIGH or notification priority HIGH/MAX |
| Normal          | Channel importance DEFAULT or priority DEFAULT            |
| Low             | Channel importance LOW                                    |
| Least Important | Channel importance MIN                                    |
| Blocked         | Channel importance NONE                                   |

### Clear All Noise

Tap the "Clear All Noise" button → confirm in the dialog → all stored notifications are permanently deleted. The "Signals Cleaned" counter on the dashboard resets to 0 automatically via Flow observation.

### Granting Access

The notification listener must be explicitly enabled in Android system settings. The app provides a button and FAB that navigate directly to the correct settings page.

---

### 3. Dev Mode

### Overview

A demonstration screen showcasing Android UI components: Toolbars, Menus, Fragments, and Dialogs.

### Components Demonstrated

| Component              | How to Trigger                                                      |
| ---------------------- | ------------------------------------------------------------------- |
| **Options Menu**       | Visible as icons (Search, Settings) in the green toolbar at the top |
| **Context Menu**       | Long-press the "Long press for context menu" text                   |
| **Pop-up Menu**        | Tap the "SHOW POPUP MENU" button                                    |
| **Fragment Lifecycle** | Automatically visible at the bottom — logs callbacks in real-time   |
| **AlertDialog**        | Tap "Delete" in the context menu or "Archive" in the popup menu     |

### Menu Icons

All three menu types include icons defined in `res/drawable/`:

- `ic_search`, `ic_settings` (Options Menu)
- `ic_delete`, `ic_share` (Context Menu)
- `ic_archive`, `ic_move` (Pop-up Menu)

The toolbar color uses the app's `colorPrimary` (`#1B5E20`, dark green).

---

### 4. Utility Hub

### Overview

A tools hub with rescan functionality and placeholders for upcoming features.

### Active Features

- **Rescan Files** — Triggers a manual `AssetScannerWorker` scan with confirmation dialog.

### Upcoming (Placeholder)

- Video Editor
- File Editor
- Data Extractor
- Price Tracker

---

### 5. Dashboard (Main Screen)

### System Status

Displays two live counters powered by Room `Flow<Int>` queries:

- **Assets Organized** — Total files in the `processed_files` table.
- **Signals Cleaned** — Total notifications in the `processed_notifications` table.

### Recent Activity

A RecyclerView showing the latest 5 file organizations and 5 notification captures. Tapping an item opens an AlertDialog with timestamp details.

### Quick Actions

- **Asset Hub** card → File organizer
- **Signal Cleaner** card → Notification viewer
- **Re-Sync** card → Manual scan
- **Utility Hub** card → Tools
- **Dev Mode** card → Menu and fragment demo
- **FAB** → Quick rescan trigger
