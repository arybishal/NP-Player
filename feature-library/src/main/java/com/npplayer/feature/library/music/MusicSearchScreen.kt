package com.npplayer.feature.library.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.database.entity.MusicPlaylistEntity
import com.npplayer.core.data.database.entity.SongEntity
import com.npplayer.core.data.repository.MusicAlbum
import com.npplayer.core.data.repository.MusicArtist
import com.npplayer.core.data.repository.MusicSearchResult
import com.npplayer.core.ui.component.MusicArtwork
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.core.ui.util.FormatUtils

@Composable
fun MusicSearchScreen(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    searchResult: MusicSearchResult?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSongClick: (SongEntity) -> Unit,
    onAlbumClick: (MusicAlbum) -> Unit,
    onArtistClick: (MusicArtist) -> Unit,
    onPlaylistClick: (MusicPlaylistEntity) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NPBlack)
    ) {
        // Search Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = NPWhite
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NPWhite.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Search songs, albums, artists...",
                        color = NPWhite.copy(alpha = 0.4f),
                        fontSize = 15.sp
                    )
                }

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = NPWhite,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(NPYellow)
                )
            }

            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Outlined.Clear,
                        contentDescription = "Clear",
                        tint = NPWhite.copy(alpha = 0.7f)
                    )
                }
            }
        }

        HorizontalDivider(color = NPWhite.copy(alpha = 0.1f))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NPYellow)
            }
        } else if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.MusicNote,
                        contentDescription = null,
                        tint = NPWhite.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Search your music library",
                        color = NPWhite.copy(alpha = 0.5f),
                        fontSize = 16.sp
                    )
                }
            }
        } else if (searchResult == null || (searchResult.songs.isEmpty() && searchResult.albums.isEmpty() && searchResult.artists.isEmpty() && searchResult.playlists.isEmpty())) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No results for \"$searchQuery\"",
                    color = NPWhite.copy(alpha = 0.6f),
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Songs section
                if (searchResult.songs.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "Songs (${searchResult.songs.size})")
                    }
                    items(searchResult.songs, key = { "song_${it.id}" }) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSongClick(song) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MusicArtwork(
                                albumId = song.albumId,
                                contentDescription = song.title,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                HighlightedText(
                                    fullText = song.title,
                                    query = searchQuery,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
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
                        }
                    }
                }

                // Albums section
                if (searchResult.albums.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "Albums (${searchResult.albums.size})")
                    }
                    items(searchResult.albums, key = { "album_${it.name}" }) { album ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onAlbumClick(album) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MusicArtwork(
                                albumId = album.albumId,
                                contentDescription = album.name,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                HighlightedText(
                                    fullText = album.name,
                                    query = searchQuery,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${album.artist} • ${album.trackCount} tracks",
                                    color = NPWhite.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Artists section
                if (searchResult.artists.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "Artists (${searchResult.artists.size})")
                    }
                    items(searchResult.artists, key = { "artist_${it.name}" }) { artist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onArtistClick(artist) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MusicArtwork(
                                albumId = artist.firstAlbumId,
                                contentDescription = artist.name,
                                shape = CircleShape,
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                HighlightedText(
                                    fullText = artist.name,
                                    query = searchQuery,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${artist.albumCount} albums • ${artist.songCount} songs",
                                    color = NPWhite.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Playlists section
                if (searchResult.playlists.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "Playlists (${searchResult.playlists.size})")
                    }
                    items(searchResult.playlists, key = { "playlist_${it.id}" }) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onPlaylistClick(playlist) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
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
                            Spacer(modifier = Modifier.width(12.dp))
                            HighlightedText(
                                fullText = playlist.name,
                                query = searchQuery,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    Text(
        text = title,
        color = NPYellow,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun HighlightedText(
    fullText: String,
    query: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight
) {
    val annotated = remember(fullText, query) {
        buildAnnotatedString {
            val lowerFull = fullText.lowercase()
            val lowerQuery = query.lowercase().trim()
            if (lowerQuery.isBlank() || !lowerFull.contains(lowerQuery)) {
                append(fullText)
            } else {
                var currentIndex = 0
                while (currentIndex < fullText.length) {
                    val matchIndex = lowerFull.indexOf(lowerQuery, currentIndex)
                    if (matchIndex < 0) {
                        append(fullText.substring(currentIndex))
                        break
                    }
                    if (matchIndex > currentIndex) {
                        append(fullText.substring(currentIndex, matchIndex))
                    }
                    val matchEnd = matchIndex + lowerQuery.length
                    withStyle(
                        SpanStyle(
                            background = NPYellow,
                            color = NPBlack,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(fullText.substring(matchIndex, matchEnd))
                    }
                    currentIndex = matchEnd
                }
            }
        }
    }

    Text(
        text = annotated,
        color = NPWhite,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
