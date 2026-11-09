package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.npplayer.core.data.database.entity.MusicLyricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicLyricsDao {

    @Query("SELECT * FROM music_lyrics WHERE songId = :songId LIMIT 1")
    fun getLyricsForSongFlow(songId: Long): Flow<MusicLyricsEntity?>

    @Query("SELECT * FROM music_lyrics WHERE songId = :songId LIMIT 1")
    suspend fun getLyricsForSong(songId: Long): MusicLyricsEntity?

    @Query("SELECT * FROM music_lyrics WHERE trackName = :trackName AND artistName = :artistName LIMIT 1")
    suspend fun getLyricsByTrackAndArtist(trackName: String, artistName: String): MusicLyricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLyrics(lyrics: MusicLyricsEntity)

    @Query("UPDATE music_lyrics SET offsetMs = :offsetMs WHERE songId = :songId")
    suspend fun updateOffset(songId: Long, offsetMs: Long)

    @Query("DELETE FROM music_lyrics WHERE songId = :songId")
    suspend fun deleteLyrics(songId: Long)

    @Query("DELETE FROM music_lyrics")
    suspend fun clearAllLyrics()
}
