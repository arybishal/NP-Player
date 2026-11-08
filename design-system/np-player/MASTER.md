# NP Player — UI/UX Design System Master Specification
> **Platform:** Android (Min SDK 24, Target SDK 35+)  
> **UI Framework:** Kotlin + Jetpack Compose (Material 3)  
> **Architecture:** MVVM + Unidirectional Data Flow (UDF)  
> **Source PRD:** `Readme/NP_Player_PRD.md`  
> **Global Source of Truth:** Any screen-specific rule in `design-system/np-player/pages/[page].md` overrides this master specification.

---

## 1. Visual Identity & Design Tokens

### 1.1 Strict Three-Colour System
NP Player adheres to a non-negotiable **three-colour palette**. No other chromatic hues (red, green, blue, purple) are permitted anywhere in the user interface—including status indicators, badges, error states, and switches.

| Token | Hex | Jetpack Compose Definition | Role & Usage |
|---|---|---|---|
| `NPWhite` | `#FFFFFF` | `Color(0xFFFFFFFF)` | Light theme background, Dark theme primary text, controls on dark video overlay |
| `NPBlack` | `#000000` | `Color(0xFF000000)` | Dark theme background (AMOLED pure black), Light theme primary text, player scrim, text on yellow |
| `NPYellow` | `#FFD60A` | `Color(0xFFFFD60A)` | Accent / CTA: primary buttons, active toggles, seek bar progress, NEW badges, FAB, links, slider thumbs |

### 1.2 Monochrome Opacity Scale (Grey Substitutes)
Greys exist **only** as alpha tints of `NPBlack` (in light mode) or `NPWhite` (in dark mode).

| Semantic Tint Token | Light Mode Value (Black Alpha) | Dark Mode Value (White Alpha) | Use Cases |
|---|---|---|---|
| `SurfaceContainer` | `NPBlack.copy(alpha = 0.04f)` | `NPWhite.copy(alpha = 0.06f)` | Card backgrounds, list item backgrounds |
| `SurfaceContainerHigh`| `NPBlack.copy(alpha = 0.08f)` | `NPWhite.copy(alpha = 0.12f)` | Dialog backgrounds, elevated bottom sheets |
| `BorderSubtle` | `NPBlack.copy(alpha = 0.12f)` | `NPWhite.copy(alpha = 0.15f)` | Card outlines, dividers, inactive track lines |
| `TextMuted` | `NPBlack.copy(alpha = 0.40f)` | `NPWhite.copy(alpha = 0.40f)` | Secondary captions, timestamps, durations |
| `TextSecondary` | `NPBlack.copy(alpha = 0.65f)` | `NPWhite.copy(alpha = 0.70f)` | Metadata labels, folder paths, subtitle previews |
| `TextPrimary` | `NPBlack` (`1.00f`) | `NPWhite` (`1.00f`) | Primary titles, video titles, menu headers |
| `Disabled` | `NPBlack.copy(alpha = 0.25f)` | `NPWhite.copy(alpha = 0.25f)` | Disabled buttons, inactive action icons |
| `Scrim` | `NPBlack.copy(alpha = 0.60f)` | `NPBlack.copy(alpha = 0.75f)` | Modal dimming, player controls overlay gradient |

### 1.3 Strict Text-On-Color Contrast Rules
* **Text on Yellow (`#FFD60A`):** **ALWAYS `NPBlack`** (`#000000`). Contrast ratio is **~14.2:1** (AAA compliant).
* **Text on Yellow Prohibition:** **NEVER `NPWhite`** on Yellow (Contrast ratio is **1.47:1**, which fails WCAG accessibility completely).
* **Text on Black:** `NPWhite` or `NPWhite` tints (Contrast ratio is **21:1** for pure white).
* **Text on White:** `NPBlack` or `NPBlack` tints (Contrast ratio is **21:1** for pure black).

---

## 2. Theme Architecture

The app supports 3 user-selectable modes in Settings:
1. **Follow device (Default):** Syncs with Android system dark mode toggle.
2. **Light Mode:** Pure `#FFFFFF` canvas, high legibility.
3. **Dark Mode:** Pure `#000000` AMOLED canvas (energy efficient, zero-emission black).

> **Player Screen Exception:** Regardless of the global app theme, the **Video Player screen is ALWAYS in Dark Mode** (`NPBlack` background, `NPWhite` icons/labels, `NPYellow` seek bar/accents).

### 2.1 Material 3 ColorScheme Mapping

```kotlin
// Light Color Scheme
val NPLightColorScheme = lightColorScheme(
    primary = NPYellow,
    onPrimary = NPBlack,
    primaryContainer = NPYellow.copy(alpha = 0.20f),
    onPrimaryContainer = NPBlack,
    secondary = NPBlack,
    onSecondary = NPWhite,
    background = NPWhite,
    onBackground = NPBlack,
    surface = NPWhite,
    onSurface = NPBlack,
    surfaceVariant = NPBlack.copy(alpha = 0.05f),
    onSurfaceVariant = NPBlack.copy(alpha = 0.70f),
    outline = NPBlack.copy(alpha = 0.15f),
    outlineVariant = NPBlack.copy(alpha = 0.08f)
)

// Dark (AMOLED) Color Scheme
val NPDarkColorScheme = darkColorScheme(
    primary = NPYellow,
    onPrimary = NPBlack,
    primaryContainer = NPYellow.copy(alpha = 0.25f),
    onPrimaryContainer = NPYellow,
    secondary = NPWhite,
    onSecondary = NPBlack,
    background = NPBlack,
    onBackground = NPWhite,
    surface = NPBlack,
    onSurface = NPWhite,
    surfaceVariant = NPWhite.copy(alpha = 0.08f),
    onSurfaceVariant = NPWhite.copy(alpha = 0.70f),
    outline = NPWhite.copy(alpha = 0.18f),
    outlineVariant = NPWhite.copy(alpha = 0.10f)
)
```

---

## 3. Typography & Hierarchy

A single clean sans-serif typeface is used: **Inter** (fallback to system **Roboto**). Three primary font weights: Regular (400), Medium (500), Bold (700).

| Style | Size | Weight | Line Height | Tracking | Purpose |
|---|---|---|---|---|---|
| `DisplayLarge` | 28sp | Bold (700) | 34sp | -0.2sp | Screen headers (e.g., Settings, Folder Detail) |
| `HeadlineMedium`| 20sp | Bold (700) | 26sp | 0.0sp | Section titles, Dialog headlines |
| `TitleMedium` | 16sp | Medium (500) | 22sp | 0.1sp | Video item title, Folder name, Menu item |
| `BodyLarge` | 15sp | Regular (400) | 20sp | 0.2sp | Settings item description, Dialog body |
| `BodyMedium` | 13sp | Regular (400) | 18sp | 0.2sp | Video metadata (size, resolution, duration) |
| `LabelLarge` | 14sp | Bold (700) | 18sp | 0.1sp | Button text, Pill chip labels |
| `LabelSmall` | 11sp | Bold (700) | 14sp | 0.4sp | `NEW` badge, Video resolution tag (`4K`, `1080p`) |

---

## 4. Spacing, Shapes & Touch Targets

### 4.1 Spacing Grid (8dp Rhythm)
* `space_xxs` = 2dp (Micro gap, border widths)
* `space_xs` = 4dp (Badge internal padding, icon-text tight inline)
* `space_sm` = 8dp (Standard element spacing, chip horizontal gap)
* `space_md` = 12dp (Inner card padding, list item vertical padding)
* `space_lg` = 16dp (Screen horizontal gutter, card padding)
* `space_xl` = 24dp (Section spacing, dialog internal margin)
* `space_xxl` = 32dp (Header bottom margin)

### 4.2 Corner Shapes (Curvature Hierarchy)
NP Player utilizes smooth, modern rounded corners adhering strictly to the **16dp–28dp** rule:
* **Cards & Containers:** `RoundedCornerShape(16.dp)` (Folder cards, video item cards, bottom sheets).
* **Dialogs & Menus:** `RoundedCornerShape(20.dp)` (Confirmation dialogs, sort modal).
* **Pills & Buttons:** `RoundedCornerShape(28.dp)` or `CircleShape` (Category filter chips, Action buttons, Badges, FAB).

### 4.3 Touch Targets & Interaction Areas
* **Minimum Interactive Hit Box:** `48.dp x 48.dp` (Complies with Android Accessibility standards).
* Even if an icon visually renders at `24.dp`, its hit box must be expanded using `Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)` or `IconButton`.
* **Touch Spacing:** Minimum `8.dp` gap between adjacent clickable elements to eliminate accidental taps.

---

## 5. Iconography

* **Style:** Outlined vector icons with consistent **2dp stroke width**.
* **Source:** Material Symbols Outlined / Android Vector Drawables. No raster PNGs. No emoji as UI icons.
* **Colors:** Single monochrome colour (White or Black depending on theme; Yellow when active/selected).
* **Standard Sizes:**
  * Small: `18.dp` (metadata icons, mini badges)
  * Medium: `24.dp` (app bar actions, list trailing actions, player controls)
  * Large: `32.dp` (player central play/pause, empty state illustrations)
  * Splash / Hero: `48.dp` – `64.dp`

---

## 6. Motion & Interaction Feedback

* **Tap Feedback:** Bounded ripple for enclosed cards/buttons; Unbounded circular ripple for icon buttons.
  * In Light Mode: `NPBlack.copy(alpha = 0.12f)`
  * In Dark Mode: `NPWhite.copy(alpha = 0.15f)`
* **Micro-interaction Durations:**
  * Fast / Snappy: `150ms` (Toggle switches, button press scale)
  * Standard: `250ms` (BottomSheet slide-in, Dialog enter/exit)
  * Transitions: `300ms` (Screen transitions, player controls fade in/out)
* **Easing Curves:**
  * `FastOutSlowInEasing` (Enter motion)
  * `LinearOutSlowInEasing` (Controls reveal)
  * `FastOutLinearInEasing` (Controls hide)
* **Accessibility Motion:** Honor system `prefers-reduced-motion` or system animation scale. When animations are disabled, replace alpha fades with instant visibility transitions (`0ms`).

---

## 7. Component Specifications

### 7.1 Buttons
* **Primary Button (Yellow Accent):**
  * Container: `NPYellow` (`#FFD60A`)
  * Content: `NPBlack` (`#000000`, Bold 14sp)
  * Shape: `RoundedCornerShape(28.dp)`
  * Height: `48.dp`
* **Secondary / Outlined Button:**
  * Container: `Color.Transparent`
  * Border: `1.5.dp` solid `Outline` (Theme black/white at 20%)
  * Content: Theme Primary Text (`NPBlack` or `NPWhite`)
  * Shape: `RoundedCornerShape(28.dp)`
* **Floating Action Button (Resume FAB):**
  * Container: `NPYellow` (`#FFD60A`)
  * Content: `NPBlack` (`#000000`)
  * Shape: `RoundedCornerShape(28.dp)` or `CircleShape`
  * Elevation: `6.dp` default, `12.dp` pressed

### 7.2 Chips (Filter & Category)
* **Height:** `36.dp`
* **Shape:** `RoundedCornerShape(18.dp)`
* **Inactive State:**
  * Background: `SurfaceVariant` (`alpha = 0.06f`)
  * Text/Icon: `TextSecondary` (`alpha = 0.70f`)
  * Border: `1.dp` subtle border (`alpha = 0.10f`)
* **Active State:**
  * Background: `NPYellow`
  * Text/Icon: `NPBlack` (Bold)
  * Border: None

### 7.3 Badges (`NEW` Badge)
* **Container:** `NPYellow` (`#FFD60A`)
* **Content:** `NPBlack` (`#000000`, ExtraBold 11sp)
* **Padding:** `Horizontal 6.dp, Vertical 2.dp`
* **Shape:** `RoundedCornerShape(6.dp)`

### 7.4 Cards & List Items
* **Folder & Video Cards:**
  * Shape: `RoundedCornerShape(16.dp)`
  * Background: `SurfaceVariant`
  * Border: `1.dp` solid `BorderSubtle`
  * Padding: `12.dp` internal padding
  * Thumbnail ratio: `16:9` for video files, `1:1` for folder icon preview.

---

## 8. Anti-Patterns & Prohibited Practices

1. ❌ **No Chromatic Colors:** Never introduce green for "success", red for "errors/delete", or blue for "links". Deletions use yellow or monochrome alert dialogs with high contrast copy.
2. ❌ **No White Text on Yellow:** White text on `#FFD60A` is strictly prohibited. All text/icons on yellow elements must be `#000000`.
3. ❌ **No Small Touch Targets:** Never produce an interactive icon without a `48.dp` minimum touch bound.
4. ❌ **No Emojis as UI Controls:** All icons must be scalable vector drawables (`2dp` stroke).
5. ❌ **No Layout Shifts on Press:** Never change padding or border width during tap states; use background opacity or elevation shifts only.
6. ❌ **No Light Player Screen:** The video playback UI must always be dark mode, irrespective of the system theme.

---

## 9. Jetpack Compose Architecture Rules

1. **State Hoisting:** Composables are stateless by default. Accept `uiState: ScreenUiState` and event lambdas `(ScreenUiEvent) -> Unit`.
2. **Recomposition Optimization:**
   * Annotate stable models with `@Immutable` or `@Stable`.
   * Pass primitive or immutable parameters (use `kotlinx.collections.immutable.ImmutableList` instead of raw `List`).
   * Wrap scroll positions or dynamic calculations in `derivedStateOf`.
3. **List Keys:** Always specify `key = { item.id }` in `LazyColumn` and `LazyVerticalGrid` to prevent layout thrashing and maintain scroll position.
4. **Lifecycle-Aware Collection:** Always collect UI StateFlows via `collectAsStateWithLifecycle()` in composables.
5. **No Business Logic in Composables:** All sorting, filtering, folder scanning aggregation, and time conversions belong in ViewModel/UseCases.
