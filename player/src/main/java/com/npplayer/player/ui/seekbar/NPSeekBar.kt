package com.npplayer.player.ui.seekbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.npplayer.core.data.preferences.SeekBarStylePreference

@Composable
fun NPSeekBar(
    style: SeekBarStylePreference,
    currentPositionMs: Long,
    durationMs: Long,
    bufferedPositionMs: Long = 0L,
    isPlaying: Boolean = false,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when (style) {
        SeekBarStylePreference.CLASSIC -> {
            ClassicSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                bufferedPositionMs = bufferedPositionMs,
                onSeek = onSeek,
                modifier = modifier
            )
        }
        SeekBarStylePreference.CHAPTERS -> {
            ChaptersSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onSeek = onSeek,
                modifier = modifier
            )
        }
        SeekBarStylePreference.WAVE -> {
            WaveSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
                onSeek = onSeek,
                modifier = modifier
            )
        }
        SeekBarStylePreference.GLOW -> {
            GlowSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onSeek = onSeek,
                modifier = modifier
            )
        }
    }
}
