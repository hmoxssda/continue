# Building the Network Analyzer sample with AIDE

This guide explains how to import and build the **network-analyzer-app** project inside the AIDE Android IDE directly on an Android device.

## 1. Prepare your device
- Install the latest AIDE app from the Play Store.
- Grant the app storage permissions and, on Android 11+, enable **Manage all files** access for AIDE so it can read the project directory.
- Ensure you have at least 2 GB of free storage. Large Gradle syncs can require additional space.

## 2. Copy the project to the device
1. On your development machine, open this repository and locate `manual-testing-sandbox/network-analyzer-app/`.
2. Zip the entire folder so that the archive contains `app/`, `build.gradle.kts`, and `settings.gradle.kts` at its root.
3. Transfer the zip to your device (USB, cloud drive, or ADB `push`).
4. On the device, extract the zip into `Internal storage/AIDE/AppProjects/`. The resulting path should look like:
   ```
   Internal storage/AIDE/AppProjects/network-analyzer-app/
   ├── app/
   ├── build.gradle.kts
   └── settings.gradle.kts
   ```

## 3. Open the project in AIDE
1. Launch AIDE and choose **Open Project**.
2. Navigate to `AppProjects/network-analyzer-app` and select `build.gradle.kts` when prompted.
3. AIDE will convert the Gradle Kotlin DSL scripts to Groovy the first time it loads. Accept the conversion when asked, or manually provide Groovy equivalents (see below).

### Optional: Provide Groovy build scripts manually
If the automatic conversion fails, create two new files inside the project directly on the device:
- `build.gradle`
- `app/build.gradle`

Populate them with the Groovy versions of the scripts (you can translate from the existing Kotlin DSL using Android Studio). Delete the `.kts` files afterward so AIDE uses the Groovy ones.

## 4. Resolve dependencies
- AIDE ships with a pared-down Gradle distribution. Open `Project Settings` → `Build` and ensure the Gradle version matches `8.x`.
- In `Project Settings` → `Android SDK`, download the API level declared in `compileSdk` (currently 34) if it is not already installed.
- Tap **Sync Gradle**. Wait for the sync to finish; warnings about missing `google()` or `mavenCentral()` repositories usually mean the device is offline.

## 5. Run the app
1. From the main editor, open `MainActivity.kt` to ensure sources index correctly.
2. Tap the **Run** triangle. Choose **Run app**.
3. Grant the VPN permission prompt at runtime so the proxy service can start.
4. Monitor the **LogCat** panel for build or runtime errors. Common issues:
   - *"Cannot find symbol"* → Sync again after the initial Gradle import.
   - *Dexer errors* → Ensure `minSdk` in `app/build.gradle` matches the device API level.

## 6. Debugging tips specific to AIDE
- Use **Menu → More → Gradle Console** to view build output.
- If Kotlin compilation fails due to memory, enable **Use DX compiler** in settings and close other apps to free RAM.
- To update dependencies, edit `build.gradle` files and run **Sync Gradle** again.

## 7. Keeping the project up to date
Whenever the repository changes on your computer:
1. Re-create the zip for `network-analyzer-app/`.
2. Replace the directory under `AppProjects/` on the device (or use a Git client on Android if preferred).
3. Re-open AIDE and sync.

Following these steps will let you build, run, and iterate on the sample entirely within AIDE.
