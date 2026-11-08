package com.npplayer.player.engine

import kotlinx.coroutines.flow.StateFlow

data class PlayerPlaybackState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val videoTitle: String = "",
    val videoPath: String = "",
    val isEnded: Boolean = false,
    val isLoading: Boolean = false
)

interface PlayerEngine {
    val state: StateFlow<PlayerPlaybackState>

    fun play(path: String, title: String, startPositionMs: Long = 0L)
    fun pause()
    fun resume()
    fun seekTo(positionMs: Long)
    fun setPlaybackSpeed(speed: Float)
    fun setVolume(volume: Float)
    fun release()
}
