# Page Specification: Home & Video Library
> **Target Screen:** `feature-library` Home & Folder View  
> **Source PRD:** Section 4.1, 4.2  
> **Master Reference:** `../MASTER.md`

---

## 1. Screen Anatomy

```
┌────────────────────────────────────────────────────────┐
│ [NP Mark] NP Player            [Search] [Grid/List] [Menu] │ Top Bar (56dp)
├────────────────────────────────────────────────────────┤
│ (Music)  (Private)  (Network)  (Playlists)             │ Quick Chips (Row, 44dp)
├────────────────────────────────────────────────────────┤
│ [Folder Card 1: Camera]            [NEW 3]             │
│   124 videos • 14.2 GB • Last played 2h ago            │
├────────────────────────────────────────────────────────┤
│ [Folder Card 2: Downloads]         [NEW 1]             │
│   45 videos • 8.1 GB                                   │
├────────────────────────────────────────────────────────┤
│ [Folder Card 3: Movies]                                │
│   12 videos • 32.0 GB                                  │
│                                                        │
│                                           ┌──────────┐ │
│                                           │  [▶]     │ │ Resume FAB (56dp, Yellow)
│                                           └──────────┘ │
└────────────────────────────────────────────────────────┘
```

---

## 2. Component Specifications

### 2.1 Top App Bar
* **Height:** `56.dp`
* **Title:** "NP Player" (`HeadlineMedium`, 20sp Bold).
* **App Icon / Brand:** Rounded yellow square with black play triangle and "NP" glyph.
* **Actions:**
  * Search icon (`Icons.Outlined.Search`, 24dp)
  * View toggle icon (`Icons.Outlined.GridView` or `Icons.Outlined.ViewList`, 24dp)
  * Menu icon / Profile avatar (`Icons.Outlined.Menu` or custom circle avatar, 24dp)
* **Touch Targets:** Each icon has minimum `48.dp x 48.dp` click bounds.

### 2.2 Quick-Access Chips Row
* **Items:** `Music`, `Private`, `Network`, `Playlists`.
* **Container:** Horizontal scrollable row with `16.dp` start/end padding, `8.dp` spacing between chips.
* **Chip Height:** `36.dp`
* **Inactive State:**
  * Background: `NPWhite.copy(0.08f)` (Dark) / `NPBlack.copy(0.05f)` (Light)
  * Border: `1.dp` solid `OutlineVariant`
  * Text & Icon: `TextSecondary` (70% opacity)
* **Active / Selected State:**
  * Background: `NPYellow` (`#FFD60A`)
  * Text & Icon: `NPBlack` (`#000000`, 700 Bold)
  * Ripple: Dark ripple on yellow.

### 2.3 Folder Cards
* **Dimensions:**
  * List Mode: Full width minus `32.dp` gutters, height `80.dp`.
  * Grid Mode: 2-column adaptive grid, aspect ratio `1:1` or `4:3`.
* **Surface:**
  * Background: `SurfaceContainer` (`NPWhite.copy(0.06f)` in Dark, `NPBlack.copy(0.04f)` in Light).
  * Shape: `RoundedCornerShape(16.dp)`.
  * Border: `1.dp` solid `BorderSubtle`.
* **Last-Played Folder Highlight:**
  * Folder name text color switches to `NPYellow` (`#FFD60A`) to instantly identify the active folder.
  * Subtle yellow outline (`NPYellow.copy(alpha = 0.40f)`).
* **NEW Badge:**
  * Displayed on the top-right of the card when new videos were added within the configured threshold (1, 3, 7, 14, 30 days).
  * Container: `NPYellow` (`#FFD60A`).
  * Text: `NPBlack` (`#000000`), `LabelSmall` (11sp, ExtraBold), e.g., `NEW 3`.
  * Corner radius: `6.dp`.

### 2.4 Floating Resume Action Button (Resume FAB)
* **Visual:** `56.dp` circular or `RoundedCornerShape(28.dp)` floating button.
* **Position:** Bottom-right corner, `16.dp` end margin, `16.dp` bottom margin (clearing bottom system nav bar).
* **Color:** Background `NPYellow` (`#FFD60A`), Icon `NPBlack` (`#000000`).
* **Icon:** Play triangle (`Icons.Filled.PlayArrow`, 28dp).
* **Behavior:**
  * Tapping resumes the last played video at the exact saved position.
  * Hides automatically when scrolling down in list; reappears when scrolling up or idle.
  * Can be toggled on/off in Settings > List.

### 2.5 Pull-to-Refresh & Scanning State
* **Indicator:** Circular progress indicator with `NPYellow` stroke (`3.dp` stroke width).
* **Scanning Bar:** While background scanning 10,000+ files, show a non-intrusive slim linear progress bar at the bottom of the Top Bar in `NPYellow`.

---

## 3. Folder Detail Screen (Video List)

### 3.1 Video Item Layout
* **Thumbnail:** `16:9` ratio, rounded `12.dp`, Coil async image loader with video frame extraction.
  * Bottom overlay on thumbnail: Semi-transparent black pill showing video duration (e.g. `14:22`) and resolution tag (e.g. `4K`, `1080p`).
  * Watched Progress Bar: Slim `3.dp` bar at bottom edge of thumbnail. Progress is filled in `NPYellow`, background is `NPBlack.copy(0.5f)`.
* **Metadata Column:**
  * Video Title: `TitleMedium` (15sp, Max 2 lines with ellipsis).
  * Sub-text: File size (e.g., `1.8 GB`), Date modified, Frame rate (`60 fps`).
  * Trailing Action: 3-dot overflow menu (`Icons.Outlined.MoreVert`, 48dp touch target).
* **Multi-Select Mode:**
  * Long-press enters multi-select mode.
  * Circular checkbox on left: Unchecked is subtle white/black border; Checked is `NPYellow` filled with `NPBlack` checkmark.
  * Bottom Action Bar appears: Rename, Share, Delete, Add to Playlist, Move to Private.
