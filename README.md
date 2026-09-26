# Cloudihub ☁️

A modern, cloud-themed premium Android application built with Kotlin and Jetpack Compose. Featuring custom organic cloud shapes, dynamic frosted glassmorphic bottom navigation, download hub, voice-activated searching, custom videos, sites, web browsing, music bubble player, and secure private vault.

## ✨ Features

- **Sky & Cloud UI**: Organic drifting cloud visual effects with Material Design 3 theming.
- **Glassmorphic Navigation Bar**: Ultra-responsive floating pill bar with instant tactile feedback.
- **Video & Stream Hub**: Dual-mode stream extractor and player with picture-in-picture / expanded controls.
- **Music Hub & Floating Bubble**: Integrated audio playback with floating draggable bubble across screens.
- **Full Cloud Browser**: Integrated WebView browser with quick shortcuts, back/forward history, and security sandboxing.
- **Downloads Hub**: Complete download manager with progress tracking, pause/resume, and offline playback.
- **Private Vault**: Biometric & encrypted folder security for personal documents and media.
- **AI Voice Assistant**: Voice search and copilot for fast navigation and search suggestions.

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.2.10
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with Kotlin Coroutines & StateFlow
- **Media**: ExoPlayer (AndroidX Media3) & Coil
- **Animation**: Lottie Android & Compose Animation
- **Build System**: Gradle 8.13 with Android Gradle Plugin (AGP) 9.1.1

## 🚀 Building & Generating Debug APK

### Via GitHub Actions (Automated CI/CD)
Every push to `main` automatically triggers the **Android CI** workflow, which compiles the project and generates a ready-to-install debug APK artifact.
You can also manually trigger the build from the **Actions** tab on GitHub:
1. Go to the **Actions** tab in this repository.
2. Select **Android CI**.
3. Click **Run workflow**.
4. Once completed, download `Cloudihub-Debug-APK` from the workflow summary.

### Local Build
```bash
./gradlew assembleDebug
```
The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`
