# Feature Tracking Logs

## Versioning Scheme: x.y.z.w

| Segment | Meaning           | Values                      |
| ------- | ----------------- | --------------------------- |
| **x**   | Version number    | 1, 2, 3...                  |
| **y**   | App lifecycle     | 1 = Testing, 2 = Production |
| **z**   | Feature number    | Sequential feature count    |
| **w**   | Feature lifecycle | 1 = Testing, 2 = Production |

**Current Version: 2.1.4.1**
(Version 2, App in Testing, Feature 4 — UI Design System, Feature in Testing)

---

## Version History

| Version     | Feature (z)              | Description                                                                                                                                   |
| ----------- | ------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------- |
| 2.1.1.1     | z=1 Core Engine          | File scanning (SAF + MediaStore), keyword classification, notification capture, reset to Downloads                                            |
| 2.1.1.2     | z=1 Core Engine          | Core engine tested and stable                                                                                                                 |
| 2.1.2.1     | z=2 UI Overhaul          | Material 3, animated splash, premium colors, card UI, activity transitions                                                                    |
| 2.1.2.2     | z=2 UI Overhaul          | UI overhaul tested and stable                                                                                                                 |
| 2.1.3.1     | z=3 ML + Analytics       | Dual-model ML engine (Naive Bayes + TFLite), folder-card browsing, re-sync to original locations, Signal analytics dashboard, DB v5 migration |
| 2.1.3.2     | z=3 ML + Analytics       | ML and analytics tested and stable                                                                                                            |
| **2.1.4.1** | **z=4 UI Design System** | **Section-specific color themes, system bar overlap fix, tinted card backgrounds, text contrast fixes**                                       |
| 2.1.4.2     | z=4 UI Design System     | _(after testing)_                                                                                                                             |
| 2.1.5.1     | z=5 Device Analyser      | _(planned)_ Real-time monitoring, charts, PDF reports, historical tracking                                                                    |
| 2.1.5.2     | z=5 Device Analyser      | _(after testing)_                                                                                                                             |
| 2.2.5.2     | —                        | App moves to Production after all features tested                                                                                             |

---

## Feature Tracker

| Feature                | z   | Current w   | Version     |
| ---------------------- | --- | ----------- | ----------- |
| Core Engine            | 1   | 2 (prod)    | 2.1.1.2     |
| UI Overhaul            | 2   | 2 (prod)    | 2.1.2.2     |
| ML + Analytics         | 3   | 2 (prod)    | 2.1.3.2     |
| UI Design System       | 4   | 1 (testing) | **2.1.4.1** |
| Device Analyser        | 5   | — (planned) | 2.1.5.1     |
| Content Classification | 6   | — (planned) | 2.1.6.1     |
| Video Editor           | 7   | — (planned) | 2.1.7.1     |
| File Editor            | 8   | — (planned) | 2.1.8.1     |
| Data Extractor (OCR)   | 9   | — (planned) | 2.1.9.1     |
| Price Tracker          | 10  | — (planned) | 2.1.10.1    |

---

## Future Scope

- Offline model training with device data
- Content-aware file classification (file headers, metadata, EXIF)
- Alert system for high RAM/battery thresholds
- Advanced trend predictions from historical snapshots
