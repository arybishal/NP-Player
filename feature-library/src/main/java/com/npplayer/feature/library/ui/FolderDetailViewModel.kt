package com.npplayer.feature.library.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.database.dao.FolderDao
import com.npplayer.core.data.database.dao.LabelDao
import com.npplayer.core.data.database.dao.PlaylistDao
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.entity.FolderEntity
import com.npplayer.core.data.database.entity.LabelEntity
import com.npplayer.core.data.database.entity.PlaylistEntity
import com.npplayer.core.data.database.entity.PlaylistItemEntity
import com.npplayer.core.data.database.entity.VideoEntity
import com.npplayer.core.data.database.entity.VideoLabelCrossRef
import com.npplayer.core.data.repository.VaultRepository
import com.npplayer.core.data.subtitles.OnlineSubtitleManager
import com.npplayer.core.data.subtitles.SubtitleItem
import com.npplayer.feature.library.ui.selection.SelectionModeState
import com.npplayer.feature.library.util.FileOperationsHelper
import com.npplayer.player.ui.sheet.OnlineSubtitlesUiState
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

data class FolderDetailUiState(
    val folderPath: String = "",
    val folderName: String = "",
    val videos: List<VideoEntity> = emptyList(),
    val filteredVideos: List<VideoEntity> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectionState: SelectionModeState = SelectionModeState(),
    val allLabels: List<LabelEntity> = emptyList(),
    val videoLabelsMap: Map<Long, List<String>> = emptyMap(),
    val selectedLabelFilterId: Long? = null,
    val allFolders: List<FolderEntity> = emptyList(),
    val allPlaylists: List<PlaylistEntity> = emptyList(),
    val operationProgress: String? = null,
    val isOperationInProgress: Boolean = false,
    val onlineSubtitlesState: OnlineSubtitlesUiState = OnlineSubtitlesUiState.Idle
)

@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    private val videoDao: VideoDao,
    private val folderDao: FolderDao,
    private val playlistDao: PlaylistDao,
    private val labelDao: LabelDao,
    private val vaultRepository: VaultRepository,
    private val fileOperationsHelper: FileOperationsHelper,
    private val onlineSubtitleManager: OnlineSubtitleManager,
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

    private val videosFlow = videoDao.getVideosByFolder(folderPath)
    private val labelsFlow = labelDao.getAllLabels()
    private val crossRefsFlow = labelDao.getAllCrossRefs()
    private val foldersFlow = folderDao.getAllFolders()
    private val playlistsFlow = playlistDao.getAllPlaylists()

    val uiState: StateFlow<FolderDetailUiState> = combine(
        combine(_uiState, videosFlow) { state, videos -> Pair(state, videos) },
        combine(labelsFlow, crossRefsFlow) { labels, crossRefs -> Pair(labels, crossRefs) },
        combine(foldersFlow, playlistsFlow) { folders, playlists -> Pair(folders, playlists) }
    ) { (state, videos), (labels, crossRefs), (folders, playlists) ->
        val labelMap = labels.associateBy { it.id }
        val videoLabels = crossRefs.groupBy({ it.videoId }, { labelMap[it.labelId]?.name }).mapValues { (_, names) ->
            names.filterNotNull()
        }

        val videoIdsWithSelectedLabel = if (state.selectedLabelFilterId != null) {
            crossRefs.filter { it.labelId == state.selectedLabelFilterId }.map { it.videoId }.toSet()
        } else null

        val filtered = videos.filter { video ->
            val matchesQuery = if (state.searchQuery.isBlank()) true else video.title.contains(state.searchQuery, ignoreCase = true)
            val matchesLabel = if (videoIdsWithSelectedLabel == null) true else videoIdsWithSelectedLabel.contains(video.id)
            matchesQuery && matchesLabel
        }

        state.copy(
            videos = videos,
            filteredVideos = filtered,
            allLabels = labels,
            videoLabelsMap = videoLabels,
            allFolders = folders,
            allPlaylists = playlists
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _uiState.value
    )

    fun startSelection(videoId: Long) {
        _uiState.update { it.copy(selectionState = SelectionModeState(isActive = true, selectedIds = setOf(videoId))) }
    }

    fun toggleSelection(videoId: Long) {
        _uiState.update { it.copy(selectionState = it.selectionState.toggle(videoId)) }
    }

    fun toggleSelectAll() {
        val currentList = uiState.value.filteredVideos.map { it.id }
        _uiState.update { it.copy(selectionState = it.selectionState.selectAll(currentList)) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectionState = it.selectionState.clear()) }
    }

    fun selectLabelFilter(labelId: Long?) {
        _uiState.update { it.copy(selectedLabelFilterId = labelId) }
    }

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

    // --- File Operations ---

    fun copySelected(targetFolderPath: String, onComplete: (Int) -> Unit) {
        val selected = getSelectedVideos()
        if (selected.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isOperationInProgress = true, operationProgress = "Copying 0 / ${selected.size}...") }
            val res = fileOperationsHelper.copyVideos(selected, targetFolderPath) { current, total, _ ->
                _uiState.update { it.copy(operationProgress = "Copying $current / $total...") }
            }
            _uiState.update { it.copy(isOperationInProgress = false, operationProgress = null) }
            clearSelection()
            onComplete(res.getOrDefault(0))
        }
    }

    fun moveSelected(targetFolderPath: String, onComplete: (Int) -> Unit) {
        val selected = getSelectedVideos()
        if (selected.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isOperationInProgress = true, operationProgress = "Moving 0 / ${selected.size}...") }
            val res = fileOperationsHelper.moveVideos(selected, targetFolderPath) { current, total, _ ->
                _uiState.update { it.copy(operationProgress = "Moving $current / $total...") }
            }
            _uiState.update { it.copy(isOperationInProgress = false, operationProgress = null) }
            clearSelection()
            onComplete(res.getOrDefault(0))
        }
    }

    fun renameSingle(videoId: Long, newName: String, onComplete: (Boolean, String) -> Unit) {
        val video = uiState.value.videos.find { it.id == videoId } ?: return
        viewModelScope.launch {
            val res = fileOperationsHelper.renameVideo(video, newName)
            if (res.isSuccess) {
                clearSelection()
                onComplete(true, "Renamed to $newName")
            } else {
                onComplete(false, res.exceptionOrNull()?.localizedMessage ?: "Failed to rename")
            }
        }
    }

    fun batchRename(pattern: String, startNumber: Int, onComplete: (Int) -> Unit) {
        val selected = getSelectedVideos()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationInProgress = true, operationProgress = "Renaming...") }
            val res = fileOperationsHelper.batchRenameVideos(selected, pattern, startNumber)
            _uiState.update { it.copy(isOperationInProgress = false, operationProgress = null) }
            clearSelection()
            onComplete(res.getOrDefault(0))
        }
    }

    fun deleteSelected(deleteMatchingSubs: Boolean, onComplete: (Int) -> Unit) {
        val selected = getSelectedVideos()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            val res = fileOperationsHelper.deleteVideos(selected, deleteMatchingSubs)
            clearSelection()
            onComplete(res.getOrDefault(0))
        }
    }

    fun addToPlaylist(playlistId: Long, onComplete: () -> Unit) {
        val selected = getSelectedVideos()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            val currentCount = playlistDao.getItemCount(playlistId)
            selected.forEachIndexed { idx, video ->
                playlistDao.insertItem(
                    PlaylistItemEntity(
                        playlistId = playlistId,
                        videoId = video.id,
                        orderIndex = currentCount + idx
                    )
                )
            }
            playlistDao.updateCount(playlistId, currentCount + selected.size)
            clearSelection()
            onComplete()
        }
    }

    fun createPlaylistAndAdd(name: String, onComplete: () -> Unit) {
        val selected = getSelectedVideos()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            val playlistId = playlistDao.insertPlaylist(
                PlaylistEntity(
                    name = name,
                    videoCount = selected.size
                )
            )
            selected.forEachIndexed { idx, video ->
                playlistDao.insertItem(
                    PlaylistItemEntity(
                        playlistId = playlistId,
                        videoId = video.id,
                        orderIndex = idx
                    )
                )
            }
            clearSelection()
            onComplete()
        }
    }

    fun createLabel(name: String) {
        viewModelScope.launch {
            labelDao.insertLabel(LabelEntity(name = name))
        }
    }

    fun searchOnlineSubtitles(video: VideoEntity, manualQuery: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(onlineSubtitlesState = OnlineSubtitlesUiState.Loading) }
            val res = onlineSubtitleManager.searchSubtitles(
                videoPath = video.path,
                videoTitle = video.title,
                manualQuery = manualQuery,
                preferredLanguages = listOf("en")
            )
            res.fold(
                onSuccess = { items ->
                    _uiState.update {
                        it.copy(onlineSubtitlesState = if (items.isEmpty()) OnlineSubtitlesUiState.Empty else OnlineSubtitlesUiState.Success(items))
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(onlineSubtitlesState = OnlineSubtitlesUiState.Error(error.localizedMessage ?: "Failed to find subtitles"))
                    }
                }
            )
        }
    }

    fun downloadSubtitle(item: SubtitleItem, videoPath: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = onlineSubtitleManager.downloadAndApplySubtitle(item, videoPath)
            res.fold(
                onSuccess = { file ->
                    onComplete(true, "Subtitle saved: ${file.name}")
                },
                onFailure = { err ->
                    onComplete(false, err.localizedMessage ?: "Download failed")
                }
            )
        }
    }

    fun dismissOnlineSubtitles() {
        _uiState.update { it.copy(onlineSubtitlesState = OnlineSubtitlesUiState.Idle) }
    }

    fun applyLabels(assignedLabelIds: Set<Long>, removedLabelIds: Set<Long>) {
        val selectedIds = uiState.value.selectionState.selectedIds
        if (selectedIds.isEmpty()) return
        viewModelScope.launch {
            assignedLabelIds.forEach { labelId ->
                val refs = selectedIds.map { videoId -> VideoLabelCrossRef(videoId = videoId, labelId = labelId) }
                labelDao.addLabelsToVideos(refs)
            }
            removedLabelIds.forEach { labelId ->
                labelDao.removeLabelFromVideos(selectedIds.toList(), labelId)
            }
        }
    }

    fun lockSelectedInVault(onComplete: (Int) -> Unit) {
        val selected = getSelectedVideos()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationInProgress = true, operationProgress = "Locking in Private Folder...") }
            var count = 0
            for (video in selected) {
                if (vaultRepository.moveToVault(video)) {
                    videoDao.deleteVideoById(video.id)
                    count++
                }
            }
            _uiState.update { it.copy(isOperationInProgress = false, operationProgress = null) }
            clearSelection()
            onComplete(count)
        }
    }

    fun shareSelected(context: Context) {
        val selected = getSelectedVideos()
        fileOperationsHelper.shareVideos(context, selected)
    }

    fun rebuildThumbnails(context: Context, onComplete: () -> Unit) {
        val selected = getSelectedVideos()
        viewModelScope.launch {
            fileOperationsHelper.rebuildThumbnail(context, selected)
            onComplete()
        }
    }

    fun getSelectedVideos(): List<VideoEntity> {
        val selectedIds = uiState.value.selectionState.selectedIds
        return uiState.value.videos.filter { selectedIds.contains(it.id) }
    }
}
