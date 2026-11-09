package com.npplayer.player.audio

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

enum class AudioPreset(val displayName: String, val bands: List<Int>, val bassBoost: Int) {
    FLAT("Flat", listOf(0, 0, 0, 0, 0), 0),
    BASS_BOOST("Bass Boost", listOf(6, 4, 1, 0, 0), 75),
    ROCK("Rock", listOf(5, 3, -1, 3, 5), 30),
    POP("Pop", listOf(1, 3, 4, 2, -1), 20),
    VOCAL("Vocal / Dialogue", listOf(-3, 0, 5, 4, 1), 0),
    MOVIE("Movie / Cinema", listOf(4, 2, 0, 3, 4), 45),
    ELECTRONIC("Electronic", listOf(6, 4, 1, 2, 5), 60),
    CUSTOM("Custom", listOf(0, 0, 0, 0, 0), 0)
}

data class AudioFxState(
    val isEnabled: Boolean = true,
    val selectedPreset: AudioPreset = AudioPreset.FLAT,
    val bandLevels: List<Int> = listOf(0, 0, 0, 0, 0), // in dB: -10 to +10 dB
    val bassBoostStrengthPercent: Int = 0, // 0 to 100%
    val audioDelayMs: Long = 0L, // -1000 to +1000 ms
    val isNightMode: Boolean = false // Dialogue enhancement
)

@Singleton
class AudioFxManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _state = MutableStateFlow(AudioFxState())
    val state: StateFlow<AudioFxState> = _state.asStateFlow()

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var currentAudioSessionId: Int = 0

    fun bindAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == currentAudioSessionId) return
        currentAudioSessionId = audioSessionId

        releaseEffects()

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            applyCurrentStateToEffects()
        } catch (_: Exception) {
            // Some hardware platforms/emulators lack AudioFx support; software fallback state remains active
        }
    }

    fun setEnabled(enabled: Boolean) {
        _state.update { it.copy(isEnabled = enabled) }
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
        } catch (_: Exception) {}
    }

    fun setPreset(preset: AudioPreset) {
        _state.update {
            it.copy(
                selectedPreset = preset,
                bandLevels = preset.bands,
                bassBoostStrengthPercent = preset.bassBoost
            )
        }
        applyCurrentStateToEffects()
    }

    fun setBandLevel(bandIndex: Int, levelDb: Int) {
        val clamped = levelDb.coerceIn(-10, 10)
        val currentBands = _state.value.bandLevels.toMutableList()
        if (bandIndex in currentBands.indices) {
            currentBands[bandIndex] = clamped
            _state.update {
                it.copy(
                    selectedPreset = AudioPreset.CUSTOM,
                    bandLevels = currentBands
                )
            }
            applyBandToEffect(bandIndex, clamped)
        }
    }

    fun setBassBoost(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _state.update {
            it.copy(
                selectedPreset = if (it.selectedPreset == AudioPreset.FLAT) AudioPreset.CUSTOM else it.selectedPreset,
                bassBoostStrengthPercent = clamped
            )
        }
        try {
            bassBoost?.setStrength((clamped * 10).toShort())
        } catch (_: Exception) {}
    }

    fun setAudioDelayMs(delayMs: Long) {
        _state.update { it.copy(audioDelayMs = delayMs.coerceIn(-1000L, 1000L)) }
    }

    fun toggleNightMode() {
        val next = !_state.value.isNightMode
        _state.update { it.copy(isNightMode = next) }
        if (next) {
            // Apply night mode: suppress loud booms (-4dB sub-bass), boost dialogue (+5dB midrange)
            setBandLevel(0, -4)
            setBandLevel(1, -2)
            setBandLevel(2, 4)
            setBandLevel(3, 4)
            setBandLevel(4, 0)
        } else {
            setPreset(AudioPreset.FLAT)
        }
    }

    fun releaseEffects() {
        try {
            equalizer?.release()
        } catch (_: Exception) {}
        equalizer = null

        try {
            bassBoost?.release()
        } catch (_: Exception) {}
        bassBoost = null
    }

    private fun applyCurrentStateToEffects() {
        val s = _state.value
        try {
            equalizer?.enabled = s.isEnabled
            bassBoost?.enabled = s.isEnabled
            if (s.isEnabled) {
                equalizer?.let { eq ->
                    val minMb = eq.bandLevelRange?.getOrNull(0) ?: -1000
                    val maxMb = eq.bandLevelRange?.getOrNull(1) ?: 1000
                    val numBands = eq.numberOfBands.toInt()

                    for (i in 0 until minOf(numBands, s.bandLevels.size)) {
                        val db = s.bandLevels[i]
                        val mb = (db * 100).toShort().coerceIn(minMb, maxMb)
                        eq.setBandLevel(i.toShort(), mb)
                    }
                }
                bassBoost?.setStrength((s.bassBoostStrengthPercent * 10).toShort())
            }
        } catch (_: Exception) {}
    }

    private fun applyBandToEffect(bandIndex: Int, levelDb: Int) {
        try {
            equalizer?.let { eq ->
                val minMb = eq.bandLevelRange?.getOrNull(0) ?: -1000
                val maxMb = eq.bandLevelRange?.getOrNull(1) ?: 1000
                if (bandIndex < eq.numberOfBands.toInt()) {
                    val mb = (levelDb * 100).toShort().coerceIn(minMb, maxMb)
                    eq.setBandLevel(bandIndex.toShort(), mb)
                }
            }
        } catch (_: Exception) {}
    }
}
