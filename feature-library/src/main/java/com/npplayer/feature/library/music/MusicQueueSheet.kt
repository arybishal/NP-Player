package com.npplayer.feature.library.music

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.database.entity.SongEntity
import com.npplayer.core.ui.component.MusicArtwork
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.core.ui.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicQueueSheet(
    sheetState: SheetState,
    queue: List<SongEntity>,
    currentIndex: Int,
    currentPositionMs: Long,
    onSongClick: (Int) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onRemoveSong: (Int) -> Unit,
    onClearQueue: () -> Unit,
    onSaveAsPlaylist: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showSavePlaylistDialog by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }

    // Calculate total time remaining from current position to end of queue
    val remainingSongs = if (currentIndex in queue.indices) queue.drop(currentIndex) else queue
    val remainingSongsTotalDuration = remainingSongs.sumOf { it.durationMs }
    val totalRemainingMs = (remainingSongsTotalDuration - currentPositionMs).coerceAtLeast(0L)
    val totalRemainingFormatted = FormatUtils.formatDuration(totalRemainingMs)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NPBlack,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Playback Queue",
                        color = NPWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${queue.size} songs • $totalRemainingFormatted remaining",
                        color = NPWhite.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = NPWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: Save as Playlist, Clear Queue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        playlistName = "Queue ${System.currentTimeMillis() % 10000}"
                        showSavePlaylistDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NPWhite.copy(alpha = 0.08f))
                        .border(1.dp, NPWhite.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkAdd,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save as playlist",
                        color = NPWhite,
                        fontSize = 13.sp
                    )
                }

                TextButton(
                    onClick = onClearQueue,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NPWhite.copy(alpha = 0.08f))
                        .border(1.dp, NPWhite.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ClearAll,
                        contentDescription = null,
                        tint = NPWhite.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Clear queue",
                        color = NPWhite.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = NPWhite.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            if (queue.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Queue is empty",
                        color = NPWhite.copy(alpha = 0.5f),
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(
                        items = queue,
                        key = { index, song -> "${song.id}_$index" }
                    ) { index, song ->
                        val isCurrent = index == currentIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCurrent) NPYellow.copy(alpha = 0.14f) else NPWhite.copy(alpha = 0.04f))
                                .border(
                                    width = if (isCurrent) 1.dp else 0.5.dp,
                                    color = if (isCurrent) NPYellow else NPWhite.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSongClick(index) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Current indicator or index
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier.size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.PlayArrow,
                                        contentDescription = "Currently Playing",
                                        tint = NPYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    color = NPWhite.copy(alpha = 0.45f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            MusicArtwork(
                                albumId = song.albumId,
                                contentDescription = song.title,
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    color = if (isCurrent) NPYellow else NPWhite,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${song.artist} • ${FormatUtils.formatDuration(song.durationMs)}",
                                    color = NPWhite.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Reorder buttons (Up/Down)
                            if (index > 0) {
                                IconButton(
                                    onClick = { onReorder(index, index - 1) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.KeyboardArrowUp,
                                        contentDescription = "Move Up",
                                        tint = NPWhite.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (index < queue.lastIndex) {
                                IconButton(
                                    onClick = { onReorder(index, index + 1) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.KeyboardArrowDown,
                                        contentDescription = "Move Down",
                                        tint = NPWhite.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Delete / Remove button
                            IconButton(
                                onClick = { onRemoveSong(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "Remove from Queue",
                                    tint = NPWhite.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSavePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showSavePlaylistDialog = false },
            title = {
                Text(
                    text = "Save Queue as Playlist",
                    color = NPWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NPWhite,
                        unfocusedTextColor = NPWhite,
                        focusedBorderColor = NPYellow,
                        unfocusedBorderColor = NPWhite.copy(alpha = 0.3f),
                        focusedLabelColor = NPYellow,
                        unfocusedLabelColor = NPWhite.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = playlistName.trim()
                        if (trimmed.isNotBlank()) {
                            onSaveAsPlaylist(trimmed)
                            showSavePlaylistDialog = false
                        }
                    }
                ) {
                    Text(text = "Save", color = NPYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePlaylistDialog = false }) {
                    Text(text = "Cancel", color = NPWhite.copy(alpha = 0.7f))
                }
            },
            containerColor = NPBlack,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
