package com.npplayer.core.data.di

import android.content.Context
import androidx.room.Room
import com.npplayer.core.data.database.NPPlayerDatabase
import com.npplayer.core.data.database.dao.FolderDao
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
}
