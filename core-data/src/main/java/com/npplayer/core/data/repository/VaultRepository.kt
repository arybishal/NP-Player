package com.npplayer.core.data.repository

import android.content.Context
import com.npplayer.core.data.database.dao.VaultDao
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.entity.VaultVideoEntity
import com.npplayer.core.data.database.entity.VideoEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vaultDao: VaultDao,
    private val videoDao: VideoDao
) {
    private val vaultDir: File
        get() {
            val dir = File(context.filesDir, "vault")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val noMedia = File(dir, ".nomedia")
            if (!noMedia.exists()) {
                try {
                    noMedia.createNewFile()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return dir
        }

    val vaultVideos: Flow<List<VaultVideoEntity>> = vaultDao.getAllVaultVideos()
    val vaultCount: Flow<Int> = vaultDao.getVaultCount()

    suspend fun moveToVault(video: VideoEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val srcFile = File(video.path)
            if (!srcFile.exists()) return@withContext false

            val ext = srcFile.extension.ifEmpty { "mp4" }
            val id = UUID.randomUUID().toString()
            val destFile = File(vaultDir, "vault_$id.$ext")

            // Copy file to vault
            FileInputStream(srcFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val vaultEntity = VaultVideoEntity(
                id = id,
                originalPath = video.path,
                vaultPath = destFile.absolutePath,
                displayName = video.title.ifEmpty { srcFile.name },
                durationMs = video.durationMs,
                sizeBytes = destFile.length(),
                dateAddedMs = System.currentTimeMillis()
            )

            vaultDao.insert(vaultEntity)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restoreFromVault(vaultVideo: VaultVideoEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val srcFile = File(vaultVideo.vaultPath)
            if (!srcFile.exists()) {
                vaultDao.delete(vaultVideo)
                return@withContext false
            }

            val targetDir = File(vaultVideo.originalPath).parentFile ?: context.getExternalFilesDir(null)
            val destFile = File(targetDir, vaultVideo.displayName)

            FileInputStream(srcFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Remove from vault
            srcFile.delete()
            vaultDao.delete(vaultVideo)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteFromVault(vaultVideo: VaultVideoEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(vaultVideo.vaultPath)
            if (file.exists()) {
                file.delete()
            }
            vaultDao.delete(vaultVideo)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
