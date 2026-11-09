package com.npplayer.feature.library.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import coil.ImageLoader
import com.npplayer.core.data.database.dao.FolderDao
import com.npplayer.core.data.database.dao.PlaylistDao
import com.npplayer.core.data.database.dao.VideoAudioPreferenceDao
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.entity.VideoEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileOperationsHelper @Inject constructor(
    private val videoDao: VideoDao,
    private val folderDao: FolderDao,
    private val playlistDao: PlaylistDao,
    private val videoAudioPreferenceDao: VideoAudioPreferenceDao
) {

    suspend fun copyVideos(
        videos: List<VideoEntity>,
        targetFolderPath: String,
        onProgress: (current: Int, total: Int, name: String) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        val targetDir = File(targetFolderPath)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        var copiedCount = 0
        val total = videos.size

        for ((index, video) in videos.withIndex()) {
            onProgress(index + 1, total, video.title)
            val srcFile = File(video.path)
            if (!srcFile.exists()) continue

            var destFile = File(targetDir, srcFile.name)
            if (destFile.exists()) {
                val base = srcFile.nameWithoutExtension
                val ext = srcFile.extension
                destFile = File(targetDir, "${base}_copy_${System.currentTimeMillis()}.$ext")
            }

            val tempFile = File(targetDir, "${destFile.name}.np_tmp_${System.currentTimeMillis()}")
            try {
                FileInputStream(srcFile).use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                        }
                        output.flush()
                    }
                }

                if (!tempFile.renameTo(destFile)) {
                    tempFile.delete()
                    continue
                }

                val newVideo = video.copy(
                    id = System.currentTimeMillis() + index,
                    path = destFile.absolutePath,
                    folderPath = targetDir.absolutePath,
                    folderName = targetDir.name,
                    sizeBytes = destFile.length(),
                    dateModified = System.currentTimeMillis() / 1000
                )
                videoDao.insertVideos(listOf(newVideo))
                copiedCount++
            } catch (e: Exception) {
                if (tempFile.exists()) tempFile.delete()
                if (e is CancellationException) throw e
            }
        }
        Result.success(copiedCount)
    }

    suspend fun moveVideos(
        videos: List<VideoEntity>,
        targetFolderPath: String,
        onProgress: (current: Int, total: Int, name: String) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        val targetDir = File(targetFolderPath)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        var movedCount = 0
        val total = videos.size

        for ((index, video) in videos.withIndex()) {
            onProgress(index + 1, total, video.title)
            val srcFile = File(video.path)
            if (!srcFile.exists()) continue

            var destFile = File(targetDir, srcFile.name)
            if (destFile.exists()) {
                val base = srcFile.nameWithoutExtension
                val ext = srcFile.extension
                destFile = File(targetDir, "${base}_moved_${System.currentTimeMillis()}.$ext")
            }

            val moved = if (srcFile.renameTo(destFile)) {
                true
            } else {
                // Cross-filesystem fallback
                val tempFile = File(targetDir, "${destFile.name}.np_tmp_${System.currentTimeMillis()}")
                try {
                    FileInputStream(srcFile).use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (tempFile.renameTo(destFile)) {
                        srcFile.delete()
                        true
                    } else {
                        tempFile.delete()
                        false
                    }
                } catch (e: Exception) {
                    if (tempFile.exists()) tempFile.delete()
                    false
                }
            }

            if (moved) {
                // Update audio preference mapping
                videoAudioPreferenceDao.updatePath(video.path, destFile.absolutePath, targetDir.absolutePath)

                val updatedVideo = video.copy(
                    path = destFile.absolutePath,
                    folderPath = targetDir.absolutePath,
                    folderName = targetDir.name,
                    dateModified = System.currentTimeMillis() / 1000
                )
                videoDao.updateVideo(updatedVideo)
                movedCount++
            }
        }
        Result.success(movedCount)
    }

    suspend fun renameVideo(
        video: VideoEntity,
        newNameWithoutExtension: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val srcFile = File(video.path)
        if (!srcFile.exists()) return@withContext Result.failure(IOException("File not found"))

        val ext = srcFile.extension
        val parent = srcFile.parentFile ?: return@withContext Result.failure(IOException("Parent folder invalid"))
        val destFile = File(parent, "$newNameWithoutExtension.$ext")

        if (destFile.exists() && destFile.absolutePath != srcFile.absolutePath) {
            return@withContext Result.failure(IOException("A file with this name already exists"))
        }

        if (srcFile.renameTo(destFile)) {
            // Also rename matching subtitle files (.srt, .vtt, etc.)
            val oldBase = srcFile.nameWithoutExtension
            parent.listFiles { f -> f.isFile && f.name.startsWith(oldBase) && f.name != destFile.name }?.forEach { subFile ->
                val subExt = subFile.extension
                val newSubFile = File(parent, "$newNameWithoutExtension.$subExt")
                if (!newSubFile.exists()) {
                    subFile.renameTo(newSubFile)
                }
            }

            videoAudioPreferenceDao.updatePath(video.path, destFile.absolutePath, parent.absolutePath)

            val updatedVideo = video.copy(
                title = newNameWithoutExtension,
                path = destFile.absolutePath,
                dateModified = System.currentTimeMillis() / 1000
            )
            videoDao.updateVideo(updatedVideo)
            Result.success(destFile.absolutePath)
        } else {
            Result.failure(IOException("Could not rename file"))
        }
    }

    suspend fun batchRenameVideos(
        videos: List<VideoEntity>,
        pattern: String,
        startNumber: Int
    ): Result<Int> = withContext(Dispatchers.IO) {
        var count = 0
        for ((idx, video) in videos.withIndex()) {
            val num = startNumber + idx
            val newBase = pattern.replace("{n}", "$num")
            val res = renameVideo(video, newBase)
            if (res.isSuccess) count++
        }
        Result.success(count)
    }

    suspend fun deleteVideos(
        videos: List<VideoEntity>,
        deleteMatchingSubs: Boolean
    ): Result<Int> = withContext(Dispatchers.IO) {
        var deletedCount = 0
        for (video in videos) {
            val file = File(video.path)
            if (deleteMatchingSubs) {
                val parent = file.parentFile
                val base = file.nameWithoutExtension
                parent?.listFiles { f ->
                    f.isFile && f.name.startsWith(base) && (f.name.endsWith(".srt") || f.name.endsWith(".vtt") || f.name.endsWith(".ass"))
                }?.forEach { it.delete() }
            }

            if (file.exists()) {
                file.delete()
            }

            videoDao.deleteVideoById(video.id)
            videoAudioPreferenceDao.deletePreferenceForPath(video.path)
            deletedCount++
        }
        Result.success(deletedCount)
    }

    fun shareVideos(context: Context, videos: List<VideoEntity>) {
        Companion.shareVideos(context, videos)
    }

    companion object {
        fun shareVideos(context: Context, videos: List<VideoEntity>) {
            if (videos.isEmpty()) return
            val totalBytes = videos.sumOf { it.sizeBytes }
            if (totalBytes > 1024L * 1024L * 1024L) {
                Toast.makeText(context, "Note: Sharing > 1 GB of video files", Toast.LENGTH_SHORT).show()
            }

            try {
                val uris = ArrayList<Uri>()
                for (video in videos) {
                    val file = File(video.path)
                    if (file.exists()) {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        uris.add(uri)
                    }
                }

                if (uris.isEmpty()) {
                    Toast.makeText(context, "No accessible video files to share", Toast.LENGTH_SHORT).show()
                    return
                }

                val intent = if (uris.size == 1) {
                    Intent(Intent.ACTION_SEND).apply {
                        type = "video/*"
                        putExtra(Intent.EXTRA_STREAM, uris.first())
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                } else {
                    Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = "video/*"
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }

                context.startActivity(Intent.createChooser(intent, "Share Video${if (uris.size > 1) "s" else ""}"))
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to share: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun rebuildThumbnail(context: Context, videos: List<VideoEntity>) = withContext(Dispatchers.IO) {
        val imageLoader = ImageLoader(context)
        imageLoader.diskCache?.clear()
        imageLoader.memoryCache?.clear()
    }
}
