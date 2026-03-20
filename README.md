# Calendar Alarm — Android

A full-featured Android app that syncs your phone's calendar events and fires loud, full-screen alarms at the start of every event. Built with Kotlin and Jetpack Compose.

## Features

- 📅 **Calendar Sync** — Reads all events from your device calendars
- ⏰ **Loud Alarms** — Full-screen alarm with max-volume sound + vibration at every event start
- 🔄 **Manual Sync** — "Sync Now" floating button to refresh events on demand
- ⏱️ **Auto Daily Sync** — Configurable daily sync time via WorkManager
- 🔕 **Per-Event Mute** — Toggle alarm on/off for individual events
- ⏳ **Configurable Lead Time** — Set alarm 0–30 minutes before event start
- 💤 **Snooze** — 5-minute snooze on alarm screen
- 🔁 **Boot Persistence** — Re-schedules all alarms after device reboot
- 🔒 **Lock Screen Alarm** — Alarm shows over lock screen, wakes device

---

## Prerequisites

| What | Version | Notes |
|------|---------|-------|
| **Android Studio** | 2023.1+ (Hedgehog) | [Download here](https://developer.android.com/studio) |
| **JDK** | 17 | Bundled with Android Studio |
| **Android SDK** | API 34 | Installed via SDK Manager |
| **A Pixel phone** | Android 8.0+ | For testing |

---

## Setting Up Android Studio (Any System)

### Windows

1. Download Android Studio from https://developer.android.com/studio
2. Run the installer — accept defaults
3. On first launch, the **Setup Wizard** runs:
   - Choose **Standard** install type
   - Accept all SDK licenses
   - Wait for SDK and build tools to download
4. When done, you'll see the **Welcome** screen

### macOS

1. Download the `.dmg` from https://developer.android.com/studio
2. Drag Android Studio into **Applications**
3. Open it — first-run wizard same as Windows
4. If prompted, install Intel HAXM or use the ARM emulator for M1/M2

### Linux

1. Download the `.tar.gz` from https://developer.android.com/studio
2. Extract: `tar -xzf android-studio-*.tar.gz`
3. Run: `cd android-studio/bin && ./studio.sh`
4. Follow the Setup Wizard (same as above)
5. Install required libraries if prompted:
   ```bash
   # Ubuntu/Debian
   sudo apt install libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 libbz2-1.0:i386
   ```

---

## Opening the Project

1. Open Android Studio
2. Click **Open** (not "New Project")
3. Navigate to this `android/` folder and select it
4. Wait for Gradle sync to complete (may take a few minutes on first open)
5. If prompted to install missing SDK components, click **Install**

### Gradle Wrapper

On first open, Android Studio will generate the Gradle wrapper. If you get a wrapper error:

```bash
# In the android/ directory
gradle wrapper --gradle-version 8.5
```

Or just let Android Studio handle it — it auto-downloads the correct wrapper.

---

## Testing on Your Pixel Phone

### Step 1: Enable Developer Options

1. On your Pixel, go to **Settings → About Phone**
2. Tap **Build number** 7 times rapidly
3. You'll see "You are now a developer!"

### Step 2: Enable USB Debugging

1. Go to **Settings → System → Developer options**
2. Toggle ON **USB debugging**
3. (Recommended) Toggle ON **Stay awake** — keeps screen on while charging

### Step 3: Connect Phone to Computer

1. Plug your Pixel into your computer via USB-C cable
2. On the phone, a dialog appears: **"Allow USB debugging?"**
3. Check **"Always allow from this computer"** and tap **Allow**

### Step 4: Run the App

1. In Android Studio, your Pixel should appear in the device dropdown (top toolbar)
   - It shows as something like **"Pixel 7 (API 34)"**
2. Click the green **▶ Run** button (or press `Shift+F10`)
3. The app builds, installs, and launches on your phone

### Troubleshooting Phone Connection

| Problem | Fix |
|---------|-----|
| Phone not showing up | Try a different USB cable (must support data, not charge-only) |
| "Unauthorized" device | Revoke USB debugging auth on phone, reconnect, tap Allow again |
| ADB not found | In Android Studio: **Tools → SDK Manager → SDK Tools** → ensure "Android SDK Platform-Tools" is installed |
| Windows driver issue | Install [Google USB Driver](https://developer.android.com/studio/run/win-usb) via SDK Manager |

### Wireless Debugging (Optional, Android 11+)

1. Enable **Wireless debugging** in Developer options
2. In Android Studio terminal:
   ```bash
   adb pair <ip>:<port>   # use pairing code from phone
   adb connect <ip>:<port>
   ```

---

## Building a Release APK

To build an APK you can share or sideload:

1. In Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. The APK is generated at: `app/build/outputs/apk/debug/app-debug.apk`
3. Transfer to your phone and install (enable "Install from unknown sources" if needed)

For a signed release APK:

1. **Build → Generate Signed Bundle / APK**
2. Choose **APK**
3. Create a new keystore or use an existing one
4. Build the release variant

---

## Permissions the App Needs

| Permission | Why | When Requested |
|-----------|-----|----------------|
| `READ_CALENDAR` | Read events from your calendar | On first launch |
| `POST_NOTIFICATIONS` | Show alarm notifications (Android 13+) | On first launch |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Fire alarms at exact event times | Auto-granted for alarm apps |
| `RECEIVE_BOOT_COMPLETED` | Re-schedule alarms after reboot | Auto-granted |
| `VIBRATE` | Vibration during alarm | Auto-granted |
| `WAKE_LOCK` | Wake device for alarm | Auto-granted |

---

## Project Structure

```
android/
├── build.gradle.kts              # Root Gradle config
├── settings.gradle.kts           # Project settings
├── gradle.properties             # Gradle properties
├── app/
│   ├── build.gradle.kts          # App module config (dependencies, SDK versions)
│   └── src/main/
│       ├── AndroidManifest.xml   # App manifest (permissions, activities, receivers)
│       ├── java/com/sahil/calendaralarm/
│       │   ├── CalendarAlarmApplication.kt  # App init, notification channel
│       │   ├── MainActivity.kt              # Entry point, permission handling
│       │   ├── model/
│       │   │   └── CalendarEvent.kt         # Event data class
│       │   ├── data/
│       │   │   ├── CalendarRepository.kt    # Reads from CalendarContract
│       │   │   └── PreferencesManager.kt    # DataStore for settings
│       │   ├── alarm/
│       │   │   ├── AlarmScheduler.kt        # Schedules exact alarms
│       │   │   ├── AlarmReceiver.kt         # Receives alarm broadcasts
│       │   │   ├── AlarmActivity.kt         # Full-screen alarm UI
│       │   │   └── BootReceiver.kt          # Re-schedules on reboot
│       │   ├── sync/
│       │   │   └── SyncWorker.kt            # WorkManager daily sync
│       │   ├── viewmodel/
│       │   │   └── MainViewModel.kt         # UI state management
│       │   └── ui/
│       │       ├── theme/                   # Material 3 theme (Color, Type, Theme)
│       │       ├── navigation/              # Bottom nav + NavHost
│       │       └── screens/                 # EventListScreen, SettingsScreen
│       └── res/
│           ├── values/                      # Strings, colors, themes
│           ├── xml/                         # Backup rules
│           └── drawable/                    # Vector drawables
```

---

## How It Works

1. **On launch**: Requests calendar + notification permissions, then syncs events
2. **Sync**: Reads upcoming 7 days of events via `CalendarContract.Instances`, schedules an `AlarmManager` exact alarm for each non-muted event
3. **Alarm fires**: `AlarmReceiver` launches `AlarmActivity` — a full-screen red pulsing overlay with max-volume alarm sound and vibration
4. **Dismiss/Snooze**: User taps Dismiss to stop, or Snooze to re-fire in 5 minutes
5. **Auto sync**: WorkManager runs daily at configured time, re-syncing events and alarms
6. **Reboot**: `BootReceiver` re-schedules all alarms when the device restarts

---

## Customization

- **Alarm sound**: Currently uses the device's default alarm ringtone. To use a custom sound, place an audio file in `res/raw/` and update `AlarmActivity.startAlarmSound()`
- **Snooze duration**: Change the `5 * 60 * 1000` in `AlarmActivity.snoozeAlarm()`
- **Days ahead**: Change `daysAhead` parameter in `CalendarRepository.getUpcomingEvents()`
- **Lead time options**: Edit the `leadTimeOptions` list in `SettingsScreen.kt`
