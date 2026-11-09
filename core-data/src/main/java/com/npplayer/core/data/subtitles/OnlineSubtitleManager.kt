package com.npplayer.core.data.subtitles

import android.content.Context
import com.npplayer.core.data.database.dao.DownloadedSubtitleDao
import com.npplayer.core.data.database.entity.DownloadedSubtitleEntity
import com.npplayer.core.data.subtitles.subdl.SubdlProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnlineSubtitleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val subdlProvider: SubdlProvider,
    private val downloadedSubtitleDao: DownloadedSubtitleDao
) {

    suspend fun searchSubtitles(
        videoPath: String,
        videoTitle: String,
        manualQuery: String = "",
        preferredLanguages: List<String> = listOf("en")
    ): Result<List<SubtitleItem>> = withContext(Dispatchers.IO) {
        val cleanInfo = SubtitleFilenameCleaner.clean(
            if (videoTitle.isNotBlank()) videoTitle else videoPath
        )
        val fileHash = SubtitleHasher.computeHash(videoPath)
        val file = File(videoPath)
        val fileSize = if (file.exists()) file.length() else 0L

        val query = SubtitleSearchQuery(
            cleanTitle = cleanInfo.cleanTitle,
            originalFileName = cleanInfo.originalFileName,
            queryText = manualQuery,
            season = cleanInfo.season,
            episode = cleanInfo.episode,
            year = cleanInfo.year,
            type = cleanInfo.type,
            languages = preferredLanguages,
            fileHash = fileHash,
            fileSize = fileSize
        )

        subdlProvider.search(query)
    }

    suspend fun downloadAndApplySubtitle(
        item: SubtitleItem,
        videoPath: String
    ): Result<File> = withContext(Dispatchers.IO) {
        val videoFile = File(videoPath)
        val baseName = if (videoFile.name.contains('.')) {
            videoFile.name.substringBeforeLast('.')
        } else {
            videoFile.name
        }

        // Target directory: try sibling folder if writable, else use app external storage
        val targetDir = if (videoFile.parentFile?.canWrite() == true) {
            videoFile.parentFile!!
        } else {
            val appSubDir = File(context.getExternalFilesDir("subtitles") ?: context.cacheDir, "downloaded")
            appSubDir.mkdirs()
            appSubDir
        }

        val downloadResult = subdlProvider.download(
            item = item,
            targetDirectory = targetDir,
            preferredBaseName = baseName
        )

        downloadResult.onSuccess { savedFile ->
            // Remember in Room database
            downloadedSubtitleDao.insertSubtitle(
                DownloadedSubtitleEntity(
                    videoPath = videoPath,
                    subtitlePath = savedFile.absolutePath,
                    language = item.language,
                    languageCode = item.languageCode,
                    releaseName = item.releaseName,
                    provider = item.provider
                )
            )
        }

        downloadResult
    }

    suspend fun getRememberedSubtitles(videoPath: String): List<DownloadedSubtitleEntity> {
        return withContext(Dispatchers.IO) {
            downloadedSubtitleDao.getSubtitlesForVideoOnce(videoPath)
        }
    }
}
