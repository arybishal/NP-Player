package com.npplayer.player.engine

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Media3PlayerEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : PlayerEngine {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(PlayerPlaybackState())
    override val state: StateFlow<PlayerPlaybackState> = _state.asStateFlow()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _state.update {
                            it.copy(
                                durationMs = exoPlayer.duration.coerceAtLeast(0L),
                                isLoading = false
                            )
                        }
                    }
                    Player.STATE_BUFFERING -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                    Player.STATE_ENDED -> {
                        _state.update { it.copy(isEnded = true, isPlaying = false) }
                        stopProgressTracker()
                    }
                    Player.STATE_IDLE -> {
                        _state.update { it.copy(isLoading = false) }
                    }
                }
            }
        })
    }

    override fun play(path: String, title: String, startPositionMs: Long) {
        val uri = if (path.startsWith("content://") || path.startsWith("http://") || path.startsWith("https://")) {
            Uri.parse(path)
        } else {
            Uri.fromFile(java.io.File(path))
        }

        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaId(path)
            .build()

        _state.update {
            it.copy(
                videoPath = path,
                videoTitle = title,
                isEnded = false,
                currentPositionMs = startPositionMs
            )
        }

        exoPlayer.setMediaItem(mediaItem)
        if (startPositionMs > 0) {
            exoPlayer.seekTo(startPositionMs)
        }
        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun resume() {
        exoPlayer.play()
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _state.update { it.copy(currentPositionMs = positionMs) }
    }

    override fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
        _state.update { it.copy(playbackSpeed = speed) }
    }

    override fun setVolume(volume: Float) {
        exoPlayer.volume = volume.coerceIn(0f, 1f)
        _state.update { it.copy(volume = volume) }
    }

    override fun release() {
        stopProgressTracker()
        exoPlayer.release()
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                _state.update {
                    it.copy(
                        currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                        durationMs = exoPlayer.duration.coerceAtLeast(0L),
                        bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(0L)
                    )
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }
}
