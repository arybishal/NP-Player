package com.npplayer.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.preferences.SeekBarStylePreference
import com.npplayer.core.data.preferences.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeekBarStyleUiState(
    val selectedStyle: SeekBarStylePreference = SeekBarStylePreference.CLASSIC,
    val sandboxPositionMs: Long = 255000L, // 04:15 demo
    val sandboxDurationMs: Long = 930000L  // 15:30 demo
)

@HiltViewModel
class SeekBarStyleViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val sandboxPos = MutableStateFlow(255000L)

    val uiState: StateFlow<SeekBarStyleUiState> = combine(
        settingsRepository.playerPreferencesFlow,
        sandboxPos
    ) { prefs, pos ->
        SeekBarStyleUiState(
            selectedStyle = prefs.seekBarStyle,
            sandboxPositionMs = pos,
            sandboxDurationMs = 930000L
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SeekBarStyleUiState()
    )

    fun selectStyle(style: SeekBarStylePreference) {
        viewModelScope.launch {
            settingsRepository.setSeekBarStyle(style)
        }
    }

    fun updateSandboxPosition(positionMs: Long) {
        sandboxPos.value = positionMs
    }
}
