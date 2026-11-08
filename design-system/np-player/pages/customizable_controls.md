# Page Specification: Customizable Player Controls Editor
> **Target Screen:** `feature-settings` Player Controls Customizer  
> **Source PRD:** Section 4.4 (Signature Feature)  
> **Master Reference:** `../MASTER.md`

---

## 1. Concept & Hierarchy

NP Player provides a unique visual layout editor allowing users to configure which action buttons appear on their player overlay across **4 dedicated slots**. The configuration persists to DataStore and is dynamically consumed by the actual player engine.

```
┌────────────────────────────────────────────────────────┐
│ [←] Player Controls Editor                 [RESET]     │ Top Bar
├────────────────────────────────────────────────────────┤
│ ┌────────────────────────────────────────────────────┐ │
│ │                  LIVE PREVIEW                      │ │ Scaled-down 16:9 Player Preview
│ │ [Title] [Slot 1 (2/3)]           [Slot 2 (4/6)]    │ │ Visual representations of buttons
│ │                                                    │ │
│ │ [Slot 3 (3/6)]                   [Slot 4 (5/7)]    │ │
│ └────────────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────┤
│ (Beside Title: 2/3) (Top Right: 4/6)                   │ Slot Tabs (with used/max counters)
│ (Bottom Left: 3/6)  (Bottom Right: 5/7)                │
├────────────────────────────────────────────────────────┤
│ Selected Action Details: [Lock Player]                 │
│ Prevents accidental touches during video playback.     │ Action Description Card
│ [Move to Slot ▼]                     [HIDE ACTION]     │
├────────────────────────────────────────────────────────┤
│ Available Actions Grid:                                │
│ [Lock] [Subtitles] [Audio FX] [Speed] [PiP]            │ Outlined Action Icons
│ [Aspect] [Sleep] [Screenshot] [Record] [A-B Repeat]    │
└────────────────────────────────────────────────────────┘
```

---

## 2. Slot Architecture

| Slot Name | Max Items | Typical Position | Intended Actions |
|---|---|---|---|
| **1. Beside Title** | **3** | Top-left, right next to media title | Video Info, Audio track indicator, Quality badge |
| **2. Top Right** | **6** | Top-right corner | Screen Record, Screenshot, Sleep Timer, Dim/Night mode, Player Settings |
| **3. Bottom Left** | **6** | Bottom-left, above seek bar | Lock screen, Subtitle toggle, Audio Effects (Equalizer), Speed selector |
| **4. Bottom Right** | **7** | Bottom-right, above seek bar | Aspect ratio, PiP mode, Orientation lock, Playlist queue, A-B repeat |

---

## 3. Component Details

### 3.1 Live Player Preview (Interactive Canvas)
* **Aspect Ratio:** `16:9` fixed aspect container.
* **Background:** Deep black (`#000000`) with sample video frame thumbnail.
* **Live Sync:** Any action added, removed, or reordered in the editor immediately reflects in this preview box.
* **Interactive Drag/Tap:** Tapping an active slot in the preview highlights the corresponding slot tab below.

### 3.2 Slot Selector Chips (with Counter Badges)
* 4 Segmented Chips arranged in a 2x2 grid or horizontal carousel:
  * Format: `[Slot Name] • [Used]/[Max]`
  * Example: `Beside Title (2/3)`, `Top Right (4/6)`, `Bottom Left (3/6)`, `Bottom Right (5/7)`
  * When full (`Used == Max`): Counter chip border is highlighted in `NPYellow`.

### 3.3 Action Description Card
* **Container:** `SurfaceContainer` (`RoundedCornerShape(16.dp)`), `16.dp` padding.
* **Header:** Icon + Action Name (`TitleMedium` in theme text).
* **Body:** 2-line explanation of what the button does during playback (`BodyMedium`, 65% opacity).
* **Controls:**
  * **Slot Destination Picker:** Dropdown menu allowing instant reassignment to another slot (if that slot has available capacity).
  * **Hide / Remove Button:** Outlined button in subtle border with text `Hide Action` or `Remove from Slot`.

### 3.4 Available Actions Grid
* 24 Available Core Actions from PRD:
  1. `info` (Video specs & metadata)
  2. `record` (Screen / clip recording)
  3. `screenshot` (Capture high-res frame)
  4. `night_dim` (Extra dim overlay)
  5. `ab_repeat` (A-B loop maker)
  6. `playlist` (Quick queue & playlist drawer)
  7. `lock` (Child/touch lock)
  8. `subtitles` (Subtitle track & style selector)
  9. `audio_fx` (Equalizer & bass boost)
  10. `speed` (Playback speed menu: 0.25x - 4x)
  11. `framerate_memc` (Frame-rate interpolation toggle)
  12. `battery_saver` (Low-power rendering profile)
  13. `quality_hd` (Decoder & resolution stream info)
  14. `audio_track` (Multi-audio stream switcher)
  15. `aspect_ratio` (Fit, Fill, Stretch, 16:9, 4:3, Zoom)
  16. `pip` (Launch Picture-in-Picture)
  17. `orientation` (Rotate / sensor lock)
  18. `sleep_timer` (Playback auto-stop countdown)
  19. `background_play` (Continue audio in background)
  20. `audio_only` (Blank screen audio mode)
  21. `mirror_flip` (Horizontal flip video)
  22. `mute` (Quick volume toggle)
  23. `menu_more` (Overflow menu)
  24. `decoder_mode` (HW / HW+ / SW switch)

* **Grid Presentation:** `LazyVerticalGrid(columns = GridCells.Fixed(4))`, `12.dp` spacing.
  * Active (Assigned) Item: Solid `BorderSubtle`, icon tinted in `NPYellow`, slot badge on top corner.
  * Inactive (Available) Item: Subtle border, icon in `TextSecondary`. Tap to assign to active slot.

### 3.5 Top App Bar Reset Action
* Text button: `[RESET]` in `NPYellow` (Bold, 14sp).
* On click: Displays confirmation dialog to revert all 4 slots to standard defaults.
