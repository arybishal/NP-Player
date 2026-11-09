package com.npplayer.core.data.di

import android.content.Context
import androidx.room.Room
import com.npplayer.core.data.database.NPPlayerDatabase
import com.npplayer.core.data.database.dao.FolderDao
import com.npplayer.core.data.database.dao.PlaylistDao
import com.npplayer.core.data.database.dao.StreamBookmarkDao
import com.npplayer.core.data.database.dao.VaultDao
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.dao.WatchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNPPlayerDatabase(
        @ApplicationContext context: Context
    ): NPPlayerDatabase {
        return Room.databaseBuilder(
            context,
            NPPlayerDatabase::class.java,
            "np_player_database.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideVideoDao(database: NPPlayerDatabase): VideoDao = database.videoDao()

    @Provides
    fun provideFolderDao(database: NPPlayerDatabase): FolderDao = database.folderDao()

    @Provides
    fun provideWatchHistoryDao(database: NPPlayerDatabase): WatchHistoryDao = database.watchHistoryDao()

    @Provides
    fun provideVaultDao(database: NPPlayerDatabase): VaultDao = database.vaultDao()

    @Provides
    fun providePlaylistDao(database: NPPlayerDatabase): PlaylistDao = database.playlistDao()

    @Provides
    fun provideStreamBookmarkDao(database: NPPlayerDatabase): StreamBookmarkDao = database.streamBookmarkDao()

    @Provides
    fun provideDownloadedSubtitleDao(database: NPPlayerDatabase) = database.downloadedSubtitleDao()

    @Provides
    fun provideLabelDao(database: NPPlayerDatabase) = database.labelDao()

    @Provides
    fun provideVideoAudioPreferenceDao(database: NPPlayerDatabase) = database.videoAudioPreferenceDao()

    @Provides
    fun provideSongDao(database: NPPlayerDatabase) = database.songDao()

    @Provides
    fun provideMusicPlaylistDao(database: NPPlayerDatabase) = database.musicPlaylistDao()

    @Provides
    fun provideFavoriteSongDao(database: NPPlayerDatabase) = database.favoriteSongDao()

    @Provides
    fun provideMusicPlayHistoryDao(database: NPPlayerDatabase) = database.musicPlayHistoryDao()

    @Provides
    fun provideMusicQueueDao(database: NPPlayerDatabase) = database.musicQueueDao()

    @Provides
    fun provideMusicLyricsDao(database: NPPlayerDatabase) = database.musicLyricsDao()

    @Provides
    fun provideSearchHistoryStore(
        settingsRepository: com.npplayer.core.data.preferences.SettingsRepository
    ): com.npplayer.core.data.repository.SearchHistoryStore = settingsRepository
}
