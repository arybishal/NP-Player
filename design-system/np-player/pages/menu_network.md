# Page Specification: Menu, Network Hub & Private Vault
> **Target Screens:** Menu Screen, Network Hub, Private Folder  
> **Source PRD:** Section 3, 4.9, 4.10, 4.13  
> **Master Reference:** `../MASTER.md`

---

## 1. Menu Screen Layout

```
┌────────────────────────────────────────────────────────┐
│ [←] Menu & Features                                    │ Top Bar (56dp)
├────────────────────────────────────────────────────────┤
│ Quick Tools:                                           │
│ [File Transfer]  [Private Vault]  [Video Playlists]    │ Grid (3 Columns)
├────────────────────────────────────────────────────────┤
│ Network & Streaming:                                   │ Section Header
│ [Network Stream] [Local Network] [Media Manager]       │ Grid (3 Columns)
│ [Cloud Drive]    [Status Saver]                        │
├────────────────────────────────────────────────────────┤
│ Application:                                           │ Section Header
│ [App Theme: Follow Device]                             │ List item with current value
│ [Settings]                                             │ List item with chevron
│ [App Language: English]                                │ List item with current value
├────────────────────────────────────────────────────────┤
│ [Help & FAQ]     [Legal & Licences]                    │ Compact Footer Links
└────────────────────────────────────────────────────────┘
```

### 1.1 Visual Presentation
* **Grid Tiles:** `SurfaceContainer` (`RoundedCornerShape(16.dp)`), `16.dp` padding.
  * Vector icon in `NPYellow` (or monochrome with yellow accent dot).
  * Label below icon in `TitleMedium` (14sp Medium, max 2 lines).
* **Dividers:** `BorderSubtle` (`alpha = 0.10f`), `0.8.dp` height.

---

## 2. Network Hub (Section 4.9)

### 2.1 Hub Sections
1. **Network Shares:** SMB (Windows Share), SFTP, FTP, WebDAV, NFS.
   * Add Share FAB or top action: Modal bottom sheet requesting Host, Port, Username, Password, Path.
2. **Media Servers:** Dedicated connection cards for Jellyfin, Emby, Plex.
   * Server card displays server name, host address, connection status (Online/Offline indicator).
   * Optional poster/synopsis toggle: User-controlled toggle to pull metadata only upon tapping video details.
3. **Network Stream (URL Input):**
   * High-contrast text input field with "Paste from Clipboard" button in `NPYellow`.
   * Protocol compatibility badges: `HLS`, `DASH`, `RTMP`, `RTSP`, `HTTP/S`.
   * Stream History list: Recent URLs with 1-tap replay and trash icon to clear.
4. **Live TV (IPTV):**
   * M3U / M3U8 / XMLTV playlist loader.
   * EPG channel guide grid: Channel logo thumbnail, channel number, current program name.
5. **Radio & Podcasts:**
   * Radio: Integration with Radio Browser API, categorized by country / genre, favourite heart icon.
   * Podcasts: Episode search, episode list with yellow progress indicators and resume points.
6. **Local Network (DLNA/UPnP):**
   * Discovers smart TVs and DLNA servers on Wi-Fi automatically.
   * Pull-to-refresh network rescan.

### 2.2 Metered Connection Warning Banner
* When playing over mobile data and user has enabled "Warn" in settings:
  * Modal alert dialog: `You are on a metered connection. Continue streaming?`
  * Buttons: `[STREAM ONCE]` (Outlined), `[ALWAYS ALLOW]` (`NPYellow` container, Black text).

---

## 3. Private Vault (Biometric / PIN)

### 3.1 Security Flow
1. **Unlock Screen:**
   * Clean AMOLED black background.
   * App Logo + Padlock icon (`Icons.Outlined.Lock`, 48dp, `NPYellow`).
   * "Unlock Private Folder" headline.
   * Biometric prompt trigger (Fingerprint / Face Unlock via AndroidX Biometric).
   * Fallback numeric PIN keypad (`3x4` grid, large 64dp circular keys with haptic feedback).
2. **Vault Content:**
   * Encrypted video grid/list.
   * Security banner: `Videos in this vault are hidden from Android Gallery and other apps.`
   * `FLAG_SECURE` enabled on window: Prevents screenshots and recents thumbnail caching.
   * Quick Lock action in top bar: Instantly locks and exits vault.
