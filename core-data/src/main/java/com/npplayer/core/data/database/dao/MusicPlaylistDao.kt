package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.npplayer.core.data.database.entity.MusicPlaylistEntity
import com.npplayer.core.data.database.entity.MusicPlaylistSongCrossRef
import com.npplayer.core.data.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicPlaylistDao {

    @Query("SELECT * FROM music_playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<MusicPlaylistEntity>>

    @Query("SELECT * FROM music_playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistById(playlistId: Long): MusicPlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: MusicPlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: MusicPlaylistEntity)

    @Query("DELETE FROM music_playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("DELETE FROM music_playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylistSongs(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongs(refs: List<MusicPlaylistSongCrossRef>)

    @Query("DELETE FROM music_playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("SELECT s.* FROM songs s INNER JOIN music_playlist_songs r ON s.id = r.songId WHERE r.playlistId = :playlistId ORDER BY r.orderIndex ASC")
    fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM music_playlist_songs WHERE playlistId = :playlistId")
    fun getSongCountForPlaylist(playlistId: Long): Flow<Int>

    @Transaction
    suspend fun replacePlaylistTracks(playlistId: Long, songIds: List<Long>) {
        clearPlaylistSongs(playlistId)
        val refs = songIds.mapIndexed { index, songId ->
            MusicPlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                orderIndex = index
            )
        }
        insertPlaylistSongs(refs)
    }
}
