package com.npplayer.player.ui.sheet

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
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerMoreBottomSheet(
    onOpenPictureQuality: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onOpenSubtitles: () -> Unit,
    onOpenSpeed: () -> Unit,
    onOpenAspectRatio: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenInfo: () -> Unit,
    onTakeScreenshot: () -> Unit,
    onLockScreen: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NPBlack,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = null,
                    tint = NPYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Playback Options",
                    color = NPWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            MoreOptionItem(
                icon = Icons.Outlined.Tune,
                title = "Picture Quality & Color",
                subtitle = "GPU shader presets, brightness, contrast, warm filter",
                onClick = {
                    onDismiss()
                    onOpenPictureQuality()
                }
            )

            MoreOptionItem(
                icon = Icons.Outlined.Equalizer,
                title = "Audio FX & Equalizer",
                subtitle = "Bass boost, 5-band EQ, 200% volume boost",
                onClick = {
                    onDismiss()
                    onOpenEqualizer()
                }
            )

            MoreOptionItem(
                icon = Icons.Outlined.Translate,
                title = "Audio Tracks",
                subtitle = "Select audio language stream",
                onClick = {
                    onDismiss()
                    onOpenAudioTracks()
                }
            )

            MoreOptionItem(
                icon = Icons.Outlined.Subtitles,
                title = "Subtitles",
                subtitle = "Track selection, sync, and online search",
                onClick = {
                    onDismiss()
                    onOpenSubtitles()
                }
            )

            MoreOptionItem(
                icon = Icons.Outlined.CameraAlt,
                title = "Take Screenshot",
                subtitle = "Capture frame to gallery",
                onClick = {
                    onDismiss()
                    onTakeScreenshot()
                }
            )

            MoreOptionItem(
                icon = Icons.Outlined.Speed,
                title = "Playback Speed",
                subtitle = "Cycle speed from 0.5x to 2.0x",
                onClick = {
                    onDismiss()
                    onOpenSpeed()
                }
            )

            MoreOptionItem(
                icon = Icons.Outlined.AspectRatio,
                title = "Aspect Ratio",
                subtitle = "Fit, zoom, stretch, fill",
                onClick = {
                    onDismiss()
                    onOpenAspectRatio()
                }
            )

            MoreOptionItem(
                icon = Icons.Outlined.Bedtime,
                title = "Sleep Timer",
                subtitle = "Automatically pause after a set duration",
                onClick = {
                    onDismiss()
                    onOpenSleepTimer()
                }
            )

            MoreOptionItem(
                icon = Icons.Outlined.Info,
                title = "Media Details",
                subtitle = "Codec, resolution, bitrate, format",
                onClick = {
                    onDismiss()
                    onOpenInfo()
                }
            )

            MoreOptionItem(
                icon = Icons.Outlined.Lock,
                title = "Lock Screen",
                subtitle = "Prevent accidental touch gestures",
                onClick = {
                    onDismiss()
                    onLockScreen()
                }
            )
        }
    }
}

@Composable
private fun MoreOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = NPWhite.copy(alpha = 0.04f),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NPYellow,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = NPWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = NPWhite.copy(alpha = 0.50f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
