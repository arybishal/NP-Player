package com.npplayer.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
    GLOW
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

data class PlayerPreferences(
    val controlsTimeoutSeconds: Int = 3,
    val doubleTapSeekSeconds: Int = 10,
    val rememberVolumeAndBrightness: Boolean = true,
    val autoplayNext: Boolean = true,
    val seekUndoEnabled: Boolean = true,
    val seekBarStyle: SeekBarStylePreference = SeekBarStylePreference.CLASSIC,
    val readAheadCacheMb: Int = 150,
    val backSeekCacheMb: Int = 50,
    val hardwareAcceleration: String = "AUTOMATIC" // AUTOMATIC, DISABLED, DECODING_ONLY, FULL
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
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
}
