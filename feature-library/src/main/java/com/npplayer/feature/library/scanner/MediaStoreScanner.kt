package com.npplayer.feature.library.scanner

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.npplayer.core.data.database.dao.FolderDao
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.entity.FolderEntity
import com.npplayer.core.data.database.entity.VideoEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val videoDao: VideoDao,
    private val folderDao: FolderDao
) {

    suspend fun scanMedia(newPeriodDays: Int = 7): ScanResult = withContext(Dispatchers.IO) {
        val resolver: ContentResolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATE_MODIFIED
        )

        val videos = mutableListOf<VideoEntity>()
        val folderMap = mutableMapOf<String, FolderAccumulator>()
        val nowSeconds = System.currentTimeMillis() / 1000
        val newThresholdSeconds = nowSeconds - TimeUnit.DAYS.toSeconds(newPeriodDays.toLong())

        resolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: "Video"
                val path = cursor.getString(dataCol) ?: continue
                val duration = cursor.getLong(durationCol)
                val size = cursor.getLong(sizeCol)
                val width = cursor.getInt(widthCol)
                val height = cursor.getInt(heightCol)
                val dateModified = cursor.getLong(dateModifiedCol)

                val file = File(path)
                val parentFile = file.parentFile
                val folderPath = parentFile?.absolutePath ?: "Unknown"
                val folderName = parentFile?.name ?: "Unknown"

                val isNew = dateModified >= newThresholdSeconds

                videos.add(
                    VideoEntity(
                        id = id,
                        title = title,
                        path = path,
                        folderPath = folderPath,
                        folderName = folderName,
                        durationMs = duration,
                        sizeBytes = size,
                        width = width,
                        height = height,
                        dateModified = dateModified
                    )
                )

                val acc = folderMap.getOrPut(folderPath) {
                    FolderAccumulator(
                        path = folderPath,
                        name = folderName,
                        lastModified = dateModified
                    )
                }
                acc.videoCount++
                acc.totalSizeBytes += size
                if (isNew) {
                    acc.newVideoCount++
                }
                if (dateModified > acc.lastModified) {
                    acc.lastModified = dateModified
                }
            }
        }

        val folderEntities = folderMap.values.map {
            FolderEntity(
                path = it.path,
                name = it.name,
                videoCount = it.videoCount,
                totalSizeBytes = it.totalSizeBytes,
                lastModified = it.lastModified,
                newVideoCount = it.newVideoCount
            )
        }

        videoDao.insertVideos(videos)
        folderDao.clearFolders()
        folderDao.insertFolders(folderEntities)

        ScanResult(
            totalVideos = videos.size,
            totalFolders = folderEntities.size
        )
    }

    private class FolderAccumulator(
        val path: String,
        val name: String,
        var videoCount: Int = 0,
        var totalSizeBytes: Long = 0L,
        var lastModified: Long = 0L,
        var newVideoCount: Int = 0
    )
}

data class ScanResult(
    val totalVideos: Int,
    val totalFolders: Int
)
