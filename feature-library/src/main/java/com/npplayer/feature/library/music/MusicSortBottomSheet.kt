package com.npplayer.feature.library.music

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.repository.SortDirection
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

data class SortOptionItem(
    val key: String,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicSortBottomSheet(
    sheetState: SheetState,
    tab: MusicTab,
    selectedOption: String,
    sortDirection: SortDirection,
    onOptionSelected: (String) -> Unit,
    onDirectionChanged: (SortDirection) -> Unit,
    onDismiss: () -> Unit
) {
    val options = when (tab) {
        MusicTab.SONGS -> listOf(
            SortOptionItem("TITLE", "Title"),
            SortOptionItem("ARTIST", "Artist"),
            SortOptionItem("ALBUM", "Album"),
            SortOptionItem("TRACK_NUMBER", "Track number"),
            SortOptionItem("DURATION", "Duration"),
            SortOptionItem("DATE_ADDED", "Date added"),
            SortOptionItem("YEAR", "Year"),
            SortOptionItem("PLAY_COUNT", "Play count"),
            SortOptionItem("SIZE", "File size")
        )
        MusicTab.ALBUMS -> listOf(
            SortOptionItem("NAME", "Album name"),
            SortOptionItem("ARTIST", "Artist"),
            SortOptionItem("YEAR", "Year"),
            SortOptionItem("DATE_ADDED", "Date added"),
            SortOptionItem("TRACK_COUNT", "Number of tracks")
        )
        MusicTab.ARTISTS -> listOf(
            SortOptionItem("NAME", "Artist name"),
            SortOptionItem("ALBUMS_COUNT", "Number of albums"),
            SortOptionItem("SONGS_COUNT", "Number of songs")
        )
        MusicTab.GENRES -> listOf(
            SortOptionItem("NAME", "Genre name"),
            SortOptionItem("SONGS_COUNT", "Number of songs")
        )
        MusicTab.PLAYLISTS -> listOf(
            SortOptionItem("NAME", "Playlist name"),
            SortOptionItem("DATE_CREATED", "Date created"),
            SortOptionItem("SONGS_COUNT", "Number of songs")
        )
        MusicTab.FOLDERS -> listOf(
            SortOptionItem("NAME", "Folder name"),
            SortOptionItem("SONGS_COUNT", "Number of songs")
        )
        MusicTab.FAVORITES -> listOf(
            SortOptionItem("DATE_LIKED", "Date liked"),
            SortOptionItem("TITLE", "Title"),
            SortOptionItem("ARTIST", "Artist")
        )
        MusicTab.RECENTLY_ADDED -> listOf(
            SortOptionItem("DATE_ADDED", "Date added")
        )
    }

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
            // Title & Direction Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort ${tab.label}",
                    color = NPWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(NPWhite.copy(alpha = 0.08f))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DirectionChip(
                        label = "Ascending",
                        icon = Icons.Outlined.ArrowUpward,
                        selected = sortDirection == SortDirection.ASCENDING,
                        onClick = { onDirectionChanged(SortDirection.ASCENDING) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    DirectionChip(
                        label = "Descending",
                        icon = Icons.Outlined.ArrowDownward,
                        selected = sortDirection == SortDirection.DESCENDING,
                        onClick = { onDirectionChanged(SortDirection.DESCENDING) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(options, key = { it.key }) { item ->
                    val isSelected = item.key == selectedOption
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onOptionSelected(item.key)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.label,
                            color = if (isSelected) NPYellow else NPWhite,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = NPYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DirectionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) NPYellow else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) NPBlack else NPWhite,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = if (selected) NPBlack else NPWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
