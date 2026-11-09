package com.npplayer.player.service

import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.npplayer.player.engine.Media3PlayerEngine
import com.npplayer.player.music.MusicPlayerEngine
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class PlaybackMediaSessionService : MediaSessionService() {

    @Inject
    lateinit var playerEngine: Media3PlayerEngine

    @Inject
    lateinit var musicPlayerEngine: MusicPlayerEngine

    private var videoMediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        try {
            val notificationProvider = DefaultMediaNotificationProvider(this)
            notificationProvider.setSmallIcon(com.npplayer.core.ui.R.drawable.ic_stat_np)
            setMediaNotificationProvider(notificationProvider)
        } catch (_: Exception) {}

        addSession(musicPlayerEngine.mediaSession)
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_WIDGET_TOGGLE" -> musicPlayerEngine.togglePlayPause()
            "ACTION_WIDGET_PREV" -> musicPlayerEngine.skipToPrevious()
            "ACTION_WIDGET_NEXT" -> musicPlayerEngine.skipToNext()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return if (musicPlayerEngine.isSessionActive()) {
            musicPlayerEngine.mediaSession
        } else {
            if (videoMediaSession == null) {
                videoMediaSession = MediaSession.Builder(this, playerEngine.exoPlayer).build()
            }
            videoMediaSession
        }
    }

    override fun onDestroy() {
        videoMediaSession?.release()
        videoMediaSession = null
        super.onDestroy()
    }
}
