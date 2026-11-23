# TCL TV PIN Demo — source (not a built APK)

**Important:** This is an experimental, installable Android TV demonstration, not a full-device lock. A normal sideloaded app cannot prevent the user from pressing Home, changing HDMI input, rebooting, uninstalling the app, or accessing system settings. It also does NOT provide remote lock/unlock over VPN yet.

## Features
- Leanback launcher tile for Android TV / Google TV.
- Set a local numeric PIN (4–8 digits) on the TV and store a salted PBKDF2-HMAC-SHA256 PIN verifier.
- Show a fullscreen demo lock screen while the app is open.
- Unlock or relock inside the app.

## Build
Open the folder in Android Studio (JDK 17 / Android SDK 35) and choose Build > Build APK(s). Alternatively install Gradle 8.10.2 and run `gradle :app:assembleDebug` with Android SDK available.

The APK will be `app/build/outputs/apk/debug/app-debug.apk`.

The included GitHub Actions workflow builds the APK in the GitHub cloud if you push this source to your own repository and manually run the workflow.

## Install on TV (no ADB)
Copy the resulting APK to a USB drive, connect it to TCL Google TV, open a file manager and select the APK. Allow the file manager to install unknown apps if prompted.

## Real full-device lock
For blocking the physical remote's HOME button and preventing escape, investigate Android Enterprise Device Owner / lock-task mode on the actual firmware. This can require provisioning/reset and is not supported by this demo. Do not reset the family TV without a verified recovery plan.
