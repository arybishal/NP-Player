package com.npplayer.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.preferences.PlayerPreferences
import com.npplayer.core.data.preferences.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdvancedSettingsUiState(
    val readAheadCacheMb: Int = 150,
    val backSeekCacheMb: Int = 50,
    val customEngineOptions: String = "",
    val isDebugOverlayEnabled: Boolean = false,
    val statusMessage: String? = null
)

@HiltViewModel
class AdvancedSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val customOptionsText = MutableStateFlow<String?>(null)
    private val debugOverlay = MutableStateFlow(false)
    private val status = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AdvancedSettingsUiState> = combine(
        settingsRepository.playerPreferencesFlow,
        settingsRepository.customEngineOptionsFlow,
        customOptionsText,
        debugOverlay,
        status
    ) { prefs, savedOptions, editedOptions, debug, statusMsg ->
        AdvancedSettingsUiState(
            readAheadCacheMb = prefs.readAheadCacheMb,
            backSeekCacheMb = prefs.backSeekCacheMb,
            customEngineOptions = editedOptions ?: savedOptions,
            isDebugOverlayEnabled = debug,
            statusMessage = statusMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdvancedSettingsUiState()
    )

    fun setReadAheadCache(mb: Int) {
        viewModelScope.launch {
            val current = settingsRepository.playerPreferencesFlow.first()
            settingsRepository.updatePlayerPreferences(current.copy(readAheadCacheMb = mb))
        }
    }

    fun setBackSeekCache(mb: Int) {
        viewModelScope.launch {
            val current = settingsRepository.playerPreferencesFlow.first()
            settingsRepository.updatePlayerPreferences(current.copy(backSeekCacheMb = mb))
        }
    }

    fun onEngineOptionsChange(text: String) {
        customOptionsText.value = text
    }

    fun applyEngineOptions() {
        viewModelScope.launch {
            val text = uiState.value.customEngineOptions
            settingsRepository.saveCustomEngineOptions(text)
            status.value = "Options successfully applied!"
        }
    }

    fun revertEngineOptions() {
        viewModelScope.launch {
            settingsRepository.saveCustomEngineOptions("# Default Engine Parameters\npreferred-audio-lang=en,ne,hi\npreferred-subtitle-lang=en\nread-ahead-cache-seconds=30\nhardware-decoding=auto")
            customOptionsText.value = null
            status.value = "Reverted to defaults."
        }
    }

    fun toggleDebugOverlay(enabled: Boolean) {
        debugOverlay.value = enabled
    }
}
