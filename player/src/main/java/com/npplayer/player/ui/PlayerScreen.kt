package com.npplayer.player.ui

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.preferences.PlayerAction
import com.npplayer.core.data.preferences.PlayerControlsSlots
import com.npplayer.core.data.preferences.SeekBarStylePreference
import com.npplayer.core.data.preferences.SettingsRepository
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPPlayerTheme
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.player.engine.Media3PlayerEngine
import com.npplayer.player.engine.PlayerPlaybackState
import com.npplayer.player.ui.gesture.BrightnessHUD
import com.npplayer.player.ui.gesture.DoubleTapSeekRipple
import com.npplayer.player.ui.gesture.SeekUndoSnackBar
import com.npplayer.player.ui.gesture.SpeedBurstBadge
import com.npplayer.player.ui.gesture.StartOverPrompt
import com.npplayer.player.ui.gesture.VolumeHUD
import com.npplayer.player.ui.seekbar.NPSeekBar
import com.npplayer.player.ui.slots.PlayerActionIconButton
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val playerEngine: Media3PlayerEngine,
    val settingsRepository: SettingsRepository,
    private val videoDao: VideoDao
) : ViewModel() {
    val playbackState = playerEngine.state
    val controlsSlots = settingsRepository.controlsSlotsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerControlsSlots.DEFAULT
    )
    val playerPreferences = settingsRepository.playerPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = com.npplayer.core.data.preferences.PlayerPreferences()
    )

    fun play(path: String, title: String) {
        viewModelScope.launch {
            val video = videoDao.getVideoByPath(path)
            val startPos = video?.lastPlayedPositionMs ?: 0L
            playerEngine.play(path, title, startPos)
        }
    }

    fun togglePlayPause() {
        if (playbackState.value.isPlaying) {
            playerEngine.pause()
        } else {
            playerEngine.resume()
        }
    }

    fun seekTo(positionMs: Long) {
        playerEngine.seekTo(positionMs)
    }

    fun seekRelative(deltaMs: Long) {
        val current = playbackState.value.currentPositionMs
        val duration = playbackState.value.durationMs
        val target = (current + deltaMs).coerceIn(0L, duration)
        playerEngine.seekTo(target)
    }

    fun setTemporarySpeed(speed: Float) {
        playerEngine.setPlaybackSpeed(speed)
    }

    fun saveProgress() {
        val current = playbackState.value.currentPositionMs
        val path = playbackState.value.videoPath
        val title = playbackState.value.videoTitle
        if (path.isNotBlank() && current > 0) {
            viewModelScope.launch {
                settingsRepository.saveLastPlayed(path, title, current.toInt())
                videoDao.updatePlaybackPosition(path, current, System.currentTimeMillis())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveProgress()
        playerEngine.pause()
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    videoPath: String,
    videoTitle: String,
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    NPPlayerTheme(forceDark = true) {
        val state by viewModel.playbackState.collectAsState()
        val slots by viewModel.controlsSlots.collectAsState()
        val playerPrefs by viewModel.playerPreferences.collectAsState()

        val context = LocalContext.current
        val activity = context as? Activity
        val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

        var areControlsVisible by remember { mutableStateOf(true) }

        // HUD states
        var showVolumeHUD by remember { mutableStateOf(false) }
        var currentVolumePercent by remember { mutableIntStateOf(100) }
        var showBrightnessHUD by remember { mutableStateOf(false) }
        var currentBrightnessPercent by remember { mutableIntStateOf(50) }
        var show2xSpeedBadge by remember { mutableStateOf(false) }

        // Double tap ripple
        var showSeekRipple by remember { mutableStateOf(false) }
        var isSeekForward by remember { mutableStateOf(true) }

        // Seek Undo
        var showSeekUndo by remember { mutableStateOf(false) }
        var undoTargetPosition by remember { mutableLongStateOf(0L) }
        var undoOriginalPosition by remember { mutableLongStateOf(0L) }

        // Start Over prompt
        var showStartOver by remember { mutableStateOf(false) }

        // Aspect ratio mode
        var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }

        // Start playback on enter
        LaunchedEffect(videoPath) {
            if (videoPath.isNotBlank() && (state.videoPath != videoPath || !state.isPlaying)) {
                viewModel.play(videoPath, videoTitle)
            }
        }

        // Check if start over banner should display
        LaunchedEffect(state.currentPositionMs) {
            if (state.currentPositionMs > 5000L && !showStartOver) {
                showStartOver = true
            }
        }

        // Auto-dismiss Start Over after 5s
        LaunchedEffect(showStartOver) {
            if (showStartOver) {
                delay(5000)
                showStartOver = false
            }
        }

        // Auto-hide controls
        LaunchedEffect(areControlsVisible, state.isPlaying) {
            if (areControlsVisible && state.isPlaying) {
                val timeout = playerPrefs.controlsTimeoutSeconds * 1000L
                delay(timeout)
                areControlsVisible = false
            }
        }

        BackHandler {
            viewModel.saveProgress()
            onNavigateBack()
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = NPBlack
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                areControlsVisible = !areControlsVisible
                            },
                            onDoubleTap = { offset ->
                                val isRight = offset.x > size.width / 2f
                                val delta = if (isRight) {
                                    playerPrefs.doubleTapSeekSeconds * 1000L
                                } else {
                                    -playerPrefs.doubleTapSeekSeconds * 1000L
                                }
                                isSeekForward = isRight
                                showSeekRipple = true
                                viewModel.seekRelative(delta)
                            },
                            onLongPress = {
                                show2xSpeedBadge = true
                                viewModel.setTemporarySpeed(2.0f)
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (offset.x < size.width * 0.45f) {
                                    showBrightnessHUD = true
                                } else if (offset.x > size.width * 0.55f) {
                                    showVolumeHUD = true
                                }
                            },
                            onDragEnd = {
                                showVolumeHUD = false
                                showBrightnessHUD = false
                                if (show2xSpeedBadge) {
                                    show2xSpeedBadge = false
                                    viewModel.setTemporarySpeed(1.0f)
                                }
                            },
                            onDragCancel = {
                                showVolumeHUD = false
                                showBrightnessHUD = false
                                if (show2xSpeedBadge) {
                                    show2xSpeedBadge = false
                                    viewModel.setTemporarySpeed(1.0f)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val isLeft = change.position.x < size.width * 0.45f
                                val isRight = change.position.x > size.width * 0.55f

                                if (isLeft) {
                                    // Brightness swipe
                                    val delta = (-dragAmount.y / 4f).toInt()
                                    currentBrightnessPercent = (currentBrightnessPercent + delta).coerceIn(0, 100)
                                    activity?.let { act ->
                                        val lp = act.window.attributes
                                        lp.screenBrightness = (currentBrightnessPercent / 100f).coerceIn(0.01f, 1f)
                                        act.window.attributes = lp
                                    }
                                } else if (isRight) {
                                    // Volume swipe with boost up to 200%
                                    val delta = (-dragAmount.y / 3f).toInt()
                                    currentVolumePercent = (currentVolumePercent + delta).coerceIn(0, 200)
                                    val maxStreamVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                    if (currentVolumePercent <= 100) {
                                        val streamVol = (currentVolumePercent * maxStreamVol / 100).coerceIn(0, maxStreamVol)
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, streamVol, 0)
                                        viewModel.playerEngine.setVolume(1.0f)
                                    } else {
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxStreamVol, 0)
                                        // Boost volume through player engine multiplier
                                        val boostFactor = (currentVolumePercent / 100f).coerceIn(1.0f, 2.0f)
                                        viewModel.playerEngine.setVolume(boostFactor)
                                    }
                                }
                            }
                        )
                    }
            ) {
                // Video Player View
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = viewModel.playerEngine.exoPlayer
                            useController = false
                            this.resizeMode = resizeMode
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { playerView ->
                        playerView.resizeMode = resizeMode
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Loading Spinner
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = NPYellow,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // HUD Overlays
                VolumeHUD(
                    volumePercent = currentVolumePercent,
                    visible = showVolumeHUD,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )

                BrightnessHUD(
                    brightnessPercent = currentBrightnessPercent,
                    visible = showBrightnessHUD,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                SpeedBurstBadge(
                    visible = show2xSpeedBadge,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)
                )

                DoubleTapSeekRipple(
                    isForward = isSeekForward,
                    visible = showSeekRipple,
                    modifier = Modifier.align(if (isSeekForward) Alignment.CenterEnd else Alignment.CenterStart).padding(48.dp)
                )

                // Auto-dismiss seek ripple after 600ms
                LaunchedEffect(showSeekRipple) {
                    if (showSeekRipple) {
                        delay(600)
                        showSeekRipple = false
                    }
                }

                // Start Over prompt
                StartOverPrompt(
                    visible = showStartOver && state.currentPositionMs > 5000L,
                    resumedPositionFormatted = formatDuration(state.currentPositionMs),
                    onStartOver = {
                        viewModel.seekTo(0L)
                        showStartOver = false
                    },
                    modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 90.dp)
                )

                // Seek Undo snack-bar
                SeekUndoSnackBar(
                    visible = showSeekUndo,
                    targetPositionFormatted = formatDuration(undoTargetPosition),
                    onUndo = {
                        viewModel.seekTo(undoOriginalPosition)
                        showSeekUndo = false
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp)
                )

                // Controls Overlay
                AnimatedVisibility(
                    visible = areControlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    PlayerControlsOverlay(
                        state = state,
                        slots = slots,
                        seekBarStyle = playerPrefs.seekBarStyle,
                        onBack = {
                            viewModel.saveProgress()
                            onNavigateBack()
                        },
                        onTogglePlay = { viewModel.togglePlayPause() },
                        onSeek = { targetMs ->
                            val current = state.currentPositionMs
                            if (abs(targetMs - current) > 30000L && playerPrefs.seekUndoEnabled) {
                                undoOriginalPosition = current
                                undoTargetPosition = targetMs
                                showSeekUndo = true
                            }
                            viewModel.seekTo(targetMs)
                        },
                        onRewind = { viewModel.seekRelative(-10000L) },
                        onForward = { viewModel.seekRelative(10000L) },
                        onActionClick = { action ->
                            when (action) {
                                PlayerAction.ASPECT_RATIO -> {
                                    resizeMode = when (resizeMode) {
                                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    }
                                }
                                PlayerAction.SPEED -> {
                                    val nextSpeed = when (state.playbackSpeed) {
                                        1.0f -> 1.25f
                                        1.25f -> 1.5f
                                        1.5f -> 2.0f
                                        else -> 1.0f
                                    }
                                    viewModel.setTemporarySpeed(nextSpeed)
                                }
                                else -> { /* Slot action handled */ }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerControlsOverlay(
    state: PlayerPlaybackState,
    slots: PlayerControlsSlots,
    seekBarStyle: SeekBarStylePreference,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onActionClick: (PlayerAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NPBlack.copy(alpha = 0.35f))
    ) {
        // Top Bar Scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(NPBlack.copy(alpha = 0.85f), Color.Transparent)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = NPWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = state.videoTitle.ifEmpty { "Video Playback" },
                    color = NPWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Beside Title Slots
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    slots.besideTitle.forEach { action ->
                        PlayerActionIconButton(
                            action = action,
                            onClick = { onActionClick(action) },
                            iconSize = 22.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Top Right Slots
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    slots.topRight.forEach { action ->
                        PlayerActionIconButton(
                            action = action,
                            onClick = { onActionClick(action) },
                            iconSize = 22.dp
                        )
                    }
                }
            }
        }

        // Center Controls (Prev, Play/Pause, Next)
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            IconButton(
                onClick = onRewind,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.FastRewind,
                    contentDescription = "Rewind 10s",
                    tint = NPWhite,
                    modifier = Modifier.size(36.dp)
                )
            }

            Surface(
                onClick = onTogglePlay,
                shape = CircleShape,
                color = NPYellow,
                contentColor = NPBlack,
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            IconButton(
                onClick = onForward,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.FastForward,
                    contentDescription = "Forward 10s",
                    tint = NPWhite,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Bottom Bar Scrim + Bottom Action Slots + Selected Seek Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, NPBlack.copy(alpha = 0.90f))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Bottom Slots Row (Bottom Left + Bottom Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        slots.bottomLeft.forEach { action ->
                            PlayerActionIconButton(
                                action = action,
                                onClick = { onActionClick(action) },
                                iconSize = 22.dp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        slots.bottomRight.forEach { action ->
                            PlayerActionIconButton(
                                action = action,
                                onClick = { onActionClick(action) },
                                iconSize = 22.dp
                            )
                        }
                    }
                }

                // Time labels
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(state.currentPositionMs),
                        color = NPWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatDuration(state.durationMs),
                        color = NPWhite.copy(alpha = 0.70f),
                        fontSize = 12.sp
                    )
                }

                // Dynamic Seek Bar (Classic, Chapters, Wave, Glow)
                NPSeekBar(
                    style = seekBarStyle,
                    currentPositionMs = state.currentPositionMs,
                    durationMs = state.durationMs,
                    bufferedPositionMs = state.bufferedPositionMs,
                    isPlaying = state.isPlaying,
                    onSeek = onSeek,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, remainingMinutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", remainingMinutes, seconds)
    }
}
