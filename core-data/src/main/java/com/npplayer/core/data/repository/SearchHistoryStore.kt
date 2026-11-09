package com.npplayer.core.data.repository

import kotlinx.coroutines.flow.Flow

interface SearchHistoryStore {
    val recentSearchesFlow: Flow<List<String>>
    suspend fun addRecentSearch(query: String)
    suspend fun removeRecentSearch(query: String)
    suspend fun clearRecentSearches()
}
