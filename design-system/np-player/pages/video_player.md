# Page Specification: Fullscreen Video Player
> **Target Screen:** `feature-player` Video Player & Overlay  
> **Source PRD:** Section 4.3, 4.6, 4.7, 4.8  
> **Master Reference:** `../MASTER.md`

---

## 1. Screen Philosophy

* **Always Dark:** The player screen is strictly dark (`#000000` AMOLED), regardless of the global application theme setting.
* **Content First:** Controls stay hidden during undisturbed playback. Controls fade out smoothly after user inactivity (`2s`, `3s default`, `5s`, `8s`, or `hide immediately at playback start`).
* **Immersive Gestures:** Intuitive on-screen swipe zones with immediate haptic and visual feedback.

---

## 2. Player Controls Overlay Layout

```
┌────────────────────────────────────────────────────────┐
│ [←] "Big_Buck_Bunny_4K.mkv" [Beside-Title Slots]  [Top-Right Slots] │ Top Bar Scrim
├────────────────────────────────────────────────────────┤
│                                                        │
│                    [  (⏮)  (▶)  (⏭)  ]                  │ Center Controls
│                                                        │
├────────────────────────────────────────────────────────┤
│ [Start Over Bar]  "Resume at 12:45 • [Start Over]"     │ Conditional Banner
├────────────────────────────────────────────────────────┤
│ [Bottom-Left Slots]              [Bottom-Right Slots]  │ Bottom Bar Scrim
│ 04:15 ━━━━━━━━━━━━●────────────────────────── 14:22   │ Seek Bar
└────────────────────────────────────────────────────────┘
```

### 2.1 Gradient Scrims
* **Top Scrim:** Vertical linear gradient: `NPBlack.copy(0.75f)` at top to `Color.Transparent` at 120dp.
* **Bottom Scrim:** Vertical linear gradient: `Color.Transparent` at 0dp to `NPBlack.copy(0.85f)` at bottom (140dp).
* **Center Scrim:** Subtle full-screen dimming (`NPBlack.copy(0.35f)`) when controls are visible to ensure high contrast for central play/pause buttons.

### 2.2 Top Header Bar
* **Back Button:** Left arrow (`Icons.AutoMirrored.Outlined.ArrowBack`, 24dp, 48dp hit target).
* **Title:** Single line, marquee scroll if overflowing, `TitleMedium` in `NPWhite`.
* **Configurable Slots:**
  * **Beside Title:** Up to 3 configurable action icons.
  * **Top Right:** Up to 6 configurable action icons.

### 2.3 Central Playback Controls
* **Primary Play / Pause:** `64.dp x 64.dp` rounded circle or icon in center. White icon with soft black shadow.
* **Previous / Next Track:** `48.dp x 48.dp` icons flanking center (`32.dp` gap).
* **Double-Tap Seek Ripple:** Left half double-tap rewinds 10s (default, configurable); Right half fast-forwards 10s. Circular arc ripple expands from tap location displaying `-10s` / `+10s` in `NPYellow` with black drop shadow.

### 2.4 Bottom Action Bar & Seek Bar
* **Time Display:** Current position (e.g. `04:15`) in `NPWhite`; Remaining/Total duration (e.g. `-10:07` / `14:22`) in `TextSecondary`.
* **Configurable Slots:**
  * **Bottom Left:** Up to 6 configurable action icons.
  * **Bottom Right:** Up to 7 configurable action icons.
* **Seek Bar:** Progress filled in `NPYellow` (`#FFD60A`), unplayed track in `NPWhite.copy(0.25f)`, buffered buffer track in `NPWhite.copy(0.40f)`.

---

## 3. Gesture Mapping & Visual Indicators

| Gesture Zone | Motion | Action | Visual Feedback |
|---|---|---|---|
| **Left Screen Half** | Vertical Swipe | Screen Brightness (0% – 100%) | Vertical pill on left with sun icon + percentage bar in `NPYellow`. |
| **Right Screen Half**| Vertical Swipe | Audio Volume (0% – 100%) + **Boost to 200%** | Vertical pill on right with volume icon. From 100% to 200%, the indicator shifts to high-contrast `NPYellow` with "+Boost" text badge. |
| **Horizontal (Anywhere)** | Horizontal Swipe | Video Scrubbing / Seek | Center floating card: `[<< 00:15 / >> 01:20]` with target timestamp and preview thumbnail. |
| **Long Press & Hold**| Press & Hold | Instant 2x Speed | Top center pill: `2X Speed ▶▶` in `NPYellow` badge. Returns to normal speed upon release. |
| **Two-Finger** | Pinch to Zoom | Video Zoom & Fill (Fit / Fill / Custom Zoom) | Centered text indicator: e.g. `120% Zoom` or `Crop to Fill`. |

### 3.1 Seek Undo Action Bar
* When a user performs a large seek jump (>30 seconds), a compact bottom snack-pill slides up:
  * Text: `Jumped to 45:10`
  * Action Button: `[UNDO]` in bold `NPYellow`.
  * Auto-dismiss: Closes after 4 seconds of inactivity.

### 3.2 "Start Over" Resume Prompt
* When opening a previously watched video (e.g. at 24:18):
  * A slim floating pill appears near bottom: `Resumed at 24:18  •  [START OVER]`.
  * Tapping `START OVER` rewinds to 00:00.
  * Tapping anywhere else or after 5 seconds fades out the banner.

### 3.3 "Up Next" Card
* Appears 10 seconds before the current video ends (if Autoplay next is enabled):
  * Card dimensions: `240.dp x 80.dp`, rounded `16.dp`, positioned at bottom right.
  * Thumbnail + Next title + Circular countdown timer ring in `NPYellow`.
  * Action: `[PLAY NOW]` or `[CANCEL]`.

---

## 4. Picture-in-Picture (PiP) & Background Audio

* **PiP Mode:** Aspect-ratio matching PiP window, standard system play/pause and prev/next actions.
* **Background Audio Mode:** Switched via player button or home gesture (if enabled in settings). Media notification with session controls, yellow accent tint, and media artwork.
