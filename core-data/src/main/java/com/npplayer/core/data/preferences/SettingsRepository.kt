package com.npplayer.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "np_player_settings")

enum class AppTheme {
    FOLLOW_DEVICE,
    LIGHT,
    DARK
}

enum class SeekBarStylePreference {
    CLASSIC,
    CHAPTERS,
    WAVE,
    GLOW,
    NEON_MINIMAL,
    GRADIENT_FLAME,
    RETRO_BLOCKS,
    BUBBLE_POP,
    AMOLED_PULSE
}

enum class ResumePromptPreference(val id: String, val title: String) {
    ASK("ask", "Ask me"),
    ALWAYS_RESUME("always_resume", "Always resume"),
    ALWAYS_START_OVER("always_start_over", "Always start from the beginning")
}

data class ListPreferences(
    val newPeriodDays: Int = 7,
    val markLastMedia: Boolean = true,
    val autoScrollToLast: Boolean = true,
    val selectByThumbnail: Boolean = false,
    val floatingResumeButton: Boolean = true,
    val showHiddenFiles: Boolean = false,
    val respectNoMedia: Boolean = true
)

data class AudioPreferences(
    val rememberAudioTrack: Boolean = true,
    val applyToFolderSeries: Boolean = true,
    val preferredAudioLanguages: List<String> = listOf("en", "hi", "es")
)

data class SubtitlePreferences(
    val color: String = "YELLOW", // "YELLOW", "WHITE"
    val sizeSp: Int = 18,        // 14, 18, 22, 28
    val outlineType: String = "OUTLINE", // "OUTLINE", "BOX", "NONE"
    val autoDetectSibling: Boolean = true,
    val autoSearchOnline: Boolean = false,
    val preferredLanguages: Set<String> = setOf("en")
)

data class MusicPreferences(
    val minDurationSec: Int = 30,
    val gaplessEnabled: Boolean = true,
    val crossfadeSec: Int = 0,
    val replayGain: String = "OFF", // OFF, TRACK, ALBUM
    val skipSilence: Boolean = false,
    val fetchLyricsOnline: Boolean = true,
    val lyricsWifiOnly: Boolean = false,
    val autoSaveLrc: Boolean = false,
    val lyricsFontSizeSp: Int = 22,
    val lyricsAlignment: String = "CENTER", // CENTER, LEFT
    val keepScreenOnInLyrics: Boolean = true,
    val pauseOnHeadsetDisconnect: Boolean = true,
    val resumeOnHeadsetConnect: Boolean = false
)

data class PlayerPreferences(
    val controlsTimeoutSeconds: Int = 3,
    val doubleTapSeekSeconds: Int = 10,
    val rememberVolumeAndBrightness: Boolean = true,
    val autoplayNext: Boolean = true,
    val seekUndoEnabled: Boolean = true,
    val seekBarStyle: SeekBarStylePreference = SeekBarStylePreference.CLASSIC,
    val readAheadCacheMb: Int = 150,
    val backSeekCacheMb: Int = 50,
    val hardwareAcceleration: String = "AUTOMATIC" // AUTOMATIC, HARDWARE, SOFTWARE, DECODING_ONLY
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : com.npplayer.core.data.repository.SearchHistoryStore {
    private val dataStore = context.dataStore

    private object Keys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val NEW_PERIOD_DAYS = intPreferencesKey("new_period_days")
        val MARK_LAST_MEDIA = booleanPreferencesKey("mark_last_media")
        val AUTO_SCROLL_TO_LAST = booleanPreferencesKey("auto_scroll_to_last")
        val SELECT_BY_THUMBNAIL = booleanPreferencesKey("select_by_thumbnail")
        val FLOATING_RESUME_BUTTON = booleanPreferencesKey("floating_resume_button")
        val SHOW_HIDDEN_FILES = booleanPreferencesKey("show_hidden_files")
        val RESPECT_NO_MEDIA = booleanPreferencesKey("respect_no_media")

        val CONTROLS_TIMEOUT = intPreferencesKey("controls_timeout")
        val DOUBLE_TAP_SEEK = intPreferencesKey("double_tap_seek")
        val REMEMBER_VOL_BRIGHTNESS = booleanPreferencesKey("remember_vol_brightness")
        val AUTOPLAY_NEXT = booleanPreferencesKey("autoplay_next")
        val SEEK_UNDO = booleanPreferencesKey("seek_undo")
        val SEEK_BAR_STYLE = stringPreferencesKey("seek_bar_style")
        val READ_AHEAD_CACHE = intPreferencesKey("read_ahead_cache")
        val BACK_SEEK_CACHE = intPreferencesKey("back_seek_cache")
        val HW_ACCEL = stringPreferencesKey("hw_accel")

        val SLOTS_BESIDE_TITLE = stringPreferencesKey("slots_beside_title")
        val SLOTS_TOP_RIGHT = stringPreferencesKey("slots_top_right")
        val SLOTS_BOTTOM_LEFT = stringPreferencesKey("slots_bottom_left")
        val SLOTS_BOTTOM_RIGHT = stringPreferencesKey("slots_bottom_right")
        val CUSTOM_ENGINE_OPTIONS = stringPreferencesKey("custom_engine_options")

        val LAST_PLAYED_VIDEO_PATH = stringPreferencesKey("last_played_video_path")
        val LAST_PLAYED_VIDEO_TITLE = stringPreferencesKey("last_played_video_title")
        val LAST_PLAYED_POSITION = intPreferencesKey("last_played_position")

        val VAULT_PIN = stringPreferencesKey("vault_pin")
        val VAULT_BIOMETRIC_ENABLED = booleanPreferencesKey("vault_biometric_enabled")

        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val SUBTITLE_COLOR = stringPreferencesKey("subtitle_color")
        val SUBTITLE_SIZE = intPreferencesKey("subtitle_size")
        val SUBTITLE_OUTLINE = stringPreferencesKey("subtitle_outline")
        val SUBTITLE_AUTO_DETECT = booleanPreferencesKey("subtitle_auto_detect")
        val SUBTITLE_AUTO_SEARCH = booleanPreferencesKey("subtitle_auto_search")
        val SUBTITLE_PREFERRED_LANGS = stringSetPreferencesKey("subtitle_preferred_langs")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches_list")

        val PICTURE_BRIGHTNESS = floatPreferencesKey("picture_brightness")
        val PICTURE_CONTRAST = floatPreferencesKey("picture_contrast")
        val PICTURE_SATURATION = floatPreferencesKey("picture_saturation")
        val PICTURE_GAMMA = floatPreferencesKey("picture_gamma")
        val PICTURE_SHARPNESS = floatPreferencesKey("picture_sharpness")
        val PICTURE_WARMTH = floatPreferencesKey("picture_warmth")
        val PICTURE_HUE = floatPreferencesKey("picture_hue")
        val PICTURE_PRESET = stringPreferencesKey("picture_preset")
        val PICTURE_REMEMBER_PER_VIDEO = booleanPreferencesKey("picture_remember_per_video")
        val PICTURE_SCREENSHOT_FILTERS = booleanPreferencesKey("picture_screenshot_filters")
        val RESUME_PROMPT_MODE = stringPreferencesKey("resume_prompt_mode")

        val ASPECT_RATIO_MODE = stringPreferencesKey("aspect_ratio_mode")
        val ASPECT_RATIO_PRESET = stringPreferencesKey("aspect_ratio_preset")
        val ASPECT_RATIO_CUSTOM_VALUE = floatPreferencesKey("aspect_ratio_custom_value")
        val ASPECT_RATIO_REMEMBER_PER_VIDEO = booleanPreferencesKey("aspect_ratio_remember_per_video")

        val FPS_TARGET = intPreferencesKey("fps_target")
        val FPS_MEMC_ENABLED = booleanPreferencesKey("fps_memc_enabled")
        val FPS_MEMC_QUALITY = stringPreferencesKey("fps_memc_quality")

        val AUDIO_REMEMBER_TRACK = booleanPreferencesKey("audio_remember_track")
        val AUDIO_APPLY_FOLDER_SERIES = booleanPreferencesKey("audio_apply_folder_series")
        val AUDIO_PREFERRED_LANGUAGES = stringPreferencesKey("audio_preferred_languages")

        val MUSIC_SELECTED_TAB = stringPreferencesKey("music_selected_tab")
        val MUSIC_GRID_VIEW = booleanPreferencesKey("music_grid_view")
        val MUSIC_MIN_DURATION_MS = intPreferencesKey("music_min_duration_ms")
        val MUSIC_GAPLESS = booleanPreferencesKey("music_gapless")
        val MUSIC_CROSSFADE = intPreferencesKey("music_crossfade")
        val MUSIC_REPLAYGAIN = stringPreferencesKey("music_replaygain")
        val MUSIC_SKIP_SILENCE = booleanPreferencesKey("music_skip_silence")
        val MUSIC_FETCH_LYRICS_ONLINE = booleanPreferencesKey("music_fetch_lyrics_online")
        val MUSIC_LYRICS_WIFI_ONLY = booleanPreferencesKey("music_lyrics_wifi_only")
        val MUSIC_AUTO_SAVE_LRC = booleanPreferencesKey("music_auto_save_lrc")
        val MUSIC_LYRICS_FONT_SIZE = intPreferencesKey("music_lyrics_font_size")
        val MUSIC_LYRICS_ALIGNMENT = stringPreferencesKey("music_lyrics_alignment")
        val MUSIC_LYRICS_KEEP_SCREEN = booleanPreferencesKey("music_lyrics_keep_screen")
        val MUSIC_PAUSE_HEADSET_DISCONNECT = booleanPreferencesKey("music_pause_headset_disconnect")
        val MUSIC_RESUME_HEADSET_CONNECT = booleanPreferencesKey("music_resume_headset_connect")
    }

    val themeFlow: Flow<AppTheme> = dataStore.data.map { prefs ->
        when (prefs[Keys.APP_THEME]) {
            AppTheme.LIGHT.name -> AppTheme.LIGHT
            AppTheme.DARK.name -> AppTheme.DARK
            else -> AppTheme.FOLLOW_DEVICE
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { prefs ->
            prefs[Keys.APP_THEME] = theme.name
        }
    }

    val listPreferencesFlow: Flow<ListPreferences> = dataStore.data.map { prefs ->
        ListPreferences(
            newPeriodDays = prefs[Keys.NEW_PERIOD_DAYS] ?: 7,
            markLastMedia = prefs[Keys.MARK_LAST_MEDIA] ?: true,
            autoScrollToLast = prefs[Keys.AUTO_SCROLL_TO_LAST] ?: true,
            selectByThumbnail = prefs[Keys.SELECT_BY_THUMBNAIL] ?: false,
            floatingResumeButton = prefs[Keys.FLOATING_RESUME_BUTTON] ?: true,
            showHiddenFiles = prefs[Keys.SHOW_HIDDEN_FILES] ?: false,
            respectNoMedia = prefs[Keys.RESPECT_NO_MEDIA] ?: true
        )
    }

    suspend fun updateListPreferences(preferences: ListPreferences) {
        dataStore.edit { prefs ->
            prefs[Keys.NEW_PERIOD_DAYS] = preferences.newPeriodDays
            prefs[Keys.MARK_LAST_MEDIA] = preferences.markLastMedia
            prefs[Keys.AUTO_SCROLL_TO_LAST] = preferences.autoScrollToLast
            prefs[Keys.SELECT_BY_THUMBNAIL] = preferences.selectByThumbnail
            prefs[Keys.FLOATING_RESUME_BUTTON] = preferences.floatingResumeButton
            prefs[Keys.SHOW_HIDDEN_FILES] = preferences.showHiddenFiles
            prefs[Keys.RESPECT_NO_MEDIA] = preferences.respectNoMedia
        }
    }

    val playerPreferencesFlow: Flow<PlayerPreferences> = dataStore.data.map { prefs ->
        PlayerPreferences(
            controlsTimeoutSeconds = prefs[Keys.CONTROLS_TIMEOUT] ?: 3,
            doubleTapSeekSeconds = prefs[Keys.DOUBLE_TAP_SEEK] ?: 10,
            rememberVolumeAndBrightness = prefs[Keys.REMEMBER_VOL_BRIGHTNESS] ?: true,
            autoplayNext = prefs[Keys.AUTOPLAY_NEXT] ?: true,
            seekUndoEnabled = prefs[Keys.SEEK_UNDO] ?: true,
            seekBarStyle = when (prefs[Keys.SEEK_BAR_STYLE]) {
                SeekBarStylePreference.CHAPTERS.name -> SeekBarStylePreference.CHAPTERS
                SeekBarStylePreference.WAVE.name -> SeekBarStylePreference.WAVE
                SeekBarStylePreference.GLOW.name -> SeekBarStylePreference.GLOW
                SeekBarStylePreference.NEON_MINIMAL.name -> SeekBarStylePreference.NEON_MINIMAL
                SeekBarStylePreference.GRADIENT_FLAME.name -> SeekBarStylePreference.GRADIENT_FLAME
                SeekBarStylePreference.RETRO_BLOCKS.name -> SeekBarStylePreference.RETRO_BLOCKS
                SeekBarStylePreference.BUBBLE_POP.name -> SeekBarStylePreference.BUBBLE_POP
                SeekBarStylePreference.AMOLED_PULSE.name -> SeekBarStylePreference.AMOLED_PULSE
                else -> SeekBarStylePreference.CLASSIC
            },
            readAheadCacheMb = prefs[Keys.READ_AHEAD_CACHE] ?: 150,
            backSeekCacheMb = prefs[Keys.BACK_SEEK_CACHE] ?: 50,
            hardwareAcceleration = prefs[Keys.HW_ACCEL] ?: "AUTOMATIC"
        )
    }

    suspend fun updatePlayerPreferences(preferences: PlayerPreferences) {
        dataStore.edit { prefs ->
            prefs[Keys.CONTROLS_TIMEOUT] = preferences.controlsTimeoutSeconds
            prefs[Keys.DOUBLE_TAP_SEEK] = preferences.doubleTapSeekSeconds
            prefs[Keys.REMEMBER_VOL_BRIGHTNESS] = preferences.rememberVolumeAndBrightness
            prefs[Keys.AUTOPLAY_NEXT] = preferences.autoplayNext
            prefs[Keys.SEEK_UNDO] = preferences.seekUndoEnabled
            prefs[Keys.SEEK_BAR_STYLE] = preferences.seekBarStyle.name
            prefs[Keys.READ_AHEAD_CACHE] = preferences.readAheadCacheMb
            prefs[Keys.BACK_SEEK_CACHE] = preferences.backSeekCacheMb
            prefs[Keys.HW_ACCEL] = preferences.hardwareAcceleration
        }
    }

    suspend fun saveLastPlayed(path: String, title: String, positionMs: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_PLAYED_VIDEO_PATH] = path
            prefs[Keys.LAST_PLAYED_VIDEO_TITLE] = title
            prefs[Keys.LAST_PLAYED_POSITION] = positionMs
        }
    }

    val controlsSlotsFlow: Flow<PlayerControlsSlots> = dataStore.data.map { prefs ->
        val besideTitleStr = prefs[Keys.SLOTS_BESIDE_TITLE]
        val topRightStr = prefs[Keys.SLOTS_TOP_RIGHT]
        val bottomLeftStr = prefs[Keys.SLOTS_BOTTOM_LEFT]
        val bottomRightStr = prefs[Keys.SLOTS_BOTTOM_RIGHT]

        if (besideTitleStr == null && topRightStr == null && bottomLeftStr == null && bottomRightStr == null) {
            PlayerControlsSlots.DEFAULT
        } else {
            PlayerControlsSlots(
                besideTitle = parseActions(besideTitleStr, PlayerControlsSlots.DEFAULT.besideTitle),
                topRight = parseActions(topRightStr, PlayerControlsSlots.DEFAULT.topRight),
                bottomLeft = parseActions(bottomLeftStr, PlayerControlsSlots.DEFAULT.bottomLeft),
                bottomRight = parseActions(bottomRightStr, PlayerControlsSlots.DEFAULT.bottomRight)
            )
        }
    }

    suspend fun saveControlsSlots(slots: PlayerControlsSlots) {
        dataStore.edit { prefs ->
            prefs[Keys.SLOTS_BESIDE_TITLE] = slots.besideTitle.joinToString(",") { it.id }
            prefs[Keys.SLOTS_TOP_RIGHT] = slots.topRight.joinToString(",") { it.id }
            prefs[Keys.SLOTS_BOTTOM_LEFT] = slots.bottomLeft.joinToString(",") { it.id }
            prefs[Keys.SLOTS_BOTTOM_RIGHT] = slots.bottomRight.joinToString(",") { it.id }
        }
    }

    suspend fun resetControlsSlots() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.SLOTS_BESIDE_TITLE)
            prefs.remove(Keys.SLOTS_TOP_RIGHT)
            prefs.remove(Keys.SLOTS_BOTTOM_LEFT)
            prefs.remove(Keys.SLOTS_BOTTOM_RIGHT)
        }
    }

    suspend fun setSeekBarStyle(style: SeekBarStylePreference) {
        dataStore.edit { prefs ->
            prefs[Keys.SEEK_BAR_STYLE] = style.name
        }
    }

    val customEngineOptionsFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.CUSTOM_ENGINE_OPTIONS] ?: "# Default Engine Parameters\npreferred-audio-lang=en,ne,hi\npreferred-subtitle-lang=en\nread-ahead-cache-seconds=30\nhardware-decoding=auto"
    }

    suspend fun saveCustomEngineOptions(options: String) {
        dataStore.edit { prefs ->
            prefs[Keys.CUSTOM_ENGINE_OPTIONS] = options
        }
    }

    private fun parseActions(raw: String?, fallback: List<PlayerAction>): List<PlayerAction> {
        if (raw == null) return fallback
        if (raw.isBlank()) return emptyList()
        val actionMap = PlayerAction.values().associateBy { it.id }
        return raw.split(",").mapNotNull { actionMap[it.trim()] }
    }

    val lastPlayedFlow = dataStore.data.map { prefs ->
        Triple(
            prefs[Keys.LAST_PLAYED_VIDEO_PATH],
            prefs[Keys.LAST_PLAYED_VIDEO_TITLE],
            prefs[Keys.LAST_PLAYED_POSITION] ?: 0
        )
    }

    val vaultPinFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.VAULT_PIN]
    }

    suspend fun setVaultPin(pin: String?) {
        dataStore.edit { prefs ->
            if (pin == null) {
                prefs.remove(Keys.VAULT_PIN)
            } else {
                prefs[Keys.VAULT_PIN] = pin
            }
        }
    }

    val vaultBiometricEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.VAULT_BIOMETRIC_ENABLED] ?: true
    }

    suspend fun setVaultBiometricEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.VAULT_BIOMETRIC_ENABLED] = enabled
        }
    }

    val appLanguageFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.APP_LANGUAGE] ?: "en"
    }

    suspend fun setAppLanguage(lang: String) {
        dataStore.edit { prefs ->
            prefs[Keys.APP_LANGUAGE] = lang
        }
    }

    val subtitlePreferencesFlow: Flow<SubtitlePreferences> = dataStore.data.map { prefs ->
        SubtitlePreferences(
            color = prefs[Keys.SUBTITLE_COLOR] ?: "YELLOW",
            sizeSp = prefs[Keys.SUBTITLE_SIZE] ?: 18,
            outlineType = prefs[Keys.SUBTITLE_OUTLINE] ?: "OUTLINE",
            autoDetectSibling = prefs[Keys.SUBTITLE_AUTO_DETECT] ?: true,
            autoSearchOnline = prefs[Keys.SUBTITLE_AUTO_SEARCH] ?: false,
            preferredLanguages = prefs[Keys.SUBTITLE_PREFERRED_LANGS] ?: setOf("en")
        )
    }

    suspend fun updateSubtitlePreferences(subPrefs: SubtitlePreferences) {
        dataStore.edit { prefs ->
            prefs[Keys.SUBTITLE_COLOR] = subPrefs.color
            prefs[Keys.SUBTITLE_SIZE] = subPrefs.sizeSp
            prefs[Keys.SUBTITLE_OUTLINE] = subPrefs.outlineType
            prefs[Keys.SUBTITLE_AUTO_DETECT] = subPrefs.autoDetectSibling
            prefs[Keys.SUBTITLE_AUTO_SEARCH] = subPrefs.autoSearchOnline
            prefs[Keys.SUBTITLE_PREFERRED_LANGS] = subPrefs.preferredLanguages
        }
    }

    suspend fun setHardwareAcceleration(mode: String) {
        dataStore.edit { prefs ->
            prefs[Keys.HW_ACCEL] = mode
        }
    }

    override val recentSearchesFlow: Flow<List<String>> = dataStore.data.map { prefs ->
        val raw = prefs[Keys.RECENT_SEARCHES] ?: ""
        if (raw.isBlank()) emptyList() else raw.split("\n").filter { it.isNotBlank() }
    }

    override suspend fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_SEARCHES]?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()
            val updated = (listOf(trimmed) + current.filter { !it.equals(trimmed, ignoreCase = true) }).take(10)
            prefs[Keys.RECENT_SEARCHES] = updated.joinToString("\n")
        }
    }

    override suspend fun removeRecentSearch(query: String) {
        val trimmed = query.trim()
        dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_SEARCHES]?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()
            val updated = current.filter { !it.equals(trimmed, ignoreCase = true) }
            prefs[Keys.RECENT_SEARCHES] = updated.joinToString("\n")
        }
    }

    override suspend fun clearRecentSearches() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.RECENT_SEARCHES)
        }
    }

    val pictureSettingsFlow: Flow<PictureSettings> = dataStore.data.map { prefs ->
        val presetName = prefs[Keys.PICTURE_PRESET] ?: PicturePreset.DEFAULT.name
        val preset = try {
            PicturePreset.valueOf(presetName)
        } catch (_: Exception) {
            PicturePreset.DEFAULT
        }
        PictureSettings(
            brightness = prefs[Keys.PICTURE_BRIGHTNESS] ?: 0.0f,
            contrast = prefs[Keys.PICTURE_CONTRAST] ?: 1.0f,
            saturation = prefs[Keys.PICTURE_SATURATION] ?: 1.0f,
            gamma = prefs[Keys.PICTURE_GAMMA] ?: 1.0f,
            sharpness = prefs[Keys.PICTURE_SHARPNESS] ?: 0.0f,
            warmth = prefs[Keys.PICTURE_WARMTH] ?: 0.0f,
            hue = prefs[Keys.PICTURE_HUE] ?: 0.0f,
            activePreset = preset,
            rememberPerVideo = prefs[Keys.PICTURE_REMEMBER_PER_VIDEO] ?: true,
            includeFiltersInScreenshot = prefs[Keys.PICTURE_SCREENSHOT_FILTERS] ?: false
        )
    }

    suspend fun updatePictureSettings(settings: PictureSettings) {
        dataStore.edit { prefs ->
            prefs[Keys.PICTURE_BRIGHTNESS] = settings.brightness
            prefs[Keys.PICTURE_CONTRAST] = settings.contrast
            prefs[Keys.PICTURE_SATURATION] = settings.saturation
            prefs[Keys.PICTURE_GAMMA] = settings.gamma
            prefs[Keys.PICTURE_SHARPNESS] = settings.sharpness
            prefs[Keys.PICTURE_WARMTH] = settings.warmth
            prefs[Keys.PICTURE_HUE] = settings.hue
            prefs[Keys.PICTURE_PRESET] = settings.activePreset.name
            prefs[Keys.PICTURE_REMEMBER_PER_VIDEO] = settings.rememberPerVideo
            prefs[Keys.PICTURE_SCREENSHOT_FILTERS] = settings.includeFiltersInScreenshot
        }
    }

    suspend fun getVideoPictureSettings(videoPath: String): PictureSettings? {
        if (videoPath.isBlank()) return null
        val key = stringPreferencesKey("vid_pic_${videoPath.hashCode()}")
        val prefs = dataStore.data.first()
        val raw = prefs[key] ?: return null
        val parts = raw.split(";")
        if (parts.size >= 8) {
            try {
                val preset = try { PicturePreset.valueOf(parts[7]) } catch (_: Exception) { PicturePreset.DEFAULT }
                return PictureSettings(
                    brightness = parts[0].toFloat(),
                    contrast = parts[1].toFloat(),
                    saturation = parts[2].toFloat(),
                    gamma = parts[3].toFloat(),
                    sharpness = parts[4].toFloat(),
                    warmth = parts[5].toFloat(),
                    hue = parts[6].toFloat(),
                    activePreset = preset,
                    rememberPerVideo = true
                )
            } catch (_: Exception) {
                return null
            }
        }
        return null
    }

    suspend fun saveVideoPictureSettings(videoPath: String, settings: PictureSettings) {
        if (videoPath.isBlank()) return
        val key = stringPreferencesKey("vid_pic_${videoPath.hashCode()}")
        val serialized = "${settings.brightness};${settings.contrast};${settings.saturation};${settings.gamma};${settings.sharpness};${settings.warmth};${settings.hue};${settings.activePreset.name}"
        dataStore.edit { prefs ->
            prefs[key] = serialized
        }
    }

    val resumePromptPreferenceFlow: Flow<ResumePromptPreference> = dataStore.data.map { prefs ->
        when (prefs[Keys.RESUME_PROMPT_MODE]) {
            ResumePromptPreference.ALWAYS_RESUME.name -> ResumePromptPreference.ALWAYS_RESUME
            ResumePromptPreference.ALWAYS_START_OVER.name -> ResumePromptPreference.ALWAYS_START_OVER
            else -> ResumePromptPreference.ASK
        }
    }

    suspend fun setResumePromptPreference(preference: ResumePromptPreference) {
        dataStore.edit { prefs ->
            prefs[Keys.RESUME_PROMPT_MODE] = preference.name
        }
    }

    suspend fun clearLastPlayedIfMatches(path: String) {
        dataStore.edit { prefs ->
            if (prefs[Keys.LAST_PLAYED_VIDEO_PATH] == path) {
                prefs.remove(Keys.LAST_PLAYED_POSITION)
            }
        }
    }

    val aspectRatioPreferencesFlow: Flow<AspectRatioPreferences> = dataStore.data.map { prefs ->
        val modeStr = prefs[Keys.ASPECT_RATIO_MODE] ?: AspectRatioMode.FIT.id
        val preset = prefs[Keys.ASPECT_RATIO_PRESET] ?: "16:9"
        val customVal = prefs[Keys.ASPECT_RATIO_CUSTOM_VALUE] ?: (16f / 9f)
        val remember = prefs[Keys.ASPECT_RATIO_REMEMBER_PER_VIDEO] ?: false
        AspectRatioPreferences(
            mode = AspectRatioMode.fromId(modeStr),
            customPreset = preset,
            customRatioValue = customVal,
            rememberPerVideo = remember
        )
    }

    suspend fun updateAspectRatioPreferences(preferences: AspectRatioPreferences) {
        dataStore.edit { prefs ->
            prefs[Keys.ASPECT_RATIO_MODE] = preferences.mode.id
            prefs[Keys.ASPECT_RATIO_PRESET] = preferences.customPreset
            prefs[Keys.ASPECT_RATIO_CUSTOM_VALUE] = preferences.customRatioValue
            prefs[Keys.ASPECT_RATIO_REMEMBER_PER_VIDEO] = preferences.rememberPerVideo
        }
    }

    suspend fun getVideoAspectRatioPreferences(videoPath: String): AspectRatioPreferences? {
        if (videoPath.isBlank()) return null
        val key = stringPreferencesKey("vid_ar_${videoPath.hashCode()}")
        val prefs = dataStore.data.first()
        val raw = prefs[key] ?: return null
        val parts = raw.split(";")
        if (parts.size >= 3) {
            return try {
                AspectRatioPreferences(
                    mode = AspectRatioMode.fromId(parts[0]),
                    customPreset = parts[1],
                    customRatioValue = parts[2].toFloat(),
                    rememberPerVideo = true
                )
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    suspend fun saveVideoAspectRatioPreferences(videoPath: String, preferences: AspectRatioPreferences) {
        if (videoPath.isBlank()) return
        val key = stringPreferencesKey("vid_ar_${videoPath.hashCode()}")
        val serialized = "${preferences.mode.id};${preferences.customPreset};${preferences.customRatioValue}"
        dataStore.edit { prefs ->
            prefs[key] = serialized
        }
    }

    val audioPreferencesFlow: Flow<AudioPreferences> = dataStore.data.map { prefs ->
        val rawLangs = prefs[Keys.AUDIO_PREFERRED_LANGUAGES] ?: "en,hi,es"
        AudioPreferences(
            rememberAudioTrack = prefs[Keys.AUDIO_REMEMBER_TRACK] ?: true,
            applyToFolderSeries = prefs[Keys.AUDIO_APPLY_FOLDER_SERIES] ?: true,
            preferredAudioLanguages = rawLangs.split(",").map { it.trim() }.filter { it.isNotBlank() }
        )
    }

    suspend fun updateAudioPreferences(preferences: AudioPreferences) {
        dataStore.edit { prefs ->
            prefs[Keys.AUDIO_REMEMBER_TRACK] = preferences.rememberAudioTrack
            prefs[Keys.AUDIO_APPLY_FOLDER_SERIES] = preferences.applyToFolderSeries
            prefs[Keys.AUDIO_PREFERRED_LANGUAGES] = preferences.preferredAudioLanguages.joinToString(",")
        }
    }

    val musicSelectedTabFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.MUSIC_SELECTED_TAB] ?: "SONGS"
    }

    suspend fun setMusicSelectedTab(tab: String) {
        dataStore.edit { prefs ->
            prefs[Keys.MUSIC_SELECTED_TAB] = tab
        }
    }

    val musicGridViewFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.MUSIC_GRID_VIEW] ?: false
    }

    suspend fun setMusicGridView(isGrid: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.MUSIC_GRID_VIEW] = isGrid
        }
    }

    fun getMusicSortOption(tab: String, defaultOption: String): Flow<String> {
        val key = stringPreferencesKey("music_sort_opt_$tab")
        return dataStore.data.map { prefs ->
            prefs[key] ?: defaultOption
        }
    }

    suspend fun setMusicSortOption(tab: String, option: String) {
        val key = stringPreferencesKey("music_sort_opt_$tab")
        dataStore.edit { prefs ->
            prefs[key] = option
        }
    }

    fun getMusicSortDirection(tab: String, defaultAscending: Boolean): Flow<Boolean> {
        val key = booleanPreferencesKey("music_sort_dir_$tab")
        return dataStore.data.map { prefs ->
            prefs[key] ?: defaultAscending
        }
    }

    suspend fun setMusicSortDirection(tab: String, ascending: Boolean) {
        val key = booleanPreferencesKey("music_sort_dir_$tab")
        dataStore.edit { prefs ->
            prefs[key] = ascending
        }
    }

    val musicPreferencesFlow: Flow<MusicPreferences> = dataStore.data.map { prefs ->
        MusicPreferences(
            minDurationSec = prefs[Keys.MUSIC_MIN_DURATION_MS] ?: 30,
            gaplessEnabled = prefs[Keys.MUSIC_GAPLESS] ?: true,
            crossfadeSec = prefs[Keys.MUSIC_CROSSFADE] ?: 0,
            replayGain = prefs[Keys.MUSIC_REPLAYGAIN] ?: "OFF",
            skipSilence = prefs[Keys.MUSIC_SKIP_SILENCE] ?: false,
            fetchLyricsOnline = prefs[Keys.MUSIC_FETCH_LYRICS_ONLINE] ?: true,
            lyricsWifiOnly = prefs[Keys.MUSIC_LYRICS_WIFI_ONLY] ?: false,
            autoSaveLrc = prefs[Keys.MUSIC_AUTO_SAVE_LRC] ?: false,
            lyricsFontSizeSp = prefs[Keys.MUSIC_LYRICS_FONT_SIZE] ?: 22,
            lyricsAlignment = prefs[Keys.MUSIC_LYRICS_ALIGNMENT] ?: "CENTER",
            keepScreenOnInLyrics = prefs[Keys.MUSIC_LYRICS_KEEP_SCREEN] ?: true,
            pauseOnHeadsetDisconnect = prefs[Keys.MUSIC_PAUSE_HEADSET_DISCONNECT] ?: true,
            resumeOnHeadsetConnect = prefs[Keys.MUSIC_RESUME_HEADSET_CONNECT] ?: false
        )
    }

    suspend fun updateMusicPreferences(preferences: MusicPreferences) {
        dataStore.edit { prefs ->
            prefs[Keys.MUSIC_MIN_DURATION_MS] = preferences.minDurationSec
            prefs[Keys.MUSIC_GAPLESS] = preferences.gaplessEnabled
            prefs[Keys.MUSIC_CROSSFADE] = preferences.crossfadeSec
            prefs[Keys.MUSIC_REPLAYGAIN] = preferences.replayGain
            prefs[Keys.MUSIC_SKIP_SILENCE] = preferences.skipSilence
            prefs[Keys.MUSIC_FETCH_LYRICS_ONLINE] = preferences.fetchLyricsOnline
            prefs[Keys.MUSIC_LYRICS_WIFI_ONLY] = preferences.lyricsWifiOnly
            prefs[Keys.MUSIC_AUTO_SAVE_LRC] = preferences.autoSaveLrc
            prefs[Keys.MUSIC_LYRICS_FONT_SIZE] = preferences.lyricsFontSizeSp
            prefs[Keys.MUSIC_LYRICS_ALIGNMENT] = preferences.lyricsAlignment
            prefs[Keys.MUSIC_LYRICS_KEEP_SCREEN] = preferences.keepScreenOnInLyrics
            prefs[Keys.MUSIC_PAUSE_HEADSET_DISCONNECT] = preferences.pauseOnHeadsetDisconnect
            prefs[Keys.MUSIC_RESUME_HEADSET_CONNECT] = preferences.resumeOnHeadsetConnect
        }
    }
}
