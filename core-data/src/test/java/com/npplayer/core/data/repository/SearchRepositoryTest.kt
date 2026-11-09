package com.npplayer.core.data.repository

import com.npplayer.core.data.database.dao.FolderDao
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.entity.FolderEntity
import com.npplayer.core.data.database.entity.VideoEntity
import com.npplayer.core.data.preferences.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class SearchRepositoryTest {

    private val sampleVideos = listOf(
        VideoEntity(
            id = 1L,
            title = "Dead-End.Job.S01E08.1080p.WEB-DL",
            path = "/storage/emulated/0/Movies/Dead-End.Job.S01E08.1080p.WEB-DL.mkv",
            folderPath = "/storage/emulated/0/Movies",
            folderName = "Movies",
            durationMs = 2219000L,
            sizeBytes = 1048576000L,
            width = 1920,
            height = 1080,
            dateModified = 1700000000L
        ),
        VideoEntity(
            id = 2L,
            title = "One Piece - Episode 1172 [1080p]",
            path = "/storage/emulated/0/Anime/One Piece - Episode 1172.mkv",
            folderPath = "/storage/emulated/0/Anime",
            folderName = "Anime",
            durationMs = 1420000L,
            sizeBytes = 450000000L,
            width = 1920,
            height = 1080,
            dateModified = 1700001000L
        ),
        VideoEntity(
            id = 3L,
            title = "Amélie.2001.Remastered.1080p",
            path = "/storage/emulated/0/Classics/Amélie.2001.Remastered.1080p.mp4",
            folderPath = "/storage/emulated/0/Classics",
            folderName = "Classics",
            durationMs = 7320000L,
            sizeBytes = 2500000000L,
            width = 1920,
            height = 1080,
            dateModified = 1700002000L
        )
    )

    private val sampleFolders = listOf(
        FolderEntity(
            path = "/storage/emulated/0/Movies",
            name = "Movies",
            videoCount = 1
        ),
        FolderEntity(
            path = "/storage/emulated/0/Anime",
            name = "Anime",
            videoCount = 1
        ),
        FolderEntity(
            path = "/storage/emulated/0/Classics",
            name = "Classics",
            videoCount = 1
        )
    )

    // Dynamic mock for VideoDao
    private val fakeVideoDao = Proxy.newProxyInstance(
        VideoDao::class.java.classLoader,
        arrayOf(VideoDao::class.java)
    ) { _, method, args ->
        when (method.name) {
            "getAllVideos" -> flowOf(sampleVideos)
            "getRecentlyPlayedVideos" -> flowOf(sampleVideos.take(2))
            else -> null
        }
    } as VideoDao

    // Dynamic mock for FolderDao
    private val fakeFolderDao = Proxy.newProxyInstance(
        FolderDao::class.java.classLoader,
        arrayOf(FolderDao::class.java)
    ) { _, method, _ ->
        when (method.name) {
            "getAllFolders" -> flowOf(sampleFolders)
            else -> null
        }
    } as FolderDao

    private val fakeSearchHistoryStore = object : SearchHistoryStore {
        val list = mutableListOf("Dead End", "Piece")
        override val recentSearchesFlow: Flow<List<String>> = flowOf(list)
        override suspend fun addRecentSearch(query: String) { list.add(0, query) }
        override suspend fun removeRecentSearch(query: String) { list.remove(query) }
        override suspend fun clearRecentSearches() { list.clear() }
    }

    @Test
    fun search_emptyQuery_returnsEmptyResult() = runTest {
        val repo = SearchRepository(fakeVideoDao, fakeFolderDao, fakeSearchHistoryStore)
        val result = repo.search("").first()
        assertEquals(0, result.totalMatches)
        assertTrue(result.matchingVideos.isEmpty())
        assertTrue(result.matchingFolders.isEmpty())
    }

    @Test
    fun search_multiWordMatching_findsCorrectVideosAndFolders() = runTest {
        val repo = SearchRepository(fakeVideoDao, fakeFolderDao, fakeSearchHistoryStore)

        // Multi-word search: "dead job"
        val deadJobResult = repo.search("dead job").first()
        assertEquals(1, deadJobResult.matchingVideos.size)
        assertEquals("Dead-End.Job.S01E08.1080p.WEB-DL", deadJobResult.matchingVideos[0].video.title)

        // Multi-word search: "piece 1172"
        val pieceResult = repo.search("piece 1172").first()
        assertEquals(1, pieceResult.matchingVideos.size)
        assertEquals("One Piece - Episode 1172 [1080p]", pieceResult.matchingVideos[0].video.title)
    }

    @Test
    fun search_accentInsensitive_findsAccentedTitleWithPlainText() = runTest {
        val repo = SearchRepository(fakeVideoDao, fakeFolderDao, fakeSearchHistoryStore)

        // User typed "amelie" without accent
        val result = repo.search("amelie").first()
        assertEquals(1, result.matchingVideos.size)
        assertEquals("Amélie.2001.Remastered.1080p", result.matchingVideos[0].video.title)
        assertTrue(result.matchingVideos[0].titleRanges.isNotEmpty())
    }

    @Test
    fun search_folderQuery_matchesFolder() = runTest {
        val repo = SearchRepository(fakeVideoDao, fakeFolderDao, fakeSearchHistoryStore)

        val result = repo.search("anime").first()
        // Matches folder "Anime"
        assertEquals(1, result.matchingFolders.size)
        assertEquals("Anime", result.matchingFolders[0].folder.name)
        // Also matches video inside folder Anime whose title has Anime in path/title
        assertTrue(result.matchingVideos.isNotEmpty())
    }
}
