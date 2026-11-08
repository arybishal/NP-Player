# NP Player — AI Coding Prompt

Paste this whole file into Claude Code / Cursor / any AI coding agent, together with `NP_Player_PRD.md` and your reference screenshots.

---

## ROLE
You are a senior Android engineer and UI designer. Build **NP Player**, a production-quality offline-first video player for Android, following `NP_Player_PRD.md` exactly. Work in phases; after each phase, make sure the app builds and runs before continuing.

## STACK
Kotlin, Jetpack Compose (Material 3), AndroidX Media3 (ExoPlayer) with FFmpeg decoder extension, Room, DataStore, Hilt, Coroutines/Flow, Gradle KTS. Min SDK 24, target latest. Modular structure: `app`, `core-ui`, `core-data`, `player`, `feature-library`, `feature-network`, `feature-settings`, `feature-private`.

## DESIGN RULES (non-negotiable)
- Only three colours: **White `#FFFFFF`, Black `#000000`, Yellow `#FFD60A`**. Define them once as tokens in `core-ui`. Greys are allowed only as black/white at reduced opacity. No red, blue, green or other hues (badges, errors, switches included).
- Text on yellow is always black.
- Theme **follows the device (light/dark) by default**. Settings > App Theme: Follow device (default), Light, Dark.
  - Light: white background, black text, yellow accents.
  - Dark: pure black background, white text, yellow accents.
- The video player screen is always dark.
- Rounded 16–28dp shapes, outlined 2dp icons, one sans-serif font, 48dp min touch targets.
- Match the layout of the reference screenshots (folder list with count and NEW badge, top chips, resume FAB, menu grid, settings lists with toggles/checkboxes, radio dialogs, player controls editor with slots, seek-bar style cards) but restyle everything in the palette above. Do not copy the reference apps' names, logos or artwork.

## PHASES

### Phase 1 — Foundation
1. Create project, modules, Hilt, theme system (3 tokens, light/dark colour schemes, theme preference in DataStore).
2. Navigation (Compose Navigation): Home, Folder detail, Menu, Settings screens, Player.
3. Permission flow (READ_MEDIA_VIDEO / legacy storage) with friendly rationale screen.

### Phase 2 — Library
1. MediaStore scanner → Room index (folders, videos, duration, size, resolution, dateModified).
2. Home screen: top bar (title, search, grid/list toggle, menu), chips (Music, Private, Network, Playlists), folder list with video counts and yellow NEW badge (period configurable), highlight last-played folder, Resume FAB, pull-to-refresh and loading state.
3. Folder detail: video list with thumbnails (Coil), progress bars, sort, multi-select actions (rename, share, delete, add to playlist).
4. "List" settings: last media mark, scroll to last, select-by-thumbnail, floating button, NEW period, scan folders, file extensions, respect `.nomedia`, show hidden.

### Phase 3 — Player core
1. `PlayerEngine` interface + Media3 implementation.
2. Fullscreen player: play/pause, prev/next, seek bar, speed, aspect modes, rotation lock, loop, A-B repeat, sleep timer, screenshot, subtitle and audio-track pickers, PiP, background audio via MediaSession foreground service with notification.
3. Gestures: tap, double-tap seek (configurable), vertical swipe brightness/volume (boost to 200%), horizontal swipe seek, press-and-hold 2x, pinch zoom, seek undo, remember brightness/volume.
4. Resume playback with "Start over" bar, watch history, autoplay next with "Up next" card, controls timeout (2/3/5/8s/hide on start).
5. Decoder settings: HW acceleration Automatic/Disabled/Decoding/Full, per-extension override, FFmpeg software fallback.
6. Audio and subtitle settings (language priority, delay, size/colour from white/yellow only, position, external subtitle loading).

### Phase 4 — Customization
1. **Player Controls editor**: live preview; slots Beside title (3), Top right (6), Bottom left (6), Bottom right (7) with used/max counters; add/remove/move actions; per-action description card with Hide; reset to default; persist in DataStore; the real player reads the same layout.
2. **Seek bar styles**: Classic, Chapters, Wave, Glow with "drag to try" preview and persistence. Progress is yellow.
3. Advanced screen: read-ahead cache (default 150 MB) and back-seek cache (default 50 MB) sliders, engine options text box with Apply/Revert, debug overlay.

### Phase 5 — Extras
1. Private folder (biometric/PIN, encrypted storage, hidden from history).
2. Video playlists (create, reorder, shuffle, repeat, queue).
3. Network hub: Network shares (SMB/SFTP/FTP/WebDAV), Network Stream (URL), Local Network (DLNA), Media servers (Jellyfin/Emby/Plex), Live TV (M3U), Radio, Podcasts. Metered-connection action setting.
4. Media Manager, File Transfer, Cloud Drive (flag-gated), Status Saver (flag-gated), App Language (with RTL), Help, Legal.

### Phase 6 — Polish
Accessibility (TalkBack, contrast), tablet/foldable two-pane layouts, baseline profiles, R8, unit tests (scanner, settings, controls layout), UI tests (Home → Player flow), performance pass (cold start < 1.5s).

## DELIVERABLES PER PHASE
- Working code that compiles, with brief README notes.
- List of what was completed and what remains.
- No placeholder "TODO" screens for the current phase's scope.

## CODING STANDARDS
MVVM + unidirectional data flow, immutable UI state, no hard-coded colours or strings (use tokens and `strings.xml`), no analytics or trackers, handle errors with friendly messages, keep composables small and previewable (light and dark previews).

## START
Begin with **Phase 1** now. Ask me only if something blocks you; otherwise make sensible choices and continue.
