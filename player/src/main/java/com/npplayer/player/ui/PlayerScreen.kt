package com.npplayer.player.ui

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.util.Rational
import android.util.TypedValue
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.preferences.PlayerAction
import com.npplayer.core.data.preferences.PlayerControlsSlots
import com.npplayer.core.data.preferences.ResumePromptPreference
import com.npplayer.core.data.preferences.SeekBarStylePreference
import com.npplayer.core.data.preferences.SettingsRepository
import com.npplayer.core.data.preferences.SubtitlePreferences
import com.npplayer.core.data.subtitles.OnlineSubtitleManager
import com.npplayer.core.data.subtitles.SubtitleFilenameCleaner
import com.npplayer.core.data.subtitles.SubtitleItem
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPPlayerTheme
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.core.ui.util.FormatUtils
import com.npplayer.player.engine.Media3PlayerEngine
import com.npplayer.player.engine.PlayerPlaybackState
import com.npplayer.player.ui.gesture.BrightnessHUD
import com.npplayer.player.ui.gesture.DoubleTapSeekRipple
import com.npplayer.player.ui.gesture.ResumeQuestionCard
import com.npplayer.player.ui.gesture.SeekUndoSnackBar
import com.npplayer.player.ui.gesture.SpeedBurstBadge
import com.npplayer.player.ui.gesture.VolumeHUD
import com.npplayer.player.ui.seekbar.NPSeekBar
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import com.npplayer.player.ui.sheet.AudioTrackBottomSheet
import com.npplayer.player.ui.sheet.EqualizerBottomSheet
import com.npplayer.player.ui.sheet.OnlineSubtitlesBottomSheet
import com.npplayer.player.ui.sheet.OnlineSubtitlesUiState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.media3.ui.SubtitleView
import com.npplayer.core.data.preferences.AspectRatioMode
import com.npplayer.core.data.preferences.AspectRatioPreferences
import com.npplayer.core.data.preferences.AspectRatioPreset
import com.npplayer.player.scale.VideoScaleController
import com.npplayer.player.scale.VideoScaleInput
import com.npplayer.player.ui.gesture.AspectRatioBadge
import com.npplayer.player.ui.gesture.playerVideoGestures
import com.npplayer.player.ui.sheet.AspectRatioBottomSheet
import androidx.compose.material.icons.outlined.RestartAlt
import com.npplayer.player.ui.sheet.PictureQualityBottomSheet
import com.npplayer.player.ui.sheet.PlayerMoreBottomSheet
import com.npplayer.player.ui.sheet.SleepTimerBottomSheet
import com.npplayer.player.ui.sheet.SubtitleTrackBottomSheet
import com.npplayer.player.ui.sheet.VideoInfoBottomSheet
import com.npplayer.player.util.PlayerScreenshotHelper
import com.npplayer.player.ui.slots.PlayerActionIconButton
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

data class ResumePromptState(
    val show: Boolean = false,
    val savedPositionMs: Long = 0L,
    val formattedTime: String = "",
    val durationMs: Long = 0L,
    val videoPath: String = ""
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val playerEngine: Media3PlayerEngine,
    val settingsRepository: SettingsRepository,
    val onlineSubtitleManager: OnlineSubtitleManager,
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
    val subtitlePreferences = settingsRepository.subtitlePreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubtitlePreferences()
    )
    val aspectRatioPreferences = settingsRepository.aspectRatioPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AspectRatioPreferences()
    )

    fun updateAspectRatio(preferences: AspectRatioPreferences) {
        viewModelScope.launch {
            val currentPath = playbackState.value.videoPath
            if (preferences.rememberPerVideo && currentPath.isNotBlank()) {
                settingsRepository.saveVideoAspectRatioPreferences(currentPath, preferences)
            }
            settingsRepository.updateAspectRatioPreferences(preferences)
        }
    }

    private val _onlineSubtitlesState = MutableStateFlow<OnlineSubtitlesUiState>(OnlineSubtitlesUiState.Idle)
    val onlineSubtitlesState = _onlineSubtitlesState.asStateFlow()

    private val _resumePromptState = MutableStateFlow(ResumePromptState())
    val resumePromptState: StateFlow<ResumePromptState> = _resumePromptState.asStateFlow()

    private val decisionMadeSessionPaths = mutableSetOf<String>()

    var isVaultSession: Boolean = false

    fun searchOnlineSubtitles(queryText: String = "", languages: List<String>? = null) {
        val path = playbackState.value.videoPath
        val title = playbackState.value.videoTitle
        if (path.isBlank()) return

        viewModelScope.launch {
            _onlineSubtitlesState.value = OnlineSubtitlesUiState.Loading
            val preferredLangs = languages ?: subtitlePreferences.value.preferredLanguages.toList().ifEmpty { listOf("en") }
            val result = onlineSubtitleManager.searchSubtitles(
                videoPath = path,
                videoTitle = title,
                manualQuery = queryText,
                preferredLanguages = preferredLangs
            )
            result.fold(
                onSuccess = { items ->
                    _onlineSubtitlesState.value = if (items.isEmpty()) {
                        OnlineSubtitlesUiState.Empty
                    } else {
                        OnlineSubtitlesUiState.Success(items)
                    }
                },
                onFailure = { error ->
                    _onlineSubtitlesState.value = OnlineSubtitlesUiState.Error(
                        error.localizedMessage ?: "Failed to fetch online subtitles"
                    )
                }
            )
        }
    }

    fun downloadAndApplySubtitle(item: SubtitleItem, onComplete: (Boolean, String) -> Unit) {
        val path = playbackState.value.videoPath
        if (path.isBlank()) return

        viewModelScope.launch {
            val result = onlineSubtitleManager.downloadAndApplySubtitle(item, path)
            result.fold(
                onSuccess = { file ->
                    val uri = android.net.Uri.fromFile(file)
                    val label = "${item.releaseName} (${item.languageCode.uppercase()})"
                    playerEngine.addExternalSubtitle(uri, label)
                    onComplete(true, "Loaded: ${file.name}")
                },
                onFailure = { error ->
                    onComplete(false, error.localizedMessage ?: "Download failed")
                }
            )
        }
    }

    fun play(path: String, title: String, isVault: Boolean = false) {
        isVaultSession = isVault
        // If already prompting for this path, do nothing
        if (_resumePromptState.value.show && _resumePromptState.value.videoPath == path) {
            return
        }
        // If already playing this path and session decision was made, do nothing
        if (playbackState.value.videoPath == path && decisionMadeSessionPaths.contains(path)) {
            return
        }

        viewModelScope.launch {
            val savedAr = settingsRepository.getVideoAspectRatioPreferences(path)
            if (savedAr != null) {
                settingsRepository.updateAspectRatioPreferences(savedAr)
            }

            if (isVault) {
                decisionMadeSessionPaths.add(path)
                playerEngine.play(path, title, 0L)
                return@launch
            }

            if (decisionMadeSessionPaths.contains(path)) {
                if (playbackState.value.videoPath != path) {
                    val saved = videoDao.getVideoByPath(path)?.lastPlayedPositionMs ?: 0L
                    playerEngine.play(path, title, saved)
                }
                return@launch
            }

            val videoEntity = videoDao.getVideoByPath(path)
            val savedPos = videoEntity?.lastPlayedPositionMs ?: 0L
            val duration = videoEntity?.durationMs ?: 0L

            val isEligible = savedPos > 5000L && (duration <= 0L || savedPos < (duration * 0.95f).toLong())
            if (!isEligible) {
                decisionMadeSessionPaths.add(path)
                playerEngine.play(path, title, 0L)
                return@launch
            }

            val pref = settingsRepository.resumePromptPreferenceFlow.first()
            when (pref) {
                ResumePromptPreference.ALWAYS_RESUME -> {
                    decisionMadeSessionPaths.add(path)
                    playerEngine.play(path, title, savedPos)
                }
                ResumePromptPreference.ALWAYS_START_OVER -> {
                    decisionMadeSessionPaths.add(path)
                    videoDao.updatePlaybackPosition(path, 0L, System.currentTimeMillis())
                    settingsRepository.clearLastPlayedIfMatches(path)
                    playerEngine.play(path, title, 0L)
                }
                ResumePromptPreference.ASK -> {
                    playerEngine.preparePaused(path, title, 0L)
                    _resumePromptState.value = ResumePromptState(
                        show = true,
                        savedPositionMs = savedPos,
                        formattedTime = FormatUtils.formatDuration(savedPos),
                        durationMs = duration,
                        videoPath = path
                    )
                }
            }
        }
    }

    fun onResumeChoice(rememberChoice: Boolean) {
        val currentPrompt = _resumePromptState.value
        val path = currentPrompt.videoPath
        val pos = currentPrompt.savedPositionMs
        decisionMadeSessionPaths.add(path)
        _resumePromptState.value = ResumePromptState(show = false)

        viewModelScope.launch {
            if (rememberChoice) {
                settingsRepository.setResumePromptPreference(ResumePromptPreference.ALWAYS_RESUME)
            }
            playerEngine.seekTo(pos)
            playerEngine.resume()
        }
    }

    fun onStartOverChoice(rememberChoice: Boolean) {
        val currentPrompt = _resumePromptState.value
        val path = currentPrompt.videoPath
        decisionMadeSessionPaths.add(path)
        _resumePromptState.value = ResumePromptState(show = false)

        viewModelScope.launch {
            videoDao.updatePlaybackPosition(path, 0L, System.currentTimeMillis())
            settingsRepository.clearLastPlayedIfMatches(path)
            if (rememberChoice) {
                settingsRepository.setResumePromptPreference(ResumePromptPreference.ALWAYS_START_OVER)
            }
            playerEngine.seekTo(0L)
            playerEngine.resume()
        }
    }

    fun togglePlayPause() {
        if (playbackState.value.isPlaying) {
            playerEngine.pause()
        } else {
            playerEngine.resume()
        }
    }

    fun seekTo(positionMs: Long, reason: String = "USER_SEEK") {
        playerEngine.seekTo(positionMs, reason)
    }

    fun seekRelative(deltaMs: Long, reason: String = "SEEK_RELATIVE") {
        val current = playbackState.value.currentPositionMs
        val duration = playbackState.value.durationMs
        val target = if (duration > 0L) (current + deltaMs).coerceIn(0L, duration) else (current + deltaMs).coerceAtLeast(0L)
        playerEngine.seekTo(target, reason)
    }

    fun setTemporarySpeed(speed: Float) {
        playerEngine.setPlaybackSpeed(speed)
    }

    fun saveProgress() {
        if (isVaultSession || _resumePromptState.value.show) return
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
    isVault: Boolean = false,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    NPPlayerTheme(forceDark = true) {
        val state by viewModel.playbackState.collectAsState()
        val slots by viewModel.controlsSlots.collectAsState()
        val playerPrefs by viewModel.playerPreferences.collectAsState()

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val activity = context as? Activity
        val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
        val subPrefs by viewModel.subtitlePreferences.collectAsState()
        val resumePromptState by viewModel.resumePromptState.collectAsState()
        val hasMultipleAudioTracks by viewModel.playerEngine.hasMultipleAudioTracks.collectAsState()

        var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }

        // Fullscreen Immersive Mode: Completely hide status bar / notification panel & navigation bar
        DisposableEffect(activity) {
            val window = activity?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            onDispose {
                val window = activity?.window
                if (window != null) {
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

        // Enable FLAG_SECURE if playing vault video to prevent screenshots and screen recordings
        DisposableEffect(isVault) {
            if (isVault) {
                activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
            onDispose {
                if (isVault) {
                    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }

        var areControlsVisible by remember { mutableStateOf(true) }

        // Modal Sheets
        var showSubtitleSheet by remember { mutableStateOf(false) }
        var showOnlineSubtitlesSheet by remember { mutableStateOf(false) }
        var showAudioSheet by remember { mutableStateOf(false) }
        var showEqualizerSheet by remember { mutableStateOf(false) }
        var showPictureSheet by remember { mutableStateOf(false) }
        var showMoreSheet by remember { mutableStateOf(false) }

        val subtitlePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                viewModel.playerEngine.addExternalSubtitle(uri, "External Subtitle")
            }
        }

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

        // Aspect ratio & Zoom states
        val aspectRatioPrefs by viewModel.aspectRatioPreferences.collectAsState()
        var userZoom by remember { mutableFloatStateOf(1.0f) }
        var userPan by remember { mutableStateOf(Offset.Zero) }
        var showAspectRatioBadge by remember { mutableStateOf(false) }
        var aspectRatioBadgeText by remember { mutableStateOf("") }
        var showAspectRatioSheet by remember { mutableStateOf(false) }

        // Auto-dismiss aspect ratio badge after 1.5 seconds
        LaunchedEffect(showAspectRatioBadge, aspectRatioBadgeText) {
            if (showAspectRatioBadge) {
                delay(1500L)
                showAspectRatioBadge = false
            }
        }

        val cycleAspectRatio = {
            val currentMode = aspectRatioPrefs.mode
            val nextMode = when (currentMode) {
                AspectRatioMode.FIT -> AspectRatioMode.STRETCH
                AspectRatioMode.STRETCH -> AspectRatioMode.CROP
                AspectRatioMode.CROP -> AspectRatioMode.ORIGINAL_100
                AspectRatioMode.ORIGINAL_100 -> AspectRatioMode.CUSTOM
                AspectRatioMode.CUSTOM -> AspectRatioMode.FIT
            }
            userZoom = 1.0f
            userPan = Offset.Zero
            val newPrefs = aspectRatioPrefs.copy(mode = nextMode)
            viewModel.updateAspectRatio(newPrefs)
            aspectRatioBadgeText = when (nextMode) {
                AspectRatioMode.FIT -> "Fit to screen"
                AspectRatioMode.STRETCH -> "Stretch"
                AspectRatioMode.CROP -> "Crop"
                AspectRatioMode.ORIGINAL_100 -> "100%"
                AspectRatioMode.CUSTOM -> "Custom ${aspectRatioPrefs.customPreset}"
            }
            showAspectRatioBadge = true
        }

        // Screen lock & Night dim
        var isScreenLocked by remember { mutableStateOf(false) }
        var isNightDimActive by remember { mutableStateOf(false) }

        // Info & Sleep timer sheets
        var showInfoSheet by remember { mutableStateOf(false) }
        var showSleepTimerSheet by remember { mutableStateOf(false) }
        var sleepTimerMinutes by remember { mutableStateOf<Int?>(null) }

        // Sleep timer effect
        LaunchedEffect(sleepTimerMinutes) {
            val mins = sleepTimerMinutes
            if (mins != null && mins > 0) {
                delay(mins * 60 * 1000L)
                viewModel.playerEngine.pause()
                sleepTimerMinutes = null
            }
        }

        // Start playback on enter (only once per video path to avoid restarting at 0 on pause/recomposition)
        LaunchedEffect(videoPath) {
            if (videoPath.isNotBlank() && state.videoPath != videoPath) {
                viewModel.play(videoPath, videoTitle, isVault)
            }
        }

        // Keep controls hidden while resume question is asking
        LaunchedEffect(resumePromptState.show) {
            if (resumePromptState.show) {
                areControlsVisible = false
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
            if (isScreenLocked) {
                isScreenLocked = false
            } else {
                if (!resumePromptState.show) {
                    viewModel.saveProgress()
                }
                onNavigateBack()
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = NPBlack
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                val viewWidth = constraints.maxWidth.toFloat()
                val viewHeight = constraints.maxHeight.toFloat()

                val scaleResult = remember(
                    state.videoWidth,
                    state.videoHeight,
                    state.pixelWidthHeightRatio,
                    state.unappliedRotationDegrees,
                    viewWidth,
                    viewHeight,
                    aspectRatioPrefs.mode,
                    aspectRatioPrefs.customRatioValue,
                    userZoom,
                    userPan
                ) {
                    VideoScaleController.calculate(
                        VideoScaleInput(
                            videoWidth = state.videoWidth,
                            videoHeight = state.videoHeight,
                            pixelWidthHeightRatio = state.pixelWidthHeightRatio,
                            rotationDegrees = state.unappliedRotationDegrees,
                            viewWidth = viewWidth,
                            viewHeight = viewHeight,
                            mode = aspectRatioPrefs.mode,
                            customRatio = aspectRatioPrefs.customRatioValue,
                            userZoom = userZoom,
                            userPan = userPan
                        )
                    )
                }

                // Video Player Container (scaled & translated for 100%, Custom & Zoom)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scaleResult.scaleX
                            scaleY = scaleResult.scaleY
                            translationX = scaleResult.translationX
                            translationY = scaleResult.translationY
                        }
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = viewModel.playerEngine.exoPlayer
                                useController = false
                                this.resizeMode = scaleResult.playerViewResizeMode
                                subtitleView?.visibility = android.view.View.GONE
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                playerViewRef = this
                            }
                        },
                        update = { playerView ->
                            playerView.resizeMode = scaleResult.playerViewResizeMode
                            playerView.subtitleView?.visibility = android.view.View.GONE
                            playerViewRef = playerView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Independent Subtitle View (always stays inside safe screen bounds at 1.0x scale)
                AndroidView(
                    factory = { ctx ->
                        SubtitleView(ctx).apply {
                            setStyle(
                                CaptionStyleCompat(
                                    android.graphics.Color.YELLOW,
                                    android.graphics.Color.TRANSPARENT,
                                    android.graphics.Color.TRANSPARENT,
                                    CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                                    android.graphics.Color.BLACK,
                                    null
                                )
                            )
                            setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                            setApplyEmbeddedStyles(false)
                            setApplyEmbeddedFontSizes(false)
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            viewModel.playerEngine.exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
                                override fun onCues(cueGroup: androidx.media3.common.text.CueGroup) {
                                    setCues(cueGroup.cues)
                                }
                            })
                        }
                    },
                    update = { _ -> },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                )

                // Background gesture layer (pinch zoom, pan, two-finger reset, double-tap seek, swipes)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .playerVideoGestures(
                            enabled = !isScreenLocked && !resumePromptState.show,
                            onTap = {
                                areControlsVisible = !areControlsVisible
                            },
                            onDoubleTap = { offset ->
                                val isRight = offset.x > viewWidth / 2f
                                val delta = if (isRight) {
                                    playerPrefs.doubleTapSeekSeconds * 1000L
                                } else {
                                    -playerPrefs.doubleTapSeekSeconds * 1000L
                                }
                                isSeekForward = isRight
                                showSeekRipple = true
                                viewModel.seekRelative(delta, if (isRight) "DOUBLE_TAP_FORWARD" else "DOUBLE_TAP_REWIND")
                            },
                            onLongPress = {
                                show2xSpeedBadge = true
                                viewModel.setTemporarySpeed(2.0f)
                            },
                            onLongPressRelease = {
                                show2xSpeedBadge = false
                                viewModel.setTemporarySpeed(1.0f)
                            },
                            onTwoFingerDoubleTap = {
                                userZoom = 1.0f
                                userPan = Offset.Zero
                                aspectRatioBadgeText = "Zoom reset"
                                showAspectRatioBadge = true
                            },
                            onZoomAndPan = { zoomDelta, panDelta ->
                                userZoom = (userZoom * zoomDelta).coerceIn(1.0f, 4.0f)
                                userPan += panDelta
                            },
                            onBrightnessDrag = { deltaPercent ->
                                currentBrightnessPercent = (currentBrightnessPercent + deltaPercent.toInt()).coerceIn(0, 100)
                                activity?.let { act ->
                                    val lp = act.window.attributes
                                    lp.screenBrightness = (currentBrightnessPercent / 100f).coerceIn(0.01f, 1f)
                                    act.window.attributes = lp
                                }
                            },
                            onVolumeDrag = { deltaPercent ->
                                currentVolumePercent = (currentVolumePercent + deltaPercent.toInt()).coerceIn(0, 200)
                                val maxStreamVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                if (currentVolumePercent <= 100) {
                                    val streamVol = (currentVolumePercent * maxStreamVol / 100).coerceIn(0, maxStreamVol)
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, streamVol, 0)
                                    viewModel.playerEngine.setVolume(1.0f)
                                } else {
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxStreamVol, 0)
                                    val boostFactor = (currentVolumePercent / 100f).coerceIn(1.0f, 2.0f)
                                    viewModel.playerEngine.setVolume(boostFactor)
                                }
                            },
                            onDragStart = { isBr ->
                                if (isBr) showBrightnessHUD = true else showVolumeHUD = true
                            },
                            onDragEnd = {
                                showBrightnessHUD = false
                                showVolumeHUD = false
                            }
                        )
                )

                // Aspect Ratio 1.5s Overlay Badge (Top Center)
                AspectRatioBadge(
                    text = aspectRatioBadgeText,
                    visible = showAspectRatioBadge,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 70.dp)
                )

                // Quick Reset Zoom chip (Top Right, when zoomed in and controls visible)
                if (userZoom > 1.01f && areControlsVisible) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 70.dp, end = 20.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            .background(Color(0xD9000000))
                            .border(1.dp, NPYellow, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            .clickable {
                                userZoom = 1.0f
                                userPan = Offset.Zero
                                aspectRatioBadgeText = "Zoom reset"
                                showAspectRatioBadge = true
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.RestartAlt,
                                contentDescription = null,
                                tint = NPYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reset zoom (${String.format(java.util.Locale.US, "%.1f", userZoom)}x)",
                                color = NPWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

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

                // Resume Question Card
                ResumeQuestionCard(
                    visible = resumePromptState.show,
                    formattedTime = resumePromptState.formattedTime,
                    onResume = { remember -> viewModel.onResumeChoice(remember) },
                    onStartOver = { remember -> viewModel.onStartOverChoice(remember) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (areControlsVisible) 110.dp else 40.dp)
                )

                // Seek Undo snack-bar
                SeekUndoSnackBar(
                    visible = showSeekUndo,
                    targetPositionFormatted = FormatUtils.formatDuration(undoTargetPosition),
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
                        hasMultipleAudioTracks = hasMultipleAudioTracks,
                        onBack = {
                            if (!resumePromptState.show) {
                                viewModel.saveProgress()
                            }
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
                            viewModel.seekTo(targetMs, "SEEK_BAR")
                        },
                        onRewind = { viewModel.seekRelative(-10000L, "BUTTON_REWIND") },
                        onForward = { viewModel.seekRelative(10000L, "BUTTON_FORWARD") },
                        onActionClick = { action ->
                            when (action) {
                                PlayerAction.LOCK -> {
                                    isScreenLocked = true
                                    areControlsVisible = false
                                }
                                PlayerAction.ASPECT_RATIO -> {
                                    cycleAspectRatio()
                                }
                                PlayerAction.SPEED -> {
                                    val nextSpeed = when (state.playbackSpeed) {
                                        0.5f -> 0.75f
                                        0.75f -> 1.0f
                                        1.0f -> 1.25f
                                        1.25f -> 1.5f
                                        1.5f -> 2.0f
                                        else -> 1.0f
                                    }
                                    viewModel.setTemporarySpeed(nextSpeed)
                                }
                                PlayerAction.SUBTITLES -> {
                                    showSubtitleSheet = true
                                }
                                PlayerAction.AUDIO_TRACK -> {
                                    showAudioSheet = true
                                }
                                PlayerAction.AUDIO_FX -> {
                                    showEqualizerSheet = true
                                }
                                PlayerAction.INFO -> {
                                    showInfoSheet = true
                                }
                                PlayerAction.SLEEP_TIMER -> {
                                    showSleepTimerSheet = true
                                }
                                PlayerAction.NIGHT_DIM -> {
                                    isNightDimActive = !isNightDimActive
                                }
                                PlayerAction.MUTE -> {
                                    if (state.volume > 0f) {
                                        viewModel.playerEngine.setVolume(0.0f)
                                    } else {
                                        viewModel.playerEngine.setVolume(1.0f)
                                    }
                                }
                                PlayerAction.PIP -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        try {
                                            val params = PictureInPictureParams.Builder()
                                                .setAspectRatio(Rational(16, 9))
                                                .build()
                                            activity?.enterPictureInPictureMode(params)
                                        } catch (_: Exception) {}
                                    }
                                }
                                PlayerAction.ORIENTATION -> {
                                    val current = activity?.requestedOrientation
                                    if (current == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    } else {
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                    }
                                }
                                PlayerAction.PICTURE_QUALITY, PlayerAction.QUALITY_HD -> {
                                    showPictureSheet = true
                                }
                                PlayerAction.MORE -> {
                                    showMoreSheet = true
                                }
                                PlayerAction.SCREENSHOT -> {
                                    val picSettings = viewModel.playerEngine.pictureEffectManager.currentSettings.value
                                    scope.launch {
                                        PlayerScreenshotHelper.captureScreenshot(
                                            context = context,
                                            playerView = playerViewRef,
                                            videoPath = state.videoPath,
                                            videoTitle = state.videoTitle,
                                            positionMs = state.currentPositionMs,
                                            includeFilters = picSettings.includeFiltersInScreenshot,
                                            onComplete = {}
                                        )
                                    }
                                }
                                else -> { /* Slot action handled */ }
                            }
                        },
                        onBackgroundTap = { areControlsVisible = false }
                    )
                }

                // Night Dim AMOLED scrim
                if (isNightDimActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NPBlack.copy(alpha = 0.50f))
                    )
                }

                // Lock Screen Overlay with tactile unlock button
                if (isScreenLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { /* consume tap */ })
                            }
                    ) {
                        Surface(
                            onClick = {
                                isScreenLocked = false
                                areControlsVisible = true
                            },
                            shape = CircleShape,
                            color = NPYellow,
                            contentColor = NPBlack,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 24.dp, top = 28.dp)
                                .size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Unlock Controls",
                                    tint = NPBlack,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showSubtitleSheet) {
            SubtitleTrackBottomSheet(
                tracks = viewModel.playerEngine.getSubtitleTracks(),
                onSelectTrack = { track -> viewModel.playerEngine.selectSubtitleTrack(track) },
                onLoadExternalSubtitle = { subtitlePickerLauncher.launch(arrayOf("*/*")) },
                onOpenOnlineSubtitles = {
                    showOnlineSubtitlesSheet = true
                    viewModel.searchOnlineSubtitles()
                },
                onDismiss = { showSubtitleSheet = false }
            )
        }

        if (showOnlineSubtitlesSheet) {
            val onlineUiState by viewModel.onlineSubtitlesState.collectAsState()
            val cleanInfo = remember(state.videoPath, state.videoTitle) {
                SubtitleFilenameCleaner.clean(if (state.videoTitle.isNotBlank()) state.videoTitle else state.videoPath)
            }
            OnlineSubtitlesBottomSheet(
                uiState = onlineUiState,
                initialQuery = cleanInfo.suggestedQuery,
                preferredLanguages = subPrefs.preferredLanguages,
                onSearch = { query, langs -> viewModel.searchOnlineSubtitles(query, langs) },
                onDownloadSubtitle = { item ->
                    viewModel.downloadAndApplySubtitle(item) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) {
                            showOnlineSubtitlesSheet = false
                        }
                    }
                },
                onDismiss = { showOnlineSubtitlesSheet = false }
            )
        }

        if (showAudioSheet) {
            AudioTrackBottomSheet(
                tracks = viewModel.playerEngine.getAudioTracks(),
                onSelectTrack = { track -> viewModel.playerEngine.selectAudioTrack(track) },
                onOpenEqualizer = { showEqualizerSheet = true },
                onDismiss = { showAudioSheet = false }
            )
        }

        if (showEqualizerSheet) {
            EqualizerBottomSheet(
                audioFxManager = viewModel.playerEngine.audioFxManager,
                onDismiss = { showEqualizerSheet = false }
            )
        }

        if (showInfoSheet) {
            VideoInfoBottomSheet(
                title = state.videoTitle,
                path = state.videoPath,
                durationMs = state.durationMs,
                onDismiss = { showInfoSheet = false }
            )
        }

        if (showSleepTimerSheet) {
            SleepTimerBottomSheet(
                currentMinutesRemaining = sleepTimerMinutes,
                onSetTimerMinutes = { minutes -> sleepTimerMinutes = minutes },
                onDismiss = { showSleepTimerSheet = false }
            )
        }

        if (showPictureSheet) {
            PictureQualityBottomSheet(
                pictureEffectManager = viewModel.playerEngine.pictureEffectManager,
                onDismiss = { showPictureSheet = false }
            )
        }

        if (showAspectRatioSheet) {
            AspectRatioBottomSheet(
                currentPreferences = aspectRatioPrefs,
                isZoomed = userZoom > 1.01f || userPan != Offset.Zero,
                onSelectMode = { mode ->
                    userZoom = 1.0f
                    userPan = Offset.Zero
                    val newPrefs = aspectRatioPrefs.copy(mode = mode)
                    viewModel.updateAspectRatio(newPrefs)
                    aspectRatioBadgeText = mode.displayName
                    showAspectRatioBadge = true
                },
                onSelectPreset = { preset ->
                    userZoom = 1.0f
                    userPan = Offset.Zero
                    val newPrefs = aspectRatioPrefs.copy(
                        mode = AspectRatioMode.CUSTOM,
                        customPreset = preset.label,
                        customRatioValue = preset.ratio
                    )
                    viewModel.updateAspectRatio(newPrefs)
                    aspectRatioBadgeText = "Custom ${preset.label}"
                    showAspectRatioBadge = true
                },
                onSetCustomRatio = { customVal ->
                    userZoom = 1.0f
                    userPan = Offset.Zero
                    val newPrefs = aspectRatioPrefs.copy(
                        mode = AspectRatioMode.CUSTOM,
                        customPreset = String.format(java.util.Locale.US, "%.2f", customVal),
                        customRatioValue = customVal
                    )
                    viewModel.updateAspectRatio(newPrefs)
                    aspectRatioBadgeText = "Custom ${String.format(java.util.Locale.US, "%.2f", customVal)}"
                    showAspectRatioBadge = true
                },
                onToggleRememberPerVideo = { remember ->
                    viewModel.updateAspectRatio(aspectRatioPrefs.copy(rememberPerVideo = remember))
                },
                onResetZoom = {
                    userZoom = 1.0f
                    userPan = Offset.Zero
                    aspectRatioBadgeText = "Zoom reset"
                    showAspectRatioBadge = true
                },
                onDismiss = { showAspectRatioSheet = false }
            )
        }

        if (showMoreSheet) {
            PlayerMoreBottomSheet(
                onOpenPictureQuality = { showPictureSheet = true },
                onOpenEqualizer = { showEqualizerSheet = true },
                onOpenAudioTracks = { showAudioSheet = true },
                onOpenSubtitles = { showSubtitleSheet = true },
                onOpenSpeed = {
                    val nextSpeed = when (state.playbackSpeed) {
                        0.5f -> 0.75f
                        0.75f -> 1.0f
                        1.0f -> 1.25f
                        1.25f -> 1.5f
                        1.5f -> 2.0f
                        else -> 1.0f
                    }
                    viewModel.setTemporarySpeed(nextSpeed)
                },
                onOpenAspectRatio = {
                    showAspectRatioSheet = true
                },
                onOpenSleepTimer = { showSleepTimerSheet = true },
                onOpenInfo = { showInfoSheet = true },
                onTakeScreenshot = {
                    val picSettings = viewModel.playerEngine.pictureEffectManager.currentSettings.value
                    scope.launch {
                        PlayerScreenshotHelper.captureScreenshot(
                            context = context,
                            playerView = playerViewRef,
                            videoPath = state.videoPath,
                            videoTitle = state.videoTitle,
                            positionMs = state.currentPositionMs,
                            includeFilters = picSettings.includeFiltersInScreenshot,
                            onComplete = {}
                        )
                    }
                },
                onLockScreen = {
                    isScreenLocked = true
                    areControlsVisible = false
                },
                onDismiss = { showMoreSheet = false }
            )
        }
    }
}

@Composable
private fun PlayerControlsOverlay(
    state: PlayerPlaybackState,
    slots: PlayerControlsSlots,
    seekBarStyle: SeekBarStylePreference,
    hasMultipleAudioTracks: Boolean = false,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onActionClick: (PlayerAction) -> Unit,
    onBackgroundTap: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NPBlack.copy(alpha = 0.35f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onBackgroundTap() })
            }
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
                        if (action != PlayerAction.AUDIO_TRACK || hasMultipleAudioTracks) {
                            PlayerActionIconButton(
                                action = action,
                                onClick = { onActionClick(action) },
                                iconSize = 22.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Top Right Slots
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    slots.topRight.forEach { action ->
                        if (action != PlayerAction.AUDIO_TRACK || hasMultipleAudioTracks) {
                            PlayerActionIconButton(
                                action = action,
                                onClick = { onActionClick(action) },
                                iconSize = 22.dp
                            )
                        }
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
                            if (action != PlayerAction.AUDIO_TRACK || hasMultipleAudioTracks) {
                                PlayerActionIconButton(
                                    action = action,
                                    onClick = { onActionClick(action) },
                                    iconSize = 22.dp
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        slots.bottomRight.forEach { action ->
                            if (action != PlayerAction.AUDIO_TRACK || hasMultipleAudioTracks) {
                                PlayerActionIconButton(
                                    action = action,
                                    onClick = { onActionClick(action) },
                                    iconSize = 22.dp
                                )
                            }
                        }
                    }
                }

                var isScrubbing by remember { mutableStateOf(false) }
                var scrubPositionMs by remember { mutableLongStateOf(0L) }

                // Time labels
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = FormatUtils.formatDuration(if (isScrubbing) scrubPositionMs else state.currentPositionMs),
                        color = if (isScrubbing) NPYellow else NPWhite,
                        fontSize = 12.sp,
                        fontWeight = if (isScrubbing) FontWeight.Bold else FontWeight.Medium
                    )
                    Text(
                        text = FormatUtils.formatDuration(state.durationMs),
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
                    onScrubbing = { scrubbing, previewMs ->
                        isScrubbing = scrubbing
                        scrubPositionMs = previewMs
                    },
                    onSeek = { targetMs ->
                        isScrubbing = false
                        onSeek(targetMs)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
