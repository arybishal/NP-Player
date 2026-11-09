package com.npplayer.feature.library.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
fun MusicSongMenuSheet(
    sheetState: SheetState,
    song: SongEntity,
    isFavorite: Boolean,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onGoToAlbum: (String) -> Unit,
    onGoToArtist: (String) -> Unit,
    onShowDetails: () -> Unit,
    onShare: () -> Unit = {},
    onEditSongInfo: () -> Unit = {},
    onSetAsRingtone: () -> Unit = {},
    onHideSong: () -> Unit = {},
    onDeleteSong: () -> Unit = {},
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NPBlack,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header: Song artwork + title + artist
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MusicArtwork(
                    albumId = song.albumId,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = NPWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${song.artist} • ${FormatUtils.formatDuration(song.durationMs)}",
                        color = NPWhite.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = NPWhite.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            // Actions
            SongMenuActionRow(
                icon = Icons.Outlined.PlayCircleOutline,
                label = "Play next",
                onClick = { onDismiss(); onPlayNext() }
            )
            SongMenuActionRow(
                icon = Icons.AutoMirrored.Outlined.QueueMusic,
                label = "Add to queue",
                onClick = { onDismiss(); onAddToQueue() }
            )
            SongMenuActionRow(
                icon = Icons.AutoMirrored.Outlined.PlaylistAdd,
                label = "Add to playlist",
                onClick = { onDismiss(); onAddToPlaylist() }
            )
            SongMenuActionRow(
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                label = if (isFavorite) "Remove from favorites" else "Add to favorites",
                iconTint = if (isFavorite) NPYellow else NPWhite,
                onClick = { onDismiss(); onToggleFavorite() }
            )
            SongMenuActionRow(
                icon = Icons.Outlined.Album,
                label = "Go to album (${song.album})",
                onClick = { onDismiss(); onGoToAlbum(song.album) }
            )
            SongMenuActionRow(
                icon = Icons.Outlined.Person,
                label = "Go to artist (${song.artist})",
                onClick = { onDismiss(); onGoToArtist(song.artist) }
            )
            SongMenuActionRow(
                icon = Icons.Outlined.Info,
                label = "Song details",
                onClick = { onDismiss(); onShowDetails() }
            )
            SongMenuActionRow(
                icon = Icons.Outlined.Share,
                label = "Share",
                onClick = { onDismiss(); onShare() }
            )
            SongMenuActionRow(
                icon = Icons.Outlined.Edit,
                label = "Edit song info",
                onClick = { onDismiss(); onEditSongInfo() }
            )
            SongMenuActionRow(
                icon = Icons.Outlined.Notifications,
                label = "Set as ringtone",
                onClick = { onDismiss(); onSetAsRingtone() }
            )
            SongMenuActionRow(
                icon = Icons.Outlined.VisibilityOff,
                label = "Hide song",
                onClick = { onDismiss(); onHideSong() }
            )
            SongMenuActionRow(
                icon = Icons.Outlined.Delete,
                label = "Delete from device",
                iconTint = NPWhite.copy(alpha = 0.8f),
                onClick = { onDismiss(); onDeleteSong() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SongMenuActionRow(
    icon: ImageVector,
    label: String,
    iconTint: androidx.compose.ui.graphics.Color = NPWhite,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = NPWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
