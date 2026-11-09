package com.npplayer.feature.library.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.database.dao.FolderDao
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.entity.FolderEntity
import com.npplayer.core.data.preferences.SettingsRepository
import com.npplayer.feature.library.scanner.MediaStoreScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeFilterChip {
    ALL,
    MUSIC,
    PRIVATE,
    NETWORK,
    PLAYLISTS
}

data class HomeUiState(
    val folders: List<FolderEntity> = emptyList(),
    val filteredFolders: List<FolderEntity> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isGridView: Boolean = false,
    val selectedChip: HomeFilterChip = HomeFilterChip.ALL,
    val isScanning: Boolean = false,
    val lastPlayedPath: String? = null,
    val lastPlayedTitle: String? = null,
    val showResumeFab: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val folderDao: FolderDao,
    private val videoDao: VideoDao,
    private val scanner: MediaStoreScanner,
    private val settingsRepository: SettingsRepository,
    val musicPlayerEngine: com.npplayer.player.music.MusicPlayerEngine
) : ViewModel() {

    val currentSong = musicPlayerEngine.currentSong
    val isMusicPlaying = musicPlayerEngine.isPlaying
    val musicCurrentPositionMs = musicPlayerEngine.currentPositionMs
    val musicDurationMs = musicPlayerEngine.durationMs

    fun toggleMusicPlayPause() = musicPlayerEngine.togglePlayPause()
    fun skipMusicNext() = musicPlayerEngine.skipToNext()
    fun skipMusicPrevious() = musicPlayerEngine.skipToPrevious()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = combine(
        _uiState,
        folderDao.getAllFolders(),
        settingsRepository.lastPlayedFlow,
        settingsRepository.listPreferencesFlow
    ) { currentUiState, dbFolders, lastPlayed, listPrefs ->
        val filtered = if (currentUiState.searchQuery.isBlank()) {
            dbFolders
        } else {
            dbFolders.filter { it.name.contains(currentUiState.searchQuery, ignoreCase = true) }
        }

        currentUiState.copy(
            folders = dbFolders,
            filteredFolders = filtered,
            lastPlayedPath = lastPlayed.first,
            lastPlayedTitle = lastPlayed.second,
            showResumeFab = listPrefs.floatingResumeButton && !lastPlayed.first.isNullOrBlank()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        rescanMedia()
    }

    fun rescanMedia() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            try {
                scanner.scanMedia()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isScanning = false) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleSearchActive() {
        _uiState.update {
            val nextState = !it.isSearchActive
            it.copy(
                isSearchActive = nextState,
                searchQuery = if (!nextState) "" else it.searchQuery
            )
        }
    }

    fun toggleGridView() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    fun selectChip(chip: HomeFilterChip) {
        _uiState.update {
            val next = if (it.selectedChip == chip) HomeFilterChip.ALL else chip
            it.copy(selectedChip = next)
        }
    }
}
