package com.npplayer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.npplayer.MainActivity
import com.npplayer.R

class NPPlayerMusicWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_PLAY_PAUSE = "com.npplayer.widget.ACTION_PLAY_PAUSE"
        const val ACTION_WIDGET_PREV = "com.npplayer.widget.ACTION_PREV"
        const val ACTION_WIDGET_NEXT = "com.npplayer.widget.ACTION_NEXT"

        private var currentTitle: String = "NP Player"
        private var currentArtist: String = "Tap to play music"
        private var isPlayingState: Boolean = false

        fun updateAllWidgets(
            context: Context,
            title: String,
            artist: String,
            isPlaying: Boolean
        ) {
            currentTitle = title
            currentArtist = artist
            isPlayingState = isPlaying

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, NPPlayerMusicWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            for (widgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_music_4x1)

            views.setTextViewText(R.id.widget_song_title, currentTitle)
            views.setTextViewText(R.id.widget_song_artist, currentArtist)

            val playIcon = if (isPlayingState) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
            views.setImageViewResource(R.id.widget_btn_play_pause, playIcon)

            // Pending intent to open app
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("navigate_to", "music")
            }
            val appPendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, appPendingIntent)

            // Controls PendingIntents
            views.setOnClickPendingIntent(
                R.id.widget_btn_play_pause,
                createBroadcastIntent(context, ACTION_WIDGET_PLAY_PAUSE, 1)
            )
            views.setOnClickPendingIntent(
                R.id.widget_btn_prev,
                createBroadcastIntent(context, ACTION_WIDGET_PREV, 2)
            )
            views.setOnClickPendingIntent(
                R.id.widget_btn_next,
                createBroadcastIntent(context, ACTION_WIDGET_NEXT, 3)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun createBroadcastIntent(context: Context, action: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, NPPlayerMusicWidgetProvider::class.java).apply {
                this.action = action
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_WIDGET_PLAY_PAUSE -> {
                val serviceIntent = Intent(context, com.npplayer.player.service.PlaybackMediaSessionService::class.java).apply {
                    action = "ACTION_WIDGET_TOGGLE"
                }
                context.startService(serviceIntent)
            }
            ACTION_WIDGET_PREV -> {
                val serviceIntent = Intent(context, com.npplayer.player.service.PlaybackMediaSessionService::class.java).apply {
                    action = "ACTION_WIDGET_PREV"
                }
                context.startService(serviceIntent)
            }
            ACTION_WIDGET_NEXT -> {
                val serviceIntent = Intent(context, com.npplayer.player.service.PlaybackMediaSessionService::class.java).apply {
                    action = "ACTION_WIDGET_NEXT"
                }
                context.startService(serviceIntent)
            }
        }
    }
}
