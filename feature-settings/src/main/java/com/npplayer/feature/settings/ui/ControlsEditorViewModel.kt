package com.npplayer.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.preferences.PlayerAction
import com.npplayer.core.data.preferences.PlayerControlsSlots
import com.npplayer.core.data.preferences.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SelectedSlotTier {
    BESIDE_TITLE,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

data class ControlsEditorUiState(
    val slots: PlayerControlsSlots = PlayerControlsSlots.DEFAULT,
    val selectedTier: SelectedSlotTier = SelectedSlotTier.TOP_RIGHT,
    val selectedAction: PlayerAction? = PlayerAction.SCREENSHOT,
    val isResetDialogVisible: Boolean = false
)

@HiltViewModel
class ControlsEditorViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ControlsEditorUiState())

    val uiState: StateFlow<ControlsEditorUiState> = combine(
        _uiState,
        settingsRepository.controlsSlotsFlow
    ) { current, dbSlots ->
        current.copy(slots = dbSlots)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ControlsEditorUiState()
    )

    fun selectTier(tier: SelectedSlotTier) {
        _uiState.update { it.copy(selectedTier = tier) }
    }

    fun selectAction(action: PlayerAction) {
        _uiState.update { it.copy(selectedAction = action) }
    }

    fun toggleActionInCurrentTier(action: PlayerAction) {
        val currentSlots = uiState.value.slots
        val tier = uiState.value.selectedTier

        val newSlots = when (tier) {
            SelectedSlotTier.BESIDE_TITLE -> {
                val list = currentSlots.besideTitle.toMutableList()
                if (list.contains(action)) {
                    list.remove(action)
                } else if (list.size < PlayerControlsSlots.MAX_BESIDE_TITLE) {
                    list.add(action)
                }
                currentSlots.copy(besideTitle = list)
            }
            SelectedSlotTier.TOP_RIGHT -> {
                val list = currentSlots.topRight.toMutableList()
                if (list.contains(action)) {
                    list.remove(action)
                } else if (list.size < PlayerControlsSlots.MAX_TOP_RIGHT) {
                    list.add(action)
                }
                currentSlots.copy(topRight = list)
            }
            SelectedSlotTier.BOTTOM_LEFT -> {
                val list = currentSlots.bottomLeft.toMutableList()
                if (list.contains(action)) {
                    list.remove(action)
                } else if (list.size < PlayerControlsSlots.MAX_BOTTOM_LEFT) {
                    list.add(action)
                }
                currentSlots.copy(bottomLeft = list)
            }
            SelectedSlotTier.BOTTOM_RIGHT -> {
                val list = currentSlots.bottomRight.toMutableList()
                if (list.contains(action)) {
                    list.remove(action)
                } else if (list.size < PlayerControlsSlots.MAX_BOTTOM_RIGHT) {
                    list.add(action)
                }
                currentSlots.copy(bottomRight = list)
            }
        }

        viewModelScope.launch {
            settingsRepository.saveControlsSlots(newSlots)
        }
    }

    fun removeAction(action: PlayerAction) {
        val currentSlots = uiState.value.slots
        val newSlots = currentSlots.copy(
            besideTitle = currentSlots.besideTitle.filter { it != action },
            topRight = currentSlots.topRight.filter { it != action },
            bottomLeft = currentSlots.bottomLeft.filter { it != action },
            bottomRight = currentSlots.bottomRight.filter { it != action }
        )
        viewModelScope.launch {
            settingsRepository.saveControlsSlots(newSlots)
        }
    }

    fun showResetDialog() {
        _uiState.update { it.copy(isResetDialogVisible = true) }
    }

    fun dismissResetDialog() {
        _uiState.update { it.copy(isResetDialogVisible = false) }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetControlsSlots()
            _uiState.update { it.copy(isResetDialogVisible = false) }
        }
    }
}
