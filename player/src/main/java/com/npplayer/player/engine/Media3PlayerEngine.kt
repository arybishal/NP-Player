package com.npplayer.player.engine

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.mp4.Mp4Extractor
import com.npplayer.player.audio.AudioFxManager
import com.npplayer.player.effects.PictureEffectManager
import dagger.hilt.android.qualifiers.ApplicationContext
import android.widget.Toast
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import com.npplayer.core.data.database.dao.VideoAudioPreferenceDao
import com.npplayer.core.data.database.entity.VideoAudioPreferenceEntity
import com.npplayer.core.data.preferences.SettingsRepository
import com.npplayer.player.audio.AudioTrackMatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class MediaTrackItem(
    val id: String,
    val name: String,
    val language: String,
    val isSelected: Boolean,
    val groupIndex: Int,
    val trackIndex: Int,
    val codec: String = "",
    val channels: String = "",
    val channelCount: Int = 2,
    val bitrate: Int = 0,
    val title: String = "",
    val isSupported: Boolean = true,
    val unsupportedReason: String? = null
)

@Singleton
class Media3PlayerEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    val audioFxManager: AudioFxManager,
    val pictureEffectManager: PictureEffectManager,
    val videoAudioPreferenceDao: VideoAudioPreferenceDao,
    val settingsRepository: SettingsRepository,
    private val musicPlayerEngineProvider: javax.inject.Provider<com.npplayer.player.music.MusicPlayerEngine>
) : PlayerEngine {

    var isSeekLoggingEnabled: Boolean = true
    private var pendingSeekPositionMs: Long? = null

    private val renderersFactory = DefaultRenderersFactory(context).apply {
        setEnableDecoderFallback(true) // Enables software fallback for any unsupported/failing hardware decoders (AAC, AC3, EAC3, DTS, HEVC, etc.)
        setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
    }

    private val trackSelector = DefaultTrackSelector(context).apply {
        parameters = buildUponParameters()
            .setTunnelingEnabled(false)
            .setAllowMultipleAdaptiveSelections(true)
            .build()
    }

    private val dataSourceFactory = DefaultDataSource.Factory(context)

    private val extractorsFactory = DefaultExtractorsFactory().apply {
        setConstantBitrateSeekingEnabled(true)
        setMp4ExtractorFlags(Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS)
    }

    private val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory, extractorsFactory)

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context, renderersFactory)
        .setTrackSelector(trackSelector)
        .setMediaSourceFactory(mediaSourceFactory)
        .setSeekParameters(SeekParameters.EXACT)
        .build()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(PlayerPlaybackState())
    override val state: StateFlow<PlayerPlaybackState> = _state.asStateFlow()

    private val _audioTracks = MutableStateFlow<List<MediaTrackItem>>(emptyList())
    val audioTracks: StateFlow<List<MediaTrackItem>> = _audioTracks.asStateFlow()

    private val _hasMultipleAudioTracks = MutableStateFlow(false)
    val hasMultipleAudioTracks: StateFlow<Boolean> = _hasMultipleAudioTracks.asStateFlow()

    private var lastAutoMatchedVideoPath: String = ""

    init {
        pictureEffectManager.attachPlayer(exoPlayer)

        exoPlayer.addListener(object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                updateAudioTracks()
                autoApplyRememberedAudioTrack()
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                val reasonStr = when (reason) {
                    Player.DISCONTINUITY_REASON_SEEK -> "SEEK"
                    Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> "SEEK_ADJUSTMENT"
                    Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> "AUTO_TRANSITION"
                    Player.DISCONTINUITY_REASON_INTERNAL -> "INTERNAL"
                    Player.DISCONTINUITY_REASON_REMOVE -> "REMOVE"
                    Player.DISCONTINUITY_REASON_SKIP -> "SKIP"
                    else -> "UNKNOWN($reason)"
                }
                if (isSeekLoggingEnabled) {
                    Log.d(
                        "NPSeek",
                        "onPositionDiscontinuity | reason=$reasonStr | oldPos=${oldPosition.positionMs}ms | newPos=${newPosition.positionMs}ms"
                    )
                }
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                val dur = exoPlayer.duration
                if (dur > 0L && pendingSeekPositionMs != null) {
                    val pending = pendingSeekPositionMs!!
                    pendingSeekPositionMs = null
                    seekTo(pending, "REAPPLY_PENDING_SEEK_ON_TIMELINE")
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val lastPos = _state.value.currentPositionMs.coerceAtLeast(exoPlayer.currentPosition).coerceAtLeast(0L)
                if (isSeekLoggingEnabled) {
                    Log.e("NPSeek", "onPlayerError | errorCode=${error.errorCode} | message=${error.message} | lastPos=${lastPos}ms", error)
                }
                // If the GPU video effects pipeline fails, fallback to rendering without effects
                if (error.errorCode == PlaybackException.ERROR_CODE_VIDEO_FRAME_PROCESSING_FAILED ||
                    error.errorCode == PlaybackException.ERROR_CODE_VIDEO_FRAME_PROCESSOR_INIT_FAILED
                ) {
                    try {
                        exoPlayer.setVideoEffects(emptyList())
                        exoPlayer.prepare()
                        if (lastPos > 0) {
                            seekTo(lastPos, "ERROR_RETRY_FALLBACK")
                        }
                        exoPlayer.play()
                    } catch (_: Exception) {}
                } else {
                    try {
                        exoPlayer.prepare()
                        if (lastPos > 0) {
                            seekTo(lastPos, "ERROR_RETRY_RECOVER")
                        }
                    } catch (_: Exception) {}
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                    if (exoPlayer.audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                        audioFxManager.bindAudioSession(exoPlayer.audioSessionId)
                    }
                } else {
                    stopProgressTracker()
                }
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    _state.update {
                        it.copy(
                            videoWidth = videoSize.width,
                            videoHeight = videoSize.height,
                            pixelWidthHeightRatio = if (videoSize.pixelWidthHeightRatio > 0f) videoSize.pixelWidthHeightRatio else 1.0f,
                            unappliedRotationDegrees = videoSize.unappliedRotationDegrees
                        )
                    }
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                    audioFxManager.bindAudioSession(audioSessionId)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        val dur = exoPlayer.duration.coerceAtLeast(0L)
                        val vs = exoPlayer.videoSize
                        _state.update {
                            it.copy(
                                durationMs = dur,
                                isLoading = false,
                                videoWidth = if (vs.width > 0) vs.width else it.videoWidth,
                                videoHeight = if (vs.height > 0) vs.height else it.videoHeight,
                                pixelWidthHeightRatio = if (vs.pixelWidthHeightRatio > 0f) vs.pixelWidthHeightRatio else it.pixelWidthHeightRatio,
                                unappliedRotationDegrees = vs.unappliedRotationDegrees
                            )
                        }
                        if (exoPlayer.audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                            audioFxManager.bindAudioSession(exoPlayer.audioSessionId)
                        }
                        if (dur > 0L && pendingSeekPositionMs != null) {
                            val pending = pendingSeekPositionMs!!
                            pendingSeekPositionMs = null
                            seekTo(pending, "REAPPLY_PENDING_SEEK_ON_READY")
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
        pictureEffectManager.onVideoChanged(path)

        val uri = if (path.startsWith("content://") || path.startsWith("http://") || path.startsWith("https://")) {
            Uri.parse(path)
        } else {
            Uri.fromFile(File(path))
        }

        // Auto-discover sibling external subtitles (.srt, .vtt, .ass) if local file
        val siblingSubs = findSiblingSubtitles(path)

        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaId(path)
            .setSubtitleConfigurations(siblingSubs)
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
        pauseMusicIfPlaying()
        exoPlayer.play()
    }

    private fun pauseMusicIfPlaying() {
        try {
            val musicPlayer = musicPlayerEngineProvider.get()
            if (musicPlayer.isPlaying.value) {
                musicPlayer.pause()
            }
        } catch (_: Exception) {}
    }

    fun preparePaused(path: String, title: String, startPositionMs: Long = 0L) {
        pictureEffectManager.onVideoChanged(path)

        val uri = if (path.startsWith("content://") || path.startsWith("http://") || path.startsWith("https://")) {
            Uri.parse(path)
        } else {
            Uri.fromFile(File(path))
        }

        val siblingSubs = findSiblingSubtitles(path)

        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaId(path)
            .setSubtitleConfigurations(siblingSubs)
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
        exoPlayer.pause()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun resume() {
        pauseMusicIfPlaying()
        exoPlayer.play()
    }

    override fun seekTo(positionMs: Long, reason: String) {
        val duration = exoPlayer.duration
        val isSeekable = exoPlayer.isCurrentMediaItemSeekable
        val isDynamic = exoPlayer.isCurrentMediaItemDynamic
        val playbackStateStr = when (exoPlayer.playbackState) {
            Player.STATE_IDLE -> "IDLE"
            Player.STATE_BUFFERING -> "BUFFERING"
            Player.STATE_READY -> "READY"
            Player.STATE_ENDED -> "ENDED"
            else -> "UNKNOWN"
        }
        val uri = _state.value.videoPath

        if (duration == C.TIME_UNSET || duration <= 0L) {
            // Duration is not ready yet - remember this seek so we apply once duration is known
            pendingSeekPositionMs = positionMs
            if (isSeekLoggingEnabled) {
                Log.d("NPSeek", "seekTo() deferred (duration unset) | reason=$reason | targetMs=$positionMs | uri=$uri")
            }
            _state.update { it.copy(currentPositionMs = positionMs.coerceAtLeast(0L)) }
            return
        }

        val targetMs = positionMs.coerceIn(0L, duration)
        val currentPos = exoPlayer.currentPosition

        if (isSeekLoggingEnabled) {
            Log.d(
                "NPSeek",
                "seekTo() called | reason=$reason | targetMs=$targetMs (requested=$positionMs) | currentPos=$currentPos | " +
                "duration=${duration}ms | isSeekable=$isSeekable | isDynamic=$isDynamic | playbackState=$playbackStateStr | uri=$uri"
            )
        }

        exoPlayer.setSeekParameters(SeekParameters.EXACT)
        exoPlayer.seekTo(targetMs)
        _state.update { it.copy(currentPositionMs = targetMs) }

        if (isSeekLoggingEnabled) {
            Log.d("NPSeek", "seekTo() dispatched | postSeekPos=${exoPlayer.currentPosition}ms")
        }
    }

    fun scrubTo(positionMs: Long) {
        val duration = exoPlayer.duration
        if (duration > 0L) {
            val targetMs = positionMs.coerceIn(0L, duration)
            exoPlayer.setSeekParameters(SeekParameters.CLOSEST_SYNC)
            exoPlayer.seekTo(targetMs)
            _state.update { it.copy(currentPositionMs = targetMs) }
        }
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
        audioFxManager.releaseEffects()
        exoPlayer.release()
    }

    fun getSubtitleTracks(): List<MediaTrackItem> {
        val result = mutableListOf<MediaTrackItem>()
        val tracks = exoPlayer.currentTracks
        for ((gIndex, group) in tracks.groups.withIndex()) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                for (tIndex in 0 until group.length) {
                    val format = group.getTrackFormat(tIndex)
                    val lang = format.language ?: "und"
                    val name = format.label ?: "Subtitle ${result.size + 1} (${lang.uppercase()})"
                    result.add(
                        MediaTrackItem(
                            id = format.id ?: "$gIndex-$tIndex",
                            name = name,
                            language = lang,
                            isSelected = group.isTrackSelected(tIndex),
                            groupIndex = gIndex,
                            trackIndex = tIndex
                        )
                    )
                }
            }
        }
        return result
    }

    fun selectSubtitleTrack(track: MediaTrackItem?) {
        if (track == null) {
            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .build()
        } else {
            val tracks = exoPlayer.currentTracks
            if (track.groupIndex < tracks.groups.size) {
                val group = tracks.groups[track.groupIndex]
                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, track.trackIndex))
                .build()
            }
        }
    }

    fun addExternalSubtitle(uri: Uri, label: String = "External Subtitle") {
        try {
            val currentPos = exoPlayer.currentPosition
            val isPlaying = exoPlayer.isPlaying
            val path = _state.value.videoPath

            val uriStr = uri.toString().lowercase()
            val mimeType = when {
                uriStr.endsWith(".vtt") -> MimeTypes.TEXT_VTT
                uriStr.endsWith(".ass") || uriStr.endsWith(".ssa") -> MimeTypes.TEXT_SSA
                else -> MimeTypes.APPLICATION_SUBRIP
            }

            val extSub = MediaItem.SubtitleConfiguration.Builder(uri)
                .setMimeType(mimeType)
                .setLabel(label)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT or C.SELECTION_FLAG_FORCED)
                .build()

            val mainUri = if (path.startsWith("content://") || path.startsWith("http://") || path.startsWith("https://")) {
                Uri.parse(path)
            } else {
                Uri.fromFile(File(path))
            }

            val existingSubs = findSiblingSubtitles(path)
            val updatedMediaItem = MediaItem.Builder()
                .setUri(mainUri)
                .setMediaId(path)
                .setSubtitleConfigurations(listOf(extSub) + existingSubs)
                .build()

            exoPlayer.setMediaItem(updatedMediaItem)
            exoPlayer.seekTo(currentPos)
            exoPlayer.prepare()
            if (isPlaying) {
                exoPlayer.play()
            }
        } catch (_: Exception) {}
    }

    private fun findSiblingSubtitles(videoPath: String): List<MediaItem.SubtitleConfiguration> {
        val results = mutableListOf<MediaItem.SubtitleConfiguration>()
        try {
            val file = File(videoPath)
            if (!file.exists() || !file.isFile) return results
            val parent = file.parentFile ?: return results
            val baseName = file.nameWithoutExtension

            val subExtensions = listOf("srt", "vtt", "ass", "ssa", "sub")
            val siblingFiles = parent.listFiles { f ->
                f.isFile && subExtensions.contains(f.extension.lowercase())
            } ?: return results

            for (sub in siblingFiles) {
                val subName = sub.nameWithoutExtension
                if (subName.equals(baseName, ignoreCase = true) ||
                    subName.startsWith(baseName, ignoreCase = true) ||
                    siblingFiles.size == 1
                ) {
                    val mimeType = when (sub.extension.lowercase()) {
                        "srt" -> MimeTypes.APPLICATION_SUBRIP
                        "vtt" -> MimeTypes.TEXT_VTT
                        "ass", "ssa" -> MimeTypes.TEXT_SSA
                        else -> MimeTypes.APPLICATION_SUBRIP
                    }

                    val lang = if (subName.contains(".")) {
                        subName.substringAfterLast(".").lowercase()
                    } else "und"

                    val label = "External (${lang.uppercase()}) - ${sub.name}"
                    val config = MediaItem.SubtitleConfiguration.Builder(Uri.fromFile(sub))
                        .setMimeType(mimeType)
                        .setLanguage(lang)
                        .setLabel(label)
                        .setSelectionFlags(if (results.isEmpty()) C.SELECTION_FLAG_DEFAULT else 0)
                        .build()
                    results.add(config)
                }
            }
        } catch (_: Exception) {}
        return results
    }

    fun getAudioTracks(): List<MediaTrackItem> {
        val result = mutableListOf<MediaTrackItem>()
        val tracks = exoPlayer.currentTracks
        for ((gIndex, group) in tracks.groups.withIndex()) {
            if (group.type == C.TRACK_TYPE_AUDIO) {
                for (tIndex in 0 until group.length) {
                    val format = group.getTrackFormat(tIndex)
                    val langCode = format.language ?: "und"
                    val displayLang = if (langCode == "und" || langCode.isBlank()) "Unknown" else Locale(langCode).displayLanguage.replaceFirstChar { it.uppercase() }
                    val codec = when (format.sampleMimeType) {
                        MimeTypes.AUDIO_AAC -> "AAC"
                        MimeTypes.AUDIO_AC3 -> "AC3"
                        MimeTypes.AUDIO_E_AC3, MimeTypes.AUDIO_E_AC3_JOC -> "E-AC3"
                        MimeTypes.AUDIO_DTS, MimeTypes.AUDIO_DTS_HD -> "DTS"
                        MimeTypes.AUDIO_TRUEHD -> "TrueHD"
                        MimeTypes.AUDIO_OPUS -> "Opus"
                        MimeTypes.AUDIO_FLAC -> "FLAC"
                        MimeTypes.AUDIO_VORBIS -> "Vorbis"
                        MimeTypes.AUDIO_MPEG -> "MP3"
                        else -> format.sampleMimeType?.substringAfter("/")?.uppercase() ?: "AUDIO"
                    }
                    val channels = when (format.channelCount) {
                        1 -> "1.0"
                        2 -> "2.0"
                        6 -> "5.1"
                        8 -> "7.1"
                        else -> if (format.channelCount > 0) "${format.channelCount}.0" else "2.0"
                    }
                    val isSupported = group.isTrackSupported(tIndex)
                    val unsuppReason = if (!isSupported) "Codec not supported by device" else null
                    val trackTitle = format.label ?: ""
                    val name = if (trackTitle.isNotBlank()) trackTitle else "$displayLang ($codec $channels)"

                    result.add(
                        MediaTrackItem(
                            id = format.id ?: "$gIndex-$tIndex",
                            name = name,
                            language = langCode,
                            isSelected = group.isTrackSelected(tIndex),
                            groupIndex = gIndex,
                            trackIndex = tIndex,
                            codec = codec,
                            channels = channels,
                            channelCount = format.channelCount,
                            bitrate = format.bitrate,
                            title = trackTitle,
                            isSupported = isSupported,
                            unsupportedReason = unsuppReason
                        )
                    )
                }
            }
        }
        return result
    }

    fun updateAudioTracks() {
        val tracks = getAudioTracks()
        _audioTracks.value = tracks
        _hasMultipleAudioTracks.value = tracks.size >= 2
    }

    fun selectAudioTrack(track: MediaTrackItem, showToast: Boolean = true) {
        val tracks = exoPlayer.currentTracks
        if (track.groupIndex < tracks.groups.size) {
            val group = tracks.groups[track.groupIndex]
            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                .buildUpon()
                .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, track.trackIndex))
                .build()

            updateAudioTracks()

            if (showToast) {
                val displayLang = if (track.language == "und" || track.language.isBlank()) "Audio" else Locale(track.language).displayLanguage.replaceFirstChar { it.uppercase() }
                Toast.makeText(context, "Audio: $displayLang (${track.channels})", Toast.LENGTH_SHORT).show()
            }

            scope.launch(Dispatchers.IO) {
                try {
                    val audioPrefs = settingsRepository.audioPreferencesFlow.first()
                    if (audioPrefs.rememberAudioTrack) {
                        val path = _state.value.videoPath
                        if (path.isNotBlank()) {
                            val folderPath = File(path).parent ?: ""
                            videoAudioPreferenceDao.savePreference(
                                VideoAudioPreferenceEntity(
                                    videoId = path.hashCode().toLong(),
                                    videoPath = path,
                                    folderPath = folderPath,
                                    languageCode = track.language,
                                    trackLabel = track.title,
                                    codec = track.codec,
                                    channelCount = track.channelCount,
                                    trackIndex = track.trackIndex
                                )
                            )
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun autoApplyRememberedAudioTrack() {
        val path = _state.value.videoPath
        if (path.isBlank() || path == lastAutoMatchedVideoPath) return

        val tracks = getAudioTracks()
        if (tracks.size < 2) return

        scope.launch(Dispatchers.IO) {
            try {
                val audioPrefs = settingsRepository.audioPreferencesFlow.first()
                val savedPref = if (audioPrefs.rememberAudioTrack) {
                    videoAudioPreferenceDao.getPreferenceForPath(path)
                } else null

                val folderPref = if (savedPref == null && audioPrefs.rememberAudioTrack && audioPrefs.applyToFolderSeries) {
                    videoAudioPreferenceDao.getLatestPreferenceForFolder(File(path).parent ?: "")
                } else null

                val bestMatch = AudioTrackMatcher.findBestMatch(
                    availableTracks = tracks,
                    savedPreference = savedPref,
                    folderPreference = folderPref,
                    preferredLanguages = audioPrefs.preferredAudioLanguages
                )

                if (bestMatch != null && !bestMatch.isSelected) {
                    lastAutoMatchedVideoPath = path
                    withContext(Dispatchers.Main) {
                        selectAudioTrack(bestMatch, showToast = false)
                    }
                } else if (bestMatch != null) {
                    lastAutoMatchedVideoPath = path
                }
            } catch (_: Exception) {}
        }
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
