# NP Player — Music Module v2 (Plan + Coding Prompt, with Lyrics)

Yes, this is very doable. NP Player already has a Media3 player, a library scanner, playlists, an equalizer, a sleep timer and a MediaSession service, so the music module reuses most of it. It is a new section opened by the **Music** chip on Home.

---

# PART 1 — PLAN

## 1. Goals
- A full offline music player inside NP Player, as good as the best free ones, in the same white / black / yellow identity.
- Reuse the existing engine, theme, search, playlist, equalizer, sleep-timer and notification code.
- Free, ad-free, no account, no tracking.

## 2. Navigation
Home → tap **Music** chip → Music section with its own top tabs and a persistent **mini player**. A "Videos" back button/chip returns to the video library. Music keeps playing when the user goes back to videos or leaves the app.

Music tabs (scrollable chips): **Songs · Albums · Artists · Playlists · Genres · Folders · Favorites · Recently added**

## 3. Library and sorting
Source: MediaStore Audio (incremental scan, background), with embedded-tag fallback. Minimum track length filter (default 30 s) and an "Excluded folders" list to hide voice notes and call recordings.

| View | Contents | Sort options |
|---|---|---|
| Songs | all tracks | Title, Artist, Album, Track number, Duration, Date added, Year, Play count, Size |
| Albums | grid with cover art | Album name, Artist, Year, Date added, Number of tracks |
| Artists | list/grid | Name, Number of albums, Number of songs |
| Genres | list → songs | Name, Number of songs |
| Playlists | user + smart playlists | Name, Date created, Number of songs |
| Folders | folder tree with music | Name, Number of songs |
| Favorites | liked songs | Date liked, Title, Artist |
| Recently added | last 7/14/30/90 days | Date added (default) |

Every list: ascending/descending toggle, A–Z fast-scroll bar, grid/list toggle, search, alphabet jump.

## 4. Song actions (long-press or ⋮ menu)
Play next · Add to queue · Add to playlist · Add/Remove favorite · Go to album · **Go to artist** · Share · **Edit song info** · Set as ringtone · Details · Delete · Hide song · Add to "Play later".
Multi-select mode works exactly like the video selection mode (select all, add to queue/playlist, favorite, share, delete).

## 5. Now Playing screen
- Large album art (swipe left/right for next/previous), title, artist, album, seek bar with time, play/pause, previous/next, shuffle, repeat (off/all/one), favorite heart, queue button, ⋮ menu.
- Seek-bar style follows the video seek-bar setting where it makes sense (Classic / Wave / Glow).
- Extra buttons: Sleep timer, Equalizer, Playback speed (0.5x–2x), Lyrics, Share, Add to playlist.
- Background: black (dark) / white (light) with a subtle tint of the dominant cover color mixed only toward black or white (no new hues). Yellow accent for the progress and active icons.
- Lyrics: synced lyrics from embedded tags, sidecar `.lrc` files, a local cache, and online from **LRCLIB** (free, no API key). Synced highlight line, tap a line to seek, manual offset, plain-text fallback, "Instrumental" state, paste/add lyrics manually, optional save as `.lrc` or embed in tags. LRCLIB attribution is shown in the lyrics screen and About.
- Sleep timer: 15/30/45/60 min, end of song, end of album/queue, custom; fade-out in the last 30 s.
- **Queue screen**: drag to reorder, swipe to remove, "Play next", clear queue, save queue as playlist, current song highlighted in yellow.

## 6. Playback features
Gapless playback, crossfade (0–12 s), ReplayGain/volume normalization, equalizer + bass boost + virtualizer (shared with video), playback speed, skip silence, audio focus (pause for calls, duck for notifications), pause when headphones unplugged, Bluetooth/headset buttons, resume last queue and position after the app restarts, shuffle that avoids repeats, "Play all / Shuffle all" buttons on every list.

## 7. Playlists
User playlists (create, rename, reorder, delete, cover from first 4 album arts), import/export `.m3u`, and **smart playlists** (auto): Recently added, Recently played, Most played, Never played, Favorites.

## 8. Edit song info (tags)
Edit title, artist, album, album artist, genre, year, track no., disc no., lyrics, comment and cover art (pick from gallery or remove). Supports MP3, M4A/AAC, FLAC, OGG/Opus, WAV (where tags exist). Uses a tag library (TagLib via JNI or JAudioTagger) and Android scoped-storage write requests (`MediaStore.createWriteRequest` on Android 11+). After saving, rescan the file so the library updates. Show clear errors when the file is read-only.

## 9. System integration
Media notification with album art and controls (previous, play/pause, next, favorite), lock-screen controls, Bluetooth metadata, Android Auto (optional, later), home-screen widget (4x1 and 2x2, black/white/yellow), car mode (large buttons, optional), "Open with NP Player" for audio files, share sheet, and a quick **Audio-only mode** for videos that hands the video's audio into the same queue.

## 10. Settings > Music
Minimum song length, excluded folders, show/hide folders tab, gapless on/off, crossfade length, ReplayGain mode, skip silence, headset behavior, lyrics options, tag-edit confirmation, cover-art quality, "Pause when another app plays", "Resume on headset connect" (off by default).

## 11. Non-functional
Library of 20,000 songs scrolls at 60/120 FPS, first screen under 300 ms from cache, memory-friendly cover art (Coil, correct sizes, disk cache), no wakeups when idle, full TalkBack support, contrast ≥ 7:1.

## 12. Release plan
| Version | Scope |
|---|---|
| Music v1 | Library (Songs, Albums, Artists, Playlists, Recently added), Now Playing, queue, mini player, favorites, sleep timer, notification, search |
| Music v1.1 | Genres, Folders, tag editing, share, go to artist, smart playlists, lyrics |
| Music v1.2 | Gapless/crossfade/ReplayGain, widget, m3u import/export, equalizer integration polish |
| Later | Android Auto, scrobbling (optional, opt-in), podcasts/radio merge with the Network section |

---

# PART 2 — CODING PROMPT (paste into your coding agent)

```
TASK: Add a full Music Player module to NP Player. It opens when the user taps the "Music" chip on the Home screen. Reuse existing code (Media3 player/service, theme, Room, search, selection mode, playlists, equalizer, sleep timer, notifications). Follow the existing architecture (MVVM, Hilt, Compose, Room, DataStore).

DESIGN RULES
- Colors ONLY: White #FFFFFF, Black #000000, Yellow #FFD60A (greys = black/white at lower opacity). Text on yellow is black. Theme follows the device (light/dark) by default. No other hues anywhere (no red hearts: the favorite heart is yellow when active, outlined when not).
- Same look as the video library: rounded cards (16–28dp), outlined icons, one font family, 48dp touch targets.
- Placeholder cover art: black/white square with a yellow music-note icon (no stock photos).
Diagnose the existing code first and tell me what you will reuse. Build and test after each step.

STEP 1 — DATA LAYER
- Room tables: Song (stable id, mediaStoreId, uri, title, artist, albumArtist, album, genre, year, trackNo, discNo, duration, size, dateAdded, dateModified, mimeType, bitrate, hidden), Album, Artist (derived or table), Playlist, PlaylistSong (with order), Favorite, PlayHistory (playCount, lastPlayed), QueueState (saved queue, index, position, shuffle/repeat).
- Scanner: MediaStore.Audio incremental scan on a background dispatcher (use generation/dateModified to only update changes), min-length filter (default 30 s), excluded folders, handle removed/renamed files, run on app start and when the library changes (ContentObserver).
- Repositories + Flows; indices for title/artist/album for fast search. Embedded cover art loaded with Coil using a custom fetcher + disk cache, correct sizes.

STEP 2 — MUSIC HOME SCREEN
- Top bar: back to videos, title "Music", search, view toggle (list/grid), sort/menu. Under it, scrollable chips: Songs, Albums, Artists, Playlists, Genres, Folders, Favorites, Recently added. The tab choice is remembered.
- A "Shuffle all" and "Play all" row at the top of Songs.
- Each tab has Sort options (Songs: Title, Artist, Album, Track number, Duration, Date added, Year, Play count, Size. Albums: Name, Artist, Year, Date added, Track count. Artists: Name, Albums count, Songs count. Genres: Name, Song count. Playlists: Name, Date created, Song count. Folders: Name, Song count. Favorites: Date liked, Title, Artist. Recently added: Date added with a range filter 7/14/30/90 days) and an ascending/descending toggle; remember per tab.
- A–Z fast scroller on long lists; sticky section headers; empty states with a clear message and an action.
- Albums: 2-column grid with cover art; Album detail: cover, artist, year, track list, Play / Shuffle, add to queue/playlist.
- Artists: list with circular image (use the first album art), Artist detail: albums + all songs.
- Genres: list → song list. Folders: folder browser with breadcrumb.
- Search: reuse the global search fix (auto keyboard, debounce); search songs, albums, artists, playlists in grouped results.

STEP 3 — PLAYBACK SERVICE
- Extend the existing Media3 MediaSessionService for audio: queue (play next, add to queue, reorder, remove, clear), shuffle, repeat off/all/one, gapless, resume last queue + position after app restart, audio focus, becoming noisy (pause on headphone unplug), Bluetooth/headset buttons, MediaSession metadata (title, artist, album, art) for lock screen/Bluetooth.
- Notification: album art, previous/play-pause/next and a Favorite action, yellow small icon (ic_stat_np), dismiss behaviour per standard.
- Playing a video must pause music (and the opposite), unless the user chooses "Audio-only mode for video".
- Settings: crossfade 0–12 s, ReplayGain/normalization (off/track/album), skip silence, playback speed (0.5–2x), equalizer/bass boost/virtualizer (reuse the video equalizer presets).
- Counting: increment playCount when >50% or >30 s is played; update lastPlayed.

STEP 4 — NOW PLAYING + MINI PLAYER + QUEUE
- Persistent mini player above the bottom edge in the Music section (and on Home when music is active): cover art, title, artist, play/pause, next; swipe up opens Now Playing, swipe left/right changes track, progress line in yellow.
- Now Playing: large cover (swipe to change track), title/artist/album, seek bar (reuse the seek-bar styles) with times, play/pause, previous/next, shuffle, repeat, favorite, queue, and a ⋮ menu. Quick actions row: Sleep timer, Equalizer, Speed, Lyrics, Share, Add to playlist.
- Queue screen: current song highlighted in yellow, drag handles to reorder, swipe to remove, "Clear queue", "Save as playlist", total time remaining.
- Lyrics: the lyrics button opens the lyrics view built in STEP 5B (leave a placeholder button until then).
- Sleep timer: 15/30/45/60 min, end of song, end of queue, custom; 30 s fade-out; remaining time shown on the button; cancellable.

STEP 5 — SONG ACTIONS + SELECTION MODE
- Song menu (⋮ and long-press): Play next, Add to queue, Add to playlist, Add/Remove favorite, Go to album, Go to artist, Share, Edit song info, Details, Hide song, Delete (with confirmation, MediaStore delete request on Android 11+), Set as ringtone (with WRITE_SETTINGS flow).
- Multi-select mode (same UI/behavior as the video selection mode): select all, add to queue, add to playlist, favorite, share, delete.
- Share: ACTION_SEND / SEND_MULTIPLE with FileProvider URIs, audio MIME type.
- Playlists: create/rename/delete/reorder, auto cover (up to 4 album arts), import/export .m3u. Smart playlists: Recently added, Recently played, Most played, Never played, Favorites.
- Favorites: heart toggle everywhere; Favorites tab.

STEP 5B — LYRICS (embedded + .lrc + LRCLIB)
SOURCE ORDER (first match wins)
1. Embedded lyrics in the song tags (USLT/SYLT, Vorbis LYRICS).
2. Sidecar .lrc file next to the song (same name) or in a configured "Lyrics" folder.
3. Lyrics saved earlier in the app cache (Room).
4. LRCLIB online, only if "Fetch lyrics online" is ON and the device has internet.
LRCLIB API (check https://lrclib.net/docs for the current spec)
- Base URL: https://lrclib.net/api . No API key.
- GET /api/get?track_name=&artist_name=&album_name=&duration=  (exact match, duration in seconds)
- GET /api/search?q= or track_name=&artist_name=  (fallback; pick the best by duration within about 3 s, then artist/title similarity)
- GET /api/get/{id}
- Response fields: syncedLyrics (LRC text), plainLyrics, instrumental flag.
- Send a clear User-Agent: "NPPlayer/<version> (contact: <email or website>)".
- Be polite: fetch only when the lyrics view is opened or the song starts with lyrics enabled; cache every result (cache "not found" for 7 days); limit concurrent requests; 10 s timeout; retry once.
IMPLEMENTATION
- LyricsProvider interface (Embedded, Sidecar, Cache, Lrclib) so more providers can be added later.
- LRC parser: [mm:ss.xx], [mm:ss.xxx], several timestamps per line, [offset:], metadata tags, enhanced word timestamps <mm:ss.xx> (word-level highlight when present). Unit tests for all of these.
- Query building: clean the title (remove "(Official Video)", "feat.", brackets, track number prefixes), normalize artist (try the first artist of "A, B"), use the real duration of the file.
- Lyrics UI in Now Playing: full-screen lyrics view; current line large with a yellow highlight, previous/next lines faded; smooth auto-scroll; tap a line to seek; manual scroll pauses auto-follow and shows a "Sync" button. Plain lyrics: scrollable with a "Not synced" note. Instrumental: "Instrumental". Not found: friendly empty state with "Search again" (edit title/artist), "Add .lrc file" and "Paste lyrics".
- Offset control (-5 s to +5 s, 100 ms steps) saved per song, with reset.
- Attribution "Lyrics from LRCLIB" (with link) in the lyrics screen footer and in About.
- Optional save: "Save lyrics as .lrc next to the song" and "Embed lyrics in tags" (uses the tag editor, with confirmation).
- Settings > Music > Lyrics: Fetch lyrics online (default ON, Wi-Fi only option), Auto-save .lrc, Text size, Alignment (left/center), Keep screen on in lyrics view, Clear lyrics cache.
- Optional (off by default): show the current lyric line in the mini player.
- Errors: offline, 404, timeouts, malformed LRC are handled quietly; playback is never affected.
- Tests: parser, query cleaner, best-match picker, caching of misses, offset persistence, tap-to-seek UI test.

STEP 6 — EDIT SONG INFO (TAGS)
- Editor screen: title, artist, album, album artist, genre, year, track no., disc no., lyrics, comment, cover art (choose from gallery, remove). Supports MP3, M4A/AAC, FLAC, OGG/Opus where possible.
- Use a tag library (TagLib via JNI or JAudioTagger). Write through scoped storage: request MediaStore.createWriteRequest on Android 11+, SAF/legacy permission on older versions. Write to a temp copy then replace the file safely so a failed write never corrupts the song. After saving, update MediaStore/the Room entry and thumbnails/cover cache. Show clear errors (read-only file, no permission, unsupported format). Offer "Edit multiple songs" for album/artist/genre batch edit (optional).

STEP 7 — INTEGRATION
- Home "Music" chip opens the Music section; keep a back path to Videos. Music keeps playing when leaving the section or the app.
- "Open with NP Player" intent filters for audio files and a minimal player screen for external audio launches.
- Widget (Glance): 4x1 and 2x2 sizes, black/white/yellow only, with cover art, title, play/pause, next.
- Add Settings > Music (min length, excluded folders, gapless, crossfade, ReplayGain, skip silence, show tabs, lyrics options, headset behavior, hide short tracks, cover art quality).
- Android Auto: add as a later optional step; keep a clean MediaBrowserService structure so it can be enabled.

STEP 8 — QUALITY
- Performance: 20,000 songs list stays smooth (use stable keys, paging or lazy lists, no heavy work in composables, cover art sized to the view). First screen from cache < 300 ms.
- Tests: scanner (add/remove/rename), sorting for every tab, queue operations, shuffle without repeats, sleep timer + fade, tag writing (success and failure), playlist import/export, resume of saved queue.
- Accessibility: TalkBack labels, 48dp targets, contrast >= 7:1, screen reader announcements for track changes.
- Permissions: READ_MEDIA_AUDIO (API 33+) / READ_EXTERNAL_STORAGE below, POST_NOTIFICATIONS, FOREGROUND_SERVICE_MEDIA_PLAYBACK; request lazily with rationale.

DELIVERABLES
- After each step: what you built, files changed, anything limited by Android version or device, and a short test result.
START with STEP 1 and STEP 2 only (library + Music home with working sorting), then STOP and report. Continue with the next steps only when I say so.
```

---

## Notes
- **Tag editing** is the trickiest part on modern Android because of scoped storage, so it is its own step with safe write-and-replace.
- **Crossfade and gapless** work best on newer Media3; the agent should report any device limits.
- Keep the **Music chip** tied to a bottom mini-player so users can browse videos while a song plays.
