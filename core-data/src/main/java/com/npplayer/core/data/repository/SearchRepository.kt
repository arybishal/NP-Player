package com.npplayer.core.data.repository

import com.npplayer.core.data.database.dao.FolderDao
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.entity.FolderEntity
import com.npplayer.core.data.database.entity.VideoEntity
import com.npplayer.core.data.preferences.SettingsRepository
import com.npplayer.core.data.search.SearchQueryNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

data class FolderSearchResult(
    val folder: FolderEntity,
    val nameRanges: List<IntRange>
)

data class VideoSearchResult(
    val video: VideoEntity,
    val titleRanges: List<IntRange>,
    val folderRanges: List<IntRange>
)

data class SearchResult(
    val query: String,
    val tokens: List<String>,
    val matchingFolders: List<FolderSearchResult>,
    val matchingVideos: List<VideoSearchResult>,
    val totalMatches: Int
) {
    companion object {
        val EMPTY = SearchResult(
            query = "",
            tokens = emptyList(),
            matchingFolders = emptyList(),
            matchingVideos = emptyList(),
            totalMatches = 0
        )
    }
}

@Singleton
class SearchRepository @Inject constructor(
    private val videoDao: VideoDao,
    private val folderDao: FolderDao,
    private val searchHistoryStore: SearchHistoryStore
) {

    val recentSearches: Flow<List<String>> = searchHistoryStore.recentSearchesFlow

    val recentlyPlayedVideos: Flow<List<VideoEntity>> = videoDao.getRecentlyPlayedVideos(10)

    suspend fun addRecentSearch(query: String) {
        searchHistoryStore.addRecentSearch(query)
    }

    suspend fun removeRecentSearch(query: String) {
        searchHistoryStore.removeRecentSearch(query)
    }

    suspend fun clearRecentSearches() {
        searchHistoryStore.clearRecentSearches()
    }

    fun search(query: String): Flow<SearchResult> {
        val trimmed = query.trim()
        val tokens = SearchQueryNormalizer.extractTokens(trimmed)
        if (tokens.isEmpty()) {
            return kotlinx.coroutines.flow.flowOf(SearchResult.EMPTY)
        }

        return combine(
            videoDao.getAllVideos(),
            folderDao.getAllFolders()
        ) { videos, folders ->
            // Filter Folders
            val matchingFolders = folders.mapNotNull { folder ->
                if (SearchQueryNormalizer.matchesAllTokens(folder.name, tokens)) {
                    val ranges = SearchQueryNormalizer.findMatchRanges(folder.name, tokens)
                    FolderSearchResult(folder, ranges)
                } else {
                    null
                }
            }

            // Filter Videos
            val matchingVideos = videos.mapNotNull { video ->
                val titleMatches = SearchQueryNormalizer.matchesAllTokens(video.title, tokens)
                val folderMatches = SearchQueryNormalizer.matchesAllTokens(video.folderName, tokens)
                val pathMatches = SearchQueryNormalizer.matchesAllTokens(video.path, tokens)

                if (titleMatches || folderMatches || pathMatches) {
                    val titleRanges = SearchQueryNormalizer.findMatchRanges(video.title, tokens)
                    val folderRanges = SearchQueryNormalizer.findMatchRanges(video.folderName, tokens)
                    VideoSearchResult(video, titleRanges, folderRanges)
                } else {
                    null
                }
            }

            SearchResult(
                query = trimmed,
                tokens = tokens,
                matchingFolders = matchingFolders,
                matchingVideos = matchingVideos,
                totalMatches = matchingFolders.size + matchingVideos.size
            )
        }.flowOn(Dispatchers.IO)
    }
}
