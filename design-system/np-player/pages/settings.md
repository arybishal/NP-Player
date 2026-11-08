# Page Specification: Settings Architecture
> **Target Screen:** `feature-settings` All Preference Screens & Dialogs  
> **Source PRD:** Section 3, 4.2, 4.6, 4.7, 4.8, 4.15, 4.16  
> **Master Reference:** `../MASTER.md`

---

## 1. Information Architecture

Settings is structured into 8 modular sub-screens:
1. **List Settings:** Library scanning, NEW tag duration, auto-scroll to last played, folder inclusion/exclusion, file extension filters, `.nomedia` respect.
2. **Player Settings:** Controls timeout, gesture toggles, orientation lock, double-tap seek step, aspect ratio default, seek undo.
3. **Decoder Settings:** Hardware acceleration (Auto/Disabled/Decoding only/Full), per-extension overrides, FFmpeg software fallback, MEMC frame smoothing.
4. **Audio Settings:** Audio boost up to 200%, default audio language, equalizer + presets, downmix/stereo, audio delay sync slider.
5. **Subtitle Settings:** Preferred subtitle languages, external subtitle auto-load, subtitle font size, color (white/yellow only), background outline, sync delay slider.
6. **Advanced & Cache:** Read-ahead cache slider (32–512 MB, default 150 MB), Back-seek cache slider (default 50 MB), custom engine options (one per line) with Apply/Revert, debug overlay toggle.
7. **App Theme:** Radio picker (Follow device [default], Light, Dark AMOLED).
8. **App Language & About:** In-app language picker (English, Nepali, Hindi, Arabic, Bengali, etc.), RTL support, Legal, Privacy Policy, Open Source Licenses.

---

## 2. Component Specifications

### 2.1 Settings Item Row
* **Height:** `64.dp` (or `72.dp` with two-line description).
* **Padding:** Horizontal `16.dp`.
* **Title:** `TitleMedium` (16sp, Theme Primary Text).
* **Subtitle / Value:** `BodyMedium` (13sp, Theme Secondary Text, 65% opacity).
* **Leading Icon:** `24.dp` outlined vector icon inside a `40.dp` rounded square surface (`BorderSubtle`).
* **Trailing Widget:**
  * Toggle Switch: Switch thumb and track styled in `NPYellow` (checked) and `NPWhite.copy(0.2f)` (unchecked).
  * Radio Button: `NPYellow` ring and dot when active.
  * Chevron: `Icons.Outlined.ChevronRight` for navigating to sub-screens.

### 2.2 Sliders (Cache, Volume Boost, Subtitle Size)
* **Slider Track:** Inactive track in `BorderSubtle` (`alpha = 0.20f`); Active track filled in `NPYellow` (`#FFD60A`).
* **Slider Thumb:** Circular `20.dp` thumb in `NPYellow` with subtle drop shadow.
* **Value Bubble:** Floating indicator above thumb displaying current value (e.g. `150 MB`, `150%`, `24sp`).

### 2.3 Custom Player Engine Options (Section 4.15)
* Multi-line text field (`OutlinedTextField`) with monospace font.
* Shows default engine options (preferred audio/subtitle languages, cache duration).
* Real-time line syntax validation: valid line gets subtle checkmark; invalid parameter gets warning icon.
* Action Bar:
  * `[REVERT]` (Outlined button).
  * `[APPLY]` (`NPYellow` container button, Black bold text).
* Result feedback card: Displays parsed parameters and engine initialization status.

### 2.4 Theme Selection Dialog
* Modal Dialog: `SurfaceContainerHigh` background, `RoundedCornerShape(20.dp)`.
* Title: "App Theme" (`HeadlineMedium`, 20sp).
* 3 Options:
  1. **Follow device (default):** Automatically adapts to Android system night mode.
  2. **Light:** White `#FFFFFF` canvas, black text, yellow accents.
  3. **Dark:** True AMOLED Black `#000000`, white text, yellow accents.
* Buttons: `[CANCEL]` and `[OK]` (Both 48dp min touch height).
