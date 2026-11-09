package com.npplayer.feature.library.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.database.entity.VideoEntity
import com.npplayer.core.data.repository.SearchRepository
import com.npplayer.core.data.repository.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val recentSearches: StateFlow<List<String>> = searchRepository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedVideos: StateFlow<List<VideoEntity>> = searchRepository.recentlyPlayedVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResult: StateFlow<SearchResult> = _query
        .debounce(200)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            if (q.isBlank()) {
                _isSearching.value = false
                flowOf(SearchResult.EMPTY)
            } else {
                _isSearching.value = true
                searchRepository.search(q)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResult.EMPTY)

    fun setQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
        _isSearching.value = false
    }

    fun onSearchSubmitted(submittedQuery: String) {
        val trimmed = submittedQuery.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch {
                searchRepository.addRecentSearch(trimmed)
            }
        }
    }

    fun removeRecentSearch(item: String) {
        viewModelScope.launch {
            searchRepository.removeRecentSearch(item)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            searchRepository.clearRecentSearches()
        }
    }
}
