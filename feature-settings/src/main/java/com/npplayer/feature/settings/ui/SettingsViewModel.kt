package com.npplayer.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.preferences.AppTheme
import com.npplayer.core.data.preferences.ListPreferences
import com.npplayer.core.data.preferences.PlayerPreferences
import com.npplayer.core.data.preferences.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val currentTheme: AppTheme = AppTheme.FOLLOW_DEVICE,
    val listPreferences: ListPreferences = ListPreferences(),
    val playerPreferences: PlayerPreferences = PlayerPreferences(),
    val isThemeDialogVisible: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val isDialogVisible = kotlinx.coroutines.flow.MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.themeFlow,
        settingsRepository.listPreferencesFlow,
        settingsRepository.playerPreferencesFlow,
        isDialogVisible
    ) { theme, listPrefs, playerPrefs, dialogVisible ->
        SettingsUiState(
            currentTheme = theme,
            listPreferences = listPrefs,
            playerPreferences = playerPrefs,
            isThemeDialogVisible = dialogVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun showThemeDialog() {
        isDialogVisible.value = true
    }

    fun dismissThemeDialog() {
        isDialogVisible.value = false
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
            isDialogVisible.value = false
        }
    }

    fun toggleFloatingResumeButton(enabled: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.listPreferences
            settingsRepository.updateListPreferences(current.copy(floatingResumeButton = enabled))
        }
    }
}
