# NP Player 🎬🎵

A modern, high-performance, and feature-packed Video & Music Player for Android, powered by **Jetpack Compose** and **AndroidX Media3 (ExoPlayer)**. Designed with a sleek Dark AMOLED / Monochromatic aesthetic with vibrant signature accents, intuitive gesture controls, and customizable interfaces.

[![Release](https://img.shields.io/github/v/release/arybishal/NP-Player?style=for-the-badge&color=FFE500&labelColor=121212)](https://github.com/arybishal/NP-Player/releases/latest)
[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue?style=for-the-badge)](LICENSE)

---

## ✨ Features

### 🎬 Powerful Video Player
- **Media3 / ExoPlayer Core**: Supports MKV, MP4, WebM, AVI, FLV, TS, and HLS/DASH network streams.
- **Hardware & Software Decoding**: Toggle HW/SW decoders on the fly, with automated codec diagnostics.
- **Gesture HUD**: Intuitive vertical swipes for volume (right) and brightness (left), horizontal swipe for seek, double tap to jump 10s, pinch-to-zoom, and quick aspect ratio cycling (Fit, Fill, Zoom, 16:9, 4:3, Original).
- **Customizable Seekbar Styles**:
  - Classic Seekbar
  - Wave Seekbar
  - Glow Seekbar
  - AMOLED Pulse
  - Neon Minimal
  - Bubble Pop
  - Retro Blocks
  - Chapters Seekbar
- **Controls Customizer**: Fully customizable HUD actions and quick action slot assignment.
- **Audio & Subtitle Manager**: Multi-track audio switching, embedded subtitle selection, external `.srt` / `.vtt` file picker, subtitle synchronization, style customization (font size, color, background, stroke), and online subtitle search.
- **Audio Effects**: Built-in 5-band equalizer, bass boost, virtualizer, and volume amplification.
- **Background Playback & PiP**: Keep listening with the screen off or multitask seamlessly with Picture-in-Picture mode.

---

### 🎵 Dedicated Music Section
- **Unified Media Experience**: Switch smoothly between Video Library and Music Player.
- **Library Organization**: Organize by Songs, Albums, Artists, Playlists, Genres, Folders, and Favorites.
- **Interactive Now Playing**: Dynamic background gradients inspired by album art, interactive queue management, and sleep timer with fade-out.
- **Lyrics Support**: Embedded tags, local `.lrc` files, and automatic synced lyrics fetching via **LRCLIB**.
- **Home Screen Widgets**: Clean 2x2 and 4x1 home screen audio control widgets.

---

### 🔒 Private Vault
- **Encrypted Local Storage**: Keep confidential videos safe behind biometric authentication or a custom PIN.
- Hidden from system media scanners and gallery apps.

---

### 🌐 Network Stream Hub
- Play and bookmark live video and audio streams (HLS, DASH, RTSP, RTMP, HTTP/HTTPS).
- Quick stream history and stream bookmark categories.

---

## 🛠️ Architecture & Tech Stack

NP Player is built adhering to Google's Recommended Modern Android Architecture:

- **100% Kotlin**
- **Jetpack Compose** + **Material 3** for declarative, fluid UI
- **AndroidX Media3** for audio/video playback and MediaSession service
- **Hilt** for Dependency Injection
- **Room Database** for high-performance reactive offline caching
- **Jetpack DataStore** for user preferences and settings
- **Coroutines & Kotlin Flow** for asynchronous, reactive pipelines
- **Multi-Module Clean Architecture**:
  - `:app` — Application entry point, navigation graph, widgets
  - `:core-ui` — Theme, design tokens, reusable UI components
  - `:core-data` — Database, DAOs, repositories, preferences, scanners
  - `:player` — Playback engine, MediaSession service, gestures, custom seekbars
  - `:feature-library` — Video folders & music library screens
  - `:feature-network` — Network stream hub & bookmarking
  - `:feature-settings` — Settings, controls customizer, seekbar chooser, FAQ
  - `:feature-private` — Biometric/PIN-protected private media vault

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+
- Android SDK 35
- Android device running Android 7.0 (API level 24) or higher

### Build from Source
```bash
# Clone the repository
git clone https://github.com/arybishal/NP-Player.git
cd NP-Player

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```
The output APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

---

## 📦 Download

Grab the latest standalone APK from the [Releases](https://github.com/arybishal/NP-Player/releases/latest) tab.

---

## 📄 License

```text
Copyright (c) 2026 Bishal Aryal

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
