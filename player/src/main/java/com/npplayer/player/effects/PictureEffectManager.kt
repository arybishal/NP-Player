package com.npplayer.player.effects

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.npplayer.core.data.preferences.PicturePreset
import com.npplayer.core.data.preferences.PictureSettings
import com.npplayer.core.data.preferences.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(UnstableApi::class)
class PictureEffectManager @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val settingsHolder = PictureSettingsHolder(PictureSettings.DEFAULT)
    private var glEffect: PictureGlEffect? = null
    private var exoPlayer: ExoPlayer? = null

    private val _isEffectsSupported = MutableStateFlow(true)
    val isEffectsSupported: StateFlow<Boolean> = _isEffectsSupported.asStateFlow()

    private val _currentSettings = MutableStateFlow(PictureSettings.DEFAULT)
    val currentSettings: StateFlow<PictureSettings> = _currentSettings.asStateFlow()

    private var currentVideoPath: String = ""

    fun attachPlayer(player: ExoPlayer) {
        this.exoPlayer = player
        ensureEffectAttached()
    }

    private fun ensureEffectAttached() {
        val player = exoPlayer ?: return
        if (glEffect == null) {
            glEffect = PictureGlEffect(settingsHolder)
        }
        try {
            player.setVideoEffects(listOf(glEffect!!))
            _isEffectsSupported.value = true
        } catch (e: Exception) {
            // Fallback: If hardware/platform effects pipeline is not supported on this device/surface,
            // disable effects pipeline without crashing playback.
            _isEffectsSupported.value = false
            try {
                player.setVideoEffects(emptyList())
            } catch (_: Exception) {}
        }
    }

    fun onVideoChanged(videoPath: String) {
        currentVideoPath = videoPath
        scope.launch {
            val global = settingsRepository.pictureSettingsFlow.first()
            val perVideo = if (global.rememberPerVideo && videoPath.isNotBlank()) {
                settingsRepository.getVideoPictureSettings(videoPath)
            } else null

            val initial = perVideo ?: global
            _currentSettings.value = initial
            settingsHolder.current = initial
        }
    }

    /**
     * Called on slider touch/drag: updates the atomic holder for immediate 60fps GPU frame update
     * without any pipeline recreation or rebuffering.
     */
    fun updateLive(settings: PictureSettings) {
        settingsHolder.current = settings
        _currentSettings.value = settings
    }

    /**
     * Called on slider release or preset selection: saves globally and/or per-video.
     */
    fun commit(settings: PictureSettings) {
        updateLive(settings)
        scope.launch {
            if (settings.rememberPerVideo && currentVideoPath.isNotBlank()) {
                settingsRepository.saveVideoPictureSettings(currentVideoPath, settings)
            }
            settingsRepository.updatePictureSettings(settings)
        }
    }

    fun selectPreset(preset: PicturePreset) {
        val current = _currentSettings.value
        val presetSettings = PictureSettings.fromPreset(preset).copy(
            rememberPerVideo = current.rememberPerVideo,
            includeFiltersInScreenshot = current.includeFiltersInScreenshot
        )
        commit(presetSettings)
    }

    fun reset() {
        val current = _currentSettings.value
        val defaultSettings = PictureSettings.DEFAULT.copy(
            rememberPerVideo = current.rememberPerVideo,
            includeFiltersInScreenshot = current.includeFiltersInScreenshot
        )
        commit(defaultSettings)
    }
}
