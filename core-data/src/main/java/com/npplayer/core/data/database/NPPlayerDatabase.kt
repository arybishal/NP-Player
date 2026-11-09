package com.npplayer.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.npplayer.core.data.database.dao.DownloadedSubtitleDao
import com.npplayer.core.data.database.dao.FavoriteSongDao
import com.npplayer.core.data.database.dao.FolderDao
import com.npplayer.core.data.database.dao.LabelDao
import com.npplayer.core.data.database.dao.MusicLyricsDao
import com.npplayer.core.data.database.dao.MusicPlayHistoryDao
import com.npplayer.core.data.database.dao.MusicPlaylistDao
import com.npplayer.core.data.database.dao.MusicQueueDao
import com.npplayer.core.data.database.dao.PlaylistDao
import com.npplayer.core.data.database.dao.SongDao
import com.npplayer.core.data.database.dao.StreamBookmarkDao
import com.npplayer.core.data.database.dao.VaultDao
import com.npplayer.core.data.database.dao.VideoAudioPreferenceDao
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.dao.WatchHistoryDao
import com.npplayer.core.data.database.entity.DownloadedSubtitleEntity
import com.npplayer.core.data.database.entity.FavoriteSongEntity
import com.npplayer.core.data.database.entity.FolderEntity
import com.npplayer.core.data.database.entity.LabelEntity
import com.npplayer.core.data.database.entity.MusicLyricsEntity
import com.npplayer.core.data.database.entity.MusicPlayHistoryEntity
import com.npplayer.core.data.database.entity.MusicPlaylistEntity
import com.npplayer.core.data.database.entity.MusicPlaylistSongCrossRef
import com.npplayer.core.data.database.entity.MusicQueueStateEntity
import com.npplayer.core.data.database.entity.PlaylistEntity
import com.npplayer.core.data.database.entity.PlaylistItemEntity
import com.npplayer.core.data.database.entity.SongEntity
import com.npplayer.core.data.database.entity.StreamBookmarkEntity
import com.npplayer.core.data.database.entity.VaultVideoEntity
import com.npplayer.core.data.database.entity.VideoAudioPreferenceEntity
import com.npplayer.core.data.database.entity.VideoEntity
import com.npplayer.core.data.database.entity.VideoLabelCrossRef
import com.npplayer.core.data.database.entity.WatchHistoryEntity

@Database(
    entities = [
        VideoEntity::class,
        FolderEntity::class,
        WatchHistoryEntity::class,
        VaultVideoEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        StreamBookmarkEntity::class,
        DownloadedSubtitleEntity::class,
        LabelEntity::class,
        VideoLabelCrossRef::class,
        VideoAudioPreferenceEntity::class,
        SongEntity::class,
        MusicPlaylistEntity::class,
        MusicPlaylistSongCrossRef::class,
        FavoriteSongEntity::class,
        MusicPlayHistoryEntity::class,
        MusicQueueStateEntity::class,
        MusicLyricsEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class NPPlayerDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun folderDao(): FolderDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun vaultDao(): VaultDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun streamBookmarkDao(): StreamBookmarkDao
    abstract fun downloadedSubtitleDao(): DownloadedSubtitleDao
    abstract fun labelDao(): LabelDao
    abstract fun videoAudioPreferenceDao(): VideoAudioPreferenceDao
    abstract fun songDao(): SongDao
    abstract fun musicPlaylistDao(): MusicPlaylistDao
    abstract fun favoriteSongDao(): FavoriteSongDao
    abstract fun musicPlayHistoryDao(): MusicPlayHistoryDao
    abstract fun musicQueueDao(): MusicQueueDao
    abstract fun musicLyricsDao(): MusicLyricsDao
}
