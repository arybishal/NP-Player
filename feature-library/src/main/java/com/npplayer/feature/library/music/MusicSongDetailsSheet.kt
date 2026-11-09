package com.npplayer.feature.library.music

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.database.entity.SongEntity
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.core.ui.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicSongDetailsSheet(
    sheetState: SheetState,
    song: SongEntity,
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Song Details",
                    color = NPWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = NPWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = NPWhite.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            DetailItem(label = "Title", value = song.title)
            DetailItem(label = "Artist", value = song.artist)
            val albumArtist = song.albumArtist
            if (!albumArtist.isNullOrBlank()) {
                DetailItem(label = "Album Artist", value = albumArtist)
            }
            DetailItem(label = "Album", value = song.album)
            DetailItem(label = "Genre", value = song.genre)
            if (song.year > 0) {
                DetailItem(label = "Year", value = song.year.toString())
            }
            if (song.trackNo > 0) {
                DetailItem(label = "Track", value = "${song.trackNo}")
            }
            DetailItem(label = "Duration", value = FormatUtils.formatDuration(song.durationMs))
            DetailItem(label = "Size", value = FormatUtils.formatFileSize(song.sizeBytes))
            if (song.bitrate > 0) {
                DetailItem(label = "Bitrate", value = "${song.bitrate / 1000} kbps")
            }
            DetailItem(label = "Format", value = song.mimeType)
            DetailItem(label = "Location", value = song.folderPath)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = NPYellow,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = NPWhite,
            fontSize = 14.sp
        )
    }
}
