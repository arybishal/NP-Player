package com.npplayer.core.data.subtitles.subdl

import com.npplayer.core.data.subtitles.SubdlConfig
import com.npplayer.core.data.subtitles.SubtitleExtractor
import com.npplayer.core.data.subtitles.SubtitleItem
import com.npplayer.core.data.subtitles.SubtitleProvider
import com.npplayer.core.data.subtitles.SubtitleSearchQuery
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubdlProvider @Inject constructor() : SubtitleProvider {

    override val id: String = "subdl"
    override val displayName: String = "SubDL"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestWithUserAgent = original.newBuilder()
                .header("User-Agent", SubdlConfig.USER_AGENT)
                .build()
            chain.proceed(requestWithUserAgent)
        }
        .build()

    private val apiService: SubdlApiService = Retrofit.Builder()
        .baseUrl(SubdlConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(SubdlApiService::class.java)

    override suspend fun search(query: SubtitleSearchQuery): Result<List<SubtitleItem>> {
        return try {
            val languagesParam = if (query.languages.isNotEmpty()) {
                query.languages.joinToString(",").uppercase()
            } else "EN"

            var dtoList: List<SubdlSubtitleDto> = emptyList()

            // 1. If manual query is supplied, search by film_name with that query
            if (query.queryText.isNotBlank()) {
                val res = apiService.searchSubtitles(
                    apiKey = SubdlConfig.API_KEY,
                    filmName = query.queryText.trim(),
                    languages = languagesParam
                )
                if (res.isSuccessful) {
                    dtoList = res.body()?.subtitles ?: res.body()?.results ?: emptyList()
                } else if (res.code() == 429) {
                    return Result.failure(IOException("SubDL daily rate limit exceeded. Please try again later."))
                }
            }

            // 2. Structured search: film_name + season/episode + year
            if (dtoList.isEmpty() && query.cleanTitle.isNotBlank()) {
                val res = apiService.searchSubtitles(
                    apiKey = SubdlConfig.API_KEY,
                    filmName = query.cleanTitle,
                    seasonNumber = query.season,
                    episodeNumber = query.episode,
                    type = query.type,
                    year = query.year,
                    languages = languagesParam
                )
                if (res.isSuccessful) {
                    dtoList = res.body()?.subtitles ?: res.body()?.results ?: emptyList()
                } else if (res.code() == 429) {
                    return Result.failure(IOException("SubDL daily rate limit exceeded. Please try again later."))
                }
            }

            // 3. Fallback to file_name search
            if (dtoList.isEmpty() && query.originalFileName.isNotBlank()) {
                val res = apiService.searchSubtitles(
                    apiKey = SubdlConfig.API_KEY,
                    fileName = query.originalFileName,
                    languages = languagesParam
                )
                if (res.isSuccessful) {
                    dtoList = res.body()?.subtitles ?: res.body()?.results ?: emptyList()
                }
            }

            val items = dtoList.mapNotNull { dto ->
                val rawUrl = dto.url ?: return@mapNotNull null
                val releaseName = dto.releaseName ?: dto.name ?: "Subtitle"
                val lang = dto.lang ?: dto.language ?: "English"
                val langCode = dto.language ?: dto.lang?.take(2)?.lowercase() ?: "en"

                val score = computeMatchScore(query.originalFileName, releaseName)

                SubtitleItem(
                    id = rawUrl,
                    releaseName = releaseName,
                    language = lang,
                    languageCode = langCode,
                    author = dto.author ?: "SubDL Community",
                    downloadCount = dto.downloads ?: 0,
                    hearingImpaired = dto.hi == true,
                    downloadUrl = rawUrl,
                    matchScore = score,
                    provider = id
                )
            }.sortedByDescending { it.matchScore }

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun download(
        item: SubtitleItem,
        targetDirectory: File,
        preferredBaseName: String
    ): Result<File> {
        return try {
            val fullUrl = buildAndValidateDownloadUrl(item.downloadUrl)
                ?: return Result.failure(SecurityException("Untrusted download host for subtitle URL"))

            val request = Request.Builder()
                .url(fullUrl)
                .header("User-Agent", SubdlConfig.USER_AGENT)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                if (response.code == 429) {
                    return Result.failure(IOException("SubDL download limit reached (300/day). Please try later."))
                }
                return Result.failure(IOException("SubDL download failed with HTTP ${response.code}"))
            }

            val bodyBytes = response.body?.bytes()
                ?: return Result.failure(IOException("Empty response body from SubDL"))

            val destinationFileName = "${preferredBaseName}.${item.languageCode}.srt"
            val destinationFile = File(targetDirectory, destinationFileName)

            SubtitleExtractor.extractSubtitleFromZip(bodyBytes, destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validates and normalizes SubDL download URLs.
     * Enforces that the host is dl.subdl.com and rejects arbitrary third-party hosts.
     */
    fun buildAndValidateDownloadUrl(rawUrl: String): String? {
        val trimmed = rawUrl.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            val uri = URI.create(trimmed)
            if (uri.host.equals(SubdlConfig.DOWNLOAD_HOST, ignoreCase = true)) {
                return trimmed
            }
            return null // Reject non-dl.subdl.com hosts
        }
        val path = if (trimmed.startsWith("/")) trimmed else "/$trimmed"
        return "${SubdlConfig.DOWNLOAD_BASE_URL}$path"
    }

    private fun computeMatchScore(videoName: String, releaseName: String): Int {
        if (videoName.isBlank() || releaseName.isBlank()) return 0
        val vTokens = videoName.lowercase().split(Regex("[^a-z0-9]+")).filter { it.length >= 2 }.toSet()
        val rTokens = releaseName.lowercase().split(Regex("[^a-z0-9]+")).filter { it.length >= 2 }.toSet()
        if (vTokens.isEmpty() || rTokens.isEmpty()) return 0
        val intersection = vTokens.intersect(rTokens)
        val score = ((intersection.size.toFloat() / vTokens.size.toFloat()) * 100).toInt()
        return score.coerceIn(0, 100)
    }
}
