package com.npplayer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.npplayer.core.data.database.entity.FavoriteSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteSongDao {

    @Query("SELECT songId FROM music_favorites")
    fun getAllFavoriteSongIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM music_favorites WHERE songId = :songId)")
    fun isFavoriteFlow(songId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM music_favorites WHERE songId = :songId)")
    suspend fun isFavorite(songId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteSongEntity)

    @Query("DELETE FROM music_favorites WHERE songId = :songId")
    suspend fun deleteFavorite(songId: Long)
}
