package com.npplayer.feature.library.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.database.entity.SongEntity
import com.npplayer.core.data.repository.MusicAlbum
import com.npplayer.core.ui.component.MusicArtwork
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.core.ui.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicAlbumDetailSheet(
    sheetState: SheetState,
    album: MusicAlbum,
    songs: List<SongEntity>,
    favoriteIds: Set<Long>,
    onPlaySong: (SongEntity) -> Unit,
    onPlayAll: (List<SongEntity>) -> Unit,
    onShuffleAll: (List<SongEntity>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onSongMenuClick: (SongEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val totalDuration = songs.sumOf { it.durationMs }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NPBlack,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Header: close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = NPWhite
                    )
                }
            }

            // Album Artwork & Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MusicArtwork(
                    albumId = album.albumId,
                    contentDescription = album.name,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = album.name,
                        color = NPWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = album.artist,
                        color = NPWhite.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            if (album.year > 0) append("${album.year} • ")
                            append("${songs.size} tracks")
                            if (totalDuration > 0) append(" • ${FormatUtils.formatHoursAndMinutes(totalDuration)}")
                        },
                        color = NPWhite.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Play All & Shuffle Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(NPYellow)
                        .clickable { onPlayAll(songs) }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        tint = NPBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Play All",
                        color = NPBlack,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(NPWhite.copy(alpha = 0.12f))
                        .clickable { onShuffleAll(songs) }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shuffle,
                        contentDescription = null,
                        tint = NPWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Shuffle",
                        color = NPWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Track List
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                    val isFav = favoriteIds.contains(song.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPlaySong(song) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (song.trackNo > 0) "${song.trackNo}" else "${index + 1}",
                            color = NPWhite.copy(alpha = 0.4f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(28.dp)
                        )

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
                                text = "${song.artist} • ${FormatUtils.formatDuration(song.durationMs)}",
                                color = NPWhite.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        IconButton(
                            onClick = { onToggleFavorite(song.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isFav) "Remove Favorite" else "Favorite",
                                tint = if (isFav) NPYellow else NPWhite.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { onSongMenuClick(song) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "More",
                                tint = NPWhite.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
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
