# Page Specification: Seek Bar Styles & Customization
> **Target Screen:** `feature-settings` Seek Bar Customizer  
> **Source PRD:** Section 4.5  
> **Master Reference:** `../MASTER.md`

---

## 1. Overview & "Drag to Try" Philosophy

NP Player provides 4 distinct seek bar styles. To ensure users immediately understand the kinetic response before playing videos, this screen features a live interactive sandbox at the top:
* **Interactive Sandbox Slider:** Users can drag their finger along the active style to test scrubbing resistance, chapter snapping, waveform responsiveness, and glow dispersion.
* **Persistent Setting:** Saved in DataStore and applied globally to the player overlay.

```
┌────────────────────────────────────────────────────────┐
│ [←] Seek Bar Styles                                    │ Top Bar
├────────────────────────────────────────────────────────┤
│ ┌────────────────────────────────────────────────────┐ │
│ │               "DRAG TO TRY" SANDBOX                │ │
│ │  03:42 ━━━━━━━━━●────────────────────── 12:15      │ │ Live Scrubbing Demo
│ │  "Try dragging the thumb or tapping the track"     │ │
│ └────────────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────┤
│ Style 1: Classic (Thin line + round handle)        (●) │ Selected Radio
├────────────────────────────────────────────────────────┤
│ Style 2: Chapters (Segmented markers + snap)       ( ) │
├────────────────────────────────────────────────────────┤
│ Style 3: Wave (Rippling soundwave dynamics)        ( ) │
├────────────────────────────────────────────────────────┤
│ Style 4: Glow (Light trail + illuminated head)     ( ) │
└────────────────────────────────────────────────────────┘
```

---

## 2. Style Specifications

All 4 styles strictly use the three-colour palette:
* **Progress Track:** `NPYellow` (`#FFD60A`)
* **Buffer Track:** `NPWhite.copy(alpha = 0.35f)`
* **Unplayed / Inactive Track:** `NPWhite.copy(alpha = 0.15f)`
* **Thumb / Indicator:** `NPYellow` (`#FFD60A`) with dark border or glow.

### Style 1: Classic
* **Design:** Minimalist, standard video player slider.
* **Track Height:** `3.dp` default, expands to `5.dp` when touched.
* **Thumb Handle:** Circular `14.dp` diameter in `NPYellow`. Scales to `18.dp` during active scrub.
* **Physics:** Smooth linear interpolation, zero friction.

### Style 2: Chapters
* **Design:** Segmented progress bar separated by chapter gaps (`2.dp` gap between chapter segments).
* **Behavior:**
  * When scrubbing near a chapter boundary (within `5 seconds`), the handle provides subtle haptic tick feedback and magnet snaps to the start of the chapter.
  * Active Chapter Swell: The current playing chapter segment dynamically swells from `3.dp` to `6.dp` in height.
  * Chapter Name Toast: A floating pill above the thumb displays the chapter title (e.g., `Chapter 3: The Chase`).

### Style 3: Wave
* **Design:** Dynamic soundwave / ripple visualization along the seek bar.
* **Playing State:** Animated sine wave ripple oscillations in `NPYellow` that propagate along the played portion of the bar at playback speed.
* **Paused State:** Flattened clean bar with stationary audio amplitude ridges.
* **Track Height:** Peaks at `10.dp`, troughs at `3.dp`.

### Style 4: Glow
* **Design:** High-tech illumination trail designed for OLED displays.
* **Visuals:**
  * The seek thumb emits a soft diffused yellow radial glow (`blurRadius = 12.dp`, `color = NPYellow.copy(alpha = 0.45f)`).
  * The trailing 15% of the played bar features a subtle yellow gradient fade.
  * Standout aesthetic in dark environments without causing blinding screen glare.

---

## 3. Configuration & Card Options

Each seek bar style is presented as an interactive selectable card with:
1. **Radio Button:** Outlined in monochrome, filled with `NPYellow` when selected.
2. **Style Name & Description:** e.g. `Chapters: Automatically marks scene segments with magnetic snapping.`
3. **Miniature Static Preview:** Showing the specific geometry of that style.
4. **Haptic Feedback Toggle:** Option to toggle haptic vibration when crossing chapter boundaries or scrubbing.
