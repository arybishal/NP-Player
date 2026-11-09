package com.npplayer.core.data.repository

enum class MusicRepeatMode {
    OFF, ALL, ONE;

    fun next(): MusicRepeatMode = when (this) {
        OFF -> ALL
        ALL -> ONE
        ONE -> OFF
    }
}

sealed class SleepTimerOption {
    data class Minutes(val minutes: Int) : SleepTimerOption()
    data object EndOfSong : SleepTimerOption()
    data object EndOfQueue : SleepTimerOption()
}
