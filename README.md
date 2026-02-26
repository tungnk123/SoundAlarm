# SoundAlarm

> **This project is a demo of the [mobile-best-practices](https://github.com/tungnk123/mobile-best-practices) agent skill.**
> Want to build production-quality Android apps the same way? **[Check out the skill →](https://github.com/tungnk123/mobile-best-practices)**

A feature-rich Android alarm application built with modern Android development practices. SoundAlarm goes beyond a basic alarm clock by offering custom music playlists, cognitive wake-up challenges, and detailed usage statistics — all delivered through a clean, Material 3 interface.

---

## Table of Contents

- [Built With mobile-best-practices](#built-with-mobile-best-practices)
- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Data Layer](#data-layer)
- [Navigation](#navigation)
- [Alarm Scheduling](#alarm-scheduling)
- [Getting Started](#getting-started)
- [Requirements](#requirements)

---

## Built With mobile-best-practices

SoundAlarm was built as a **real-world demonstration** of the **[mobile-best-practices](https://github.com/tungnk123/mobile-best-practices)** agent skill — a searchable database of **2,024 Android best practices** optimized for Jetpack Compose development.

The skill covers architecture patterns, UI patterns, performance rules, security practices, testing patterns, and ready-to-use Gradle declarations. Every architectural decision in this project — Clean Architecture layers, MVVM, Hilt DI, Room schema design, Foreground Service setup — was guided by that knowledge base.

**Use the skill yourself to build Android apps at the same quality level:**

### **[github.com/tungnk123/mobile-best-practices](https://github.com/tungnk123/mobile-best-practices)**

---

## Features

### Alarm Management
- Create, edit, and delete alarms with custom labels
- One-time or repeating alarms by day of week
- Per-alarm volume control and configurable fade-in duration
- Auto-delete after firing (one-shot mode)
- Random music playback from an assigned playlist group

### Music & Sound
- Browse and select **local audio files** from device storage
- Fetch **remote alarm sounds** via REST API
- Organize tracks into **Playlist Groups** for per-alarm assignment
- In-app audio preview with ExoPlayer
- Drag-and-drop track reordering (Reorderable library)

### Wake-Up Challenges
Alarms can require completing a challenge before dismissal:

| Challenge | Description |
|-----------|-------------|
| **Math** | Solve arithmetic problems (Easy / Medium / Hard, configurable count) |
| **Shake** | Shake the device a configurable number of times |
| **Walk** | Reach a step-count goal using the device pedometer |
| **Memory** | Memorize and recall a code sequence (configurable length) |

### Alarm Trigger
- Full-screen `AlarmTriggerActivity` launches over the lock screen (`showWhenLocked`, `turnScreenOn`)
- Foreground `AlarmService` handles media playback with a persistent notification
- Snooze action available from the notification and trigger screen
- Configurable snooze count limit

### Statistics
- Tracks every alarm **Fired**, **Dismissed**, and **Snoozed** event
- Summary cards: total alarms, active alarms, total dismissed, total snoozed
- Challenge breakdown with a proportional progress bar per dismiss method
- Chronological recent-events feed
- One-tap clear all history

### Settings
| Category | Options |
|----------|---------|
| Snooze | Max snooze count, alarm duration |
| Notifications | Pre-alarm notification, lead time (minutes) |
| Sound | Default volume, fade-in duration, fade-out duration |
| Voice | Read time aloud (TTS), announcement template, custom voice clip, voice-before-music order |
| Power | Trigger alarm when device is powered off (`AlarmManager.setAlarmClock`) |
| Accessibility | Language selection |
| Challenges | Math difficulty, problem count, shake count, step goal, memory code length |

---

## Architecture

SoundAlarm follows **Clean Architecture** with a strict three-layer separation, combined with the **MVVM** presentation pattern.

```
┌─────────────────────────────────────────────┐
│              Presentation Layer             │
│  Compose Screens · ViewModels · Services    │
│  AlarmTriggerActivity · AlarmReceiver       │
└──────────────────┬──────────────────────────┘
                   │ calls Use Cases / Repositories
┌──────────────────▼──────────────────────────┐
│               Domain Layer                  │
│  Models · Repository Interfaces             │
│  Use Cases · Scheduler Interface            │
│  SnoozeManager                              │
└──────────────────┬──────────────────────────┘
                   │ implemented by
┌──────────────────▼──────────────────────────┐
│                Data Layer                   │
│  Room (local DB) · Retrofit (remote API)    │
│  DataStore (settings) · MediaStore          │
│  Repository Implementations · Scheduler     │
└─────────────────────────────────────────────┘
```

### Domain Layer
Pure Kotlin — no Android framework dependencies.

- **Models**: `Alarm`, `AppSettings`, `MusicTrack`, `PlaylistGroup`, `AlarmEvent`, `DismissMethod`
- **Repository interfaces**: `AlarmRepository`, `PlaylistRepository`, `PlaylistGroupRepository`, `MusicRepository`, `SettingsRepository`, `StatisticsRepository`, `AlarmDayTrackRepository`
- **Use Cases**: `GetAlarmsUseCase`, `SaveAlarmUseCase`, `DeleteAlarmUseCase`, `ToggleAlarmUseCase`
- **Scheduler interface**: `AlarmScheduler` — decouples scheduling logic from `AlarmManager`

### Data Layer
- **Room** repositories implement domain interfaces and map entities ↔ domain models
- **`AndroidAlarmScheduler`** implements `AlarmScheduler` using `AlarmManager`
- **`SettingsDataStoreManager`** handles persistence via Jetpack DataStore
- **`LocalMusicRepositoryImpl`** queries `MediaStore` for on-device audio
- **Retrofit** service (`AlarmApiService`) fetches remote sound metadata

### Presentation Layer
- Each screen has a corresponding `ViewModel` that exposes `StateFlow` / `SharedFlow`
- UI state is collected with `collectAsStateWithLifecycle`
- `AlarmService` runs as a `FOREGROUND_SERVICE` of type `mediaPlayback`
- `AlarmReceiver` is a `BroadcastReceiver` that handles boot-completed rescheduling and pre-alarm notifications

---

## Tech Stack

| Category | Library |
|----------|---------|
| UI | Jetpack Compose, Material3 |
| DI | Hilt |
| Database | Room 7 |
| Preferences | DataStore Preferences |
| Networking | Retrofit 2, OkHttp, Kotlinx Serialization |
| Media | Media3 ExoPlayer |
| Image Loading | Coil |
| Navigation | Compose Navigation |
| Async | Kotlin Coroutines, Flow |
| Testing | JUnit 4, MockK, Turbine, Coroutines Test |
| Build | KSP, Gradle Version Catalogs |

---

## Project Structure

```
app/src/main/java/com/tungnk123/soundalarm/
│
├── data/
│   ├── local/
│   │   ├── dao/            # AlarmDao, PlaylistDao, PlaylistGroupDao,
│   │   │                   # AlarmDayTrackDao, AlarmEventDao
│   │   ├── datastore/      # SettingsDataStoreManager
│   │   └── entity/         # Room entity classes
│   ├── remote/
│   │   ├── api/            # AlarmApiService (Retrofit)
│   │   └── dto/            # AlarmSoundDto
│   ├── repository/         # Repository implementations
│   └── scheduler/          # AndroidAlarmScheduler
│
├── di/
│   ├── AppModule.kt        # Room, DAOs, DataStore bindings
│   └── NetworkModule.kt    # Retrofit / OkHttp bindings
│
├── domain/
│   ├── model/              # Alarm, AppSettings, DismissMethod, …
│   ├── repository/         # Repository interfaces
│   ├── scheduler/          # AlarmScheduler interface
│   ├── snooze/             # SnoozeManager
│   └── usecase/            # GetAlarmsUseCase, SaveAlarmUseCase, …
│
├── presentation/
│   ├── alarm/              # AlarmDetailScreen + ViewModel + components
│   ├── challenge/          # ChallengeScreen + ViewModel + components
│   ├── home/               # HomeScreen + ViewModel + components
│   ├── music/              # MusicSelectionScreen + ViewModel + audio player
│   ├── navigation/         # NavGraph, Screen, BottomNavItem
│   ├── playlist/           # PlaylistGroupsScreen, PlaylistScreen + ViewModels
│   ├── scheduler/          # AlarmReceiver (BroadcastReceiver)
│   ├── service/            # AlarmService (ForegroundService)
│   ├── settings/           # SettingsScreen + ViewModel + components
│   ├── sounds/             # SoundsScreen (remote library)
│   ├── splash/             # SplashScreen
│   ├── statistics/         # StatisticsScreen + ViewModel
│   └── trigger/            # AlarmTriggerActivity + Screen + ViewModel
│
├── ui/theme/               # Color, Type, Theme (Material3)
└── util/                   # AppConstants, LocaleManager, Resource, TimeFormatter
```

---

## Data Layer

### Room Database — `SoundAlarmDatabase` (v7)

| Entity | Purpose |
|--------|---------|
| `AlarmEntity` | Core alarm configuration |
| `PlaylistEntity` | Individual music tracks within a group |
| `PlaylistGroupEntity` | Named collections of tracks |
| `AlarmDayTrackEntity` | Per-weekday track assignment for an alarm |
| `AlarmEventEntity` | Fired / Dismissed / Snoozed history records |

Schema snapshots are exported to `app/schemas/` for migration auditability.

### DataStore
`AppSettings` is serialized as `Preferences` via `SettingsDataStoreManager`, providing a type-safe, coroutine-friendly API for global settings.

---

## Navigation

Bottom navigation bar with five top-level destinations:

| Tab | Route | Description |
|-----|-------|-------------|
| Alarms | `home` | Alarm list, toggle, add, delete |
| Sounds | `sounds` | Browse remote alarm sound library |
| Challenge | `challenge` | Configure and test wake-up challenges |
| Statistics | `statistics` | Alarm event history and breakdown |
| Settings | `settings` | App-wide preferences |

Additional full-screen destinations:

- `alarm_detail/{alarmId}` — Create or edit a single alarm
- `music_selection/{alarmId}` — Pick a track for an alarm
- `playlist_groups` / `playlist/{groupId}` — Manage playlist groups and tracks
- `AlarmTriggerActivity` — Lock-screen overlay launched by `AlarmService`

---

## Alarm Scheduling

`AndroidAlarmScheduler` computes the next trigger time (respecting repeat-day configuration) and schedules via `AlarmManager`:

- **Power-off support**: Uses `AlarmManager.setAlarmClock` so the alarm appears in the system clock and can wake a powered-off device.
- **Exact alarms**: Uses `setExactAndAllowWhileIdle` on API < 31; checks `canScheduleExactAlarms()` on API 31+.
- **Pre-alarm notifications**: A secondary broadcast fires N minutes before the alarm to give the user advance warning.
- **Boot rescheduling**: `AlarmReceiver` listens for `BOOT_COMPLETED` to reschedule all active alarms after a device restart.

---

## Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/tungnk123/SoundAlarm.git
   cd SoundAlarm
   ```

2. **Open in Android Studio** (Ladybug or newer recommended)

3. **Build & run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or run the `app` configuration directly from Android Studio.

4. **Permissions**
   On first launch the app will request:
   - `POST_NOTIFICATIONS` — alarm notifications
   - `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` — precise scheduling
   - `READ_MEDIA_AUDIO` — local music picker
   - `ACTIVITY_RECOGNITION` — step-counter challenge

---

## Requirements

| Item | Requirement |
|------|-------------|
| Minimum SDK | API 28 (Android 9 Pie) |
| Target SDK | API 36 |
| Language | Kotlin |
| Build system | Gradle (Kotlin DSL) with Version Catalogs |
| IDE | Android Studio Ladybug+ |
