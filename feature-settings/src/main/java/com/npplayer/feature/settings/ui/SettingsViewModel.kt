package com.npplayer.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.database.dao.VideoAudioPreferenceDao
import com.npplayer.core.data.preferences.AppTheme
import com.npplayer.core.data.preferences.AudioPreferences
import com.npplayer.core.data.preferences.ListPreferences
import com.npplayer.core.data.preferences.MusicPreferences
import com.npplayer.core.data.preferences.PictureSettings
import com.npplayer.core.data.preferences.PlayerPreferences
import com.npplayer.core.data.preferences.ResumePromptPreference
import com.npplayer.core.data.preferences.SettingsRepository
import com.npplayer.core.data.preferences.SubtitlePreferences
import com.npplayer.player.audio.AudioFxManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val currentTheme: AppTheme = AppTheme.FOLLOW_DEVICE,
    val listPreferences: ListPreferences = ListPreferences(),
    val playerPreferences: PlayerPreferences = PlayerPreferences(),
    val pictureSettings: PictureSettings = PictureSettings.DEFAULT,
    val appLanguage: String = "en",
    val subtitlePreferences: SubtitlePreferences = SubtitlePreferences(),
    val resumePromptPreference: ResumePromptPreference = ResumePromptPreference.ASK,
    val audioPreferences: AudioPreferences = AudioPreferences(),
    val isThemeDialogVisible: Boolean = false,
    val isDecoderDialogVisible: Boolean = false,
    val isEqualizerSheetVisible: Boolean = false,
    val isSubtitleDialogVisible: Boolean = false,
    val isLanguageDialogVisible: Boolean = false,
    val isPictureDialogVisible: Boolean = false,
    val isResumePromptDialogVisible: Boolean = false,
    val isAudioPreferencesDialogVisible: Boolean = false,
    val musicPreferences: MusicPreferences = MusicPreferences(),
    val isMusicSettingsVisible: Boolean = false
)

private data class DialogsState(
    val theme: Boolean = false,
    val decoder: Boolean = false,
    val equalizer: Boolean = false,
    val subtitle: Boolean = false,
    val language: Boolean = false,
    val picture: Boolean = false,
    val resumePrompt: Boolean = false,
    val audioPreferences: Boolean = false,
    val musicSettings: Boolean = false
)

private data class GeneralPreferencesGroup(
    val picSettings: PictureSettings,
    val lang: String,
    val subPrefs: SubtitlePreferences,
    val resumePref: ResumePromptPreference,
    val audioPrefs: AudioPreferences
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val videoAudioPreferenceDao: VideoAudioPreferenceDao,
    val audioFxManager: AudioFxManager
) : ViewModel() {

    private val dialogsState = MutableStateFlow(DialogsState())

    private val preferencesFlow = combine(
        combine(
            settingsRepository.themeFlow,
            settingsRepository.listPreferencesFlow,
            settingsRepository.playerPreferencesFlow
        ) { theme, listPrefs, playerPrefs ->
            Triple(theme, listPrefs, playerPrefs)
        },
        combine(
            settingsRepository.pictureSettingsFlow,
            settingsRepository.appLanguageFlow,
            settingsRepository.subtitlePreferencesFlow,
            settingsRepository.resumePromptPreferenceFlow,
            combine(settingsRepository.audioPreferencesFlow, settingsRepository.musicPreferencesFlow) { a, m -> Pair(a, m) }
        ) { picSettings, lang, subPrefs, resumePref, (audioPrefs, musicPrefs) ->
            SettingsUiState(
                currentTheme = AppTheme.FOLLOW_DEVICE,
                pictureSettings = picSettings,
                appLanguage = lang,
                subtitlePreferences = subPrefs,
                resumePromptPreference = resumePref,
                audioPreferences = audioPrefs,
                musicPreferences = musicPrefs
            )
        }
    ) { (theme, listPrefs, playerPrefs), partialState ->
        partialState.copy(
            currentTheme = theme,
            listPreferences = listPrefs,
            playerPreferences = playerPrefs
        )
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesFlow,
        dialogsState
    ) { baseState, dialogs ->
        baseState.copy(
            isThemeDialogVisible = dialogs.theme,
            isDecoderDialogVisible = dialogs.decoder,
            isEqualizerSheetVisible = dialogs.equalizer,
            isSubtitleDialogVisible = dialogs.subtitle,
            isLanguageDialogVisible = dialogs.language,
            isPictureDialogVisible = dialogs.picture,
            isResumePromptDialogVisible = dialogs.resumePrompt,
            isAudioPreferencesDialogVisible = dialogs.audioPreferences,
            isMusicSettingsVisible = dialogs.musicSettings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun showMusicSettings() { dialogsState.update { it.copy(musicSettings = true) } }
    fun dismissMusicSettings() { dialogsState.update { it.copy(musicSettings = false) } }
    fun updateMusicPreferences(prefs: MusicPreferences) {
        viewModelScope.launch {
            settingsRepository.updateMusicPreferences(prefs)
        }
    }

    fun showAudioPreferencesDialog() { dialogsState.update { it.copy(audioPreferences = true) } }
    fun dismissAudioPreferencesDialog() { dialogsState.update { it.copy(audioPreferences = false) } }

    fun toggleRememberAudioTrack(enabled: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.audioPreferences
            settingsRepository.updateAudioPreferences(current.copy(rememberAudioTrack = enabled))
        }
    }

    fun toggleApplyAudioToFolder(enabled: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.audioPreferences
            settingsRepository.updateAudioPreferences(current.copy(applyToFolderSeries = enabled))
        }
    }

    fun resetRememberedAudioTracks(onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            videoAudioPreferenceDao.clearAllPreferences()
            withContext(Dispatchers.Main) {
                onDone()
            }
        }
    }

    fun showResumePromptDialog() { dialogsState.update { it.copy(resumePrompt = true) } }
    fun dismissResumePromptDialog() { dialogsState.update { it.copy(resumePrompt = false) } }
    fun setResumePromptPreference(preference: ResumePromptPreference) {
        viewModelScope.launch {
            settingsRepository.setResumePromptPreference(preference)
            dismissResumePromptDialog()
        }
    }

    fun showPictureDialog() { dialogsState.update { it.copy(picture = true) } }
    fun dismissPictureDialog() { dialogsState.update { it.copy(picture = false) } }
    fun updatePictureSettings(settings: PictureSettings) {
        viewModelScope.launch {
            settingsRepository.updatePictureSettings(settings)
        }
    }

    fun showThemeDialog() { dialogsState.update { it.copy(theme = true) } }
    fun dismissThemeDialog() { dialogsState.update { it.copy(theme = false) } }
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
            dismissThemeDialog()
        }
    }

    fun showDecoderDialog() { dialogsState.update { it.copy(decoder = true) } }
    fun dismissDecoderDialog() { dialogsState.update { it.copy(decoder = false) } }
    fun setHardwareAcceleration(mode: String) {
        viewModelScope.launch {
            settingsRepository.setHardwareAcceleration(mode)
            dismissDecoderDialog()
        }
    }

    fun showEqualizerSheet() { dialogsState.update { it.copy(equalizer = true) } }
    fun dismissEqualizerSheet() { dialogsState.update { it.copy(equalizer = false) } }

    fun showSubtitleDialog() { dialogsState.update { it.copy(subtitle = true) } }
    fun dismissSubtitleDialog() { dialogsState.update { it.copy(subtitle = false) } }
    fun updateSubtitlePreferences(prefs: SubtitlePreferences) {
        viewModelScope.launch {
            settingsRepository.updateSubtitlePreferences(prefs)
        }
    }

    fun showLanguageDialog() { dialogsState.update { it.copy(language = true) } }
    fun dismissLanguageDialog() { dialogsState.update { it.copy(language = false) } }
    fun setAppLanguage(lang: String) {
        viewModelScope.launch {
            settingsRepository.setAppLanguage(lang)
            dismissLanguageDialog()
        }
    }

    fun toggleFloatingResumeButton(enabled: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.listPreferences
            settingsRepository.updateListPreferences(current.copy(floatingResumeButton = enabled))
        }
    }
}
