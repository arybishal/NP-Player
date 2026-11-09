package com.npplayer.feature.library.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.database.entity.MusicPlaylistEntity
import com.npplayer.core.data.database.entity.SongEntity
import com.npplayer.core.data.preferences.SettingsRepository
import com.npplayer.core.data.repository.AlbumSortOption
import com.npplayer.core.data.repository.ArtistSortOption
import com.npplayer.core.data.repository.FavoriteSortOption
import com.npplayer.core.data.repository.FolderSortOption
import com.npplayer.core.data.repository.GenreSortOption
import com.npplayer.core.data.repository.MusicAlbum
import com.npplayer.core.data.repository.MusicArtist
import com.npplayer.core.data.repository.MusicFolder
import com.npplayer.core.data.repository.MusicGenre
import com.npplayer.core.data.repository.MusicRepository
import com.npplayer.core.data.repository.MusicSearchResult
import com.npplayer.core.data.repository.PlaylistSortOption
import com.npplayer.core.data.repository.RecentlyAddedFilter
import com.npplayer.core.data.repository.SongSortOption
import com.npplayer.core.data.repository.SortDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MusicUiState(
    val activeTab: MusicTab = MusicTab.SONGS,
    val isGridView: Boolean = false,
    val isScanning: Boolean = false,
    val songs: List<SongEntity> = emptyList(),
    val albums: List<MusicAlbum> = emptyList(),
    val artists: List<MusicArtist> = emptyList(),
    val playlists: List<MusicPlaylistEntity> = emptyList(),
    val genres: List<MusicGenre> = emptyList(),
    val folders: List<MusicFolder> = emptyList(),
    val favorites: List<SongEntity> = emptyList(),
    val favoriteIds: Set<Long> = emptySet(),
    val recentlyAdded: List<SongEntity> = emptyList(),
    val recentlyAddedFilter: RecentlyAddedFilter = RecentlyAddedFilter.DAYS_30,
    val sortOption: String = "TITLE",
    val sortDirection: SortDirection = SortDirection.ASCENDING
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class MusicHomeViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository,
    val musicPlayerEngine: com.npplayer.player.music.MusicPlayerEngine,
    val audioFxManager: com.npplayer.player.audio.AudioFxManager,
    val lyricsRepository: com.npplayer.core.data.lyrics.LyricsRepository
) : ViewModel() {

    // Playback States forwarded from MusicPlayerEngine
    val currentSong = musicPlayerEngine.currentSong
    val isPlaying = musicPlayerEngine.isPlaying
    val currentPositionMs = musicPlayerEngine.currentPositionMs
    val durationMs = musicPlayerEngine.durationMs
    val bufferedPositionMs = musicPlayerEngine.bufferedPositionMs
    val currentQueue = musicPlayerEngine.currentQueue
    val currentIndex = musicPlayerEngine.currentIndex
    val shuffleMode = musicPlayerEngine.shuffleMode
    val repeatMode = musicPlayerEngine.repeatMode
    val isFavorite = musicPlayerEngine.isFavorite
    val playbackSpeed = musicPlayerEngine.playbackSpeed
    val sleepTimerRemainingMs = musicPlayerEngine.sleepTimerRemainingMs
    val sleepTimerOption = musicPlayerEngine.sleepTimerOption

    val seekBarStyle: StateFlow<com.npplayer.core.data.preferences.SeekBarStylePreference> =
        settingsRepository.playerPreferencesFlow
            .map { it.seekBarStyle }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.npplayer.core.data.preferences.SeekBarStylePreference.CLASSIC)

    private val _activeTab = MutableStateFlow(MusicTab.SONGS)
    val activeTab: StateFlow<MusicTab> = _activeTab.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _recentlyAddedFilter = MutableStateFlow(RecentlyAddedFilter.DAYS_30)
    val recentlyAddedFilter: StateFlow<RecentlyAddedFilter> = _recentlyAddedFilter.asStateFlow()

    // Sort states
    private val _songSortOption = MutableStateFlow(SongSortOption.TITLE)
    private val _songSortDir = MutableStateFlow(SortDirection.ASCENDING)

    private val _albumSortOption = MutableStateFlow(AlbumSortOption.NAME)
    private val _albumSortDir = MutableStateFlow(SortDirection.ASCENDING)

    private val _artistSortOption = MutableStateFlow(ArtistSortOption.NAME)
    private val _artistSortDir = MutableStateFlow(SortDirection.ASCENDING)

    private val _genreSortOption = MutableStateFlow(GenreSortOption.NAME)
    private val _genreSortDir = MutableStateFlow(SortDirection.ASCENDING)

    private val _playlistSortOption = MutableStateFlow(PlaylistSortOption.NAME)
    private val _playlistSortDir = MutableStateFlow(SortDirection.ASCENDING)

    private val _folderSortOption = MutableStateFlow(FolderSortOption.NAME)
    private val _folderSortDir = MutableStateFlow(SortDirection.ASCENDING)

    private val _favoriteSortOption = MutableStateFlow(FavoriteSortOption.DATE_LIKED)
    private val _favoriteSortDir = MutableStateFlow(SortDirection.ASCENDING)

    // Search state
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading.asStateFlow()

    val searchResult: StateFlow<MusicSearchResult?> = _searchQuery
        .debounce(200)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            _isSearchLoading.value = query.isNotBlank()
            musicRepository.searchMusic(query).map {
                _isSearchLoading.value = false
                it
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Data flows
    private val songsFlow = combine(_songSortOption, _songSortDir) { opt, dir -> opt to dir }
        .flatMapLatest { (opt, dir) -> musicRepository.getAllSongs(opt, dir) }

    private val albumsFlow = combine(_albumSortOption, _albumSortDir) { opt, dir -> opt to dir }
        .flatMapLatest { (opt, dir) -> musicRepository.getAlbums(opt, dir) }

    private val artistsFlow = combine(_artistSortOption, _artistSortDir) { opt, dir -> opt to dir }
        .flatMapLatest { (opt, dir) -> musicRepository.getArtists(opt, dir) }

    private val playlistsFlow = combine(_playlistSortOption, _playlistSortDir) { opt, dir -> opt to dir }
        .flatMapLatest { (opt, dir) -> musicRepository.getPlaylists(opt, dir) }

    private val genresFlow = combine(_genreSortOption, _genreSortDir) { opt, dir -> opt to dir }
        .flatMapLatest { (opt, dir) -> musicRepository.getGenres(opt, dir) }

    private val foldersFlow = combine(_folderSortOption, _folderSortDir) { opt, dir -> opt to dir }
        .flatMapLatest { (opt, dir) -> musicRepository.getFolders(opt, dir) }

    private val favoritesFlow = combine(_favoriteSortOption, _favoriteSortDir) { opt, dir -> opt to dir }
        .flatMapLatest { (opt, dir) -> musicRepository.getFavorites(opt, dir) }

    private val recentlyAddedFlow = combine(_recentlyAddedFilter, _songSortDir) { filter, dir -> filter to dir }
        .flatMapLatest { (filter, dir) -> musicRepository.getRecentlyAdded(filter, dir) }

    private val favoriteIdsFlow = musicRepository.getAllFavoriteIds()
        .map { it.toSet() }

    private data class PrimaryMusicData(
        val songs: List<SongEntity>,
        val albums: List<MusicAlbum>,
        val artists: List<MusicArtist>,
        val playlists: List<MusicPlaylistEntity>
    )

    private data class SecondaryMusicData(
        val genres: List<MusicGenre>,
        val folders: List<MusicFolder>,
        val favorites: List<SongEntity>,
        val favoriteIds: Set<Long>,
        val recentlyAdded: List<SongEntity>
    )

    private val primaryDataFlow = combine(
        songsFlow,
        albumsFlow,
        artistsFlow,
        playlistsFlow
    ) { songs, albums, artists, playlists ->
        PrimaryMusicData(songs, albums, artists, playlists)
    }

    private val secondaryDataFlow = combine(
        genresFlow,
        foldersFlow,
        favoritesFlow,
        favoriteIdsFlow,
        recentlyAddedFlow
    ) { genres, folders, favorites, favoriteIds, recentlyAdded ->
        SecondaryMusicData(genres, folders, favorites, favoriteIds, recentlyAdded)
    }

    val uiState: StateFlow<MusicUiState> = combine(
        _activeTab,
        _isGridView,
        musicRepository.isScanning,
        _recentlyAddedFilter,
        primaryDataFlow
    ) { tab, grid, scanning, recentFilter, primary ->
        val currentSort = getCurrentSortKey(tab)
        val currentDir = getCurrentSortDir(tab)
        MusicUiState(
            activeTab = tab,
            isGridView = grid,
            isScanning = scanning,
            songs = primary.songs,
            albums = primary.albums,
            artists = primary.artists,
            playlists = primary.playlists,
            recentlyAddedFilter = recentFilter,
            sortOption = currentSort,
            sortDirection = currentDir
        )
    }.combine(secondaryDataFlow) { state, secondary ->
        state.copy(
            genres = secondary.genres,
            folders = secondary.folders,
            favorites = secondary.favorites,
            favoriteIds = secondary.favoriteIds,
            recentlyAdded = secondary.recentlyAdded
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MusicUiState())

    init {
        // Load initial persisted tab & grid preference
        viewModelScope.launch {
            settingsRepository.musicSelectedTabFlow.collect { savedTabName ->
                try {
                    _activeTab.value = MusicTab.valueOf(savedTabName)
                } catch (_: Exception) {
                    _activeTab.value = MusicTab.SONGS
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.musicGridViewFlow.collect { isGrid ->
                _isGridView.value = isGrid
            }
        }
        // Run initial background scan
        refreshLibrary()
    }

    fun selectTab(tab: MusicTab) {
        _activeTab.value = tab
        viewModelScope.launch {
            settingsRepository.setMusicSelectedTab(tab.name)
        }
    }

    fun toggleGridView() {
        val next = !_isGridView.value
        _isGridView.value = next
        viewModelScope.launch {
            settingsRepository.setMusicGridView(next)
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            musicRepository.scanAudio()
        }
    }

    fun setSortOption(option: String) {
        when (_activeTab.value) {
            MusicTab.SONGS -> _songSortOption.value = SongSortOption.valueOf(option)
            MusicTab.ALBUMS -> _albumSortOption.value = AlbumSortOption.valueOf(option)
            MusicTab.ARTISTS -> _artistSortOption.value = ArtistSortOption.valueOf(option)
            MusicTab.GENRES -> _genreSortOption.value = GenreSortOption.valueOf(option)
            MusicTab.PLAYLISTS -> _playlistSortOption.value = PlaylistSortOption.valueOf(option)
            MusicTab.FOLDERS -> _folderSortOption.value = FolderSortOption.valueOf(option)
            MusicTab.FAVORITES -> _favoriteSortOption.value = FavoriteSortOption.valueOf(option)
            MusicTab.RECENTLY_ADDED -> {}
        }
        viewModelScope.launch {
            settingsRepository.setMusicSortOption(_activeTab.value.name, option)
        }
    }

    fun setSortDirection(direction: SortDirection) {
        val asc = direction == SortDirection.ASCENDING
        when (_activeTab.value) {
            MusicTab.SONGS -> _songSortDir.value = direction
            MusicTab.ALBUMS -> _albumSortDir.value = direction
            MusicTab.ARTISTS -> _artistSortDir.value = direction
            MusicTab.GENRES -> _genreSortDir.value = direction
            MusicTab.PLAYLISTS -> _playlistSortDir.value = direction
            MusicTab.FOLDERS -> _folderSortDir.value = direction
            MusicTab.FAVORITES -> _favoriteSortDir.value = direction
            MusicTab.RECENTLY_ADDED -> _songSortDir.value = direction
        }
        viewModelScope.launch {
            settingsRepository.setMusicSortDirection(_activeTab.value.name, asc)
        }
    }

    fun setRecentlyAddedFilter(filter: RecentlyAddedFilter) {
        _recentlyAddedFilter.value = filter
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(songId)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            musicRepository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.addSongsToPlaylist(playlistId, listOf(songId))
        }
    }

    fun startSearch() {
        _isSearching.value = true
    }

    fun stopSearch() {
        _isSearching.value = false
        _searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getSongsByAlbum(album: String) = musicRepository.getSongsByAlbum(album)
    fun getSongsByArtist(artist: String) = musicRepository.getSongsByArtist(artist)
    fun getSongsByGenre(genre: String) = musicRepository.getSongsByGenre(genre)
    fun getSongsByFolder(folderPath: String) = musicRepository.getSongsByFolder(folderPath)
    fun getSongsForPlaylist(playlistId: Long) = musicRepository.getSongsForPlaylist(playlistId)

    // --- Playback Control Delegations ---

    fun playSong(song: SongEntity, queue: List<SongEntity> = emptyList()) {
        musicPlayerEngine.playSong(song, queue)
    }

    fun playAll(songs: List<SongEntity>, startIndex: Int = 0) {
        musicPlayerEngine.playAll(songs, startIndex)
    }

    fun shuffleAll(songs: List<SongEntity>) {
        musicPlayerEngine.shuffleAll(songs)
    }

    fun playNext(song: SongEntity) {
        musicPlayerEngine.playNext(song)
    }

    fun addToQueue(song: SongEntity) {
        musicPlayerEngine.addToQueue(song)
    }

    fun addToQueue(songs: List<SongEntity>) {
        musicPlayerEngine.addToQueue(songs)
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        musicPlayerEngine.reorderQueue(fromIndex, toIndex)
    }

    fun removeFromQueue(index: Int) {
        musicPlayerEngine.removeFromQueue(index)
    }

    fun clearQueue() {
        musicPlayerEngine.clearQueue()
    }

    fun togglePlayPause() {
        musicPlayerEngine.togglePlayPause()
    }

    fun skipNext() {
        musicPlayerEngine.skipToNext()
    }

    fun skipPrevious() {
        musicPlayerEngine.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        musicPlayerEngine.seekTo(positionMs)
    }

    fun toggleShuffle() {
        musicPlayerEngine.toggleShuffle()
    }

    fun toggleRepeat() {
        musicPlayerEngine.toggleRepeat()
    }

    fun toggleFavoriteCurrentSong() {
        musicPlayerEngine.toggleFavoriteCurrentSong()
    }

    fun setPlaybackSpeed(speed: Float) {
        musicPlayerEngine.setPlaybackSpeed(speed)
    }

    fun setSleepTimer(option: com.npplayer.core.data.repository.SleepTimerOption) {
        musicPlayerEngine.setSleepTimer(option)
    }

    fun cancelSleepTimer() {
        musicPlayerEngine.cancelSleepTimer()
    }

    fun saveQueueAsPlaylist(name: String) {
        viewModelScope.launch {
            val songs = musicPlayerEngine.currentQueue.value
            if (songs.isNotEmpty()) {
                val playlistId = musicRepository.createPlaylist(name)
                musicRepository.addSongsToPlaylist(playlistId, songs.map { it.id })
            }
        }
    }

    fun hideSong(songId: Long) {
        viewModelScope.launch {
            musicRepository.hideSong(songId, true)
        }
    }

    fun deleteSong(songId: Long) {
        viewModelScope.launch {
            musicRepository.deleteSong(songId)
            musicPlayerEngine.removeSongFromQueue(songId)
        }
    }

    fun deleteSongs(songIds: List<Long>) {
        viewModelScope.launch {
            musicRepository.deleteSongs(songIds)
            for (id in songIds) {
                musicPlayerEngine.removeSongFromQueue(id)
            }
        }
    }

    fun reorderPlaylist(playlistId: Long, songIds: List<Long>) {
        viewModelScope.launch {
            musicRepository.reorderPlaylistTracks(playlistId, songIds)
        }
    }

    suspend fun exportPlaylistM3u(playlistId: Long): String? {
        return musicRepository.exportPlaylistToM3u(playlistId)
    }

    suspend fun importPlaylistM3u(name: String, content: String): Long {
        return musicRepository.importPlaylistFromM3u(name, content)
    }

    fun getRecentlyPlayedSongs(): kotlinx.coroutines.flow.Flow<List<SongEntity>> =
        musicRepository.getRecentlyPlayed()

    fun getMostPlayedSongs(): kotlinx.coroutines.flow.Flow<List<SongEntity>> =
        musicRepository.getMostPlayed()

    fun getNeverPlayedSongs(): kotlinx.coroutines.flow.Flow<List<SongEntity>> =
        musicRepository.getNeverPlayed()

    fun updateSong(song: SongEntity) {
        viewModelScope.launch {
            musicRepository.updateSong(song)
        }
    }

    private fun getCurrentSortKey(tab: MusicTab): String {
        return when (tab) {
            MusicTab.SONGS -> _songSortOption.value.name
            MusicTab.ALBUMS -> _albumSortOption.value.name
            MusicTab.ARTISTS -> _artistSortOption.value.name
            MusicTab.GENRES -> _genreSortOption.value.name
            MusicTab.PLAYLISTS -> _playlistSortOption.value.name
            MusicTab.FOLDERS -> _folderSortOption.value.name
            MusicTab.FAVORITES -> _favoriteSortOption.value.name
            MusicTab.RECENTLY_ADDED -> "DATE_ADDED"
        }
    }

    private fun getCurrentSortDir(tab: MusicTab): SortDirection {
        return when (tab) {
            MusicTab.SONGS -> _songSortDir.value
            MusicTab.ALBUMS -> _albumSortDir.value
            MusicTab.ARTISTS -> _artistSortDir.value
            MusicTab.GENRES -> _genreSortDir.value
            MusicTab.PLAYLISTS -> _playlistSortDir.value
            MusicTab.FOLDERS -> _folderSortDir.value
            MusicTab.FAVORITES -> _favoriteSortDir.value
            MusicTab.RECENTLY_ADDED -> _songSortDir.value
        }
    }
}
