package com.npplayer.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.entity.VideoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class MediaManagerStats(
    val totalVideos: Int = 0,
    val totalSizeBytes: Long = 0L,
    val largeFilesCount: Int = 0,
    val duplicateGroupsCount: Int = 0
)

@HiltViewModel
class MediaManagerViewModel @Inject constructor(
    private val videoDao: VideoDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val allVideos: StateFlow<List<VideoEntity>> = videoDao.getAllVideos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stats: StateFlow<MediaManagerStats> = allVideos.map { videos ->
        val totalBytes = videos.sumOf { it.sizeBytes }
        val large = videos.count { it.sizeBytes >= 500 * 1024 * 1024 }
        val duplicates = videos.groupBy { "${it.sizeBytes}_${it.durationMs}" }.filter { it.value.size > 1 }.size
        MediaManagerStats(
            totalVideos = videos.size,
            totalSizeBytes = totalBytes,
            largeFilesCount = large,
            duplicateGroupsCount = duplicates
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MediaManagerStats()
    )

    fun clearPlaybackHistory() {
        viewModelScope.launch {
            videoDao.clearAllPlaybackHistory()
            _statusMessage.value = "Playback history cleared"
        }
    }

    fun clearThumbnailCache() {
        viewModelScope.launch {
            try {
                val cacheDir = context.cacheDir
                cacheDir.deleteRecursively()
                _statusMessage.value = "Thumbnail & temporary cache cleared"
            } catch (e: Exception) {
                _statusMessage.value = "Cache cleanup failed: ${e.message}"
            }
        }
    }

    fun deleteVideo(video: VideoEntity) {
        viewModelScope.launch {
            try {
                val file = File(video.path)
                if (file.exists()) {
                    file.delete()
                }
                videoDao.deleteVideoById(video.id)
                _statusMessage.value = "Video deleted"
            } catch (e: Exception) {
                _statusMessage.value = "Could not delete file"
            }
        }
    }

    fun dismissStatus() {
        _statusMessage.value = null
    }
}
