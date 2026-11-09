package com.npplayer.feature.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.database.dao.StreamBookmarkDao
import com.npplayer.core.data.database.entity.StreamBookmarkEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkHubViewModel @Inject constructor(
    private val streamBookmarkDao: StreamBookmarkDao
) : ViewModel() {

    val savedStreams = streamBookmarkDao.getAllStreams().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveAndPlayStream(title: String, url: String, type: String = "HLS", onPlay: (String, String) -> Unit) {
        if (url.isBlank()) return
        val streamTitle = title.ifBlank { "Network Stream" }
        viewModelScope.launch {
            streamBookmarkDao.insertStream(
                StreamBookmarkEntity(
                    title = streamTitle,
                    url = url.trim(),
                    type = type,
                    lastPlayedMs = System.currentTimeMillis()
                )
            )
            onPlay(url.trim(), streamTitle)
        }
    }

    fun deleteStream(stream: StreamBookmarkEntity) {
        viewModelScope.launch {
            streamBookmarkDao.deleteStream(stream)
        }
    }
}
