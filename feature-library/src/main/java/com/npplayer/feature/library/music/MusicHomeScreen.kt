package com.npplayer.feature.library.music

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.npplayer.core.data.database.entity.MusicPlaylistEntity
import com.npplayer.core.data.database.entity.SongEntity
import com.npplayer.core.data.repository.MusicAlbum
import com.npplayer.core.data.repository.MusicArtist
import com.npplayer.core.data.repository.MusicFolder
import com.npplayer.core.data.repository.MusicGenre
import com.npplayer.core.data.repository.RecentlyAddedFilter
import com.npplayer.core.data.repository.SortDirection
import com.npplayer.core.ui.component.MusicArtwork
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.core.ui.util.FormatUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicHomeScreen(
    viewModel: MusicHomeViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResult by viewModel.searchResult.collectAsState()
    val isSearchLoading by viewModel.isSearchLoading.collectAsState()

    // Playback States from ViewModel
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val bufferedPositionMs by viewModel.bufferedPositionMs.collectAsState()
    val currentQueue by viewModel.currentQueue.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val shuffleMode by viewModel.shuffleMode.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isSongFavorite by viewModel.isFavorite.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val sleepTimerRemainingMs by viewModel.sleepTimerRemainingMs.collectAsState()
    val sleepTimerOption by viewModel.sleepTimerOption.collectAsState()
    val seekBarStyle by viewModel.seekBarStyle.collectAsState()

    // Sheet / Dialog states
    var showNowPlayingSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var selectedAlbumForDetail by remember { mutableStateOf<MusicAlbum?>(null) }
    var selectedArtistForDetail by remember { mutableStateOf<MusicArtist?>(null) }
    var selectedGenreForDetail by remember { mutableStateOf<MusicGenre?>(null) }
    var selectedFolderForDetail by remember { mutableStateOf<MusicFolder?>(null) }
    var selectedPlaylistForDetail by remember { mutableStateOf<MusicPlaylistEntity?>(null) }
    var selectedSongForMenu by remember { mutableStateOf<SongEntity?>(null) }
    var selectedSongForDetails by remember { mutableStateOf<SongEntity?>(null) }
    var songForAddToPlaylist by remember { mutableStateOf<SongEntity?>(null) }
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    val haptic = LocalHapticFeedback.current
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedSongIds = remember { mutableStateListOf<Long>() }
    var songForTagEdit by remember { mutableStateOf<SongEntity?>(null) }
    var songToDelete by remember { mutableStateOf<SongEntity?>(null) }
    var smartPlaylistType by remember { mutableStateOf<String?>(null) }
    val tagEditorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val importM3uLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                val displayName = uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.') ?: "Imported Playlist"
                coroutineScope.launch {
                    val id = viewModel.importPlaylistM3u(displayName, content)
                    Toast.makeText(context, "Imported playlist: $displayName", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error importing: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val nowPlayingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sortSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val albumSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val artistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val genericSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val detailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val playlistPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val songsListState = rememberLazyListState()
    val albumsGridState = rememberLazyGridState()

    fun playSong(song: SongEntity, queue: List<SongEntity> = uiState.songs) {
        viewModel.playSong(song, queue)
    }

    fun playAll(songs: List<SongEntity>, startIndex: Int = 0) {
        if (songs.isNotEmpty()) {
            viewModel.playAll(songs, startIndex)
        }
    }

    fun shuffleAll(songs: List<SongEntity>) {
        if (songs.isNotEmpty()) {
            viewModel.shuffleAll(songs)
        }
    }

    if (isSearching) {
        MusicSearchScreen(
            searchQuery = searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            searchResult = searchResult,
            isLoading = isSearchLoading,
            onBack = { viewModel.stopSearch() },
            onSongClick = { playSong(it, searchResult?.songs ?: listOf(it)) },
            onAlbumClick = {
                viewModel.stopSearch()
                selectedAlbumForDetail = it
            },
            onArtistClick = {
                viewModel.stopSearch()
                selectedArtistForDetail = it
            },
            onPlaylistClick = {
                viewModel.stopSearch()
                selectedPlaylistForDetail = it
            }
        )
        return
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = "${selectedSongIds.size} Selected",
                            color = NPWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedSongIds.clear()
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close selection",
                                tint = NPWhite
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val selectedSongs = uiState.songs.filter { selectedSongIds.contains(it.id) }
                                if (selectedSongs.isNotEmpty()) {
                                    playAll(selectedSongs)
                                    isSelectionMode = false
                                    selectedSongIds.clear()
                                }
                            },
                            enabled = selectedSongIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = "Play Selected",
                                tint = if (selectedSongIds.isNotEmpty()) NPYellow else NPWhite.copy(alpha = 0.3f)
                            )
                        }
                        IconButton(
                            onClick = {
                                if (selectedSongIds.size == uiState.songs.size) {
                                    selectedSongIds.clear()
                                } else {
                                    selectedSongIds.clear()
                                    selectedSongIds.addAll(uiState.songs.map { it.id })
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SelectAll,
                                contentDescription = "Select All",
                                tint = NPWhite
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NPBlack,
                        titleContentColor = NPWhite
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = "Music",
                            color = NPWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back to Videos",
                                tint = NPWhite
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.startSearch() }) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search Music",
                                tint = NPWhite
                            )
                        }
                        IconButton(onClick = { viewModel.toggleGridView() }) {
                            Icon(
                                imageVector = if (uiState.isGridView) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                                contentDescription = "Toggle Grid/List",
                                tint = NPWhite
                            )
                        }
                        IconButton(onClick = { showSortSheet = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Sort,
                                contentDescription = "Sort",
                                tint = NPYellow
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NPBlack,
                        titleContentColor = NPWhite
                    )
                )
            }
        },
        bottomBar = {
            if (isSelectionMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = NPBlack,
                    border = BorderStroke(1.dp, NPWhite.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SelectionBarButton(
                            icon = Icons.AutoMirrored.Outlined.QueueMusic,
                            label = "Queue",
                            onClick = {
                                val selectedSongs = uiState.songs.filter { selectedSongIds.contains(it.id) }
                                for (song in selectedSongs) {
                                    viewModel.addToQueue(song)
                                }
                                Toast.makeText(context, "Added ${selectedSongs.size} to queue", Toast.LENGTH_SHORT).show()
                                isSelectionMode = false
                                selectedSongIds.clear()
                            }
                        )
                        SelectionBarButton(
                            icon = Icons.AutoMirrored.Outlined.PlaylistAdd,
                            label = "Playlist",
                            onClick = {
                                val firstSelected = uiState.songs.firstOrNull { selectedSongIds.contains(it.id) }
                                if (firstSelected != null) {
                                    songForAddToPlaylist = firstSelected
                                }
                            }
                        )
                        SelectionBarButton(
                            icon = Icons.Filled.Favorite,
                            label = "Favorite",
                            onClick = {
                                for (id in selectedSongIds) {
                                    viewModel.toggleFavorite(id)
                                }
                                Toast.makeText(context, "Favorites updated", Toast.LENGTH_SHORT).show()
                                isSelectionMode = false
                                selectedSongIds.clear()
                            }
                        )
                        SelectionBarButton(
                            icon = Icons.Outlined.Share,
                            label = "Share",
                            onClick = {
                                val selectedSongs = uiState.songs.filter { selectedSongIds.contains(it.id) }
                                if (selectedSongs.isNotEmpty()) {
                                    val uris = ArrayList(selectedSongs.map { Uri.parse(it.uri) })
                                    val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                        type = "audio/*"
                                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share ${selectedSongs.size} Songs"))
                                }
                                isSelectionMode = false
                                selectedSongIds.clear()
                            }
                        )
                        SelectionBarButton(
                            icon = Icons.Outlined.Delete,
                            label = "Delete",
                            onClick = {
                                val idsToDelete = selectedSongIds.toList()
                                viewModel.deleteSongs(idsToDelete)
                                Toast.makeText(context, "Deleted ${idsToDelete.size} songs", Toast.LENGTH_SHORT).show()
                                isSelectionMode = false
                                selectedSongIds.clear()
                            }
                        )
                    }
                }
            } else if (currentSong != null) {
                MusicMiniPlayer(
                    song = currentSong,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSkipNext = { viewModel.skipNext() },
                    onSkipPrevious = { viewModel.skipPrevious() },
                    onOpenNowPlaying = { showNowPlayingSheet = true }
                )
            }
        },
        containerColor = NPBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable Tabs Row (8 tabs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MusicTab.entries.forEach { tab ->
                    val isSelected = tab == uiState.activeTab
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) NPYellow else NPWhite.copy(alpha = 0.08f))
                            .clickable { viewModel.selectTab(tab) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) NPBlack else NPWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.label,
                            color = if (isSelected) NPBlack else NPWhite,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content Area based on selected Tab
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isScanning && uiState.songs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NPYellow)
                    }
                } else {
                    when (uiState.activeTab) {
                        MusicTab.SONGS -> {
                            SongsTabContent(
                                songs = uiState.songs,
                                favoriteIds = uiState.favoriteIds,
                                isGridView = uiState.isGridView,
                                isSelectionMode = isSelectionMode,
                                selectedSongIds = selectedSongIds.toSet(),
                                onToggleSongSelection = { songId ->
                                    if (selectedSongIds.contains(songId)) {
                                        selectedSongIds.remove(songId)
                                        if (selectedSongIds.isEmpty()) isSelectionMode = false
                                    } else {
                                        selectedSongIds.add(songId)
                                    }
                                },
                                onSongLongClick = { songId ->
                                    if (!isSelectionMode) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isSelectionMode = true
                                        selectedSongIds.clear()
                                        selectedSongIds.add(songId)
                                    }
                                },
                                onPlaySong = { playSong(it) },
                                onPlayAll = { playAll(uiState.songs) },
                                onShuffleAll = { shuffleAll(uiState.songs) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onSongMenuClick = { selectedSongForMenu = it },
                                listState = songsListState,
                                onLetterSelected = { letter ->
                                    val index = uiState.songs.indexOfFirst {
                                        if (letter == "#") !it.title.first().isLetter()
                                        else it.title.startsWith(letter, ignoreCase = true)
                                    }
                                    if (index >= 0) {
                                        coroutineScope.launch { songsListState.scrollToItem(index) }
                                    }
                                }
                            )
                        }
                        MusicTab.ALBUMS -> {
                            AlbumsTabContent(
                                albums = uiState.albums,
                                isGridView = uiState.isGridView,
                                onAlbumClick = { selectedAlbumForDetail = it },
                                gridState = albumsGridState,
                                onLetterSelected = { letter ->
                                    val index = uiState.albums.indexOfFirst {
                                        if (letter == "#") !it.name.first().isLetter()
                                        else it.name.startsWith(letter, ignoreCase = true)
                                    }
                                    if (index >= 0) {
                                        coroutineScope.launch { albumsGridState.scrollToItem(index) }
                                    }
                                }
                            )
                        }
                        MusicTab.ARTISTS -> {
                            ArtistsTabContent(
                                artists = uiState.artists,
                                isGridView = uiState.isGridView,
                                onArtistClick = { selectedArtistForDetail = it }
                            )
                        }
                        MusicTab.PLAYLISTS -> {
                            PlaylistsTabContent(
                                playlists = uiState.playlists,
                                favoritesCount = uiState.favorites.size,
                                recentlyAddedCount = uiState.recentlyAdded.size,
                                onSmartPlaylistClick = { type -> smartPlaylistType = type },
                                onCreatePlaylistClick = { showNewPlaylistDialog = true },
                                onPlaylistClick = { selectedPlaylistForDetail = it },
                                onDeletePlaylist = { viewModel.deletePlaylist(it.id) },
                                onExportPlaylist = { playlist ->
                                    coroutineScope.launch {
                                        val content = viewModel.exportPlaylistM3u(playlist.id)
                                        if (content != null) {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, "${playlist.name}.m3u")
                                                putExtra(Intent.EXTRA_TEXT, content)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Export ${playlist.name}.m3u"))
                                        } else {
                                            Toast.makeText(context, "No songs in playlist to export", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onImportPlaylistClick = {
                                    importM3uLauncher.launch(arrayOf("*/*"))
                                }
                            )
                        }
                        MusicTab.GENRES -> {
                            GenresTabContent(
                                genres = uiState.genres,
                                onGenreClick = { selectedGenreForDetail = it }
                            )
                        }
                        MusicTab.FOLDERS -> {
                            FoldersTabContent(
                                folders = uiState.folders,
                                onFolderClick = { selectedFolderForDetail = it }
                            )
                        }
                        MusicTab.FAVORITES -> {
                            FavoritesTabContent(
                                favorites = uiState.favorites,
                                onPlaySong = { playSong(it, uiState.favorites) },
                                onPlayAll = { playAll(uiState.favorites) },
                                onShuffleAll = { shuffleAll(uiState.favorites) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onSongMenuClick = { selectedSongForMenu = it }
                            )
                        }
                        MusicTab.RECENTLY_ADDED -> {
                            RecentlyAddedTabContent(
                                songs = uiState.recentlyAdded,
                                activeFilter = uiState.recentlyAddedFilter,
                                favoriteIds = uiState.favoriteIds,
                                onFilterSelected = { viewModel.setRecentlyAddedFilter(it) },
                                onPlaySong = { playSong(it, uiState.recentlyAdded) },
                                onPlayAll = { playAll(uiState.recentlyAdded) },
                                onShuffleAll = { shuffleAll(uiState.recentlyAdded) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onSongMenuClick = { selectedSongForMenu = it }
                            )
                        }
                    }
                }
            }
        }
    }

    // Sort Bottom Sheet
    if (showSortSheet) {
        MusicSortBottomSheet(
            sheetState = sortSheetState,
            tab = uiState.activeTab,
            selectedOption = uiState.sortOption,
            sortDirection = uiState.sortDirection,
            onOptionSelected = { viewModel.setSortOption(it) },
            onDirectionChanged = { viewModel.setSortDirection(it) },
            onDismiss = { showSortSheet = false }
        )
    }

    // Album Detail Sheet
    selectedAlbumForDetail?.let { album ->
        val albumSongs by viewModel.getSongsByAlbum(album.name).collectAsState(initial = emptyList())
        MusicAlbumDetailSheet(
            sheetState = albumSheetState,
            album = album,
            songs = albumSongs,
            favoriteIds = uiState.favoriteIds,
            onPlaySong = { playSong(it, albumSongs) },
            onPlayAll = { playAll(albumSongs) },
            onShuffleAll = { shuffleAll(albumSongs) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onSongMenuClick = { selectedSongForMenu = it },
            onDismiss = { selectedAlbumForDetail = null }
        )
    }

    // Artist Detail Sheet
    selectedArtistForDetail?.let { artist ->
        val artistSongs by viewModel.getSongsByArtist(artist.name).collectAsState(initial = emptyList())
        val artistAlbums = remember(artistSongs) {
            artistSongs.groupBy { it.album }.map { (albName, albSongs) ->
                MusicAlbum(
                    name = albName,
                    artist = artist.name,
                    year = albSongs.maxOfOrNull { it.year } ?: 0,
                    trackCount = albSongs.size,
                    albumId = albSongs.first().albumId,
                    dateAdded = albSongs.maxOfOrNull { it.dateAdded } ?: 0L
                )
            }
        }
        MusicArtistDetailSheet(
            sheetState = artistSheetState,
            artist = artist,
            albums = artistAlbums,
            songs = artistSongs,
            favoriteIds = uiState.favoriteIds,
            onAlbumClick = { selectedAlbumForDetail = it },
            onPlaySong = { playSong(it, artistSongs) },
            onPlayAll = { playAll(artistSongs) },
            onShuffleAll = { shuffleAll(artistSongs) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onSongMenuClick = { selectedSongForMenu = it },
            onDismiss = { selectedArtistForDetail = null }
        )
    }

    // Genre Detail Sheet
    selectedGenreForDetail?.let { genre ->
        val genreSongs by viewModel.getSongsByGenre(genre.name).collectAsState(initial = emptyList())
        MusicDetailSheet(
            sheetState = genericSheetState,
            title = genre.name,
            subtitle = "Genre",
            icon = Icons.Outlined.Category,
            songs = genreSongs,
            favoriteIds = uiState.favoriteIds,
            onPlaySong = { playSong(it, genreSongs) },
            onPlayAll = { playAll(genreSongs) },
            onShuffleAll = { shuffleAll(genreSongs) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onSongMenuClick = { selectedSongForMenu = it },
            onDismiss = { selectedGenreForDetail = null }
        )
    }

    // Folder Detail Sheet
    selectedFolderForDetail?.let { folder ->
        val folderSongs by viewModel.getSongsByFolder(folder.path).collectAsState(initial = emptyList())
        MusicDetailSheet(
            sheetState = genericSheetState,
            title = folder.name,
            subtitle = folder.path,
            icon = Icons.Outlined.Folder,
            songs = folderSongs,
            favoriteIds = uiState.favoriteIds,
            onPlaySong = { playSong(it, folderSongs) },
            onPlayAll = { playAll(folderSongs) },
            onShuffleAll = { shuffleAll(folderSongs) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onSongMenuClick = { selectedSongForMenu = it },
            onDismiss = { selectedFolderForDetail = null }
        )
    }

    // Playlist Detail Sheet
    selectedPlaylistForDetail?.let { playlist ->
        val playlistSongs by viewModel.getSongsForPlaylist(playlist.id).collectAsState(initial = emptyList())
        MusicDetailSheet(
            sheetState = genericSheetState,
            title = playlist.name,
            subtitle = "Playlist",
            icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
            songs = playlistSongs,
            favoriteIds = uiState.favoriteIds,
            onPlaySong = { playSong(it, playlistSongs) },
            onPlayAll = { playAll(playlistSongs) },
            onShuffleAll = { shuffleAll(playlistSongs) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onSongMenuClick = { selectedSongForMenu = it },
            onDismiss = { selectedPlaylistForDetail = null }
        )
    }

    // Smart Playlist Detail Sheet
    smartPlaylistType?.let { type ->
        val recentlyPlayedSongs by viewModel.getRecentlyPlayedSongs().collectAsState(initial = emptyList())
        val mostPlayedSongs by viewModel.getMostPlayedSongs().collectAsState(initial = emptyList())
        val neverPlayedSongs by viewModel.getNeverPlayedSongs().collectAsState(initial = emptyList())

        val (title, songs, icon) = when (type) {
            "FAVORITES" -> Triple("Favorites", uiState.favorites, Icons.Filled.Favorite)
            "RECENTLY_ADDED" -> Triple("Recently Added", uiState.recentlyAdded, Icons.Outlined.History)
            "RECENTLY_PLAYED" -> Triple("Recently Played", recentlyPlayedSongs, Icons.Outlined.PlayArrow)
            "MOST_PLAYED" -> Triple("Most Played", mostPlayedSongs, Icons.Outlined.TrendingUp)
            "NEVER_PLAYED" -> Triple("Never Played", neverPlayedSongs, Icons.Outlined.MusicNote)
            else -> Triple("Playlist", emptyList(), Icons.AutoMirrored.Outlined.PlaylistPlay)
        }

        MusicDetailSheet(
            sheetState = genericSheetState,
            title = title,
            subtitle = "Smart Playlist • ${songs.size} songs",
            icon = icon,
            songs = songs,
            favoriteIds = uiState.favoriteIds,
            onPlaySong = { playSong(it, songs) },
            onPlayAll = { playAll(songs) },
            onShuffleAll = { shuffleAll(songs) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onSongMenuClick = { selectedSongForMenu = it },
            onDismiss = { smartPlaylistType = null }
        )
    }

    // Song Menu Sheet (⋮ / Long Press)
    selectedSongForMenu?.let { song ->
        val isFav = uiState.favoriteIds.contains(song.id)
        MusicSongMenuSheet(
            sheetState = menuSheetState,
            song = song,
            isFavorite = isFav,
            onPlayNext = {
                viewModel.playNext(song)
                Toast.makeText(context, "Playing next: ${song.title}", Toast.LENGTH_SHORT).show()
                selectedSongForMenu = null
            },
            onAddToQueue = {
                viewModel.addToQueue(song)
                Toast.makeText(context, "Added to queue: ${song.title}", Toast.LENGTH_SHORT).show()
                selectedSongForMenu = null
            },
            onAddToPlaylist = {
                songForAddToPlaylist = song
            },
            onToggleFavorite = {
                viewModel.toggleFavorite(song.id)
            },
            onGoToAlbum = {
                val album = uiState.albums.firstOrNull { it.name.equals(song.album, ignoreCase = true) }
                    ?: MusicAlbum(
                        name = song.album,
                        artist = song.artist,
                        year = song.year,
                        trackCount = 1,
                        albumId = song.albumId,
                        dateAdded = song.dateAdded
                    )
                selectedAlbumForDetail = album
            },
            onGoToArtist = {
                val artist = uiState.artists.firstOrNull { it.name.equals(song.artist, ignoreCase = true) }
                    ?: MusicArtist(
                        name = song.artist,
                        albumCount = 1,
                        songCount = 1,
                        firstAlbumId = song.albumId
                    )
                selectedArtistForDetail = artist
            },
            onShowDetails = {
                selectedSongForDetails = song
            },
            onShare = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(song.uri))
                    putExtra(Intent.EXTRA_TEXT, "Listening to ${song.title} by ${song.artist}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share song"))
            },
            onEditSongInfo = {
                songForTagEdit = song
            },
            onSetAsRingtone = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.System.canWrite(context)) {
                        Toast.makeText(context, "Please allow permission to write system settings", Toast.LENGTH_LONG).show()
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                            data = Uri.parse("package:" + context.packageName)
                        }
                        context.startActivity(intent)
                    } else {
                        try {
                            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, Uri.parse(song.uri))
                            Toast.makeText(context, "Set as phone ringtone", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not set ringtone: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onHideSong = {
                viewModel.hideSong(song.id)
                Toast.makeText(context, "Song hidden from library", Toast.LENGTH_SHORT).show()
            },
            onDeleteSong = {
                songToDelete = song
            },
            onDismiss = { selectedSongForMenu = null }
        )
    }

    // Tag Editor Sheet
    songForTagEdit?.let { song ->
        MusicTagEditorSheet(
            sheetState = tagEditorSheetState,
            song = song,
            onSongUpdated = { updated ->
                viewModel.updateSong(updated)
                songForTagEdit = null
            },
            onDismiss = { songForTagEdit = null }
        )
    }

    // Delete confirmation dialog
    songToDelete?.let { song ->
        AlertDialog(
            onDismissRequest = { songToDelete = null },
            title = { Text("Delete song?", color = NPWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Delete \"${song.title}\"? This cannot be undone.", color = NPWhite.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSong(song.id)
                        songToDelete = null
                        Toast.makeText(context, "Deleted from library", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = NPYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { songToDelete = null }) {
                    Text("Cancel", color = NPWhite.copy(alpha = 0.6f))
                }
            },
            containerColor = NPBlack,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Song Details Sheet
    selectedSongForDetails?.let { song ->
        MusicSongDetailsSheet(
            sheetState = detailsSheetState,
            song = song,
            onDismiss = { selectedSongForDetails = null }
        )
    }

    // Add to Playlist Dialog
    songForAddToPlaylist?.let { song ->
        AddToPlaylistDialog(
            sheetState = playlistPickerSheetState,
            playlists = uiState.playlists,
            onCreateNewPlaylistClick = {
                showNewPlaylistDialog = true
            },
            onPlaylistSelected = { playlist ->
                viewModel.addSongToPlaylist(playlist.id, song.id)
                Toast.makeText(context, "Added to ${playlist.name}", Toast.LENGTH_SHORT).show()
                songForAddToPlaylist = null
            },
            onDismiss = { songForAddToPlaylist = null }
        )
    }

    // New Playlist Dialog
    if (showNewPlaylistDialog) {
        AlertDialog(
            onDismissRequest = {
                showNewPlaylistDialog = false
                newPlaylistName = ""
            },
            containerColor = NPBlack,
            title = {
                Text(text = "New Playlist", color = NPWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist name", color = NPWhite.copy(alpha = 0.4f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NPWhite,
                        unfocusedTextColor = NPWhite,
                        focusedBorderColor = NPYellow,
                        unfocusedBorderColor = NPWhite.copy(alpha = 0.3f),
                        cursorColor = NPYellow
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName.trim())
                            showNewPlaylistDialog = false
                            newPlaylistName = ""
                        }
                    }
                ) {
                    Text("Create", color = NPYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNewPlaylistDialog = false
                    newPlaylistName = ""
                }) {
                    Text("Cancel", color = NPWhite.copy(alpha = 0.6f))
                }
            }
        )
    }

    // Now Playing Sheet
    if (showNowPlayingSheet && currentSong != null) {
        NowPlayingSheet(
            sheetState = nowPlayingSheetState,
            song = currentSong,
            isPlaying = isPlaying,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            bufferedPositionMs = bufferedPositionMs,
            shuffleMode = shuffleMode,
            repeatMode = repeatMode,
            isFavorite = isSongFavorite,
            playbackSpeed = playbackSpeed,
            sleepTimerRemainingMs = sleepTimerRemainingMs,
            sleepTimerOption = sleepTimerOption,
            seekBarStyle = seekBarStyle,
            audioFxManager = viewModel.audioFxManager,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onSkipNext = { viewModel.skipNext() },
            onSkipPrevious = { viewModel.skipPrevious() },
            onSeekTo = { viewModel.seekTo(it) },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onToggleRepeat = { viewModel.toggleRepeat() },
            onToggleFavorite = { viewModel.toggleFavoriteCurrentSong() },
            onSetPlaybackSpeed = { viewModel.setPlaybackSpeed(it) },
            onSetSleepTimer = { viewModel.setSleepTimer(it) },
            onCancelSleepTimer = { viewModel.cancelSleepTimer() },
            onOpenQueue = { showQueueSheet = true },
            onAddToPlaylist = { songForAddToPlaylist = currentSong },
            lyricsRepository = viewModel.lyricsRepository,
            onDismiss = { showNowPlayingSheet = false }
        )
    }

    // Queue Sheet
    if (showQueueSheet) {
        MusicQueueSheet(
            sheetState = queueSheetState,
            queue = currentQueue,
            currentIndex = currentIndex,
            currentPositionMs = currentPositionMs,
            onSongClick = { index ->
                if (index in currentQueue.indices) {
                    viewModel.playSong(currentQueue[index], currentQueue)
                }
            },
            onReorder = { from, to -> viewModel.reorderQueue(from, to) },
            onRemoveSong = { index -> viewModel.removeFromQueue(index) },
            onClearQueue = { viewModel.clearQueue() },
            onSaveAsPlaylist = { name -> viewModel.saveQueueAsPlaylist(name) },
            onDismiss = { showQueueSheet = false }
        )
    }
}

// ----------------------------------------------------
// TAB CONTENTS
// ----------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongsTabContent(
    songs: List<SongEntity>,
    favoriteIds: Set<Long>,
    isGridView: Boolean,
    isSelectionMode: Boolean = false,
    selectedSongIds: Set<Long> = emptySet(),
    onToggleSongSelection: (Long) -> Unit = {},
    onSongLongClick: (Long) -> Unit = {},
    onPlaySong: (SongEntity) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onSongMenuClick: (SongEntity) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onLetterSelected: (String) -> Unit
) {
    val totalDuration = remember(songs) { songs.sumOf { it.durationMs } }

    if (songs.isEmpty()) {
        EmptyTabState(
            icon = Icons.Outlined.MusicNote,
            title = "No songs found",
            subtitle = "Audio tracks will appear here once scanned."
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 28.dp) // Leave room for fast scroller
        ) {
            // Play All & Shuffle Top Action Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(NPYellow)
                            .clickable(onClick = onPlayAll)
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            tint = NPBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Play all (${songs.size})",
                            color = NPBlack,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(NPWhite.copy(alpha = 0.1f))
                            .clickable(onClick = onShuffleAll)
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shuffle,
                            contentDescription = null,
                            tint = NPWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Shuffle",
                            color = NPWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Overview line
            item {
                Text(
                    text = "${songs.size} tracks • ${FormatUtils.formatHoursAndMinutes(totalDuration)}",
                    color = NPWhite.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
                )
            }

            // Songs List
            items(songs, key = { it.id }) { song ->
                val isFav = favoriteIds.contains(song.id)
                val isSelected = selectedSongIds.contains(song.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                if (isSelectionMode) {
                                    onToggleSongSelection(song.id)
                                } else {
                                    onPlaySong(song)
                                }
                            },
                            onLongClick = {
                                onSongLongClick(song.id)
                            }
                        )
                        .background(if (isSelected) NPYellow.copy(alpha = 0.12f) else NPBlack)
                        .padding(horizontal = 16.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelectionMode) {
                        Icon(
                            imageVector = if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) NPYellow else NPWhite.copy(alpha = 0.4f),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(22.dp)
                        )
                    }

                    MusicArtwork(
                        albumId = song.albumId,
                        contentDescription = song.title,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            color = if (isSelected) NPYellow else NPWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${song.artist} • ${song.album} • ${FormatUtils.formatDuration(song.durationMs)}",
                            color = NPWhite.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!isSelectionMode) {
                        IconButton(
                            onClick = { onToggleFavorite(song.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isFav) "Favorited" else "Favorite",
                                tint = if (isFav) NPYellow else NPWhite.copy(alpha = 0.5f),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = { onSongMenuClick(song) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Menu",
                                tint = NPWhite.copy(alpha = 0.6f),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Fast Scroller
        AlphabetFastScroller(
            modifier = Modifier.align(Alignment.CenterEnd),
            onLetterSelected = onLetterSelected
        )
    }
}

@Composable
private fun AlbumsTabContent(
    albums: List<MusicAlbum>,
    isGridView: Boolean,
    onAlbumClick: (MusicAlbum) -> Unit,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onLetterSelected: (String) -> Unit
) {
    if (albums.isEmpty()) {
        EmptyTabState(
            icon = Icons.Outlined.MusicNote,
            title = "No albums found",
            subtitle = "Albums will appear here once audio files are scanned."
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isGridView) 3 else 2),
            state = gridState,
            contentPadding = PaddingValues(start = 16.dp, end = 36.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(albums, key = { it.name }) { album ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAlbumClick(album) }
                ) {
                    MusicArtwork(
                        albumId = album.albumId,
                        contentDescription = album.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = album.name,
                        color = NPWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = album.artist,
                        color = NPWhite.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            if (album.year > 0) append("${album.year} • ")
                            append("${album.trackCount} tracks")
                        },
                        color = NPWhite.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        AlphabetFastScroller(
            modifier = Modifier.align(Alignment.CenterEnd),
            onLetterSelected = onLetterSelected
        )
    }
}

@Composable
private fun ArtistsTabContent(
    artists: List<MusicArtist>,
    isGridView: Boolean,
    onArtistClick: (MusicArtist) -> Unit
) {
    if (artists.isEmpty()) {
        EmptyTabState(
            icon = Icons.Outlined.MusicNote,
            title = "No artists found",
            subtitle = "Artists will appear here once audio files are scanned."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(artists, key = { it.name }) { artist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onArtistClick(artist) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MusicArtwork(
                    albumId = artist.firstAlbumId,
                    contentDescription = artist.name,
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artist.name,
                        color = NPWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${artist.albumCount} albums • ${artist.songCount} songs",
                        color = NPWhite.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistsTabContent(
    playlists: List<MusicPlaylistEntity>,
    favoritesCount: Int,
    recentlyAddedCount: Int,
    onSmartPlaylistClick: (String) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onPlaylistClick: (MusicPlaylistEntity) -> Unit,
    onDeletePlaylist: (MusicPlaylistEntity) -> Unit,
    onExportPlaylist: (MusicPlaylistEntity) -> Unit,
    onImportPlaylistClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Smart Playlists Section
        item {
            Text(
                text = "SMART PLAYLISTS",
                color = NPYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
            )
        }

        item {
            SmartPlaylistRow(
                title = "Favorites",
                subtitle = "$favoritesCount tracks",
                icon = Icons.Filled.Favorite,
                onClick = { onSmartPlaylistClick("FAVORITES") }
            )
            SmartPlaylistRow(
                title = "Recently Added",
                subtitle = "$recentlyAddedCount tracks (30 days)",
                icon = Icons.Outlined.History,
                onClick = { onSmartPlaylistClick("RECENTLY_ADDED") }
            )
            SmartPlaylistRow(
                title = "Recently Played",
                subtitle = "Tracks you played recently",
                icon = Icons.Outlined.PlayArrow,
                onClick = { onSmartPlaylistClick("RECENTLY_PLAYED") }
            )
            SmartPlaylistRow(
                title = "Most Played",
                subtitle = "Your top listened tracks",
                icon = Icons.Outlined.TrendingUp,
                onClick = { onSmartPlaylistClick("MOST_PLAYED") }
            )
            SmartPlaylistRow(
                title = "Never Played",
                subtitle = "Unheard gems in your library",
                icon = Icons.Outlined.MusicNote,
                onClick = { onSmartPlaylistClick("NEVER_PLAYED") }
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = NPWhite.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Custom Playlists Header + Actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR PLAYLISTS",
                    color = NPYellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onImportPlaylistClick) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = null,
                        tint = NPWhite.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import .m3u", color = NPWhite.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
        }

        // Create Playlist Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCreatePlaylistClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NPYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "New Playlist",
                        tint = NPBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "New Playlist",
                        color = NPYellow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Create a custom mix",
                        color = NPWhite.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (playlists.isEmpty()) {
            item {
                Text(
                    text = "No custom playlists created yet",
                    color = NPWhite.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        } else {
            items(playlists, key = { it.id }) { playlist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlaylistClick(playlist) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NPWhite.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.PlaylistPlay,
                            contentDescription = null,
                            tint = NPYellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            color = NPWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Custom playlist",
                            color = NPWhite.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = { onExportPlaylist(playlist) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileUpload,
                            contentDescription = "Export .m3u",
                            tint = NPWhite.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDeletePlaylist(playlist) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete playlist",
                            tint = NPWhite.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartPlaylistRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NPWhite.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = NPYellow,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = NPWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = NPWhite.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun GenresTabContent(
    genres: List<MusicGenre>,
    onGenreClick: (MusicGenre) -> Unit
) {
    if (genres.isEmpty()) {
        EmptyTabState(
            icon = Icons.Outlined.Category,
            title = "No genres found",
            subtitle = "Genres from audio tags will show up here."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(genres, key = { it.name }) { genre ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGenreClick(genre) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NPWhite.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Category,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = genre.name,
                        color = NPWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${genre.songCount} songs",
                        color = NPWhite.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FoldersTabContent(
    folders: List<MusicFolder>,
    onFolderClick: (MusicFolder) -> Unit
) {
    if (folders.isEmpty()) {
        EmptyTabState(
            icon = Icons.Outlined.Folder,
            title = "No music folders found",
            subtitle = "Folders containing audio will appear here."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(folders, key = { it.path }) { folder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFolderClick(folder) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NPWhite.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        color = NPWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${folder.songCount} songs • ${folder.path}",
                        color = NPWhite.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesTabContent(
    favorites: List<SongEntity>,
    onPlaySong: (SongEntity) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onSongMenuClick: (SongEntity) -> Unit
) {
    if (favorites.isEmpty()) {
        EmptyTabState(
            icon = Icons.Outlined.FavoriteBorder,
            title = "No favorite songs yet",
            subtitle = "Tap the heart icon on any song to add it to favorites."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(NPYellow)
                        .clickable(onClick = onPlayAll)
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        tint = NPBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Play All (${favorites.size})",
                        color = NPBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(NPWhite.copy(alpha = 0.1f))
                        .clickable(onClick = onShuffleAll)
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shuffle,
                        contentDescription = null,
                        tint = NPWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Shuffle",
                        color = NPWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        items(favorites, key = { it.id }) { song ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlaySong(song) }
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MusicArtwork(
                    albumId = song.albumId,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = NPWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${song.artist} • ${song.album} • ${FormatUtils.formatDuration(song.durationMs)}",
                        color = NPWhite.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { onToggleFavorite(song.id) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favorited",
                        tint = NPYellow,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = { onSongMenuClick(song) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Menu",
                        tint = NPWhite.copy(alpha = 0.6f),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentlyAddedTabContent(
    songs: List<SongEntity>,
    activeFilter: RecentlyAddedFilter,
    favoriteIds: Set<Long>,
    onFilterSelected: (RecentlyAddedFilter) -> Unit,
    onPlaySong: (SongEntity) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onSongMenuClick: (SongEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Range Filters: 7 days, 14 days, 30 days, 90 days
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RecentlyAddedFilter.entries.forEach { filter ->
                val isSelected = filter == activeFilter
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) NPYellow else NPWhite.copy(alpha = 0.08f))
                        .clickable { onFilterSelected(filter) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${filter.days} days",
                        color = if (isSelected) NPBlack else NPWhite,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        if (songs.isEmpty()) {
            EmptyTabState(
                icon = Icons.Outlined.MusicNote,
                title = "No songs added recently",
                subtitle = "Try selecting a wider timeframe like 90 days."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(NPYellow)
                                .clickable(onClick = onPlayAll)
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = null,
                                tint = NPBlack,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Play All (${songs.size})",
                                color = NPBlack,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(NPWhite.copy(alpha = 0.1f))
                                .clickable(onClick = onShuffleAll)
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shuffle,
                                contentDescription = null,
                                tint = NPWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Shuffle",
                                color = NPWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                items(songs, key = { it.id }) { song ->
                    val isFav = favoriteIds.contains(song.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlaySong(song) }
                            .padding(horizontal = 16.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MusicArtwork(
                            albumId = song.albumId,
                            contentDescription = song.title,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                color = NPWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${song.artist} • ${song.album} • ${FormatUtils.formatDuration(song.durationMs)}",
                                color = NPWhite.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onToggleFavorite(song.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isFav) "Favorited" else "Favorite",
                                tint = if (isFav) NPYellow else NPWhite.copy(alpha = 0.5f),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = { onSongMenuClick(song) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Menu",
                                tint = NPWhite.copy(alpha = 0.6f),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTabState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NPWhite.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = NPWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = NPWhite.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SelectionBarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = NPYellow,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = NPWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
