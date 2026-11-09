package com.npplayer.player.music

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.npplayer.core.data.database.dao.FavoriteSongDao
import com.npplayer.core.data.database.dao.MusicPlayHistoryDao
import com.npplayer.core.data.database.dao.MusicQueueDao
import com.npplayer.core.data.database.dao.SongDao
import com.npplayer.core.data.database.entity.FavoriteSongEntity
import com.npplayer.core.data.database.entity.MusicQueueStateEntity
import com.npplayer.core.data.database.entity.SongEntity
import com.npplayer.core.data.repository.MusicRepeatMode
import com.npplayer.core.data.repository.SleepTimerOption
import com.npplayer.core.ui.component.getAlbumArtUri
import com.npplayer.player.audio.AudioFxManager
import com.npplayer.player.engine.Media3PlayerEngine
import com.npplayer.player.service.PlaybackMediaSessionService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class MusicPlayerEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val favoriteSongDao: FavoriteSongDao,
    private val playHistoryDao: MusicPlayHistoryDao,
    private val musicQueueDao: MusicQueueDao,
    private val audioFxManager: AudioFxManager,
    private val videoPlayerEngineProvider: Provider<Media3PlayerEngine>
) {
    companion object {
        const val ACTION_FAVORITE = "com.npplayer.music.ACTION_FAVORITE"
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val renderersFactory = DefaultRenderersFactory(context).apply {
        setEnableDecoderFallback(true)
        setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
    }

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context, renderersFactory).build().apply {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        setAudioAttributes(audioAttributes, true)
        setHandleAudioBecomingNoisy(true)
    }

    // Playback States
    private val _currentQueue = MutableStateFlow<List<SongEntity>>(emptyList())
    val currentQueue: StateFlow<List<SongEntity>> = _currentQueue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _bufferedPositionMs = MutableStateFlow(0L)
    val bufferedPositionMs: StateFlow<Long> = _bufferedPositionMs.asStateFlow()

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(MusicRepeatMode.OFF)
    val repeatMode: StateFlow<MusicRepeatMode> = _repeatMode.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // Sleep Timer
    private val _sleepTimerRemainingMs = MutableStateFlow<Long?>(null)
    val sleepTimerRemainingMs: StateFlow<Long?> = _sleepTimerRemainingMs.asStateFlow()

    private val _sleepTimerOption = MutableStateFlow<SleepTimerOption?>(null)
    val sleepTimerOption: StateFlow<SleepTimerOption?> = _sleepTimerOption.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var progressJob: Job? = null
    private var favoriteJob: Job? = null

    // Play count calculation tracking
    private var hasRecordedPlayCount = false
    private var currentSongPlayedMs = 0L

    // MediaSession
    private val sessionCallback = object : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(ACTION_FAVORITE, Bundle.EMPTY))
                .build()
            val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .setAvailablePlayerCommands(playerCommands)
                .setCustomLayout(listOf(buildFavoriteCommandButton(_isFavorite.value)))
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == ACTION_FAVORITE) {
                toggleFavoriteCurrentSong()
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    val mediaSession: MediaSession = MediaSession.Builder(context, exoPlayer)
        .setCallback(sessionCallback)
        .build()

    init {
        audioFxManager.bindAudioSession(exoPlayer.audioSessionId)

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) {
                    startForegroundServiceIfNeeded()
                    pauseVideoIfPlaying()
                    audioFxManager.bindAudioSession(exoPlayer.audioSessionId)
                }
                persistQueueState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                    }
                    Player.STATE_ENDED -> {
                        if (_sleepTimerOption.value == SleepTimerOption.EndOfQueue) {
                            cancelSleepTimer()
                            pause()
                        }
                    }
                    else -> {}
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = exoPlayer.currentMediaItemIndex
                if (index in _currentQueue.value.indices) {
                    _currentIndex.value = index
                    _currentSong.value = _currentQueue.value[index]
                    observeFavoriteForCurrentSong(_currentQueue.value[index].id)
                }
                _currentPositionMs.value = 0L
                _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                hasRecordedPlayCount = false
                currentSongPlayedMs = 0L

                // Sleep timer: End of song
                if (_sleepTimerOption.value == SleepTimerOption.EndOfSong && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    cancelSleepTimer()
                    pause()
                }

                persistQueueState()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _shuffleMode.value = shuffleModeEnabled
                persistQueueState()
            }

            override fun onRepeatModeChanged(exoRepeatMode: Int) {
                _repeatMode.value = when (exoRepeatMode) {
                    Player.REPEAT_MODE_ALL -> MusicRepeatMode.ALL
                    Player.REPEAT_MODE_ONE -> MusicRepeatMode.ONE
                    else -> MusicRepeatMode.OFF
                }
                persistQueueState()
            }
        })

        startProgressTracking()
        restoreQueueState()
    }

    private fun startForegroundServiceIfNeeded() {
        try {
            val serviceIntent = Intent(context, PlaybackMediaSessionService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (_: Exception) {}
    }

    private fun pauseVideoIfPlaying() {
        try {
            val videoEngine = videoPlayerEngineProvider.get()
            if (videoEngine.exoPlayer.isPlaying) {
                videoEngine.exoPlayer.pause()
            }
        } catch (_: Exception) {}
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    val pos = exoPlayer.currentPosition
                    val dur = exoPlayer.duration.coerceAtLeast(0L)
                    _currentPositionMs.value = pos
                    _durationMs.value = dur
                    _bufferedPositionMs.value = exoPlayer.bufferedPosition

                    // Track play count (>50% or >30 seconds played)
                    currentSongPlayedMs += 350L
                    if (!hasRecordedPlayCount && _currentSong.value != null) {
                        val isOverHalf = dur > 0L && pos >= (dur / 2)
                        val isOver30Sec = pos >= 30_000L || currentSongPlayedMs >= 30_000L
                        if (isOverHalf || isOver30Sec) {
                            hasRecordedPlayCount = true
                            val songId = _currentSong.value!!.id
                            launch(Dispatchers.IO) {
                                playHistoryDao.recordPlay(songId)
                            }
                        }
                    }
                }
                delay(350L)
            }
        }
    }

    fun isSessionActive(): Boolean {
        return _currentSong.value != null
    }

    // --- Playback Controls ---

    fun play() {
        pauseVideoIfPlaying()
        exoPlayer.play()
        startForegroundServiceIfNeeded()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _durationMs.value)
        exoPlayer.seekTo(clamped)
        _currentPositionMs.value = clamped
    }

    fun skipToNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (_repeatMode.value == MusicRepeatMode.ALL && _currentQueue.value.isNotEmpty()) {
            exoPlayer.seekToDefaultPosition(0)
        }
    }

    fun skipToPrevious() {
        if (_currentPositionMs.value > 3000L) {
            seekTo(0L)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        } else {
            seekTo(0L)
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_shuffleMode.value
        exoPlayer.shuffleModeEnabled = newShuffle
        _shuffleMode.value = newShuffle
        persistQueueState()
    }

    fun toggleRepeat() {
        val next = _repeatMode.value.next()
        val exoMode = when (next) {
            MusicRepeatMode.OFF -> Player.REPEAT_MODE_OFF
            MusicRepeatMode.ALL -> Player.REPEAT_MODE_ALL
            MusicRepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        exoPlayer.repeatMode = exoMode
        _repeatMode.value = next
        persistQueueState()
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2.0f)
        exoPlayer.playbackParameters = PlaybackParameters(clamped)
        _playbackSpeed.value = clamped
    }

    // --- Queue Management ---

    fun playSong(song: SongEntity, queue: List<SongEntity> = listOf(song)) {
        val targetQueue = if (queue.isEmpty()) listOf(song) else queue
        val index = targetQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        _currentQueue.value = targetQueue
        _currentIndex.value = index
        _currentSong.value = targetQueue[index]

        val mediaItems = targetQueue.map { it.toMediaItem() }
        exoPlayer.setMediaItems(mediaItems, index, 0L)
        exoPlayer.prepare()
        play()
        observeFavoriteForCurrentSong(song.id)
        persistQueueState()
    }

    fun playAll(songs: List<SongEntity>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        val index = startIndex.coerceIn(0, songs.lastIndex)

        _currentQueue.value = songs
        _currentIndex.value = index
        _currentSong.value = songs[index]

        val mediaItems = songs.map { it.toMediaItem() }
        exoPlayer.setMediaItems(mediaItems, index, 0L)
        exoPlayer.prepare()
        play()
        observeFavoriteForCurrentSong(songs[index].id)
        persistQueueState()
    }

    fun shuffleAll(songs: List<SongEntity>) {
        if (songs.isEmpty()) return
        val shuffled = songs.shuffled()
        _currentQueue.value = shuffled
        _currentIndex.value = 0
        _currentSong.value = shuffled[0]

        val mediaItems = shuffled.map { it.toMediaItem() }
        exoPlayer.setMediaItems(mediaItems, 0, 0L)
        exoPlayer.shuffleModeEnabled = true
        _shuffleMode.value = true
        exoPlayer.prepare()
        play()
        observeFavoriteForCurrentSong(shuffled[0].id)
        persistQueueState()
    }

    fun playNext(song: SongEntity) {
        val currentList = _currentQueue.value.toMutableList()
        if (currentList.isEmpty()) {
            playSong(song)
            return
        }

        val nextIndex = (_currentIndex.value + 1).coerceAtMost(currentList.size)
        // If already in queue, remove old copy
        val existingIndex = currentList.indexOfFirst { it.id == song.id }
        if (existingIndex >= 0) {
            currentList.removeAt(existingIndex)
            exoPlayer.removeMediaItem(existingIndex)
        }

        val insertIndex = (_currentIndex.value + 1).coerceAtMost(currentList.size)
        currentList.add(insertIndex, song)
        exoPlayer.addMediaItem(insertIndex, song.toMediaItem())
        _currentQueue.value = currentList
        persistQueueState()
    }

    fun addToQueue(song: SongEntity) {
        addToQueue(listOf(song))
    }

    fun addToQueue(songs: List<SongEntity>) {
        if (songs.isEmpty()) return
        val currentList = _currentQueue.value.toMutableList()
        if (currentList.isEmpty()) {
            playAll(songs)
            return
        }

        songs.forEach { song ->
            currentList.add(song)
            exoPlayer.addMediaItem(song.toMediaItem())
        }
        _currentQueue.value = currentList
        persistQueueState()
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val list = _currentQueue.value.toMutableList()
        if (fromIndex !in list.indices || toIndex !in list.indices || fromIndex == toIndex) return

        val movedItem = list.removeAt(fromIndex)
        list.add(toIndex, movedItem)
        exoPlayer.moveMediaItem(fromIndex, toIndex)
        _currentQueue.value = list

        val currentIdx = _currentIndex.value
        _currentIndex.value = when {
            currentIdx == fromIndex -> toIndex
            fromIndex < currentIdx && toIndex >= currentIdx -> currentIdx - 1
            fromIndex > currentIdx && toIndex <= currentIdx -> currentIdx + 1
            else -> currentIdx
        }
        persistQueueState()
    }

    fun removeFromQueue(index: Int) {
        val list = _currentQueue.value.toMutableList()
        if (index !in list.indices) return

        val currentIdx = _currentIndex.value
        list.removeAt(index)
        exoPlayer.removeMediaItem(index)

        if (list.isEmpty()) {
            clearQueue()
            return
        }

        _currentQueue.value = list
        if (index == currentIdx) {
            val newIdx = index.coerceAtMost(list.lastIndex)
            _currentIndex.value = newIdx
            _currentSong.value = list[newIdx]
            observeFavoriteForCurrentSong(list[newIdx].id)
        } else if (index < currentIdx) {
            _currentIndex.value = currentIdx - 1
        }
        persistQueueState()
    }

    fun removeSongFromQueue(songId: Long) {
        val list = _currentQueue.value
        val idx = list.indexOfFirst { it.id == songId }
        if (idx >= 0) {
            removeFromQueue(idx)
        }
    }

    fun clearQueue() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _currentQueue.value = emptyList()
        _currentIndex.value = 0
        _currentSong.value = null
        _isPlaying.value = false
        _currentPositionMs.value = 0L
        _durationMs.value = 0L
        cancelSleepTimer()
        scope.launch(Dispatchers.IO) {
            musicQueueDao.saveQueueState(
                MusicQueueStateEntity(
                    id = 1,
                    queueSongIds = "",
                    currentIndex = 0,
                    currentPositionMs = 0L,
                    shuffleEnabled = false,
                    repeatMode = 0
                )
            )
        }
    }

    // --- Favorite Integration ---

    private fun observeFavoriteForCurrentSong(songId: Long) {
        favoriteJob?.cancel()
        favoriteJob = scope.launch {
            favoriteSongDao.isFavoriteFlow(songId).collect { isFav ->
                _isFavorite.value = isFav
                updateMediaSessionFavoriteButton(isFav)
            }
        }
    }

    fun toggleFavoriteCurrentSong() {
        val song = _currentSong.value ?: return
        scope.launch(Dispatchers.IO) {
            val isFav = favoriteSongDao.isFavorite(song.id)
            if (isFav) {
                favoriteSongDao.deleteFavorite(song.id)
            } else {
                favoriteSongDao.insertFavorite(FavoriteSongEntity(songId = song.id))
            }
        }
    }

    private fun updateMediaSessionFavoriteButton(isFav: Boolean) {
        try {
            mediaSession.setCustomLayout(listOf(buildFavoriteCommandButton(isFav)))
        } catch (_: Exception) {}
    }

    private fun buildFavoriteCommandButton(isFav: Boolean): CommandButton {
        return CommandButton.Builder()
            .setDisplayName(if (isFav) "Favorited" else "Favorite")
            .setIconResId(
                if (isFav) com.npplayer.core.ui.R.drawable.ic_favorite_filled
                else com.npplayer.core.ui.R.drawable.ic_favorite_border
            )
            .setSessionCommand(SessionCommand(ACTION_FAVORITE, Bundle.EMPTY))
            .build()
    }

    // --- Sleep Timer ---

    fun setSleepTimer(option: SleepTimerOption) {
        sleepTimerJob?.cancel()
        _sleepTimerOption.value = option

        when (option) {
            is SleepTimerOption.Minutes -> {
                val totalMs = option.minutes * 60 * 1000L
                val endTime = System.currentTimeMillis() + totalMs
                sleepTimerJob = scope.launch {
                    while (isActive) {
                        val remaining = endTime - System.currentTimeMillis()
                        if (remaining <= 0) {
                            _sleepTimerRemainingMs.value = null
                            _sleepTimerOption.value = null
                            exoPlayer.volume = 1f
                            pause()
                            break
                        }
                        _sleepTimerRemainingMs.value = remaining

                        // 30-second volume fade-out before ending
                        if (remaining <= 30_000L) {
                            val fraction = (remaining / 30_000f).coerceIn(0f, 1f)
                            exoPlayer.volume = fraction
                        } else {
                            exoPlayer.volume = 1f
                        }
                        delay(1000L)
                    }
                }
            }
            is SleepTimerOption.EndOfSong -> {
                _sleepTimerRemainingMs.value = null
                exoPlayer.volume = 1f
            }
            is SleepTimerOption.EndOfQueue -> {
                _sleepTimerRemainingMs.value = null
                exoPlayer.volume = 1f
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerOption.value = null
        _sleepTimerRemainingMs.value = null
        exoPlayer.volume = 1f
    }

    // --- Queue State Persistence ---

    private fun persistQueueState() {
        val songs = _currentQueue.value
        val songIds = songs.joinToString(",") { it.id.toString() }
        val currIdx = _currentIndex.value
        val currPos = _currentPositionMs.value
        val shuffle = _shuffleMode.value
        val repeat = _repeatMode.value.ordinal

        scope.launch(Dispatchers.IO) {
            musicQueueDao.saveQueueState(
                MusicQueueStateEntity(
                    id = 1,
                    queueSongIds = songIds,
                    currentIndex = currIdx,
                    currentPositionMs = currPos,
                    shuffleEnabled = shuffle,
                    repeatMode = repeat
                )
            )
        }
    }

    private fun restoreQueueState() {
        scope.launch(Dispatchers.IO) {
            val state = musicQueueDao.getQueueState() ?: return@launch
            if (state.queueSongIds.isBlank()) return@launch

            val ids = state.queueSongIds.split(",").mapNotNull { it.toLongOrNull() }
            if (ids.isEmpty()) return@launch

            val loadedSongs = songDao.getSongsByIds(ids)
            val songMap = loadedSongs.associateBy { it.id }
            val orderedSongs = ids.mapNotNull { songMap[it] }
            if (orderedSongs.isEmpty()) return@launch

            withContext(Dispatchers.Main) {
                _currentQueue.value = orderedSongs
                val restoreIdx = state.currentIndex.coerceIn(0, orderedSongs.lastIndex)
                _currentIndex.value = restoreIdx
                _currentSong.value = orderedSongs[restoreIdx]

                val mediaItems = orderedSongs.map { it.toMediaItem() }
                exoPlayer.setMediaItems(mediaItems, restoreIdx, state.currentPositionMs)
                exoPlayer.shuffleModeEnabled = state.shuffleEnabled
                _shuffleMode.value = state.shuffleEnabled

                val restoredRepeat = when (state.repeatMode) {
                    1 -> MusicRepeatMode.ALL
                    2 -> MusicRepeatMode.ONE
                    else -> MusicRepeatMode.OFF
                }
                _repeatMode.value = restoredRepeat
                exoPlayer.repeatMode = when (restoredRepeat) {
                    MusicRepeatMode.ALL -> Player.REPEAT_MODE_ALL
                    MusicRepeatMode.ONE -> Player.REPEAT_MODE_ONE
                    MusicRepeatMode.OFF -> Player.REPEAT_MODE_OFF
                }

                _currentPositionMs.value = state.currentPositionMs
                exoPlayer.prepare()
                exoPlayer.pause()
                observeFavoriteForCurrentSong(orderedSongs[restoreIdx].id)
            }
        }
    }

    private fun SongEntity.toMediaItem(): MediaItem {
        val artUri = getAlbumArtUri(albumId)
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .apply {
                if (artUri != null) {
                    setArtworkUri(artUri)
                }
            }
            .build()

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()
    }
}
