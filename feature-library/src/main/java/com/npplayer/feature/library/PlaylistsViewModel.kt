package com.npplayer.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.database.dao.PlaylistDao
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.entity.PlaylistEntity
import com.npplayer.core.data.database.entity.PlaylistItemEntity
import com.npplayer.core.data.database.entity.VideoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val videoDao: VideoDao
) : ViewModel() {

    val playlists: StateFlow<List<PlaylistEntity>> = playlistDao.getAllPlaylists().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val availableVideos: StateFlow<List<VideoEntity>> = videoDao.getAllVideos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            playlistDao.insertPlaylist(
                PlaylistEntity(
                    name = name.trim(),
                    createdAtMs = System.currentTimeMillis(),
                    videoCount = 0
                )
            )
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistDao.deletePlaylistById(playlistId)
        }
    }

    fun addVideoToPlaylist(playlistId: Long, videoId: Long) {
        viewModelScope.launch {
            val count = playlistDao.getItemCount(playlistId)
            playlistDao.insertItem(
                PlaylistItemEntity(
                    playlistId = playlistId,
                    videoId = videoId,
                    orderIndex = count
                )
            )
            playlistDao.updateCount(playlistId, count + 1)
        }
    }

    fun removeVideoFromPlaylist(playlistId: Long, videoId: Long) {
        viewModelScope.launch {
            playlistDao.removeItem(playlistId, videoId)
            val count = playlistDao.getItemCount(playlistId)
            playlistDao.updateCount(playlistId, count)
        }
    }

    fun getVideosForPlaylist(playlistId: Long): Flow<List<VideoEntity>> {
        return playlistDao.getVideosForPlaylist(playlistId)
    }
}
