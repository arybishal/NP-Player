# NP Player — Product Requirements Document (PRD)

**Version:** 1.0 | **Platform:** Android (min SDK 24, target latest) | **Type:** Offline-first video player

---

## 1. Overview
NP Player is a fast, clean, customizable video player for Android. It plays local videos, network shares and streams, with deep player customization (controls layout, seek-bar styles, gestures, cache) and a strict three-colour identity: **White, Black, Yellow**.

**Goals**
- Smooth playback of almost any format, with hardware decoding by default.
- Folder-based library that is instantly familiar.
- Highly customizable player UI without clutter.
- Free, ad-free, privacy-first (no account, no tracking).

**Non-goals (v1):** built-in downloader, piracy/torrent features, iOS, cloud account sync.

---

## 2. Design System (strict)
Only three brand colours are allowed:

| Token | Hex | Use |
|---|---|---|
| `white` | `#FFFFFF` | Light background, text/icons on dark |
| `black` | `#000000` | Dark background, text/icons on light |
| `yellow` | `#FFD60A` | Accent: primary buttons, active states, seek bar, badges, FAB, links |

**Rules**
- Greys are allowed **only as tints of black/white** (e.g. black at 8% / 40% / 60% opacity) for dividers, disabled and secondary text. No other hues anywhere (no red badges, no blue, no green).
- Text on yellow is always black. Never white on yellow.
- **Theme:** default = **Follow device** (system light/dark). Settings > App Theme offers: *Follow device (default)*, *Light*, *Dark*.
  - Light: bg white, text black, accent yellow.
  - Dark: bg black (true AMOLED), text white, accent yellow.
- Player screen is always dark (black bg, white controls, yellow accents) regardless of theme.
- Typography: one sans-serif family (Inter or Roboto), 3 weights. Rounded corners 16–28dp. Large touch targets (min 48dp).
- Icons: outlined, 2dp stroke, single colour.
- Splash/app icon: yellow rounded square with black play triangle and "NP" mark.

---

## 3. Information Architecture
```
Home (Folders)
 ├─ Top bar: Title, Search, View toggle, Menu (profile icon)
 ├─ Chips: Music | Private | Network | Playlists
 ├─ Folder list (name, video count, NEW badge)
 └─ Resume FAB (play last video)

Menu screen
 ├─ File Transfer, Private Folder, Video Playlists
 ├─ Network Stream, Local Network, Media Manager, Cloud Drive
 ├─ Status Saver (optional module)
 ├─ App Theme, Settings
 └─ Legal, Help

Network hub: Network Shares (SMB/SFTP/FTP/WebDAV), Media Servers (Jellyfin/Emby/Plex), Live TV (M3U/URL), Radio, Podcasts

Settings: List, Player, Decoder, Audio, Subtitle, General, Development, App Language

Player: Controls, Gestures, Seek bar, Cache/Advanced
```

---

## 4. Functional Requirements

### 4.1 Home / Folders (P0)
- Auto-scan device storage for video folders; show folder name, video count.
- **NEW badge** (yellow bg, black text) with count of files modified within a configurable period.
- Highlight folder containing last-played media (yellow text).
- Search (folders and videos), grid/list view toggle.
- Quick-access chips: Music, Private, Network, Playlists.
- **Resume FAB** (yellow, black play icon) bottom-right: starts last played video. Toggleable.
- Pull-to-refresh rescan; loading indicator while scanning.
- Folder detail: video list with thumbnail, duration, size, resolution, progress bar, NEW tag. Sort (name/date/size/length), multi-select (rename, share, delete, add to playlist, move to private).

### 4.2 Library Settings — "List" (P0)
- Mark last media in each folder.
- Auto-scroll to last played.
- Select-by-thumbnail mode.
- Floating resume button on/off.
- NEW tag period (1 / 3 / 7 / 14 / 30 days).
- Scan: choose folders, file extensions (mp4, mkv, avi, mov, webm, ts, flv, 3gp, m4v, wmv…), respect `.nomedia`, show hidden files/folders.
- Auto rescan on launch.

### 4.3 Video Player (P0)
**Playback:** play/pause, prev/next, seek, speed (0.25x–4x), aspect ratio/fit/fill/zoom, rotate/lock orientation, loop, A-B repeat, sleep timer, screenshot, screen record (optional), background audio, Picture-in-Picture, audio-only mode, mini/floating player.

**Gestures:** single tap show/hide controls; double-tap left/right seek (default 10s, configurable); swipe vertical left = brightness, right = volume (with boost up to 200%); horizontal swipe = seek; press-and-hold = 2x speed (configurable); pinch to zoom; **Seek undo** after big jumps; remember brightness and volume.

**Resume:** reopen where left off with a quick "Start over" bar (toggle).
**Watch history:** list, resume, clear, pause recording (toggle).
**Autoplay next** in folder (toggle), "Up next" card, Skip-intro button (when chapter/marker data exists), Hide Skip intro/Up next after 5s.
**Controls timeout:** 2s / 3s (default) / 5s / 8s / hide at playback start.

### 4.4 Customizable Player Controls (P1 — signature feature)
- Live preview of the player at top of the settings screen.
- Four slots: **Beside title** (max 3), **Top right** (max 6), **Bottom left** (max 6), **Bottom right** (max 7). Counter shows used/max.
- Available actions: info, record, screenshot, night/dim, A-B repeat, playlist, lock, subtitles, audio effects (FX), speed, frame-rate/MEMC, battery saver, quality (HD), translate/audio track, aspect ratio, PiP, orientation, menu.
- Tap an action to add/remove/move between slots; per-action description card with **Hide** button; Reset to default.

### 4.5 Seek Bar Styles (P1)
Selectable with live "drag to try" preview: **Classic** (thin line + round handle), **Chapters** (segmented, current chapter swells, handle snaps to chapter starts), **Wave** (ripples while playing, flat when paused), **Glow** (light trail + glowing head). All styles use yellow progress on white/black-tint track.

### 4.6 Decoder (P0)
- Hardware acceleration: Automatic (default) / Disabled / Decoding only / Full.
- Per-extension decoder override (HW / HW+ / SW).
- Software fallback (FFmpeg) for unsupported codecs (HEVC, AV1, VP9, DTS, AC3, E-AC3, TrueHD).
- Frame-rate smoothing (MEMC) with **Fast MEMC** option for older phones.

### 4.7 Audio (P0)
Default audio language priority, audio boost up to 200%, equalizer + presets, audio delay, downmix/stereo, background play, audio-only playlists (Music chip).

### 4.8 Subtitles (P0)
Auto-load external .srt/.ass/.vtt/.ssa and embedded tracks; preferred language list; font, size, colour (white/yellow only), outline/background (black), position, delay; online subtitle search (optional, user-triggered); gesture-friendly subtitle sync.

### 4.9 Network (P1)
- **Network shares:** SMB, SFTP, FTP, WebDAV, NFS.
- **Media servers:** Jellyfin, Emby, Plex login and browsing; optional poster/synopsis/cast pulled only from user's own servers, only when opening video details (toggle).
- **Network Stream:** open URL (HTTP/HTTPS/HLS/DASH/RTSP/RTMP), history.
- **Live TV:** add M3U/M3U8/XMLTV playlist or link, EPG, favourites.
- **Radio:** browse stations (Radio Browser API), favourites.
- **Podcasts:** search, subscribe, episodes, resume.
- **Local Network:** discover DLNA/UPnP devices on Wi-Fi.
- Metered-connection behaviour: Do nothing / Warn / Block streams.
- **Cloud Drive:** Google Drive, OneDrive, Dropbox (OAuth, v1.1).

### 4.10 Private Folder (P1)
Biometric/PIN-protected vault; move videos in (hidden from gallery and other apps), excluded from history, optional screenshot block.

### 4.11 Video Playlists (P1)
Create, rename, reorder, shuffle, repeat; add from folder multi-select; queue.

### 4.12 Media Manager (P2)
Storage usage per folder, find duplicates, large files, bulk delete, clear watch history/thumbnails cache.

### 4.13 File Transfer (P2)
Wi-Fi Direct/local-hotspot phone-to-phone and phone-to-PC (browser-based) transfer with QR code.

### 4.14 Status Saver (P2, optional)
Browse and save the user's own viewed WhatsApp statuses from the local status folder. Must comply with Play policy; can be shipped as a flag-gated module.

### 4.15 Advanced / Development (P2)
- Read-ahead cache (default 150 MB, 32–512 MB), Back-seek cache (default 50 MB).
- Optional custom player-engine options text field (one per line) with Apply/Revert and per-line result feedback; ships with defaults (preferred audio language, subtitle language, cache seconds).
- Debug overlay (codec, bitrate, fps, dropped frames), export logs.

### 4.16 App Language & Help (P1)
In-app language picker (English, Nepali, Hindi, Arabic, Bengali + more), RTL support. Help/FAQ, Legal (privacy policy, licences, terms), feedback email.

---

## 5. Non-Functional Requirements
- **Performance:** cold start < 1.5s on mid-range device; 4K HEVC 60fps on devices that support it; folder scan of 10k videos < 5s after first index.
- **Battery:** battery-saver control; no background wakeups beyond playback.
- **Privacy:** no analytics by default; no account; opt-in crash reports only. Network features only talk to servers the user adds.
- **Accessibility:** TalkBack labels, min contrast 7:1 (black/white), yellow only on black text, font scaling, haptics optional.
- **Compatibility:** Android 7+ (SDK 24+); handle scoped storage, Android 13+ granular media permissions, foldables and tablets (two-pane layout).
- **Reliability:** crash-free sessions > 99.5%.

---

## 6. Technical Approach
- **Language/UI:** Kotlin, Jetpack Compose, Material 3 (custom colour scheme from the three tokens).
- **Playback:** AndroidX Media3 (ExoPlayer) + FFmpeg decoder extension; optional libmpv engine behind a `PlayerEngine` interface.
- **Data:** Room (library index, history, playlists), DataStore (settings), MediaStore + SAF for files.
- **Network:** SMBJ / sshj / Apache commons-net / OkHttp; Jellyfin/Plex/Emby REST clients.
- **DI/Architecture:** Hilt, MVVM + UDF, modular (`core`, `data`, `player`, `feature-*`).
- **Security:** AndroidX Biometric, EncryptedFile for private vault.
- **Build:** Gradle KTS, R8, baseline profiles, CI with GitHub Actions.

---

## 7. Permissions
`READ_MEDIA_VIDEO` (API 33+) / `READ_EXTERNAL_STORAGE`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `USE_BIOMETRIC`. Request lazily with clear rationale.

---

## 8. Monetization
Free and ad-free at launch. Optional future "NP Pro" one-time purchase (extra seek-bar styles, cloud drive, private vault extras). No dark patterns.

---

## 9. Release Plan
| Phase | Scope |
|---|---|
| **MVP (v1.0)** | Home/folders, scan, core player, gestures, resume/history, decoder, audio, subtitles, theme, settings (List/Player/General), PiP/background, playlists |
| **v1.1** | Customizable controls, seek-bar styles, private folder, network shares, network stream, language |
| **v1.2** | Media servers, Live TV, radio, podcasts, local network, cloud drive |
| **v1.3** | Media manager, file transfer, advanced engine options, status saver |

---

## 10. Success Metrics
D1 retention > 40%, D30 > 15%, playback-start success > 99%, crash-free > 99.5%, Play Store rating ≥ 4.5.

---

## 11. Risks & Mitigations
- **Codec/licensing (HEVC, AC3/DTS):** use platform decoders first; review FFmpeg licensing (LGPL build, dynamic linking).
- **Scoped storage changes:** rely on MediaStore + SAF, test Android 14/15.
- **Play Store policy:** status saver and file access flagged modules; clear privacy policy.
- **Content/piracy:** NP Player does not provide or link to copyrighted content; original branding and assets only (do not copy reference app names or art).
- **Fragmentation:** device test matrix (Xiaomi, Samsung, Pixel, Oppo).

---

## 12. Open Questions
1. Include libmpv engine in v1 or ExoPlayer only?
2. Ship Status Saver on Play Store or only on the website build?
3. Languages beyond English/Nepali/Hindi at launch?
