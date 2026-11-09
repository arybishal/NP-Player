package com.npplayer.player.ui.sheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.player.engine.MediaTrackItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleTrackBottomSheet(
    tracks: List<MediaTrackItem>,
    onSelectTrack: (MediaTrackItem?) -> Unit,
    onLoadExternalSubtitle: () -> Unit = {},
    onOpenOnlineSubtitles: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var yellowStyling by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NPBlack,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Subtitles,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Subtitles",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NPWhite
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

            Spacer(modifier = Modifier.height(16.dp))

            // External Subtitle Picker Button
            Surface(
                onClick = {
                    onLoadExternalSubtitle()
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                color = NPYellow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        tint = NPBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Load External Subtitle (.srt, .vtt, .ass)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NPBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Online Subtitles Search Button (SubDL)
            Surface(
                onClick = {
                    onOpenOnlineSubtitles()
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                color = NPWhite.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, NPYellow.copy(alpha = 0.60f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Search Online Subtitles",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NPWhite
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NPYellow
                    ) {
                        Text(
                            text = "SubDL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NPBlack,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AVAILABLE TRACKS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NPWhite.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option: Turn Off Subtitles
            val noneSelected = tracks.none { it.isSelected }
            TrackOptionRow(
                title = "None (Subtitles Off)",
                isSelected = noneSelected,
                onClick = {
                    onSelectTrack(null)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle track items
            if (tracks.isEmpty()) {
                Text(
                    text = "No embedded subtitle streams found in this video.",
                    fontSize = 14.sp,
                    color = NPWhite.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tracks) { track ->
                        TrackOptionRow(
                            title = track.name,
                            subtitle = "Language: ${track.language.uppercase()}",
                            isSelected = track.isSelected,
                            onClick = {
                                onSelectTrack(track)
                                onDismiss()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subtitle Styling Preference
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NPWhite.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, NPWhite.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "High-Contrast Yellow Text",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NPWhite
                        )
                        Text(
                            text = if (yellowStyling) "Yellow font #FFD60A" else "Standard white font",
                            fontSize = 12.sp,
                            color = NPWhite.copy(alpha = 0.6f)
                        )
                    }

                    Switch(
                        checked = yellowStyling,
                        onCheckedChange = { yellowStyling = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NPBlack,
                            checkedTrackColor = NPYellow,
                            uncheckedThumbColor = NPWhite,
                            uncheckedTrackColor = NPWhite.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTrackBottomSheet(
    tracks: List<MediaTrackItem>,
    onSelectTrack: (MediaTrackItem) -> Unit,
    onOpenEqualizer: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var volumeBoostEnabled by remember { mutableStateOf(false) }
    var nightModeEnabled by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NPBlack,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Equalizer,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Audio & Sound FX",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NPWhite
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

            Spacer(modifier = Modifier.height(16.dp))

            // Open Equalizer Button
            Surface(
                onClick = {
                    onOpenEqualizer()
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                color = NPYellow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GraphicEq,
                        contentDescription = null,
                        tint = NPBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open 5-Band Equalizer & Bass Boost",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NPBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AUDIO STREAMS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NPWhite.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (tracks.isEmpty()) {
                TrackOptionRow(
                    title = "Default Audio Stream",
                    isSelected = true,
                    onClick = onDismiss
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tracks) { track ->
                        val displayLang = if (track.language == "und" || track.language.isBlank()) "Audio Stream" else java.util.Locale(track.language).displayLanguage.replaceFirstChar { it.uppercase() }
                        val codecChannels = listOfNotNull(
                            track.codec.ifBlank { null },
                            track.channels.ifBlank { null },
                            if (track.bitrate > 0) "${track.bitrate / 1000} kbps" else null
                        ).joinToString(" • ")

                        val subText = if (!track.isSupported) {
                            "${track.unsupportedReason ?: "Unsupported codec"} ($codecChannels)"
                        } else {
                            codecChannels
                        }

                        val titleText = if (track.title.isNotBlank()) "${track.title} ($displayLang)" else displayLang

                        TrackOptionRow(
                            title = titleText,
                            subtitle = subText,
                            isSelected = track.isSelected,
                            isEnabled = track.isSupported,
                            unsupportedReason = track.unsupportedReason,
                            onClick = {
                                if (track.isSupported) {
                                    onSelectTrack(track)
                                    onDismiss()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Delay / A/V Sync link
            Surface(
                onClick = {
                    onOpenEqualizer()
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                color = NPWhite.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, NPWhite.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Equalizer,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Audio Delay & A/V Sync",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NPWhite
                        )
                        Text(
                            text = "Adjust audio sync timing and fine-tune latency",
                            fontSize = 11.sp,
                            color = NPWhite.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ENHANCEMENTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NPWhite.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Volume Boost +200%
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NPWhite.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, NPWhite.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                            contentDescription = null,
                            tint = if (volumeBoostEnabled) NPYellow else NPWhite,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Volume Boost (+200%)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NPWhite
                            )
                            Text(
                                text = "Software amplification for quiet videos",
                                fontSize = 12.sp,
                                color = NPWhite.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Switch(
                        checked = volumeBoostEnabled,
                        onCheckedChange = { volumeBoostEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NPBlack,
                            checkedTrackColor = NPYellow,
                            uncheckedThumbColor = NPWhite,
                            uncheckedTrackColor = NPWhite.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Night Mode (Dynamic Range Compression)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NPWhite.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, NPWhite.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Nightlight,
                            contentDescription = null,
                            tint = if (nightModeEnabled) NPYellow else NPWhite,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Night Listening Mode",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NPWhite
                            )
                            Text(
                                text = "Boost dialogues and suppress loud explosions",
                                fontSize = 12.sp,
                                color = NPWhite.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Switch(
                        checked = nightModeEnabled,
                        onCheckedChange = { nightModeEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NPBlack,
                            checkedTrackColor = NPYellow,
                            uncheckedThumbColor = NPWhite,
                            uncheckedTrackColor = NPWhite.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TrackOptionRow(
    title: String,
    subtitle: String? = null,
    isSelected: Boolean,
    isEnabled: Boolean = true,
    unsupportedReason: String? = null,
    onClick: () -> Unit
) {
    val titleColor = when {
        !isEnabled -> NPWhite.copy(alpha = 0.35f)
        isSelected -> NPYellow
        else -> NPWhite
    }

    val subColor = when {
        !isEnabled -> NPWhite.copy(alpha = 0.25f)
        else -> NPWhite.copy(alpha = 0.6f)
    }

    Surface(
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) NPYellow.copy(alpha = 0.12f) else NPWhite.copy(alpha = 0.05f),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) NPYellow else NPWhite.copy(alpha = 0.12f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = titleColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = subColor
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(NPYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Selected",
                        tint = NPBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
