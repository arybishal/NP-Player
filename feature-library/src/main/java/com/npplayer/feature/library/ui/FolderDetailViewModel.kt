package com.npplayer.feature.library.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.entity.VideoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class FolderDetailUiState(
    val folderPath: String = "",
    val folderName: String = "",
    val videos: List<VideoEntity> = emptyList(),
    val filteredVideos: List<VideoEntity> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false
)

@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    private val videoDao: VideoDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val folderPath: String = savedStateHandle["folderPath"] ?: ""
    private val folderName: String = savedStateHandle["folderName"] ?: "Folder"

    private val _uiState = MutableStateFlow(
        FolderDetailUiState(
            folderPath = folderPath,
            folderName = folderName
        )
    )

    val uiState: StateFlow<FolderDetailUiState> = combine(
        _uiState,
        videoDao.getVideosByFolder(folderPath)
    ) { state, videos ->
        val filtered = if (state.searchQuery.isBlank()) {
            videos
        } else {
            videos.filter { it.title.contains(state.searchQuery, ignoreCase = true) }
        }
        state.copy(
            videos = videos,
            filteredVideos = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _uiState.value
    )

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleSearchActive() {
        _uiState.update {
            val next = !it.isSearchActive
            it.copy(
                isSearchActive = next,
                searchQuery = if (!next) "" else it.searchQuery
            )
        }
    }
}
