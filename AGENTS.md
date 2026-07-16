# Repository Guidelines

## Project Structure & Module Organization

This is a single-module Android application built with Kotlin and Jetpack Compose. Code lives in `app/src/main/java/com/example/`; `MainActivity.kt` contains the UI flow, while device, kiosk, update, serial, and API integrations use manager/service classes. Compose theme files are under `ui/theme/`. Android resources belong in `app/src/main/res/`, including the kiosk video in `res/raw/` and device configuration in `res/xml/`.

Local JVM tests are in `app/src/test/java/`; screenshot baselines are stored in `app/src/test/screenshots/`. On-device tests live in `app/src/androidTest/java/`. Treat `MicrolifeSDK-Android-V3.0.7-260206V1r/` as vendor reference material; the application consumes its AAR from `app/libs/`.

## Build, Test, and Development Commands

The repository includes a Windows Gradle wrapper. Run these from the project root:

- `gradlew.bat assembleDebug` builds a debug APK.
- `gradlew.bat testDebugUnitTest` runs JUnit, Robolectric, Compose, and Roborazzi JVM tests.
- `gradlew.bat connectedDebugAndroidTest` runs instrumentation tests on a connected emulator or device.
- `gradlew.bat lintDebug` performs Android static analysis.
- `gradlew.bat assembleRelease` creates the signed release artifact; required signing variables must be configured first.

On non-Windows systems, import the project into Android Studio and use its bundled Gradle, because no Unix `gradlew` script is committed. Use JDK 17 with Android SDK 36.

## Coding Style & Naming Conventions

Follow Kotlin conventions and Android Studio formatting: two-space indentation in existing Kotlin files, trailing commas where they improve diffs, and explicit imports. Use `PascalCase` for classes, enums, and composables; `camelCase` for functions and properties; and `UPPER_SNAKE_CASE` for enum constants. Keep composables focused, move hardware/network behavior into dedicated managers or services, and place user-visible text in resources or the existing translation model.

## Testing Guidelines

Name test classes `*Test.kt` and methods after observable behavior. Use JUnit for logic, Robolectric for Android behavior, Compose test APIs for UI, and Roborazzi for visual regressions. Update screenshot baselines only after visually reviewing the change. Add instrumentation tests when behavior depends on Bluetooth, USB, permissions, or device-owner APIs.

## Commit & Pull Request Guidelines

Recent history uses concise Conventional Commit prefixes such as `feat:`, `fix:`, and `chore:`. Keep each commit scoped to one change. Pull requests should explain behavior and verification, link the relevant issue, and include screenshots for UI changes. Call out hardware requirements, manifest/permission changes, API contract changes, and release `versionCode` updates.

## Security & Configuration

Copy `.env.example` to `.env` and never commit API keys, `local.properties`, keystores, or signing passwords. Release signing reads `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD`. Follow `DEPLOYMENT.md` for device-owner provisioning and release activation.
