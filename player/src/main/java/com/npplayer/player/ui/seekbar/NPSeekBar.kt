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
    onScrubbing: (Boolean, Long) -> Unit = { _, _ -> },
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when (style) {
        SeekBarStylePreference.CLASSIC -> {
            ClassicSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                bufferedPositionMs = bufferedPositionMs,
                onScrubbing = onScrubbing,
                onSeek = onSeek,
                modifier = modifier
            )
        }
        SeekBarStylePreference.CHAPTERS -> {
            ChaptersSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onScrubbing = onScrubbing,
                onSeek = onSeek,
                modifier = modifier
            )
        }
        SeekBarStylePreference.WAVE -> {
            WaveSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
                onScrubbing = onScrubbing,
                onSeek = onSeek,
                modifier = modifier
            )
        }
        SeekBarStylePreference.GLOW -> {
            GlowSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onScrubbing = onScrubbing,
                onSeek = onSeek,
                modifier = modifier
            )
        }
        SeekBarStylePreference.NEON_MINIMAL -> {
            NeonMinimalSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onScrubbing = onScrubbing,
                onSeek = onSeek,
                modifier = modifier
            )
        }
        SeekBarStylePreference.GRADIENT_FLAME -> {
            GradientFlameSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onScrubbing = onScrubbing,
                onSeek = onSeek,
                modifier = modifier
            )
        }
        SeekBarStylePreference.RETRO_BLOCKS -> {
            RetroBlocksSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onScrubbing = onScrubbing,
                onSeek = onSeek,
                modifier = modifier
            )
        }
        SeekBarStylePreference.BUBBLE_POP -> {
            BubblePopSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onScrubbing = onScrubbing,
                onSeek = onSeek,
                modifier = modifier
            )
        }
        SeekBarStylePreference.AMOLED_PULSE -> {
            AmoledPulseSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
                onScrubbing = onScrubbing,
                onSeek = onSeek,
                modifier = modifier
            )
        }
    }
}
