package com.npplayer.feature.settings.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.preferences.MusicPreferences
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicSettingsSheet(
    sheetState: SheetState,
    preferences: MusicPreferences,
    onPreferencesChange: (MusicPreferences) -> Unit,
    onClearLyricsCache: () -> Unit,
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
                    text = "Music Settings",
                    color = NPWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = NPWhite)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = NPWhite.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(14.dp))

            // Playback Section
            Text("PLAYBACK & AUDIO", color = NPYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            MusicSettingSwitchRow(
                title = "Gapless Playback",
                subtitle = "Seamless transition between consecutive tracks",
                checked = preferences.gaplessEnabled,
                onCheckedChange = { onPreferencesChange(preferences.copy(gaplessEnabled = it)) }
            )

            MusicSettingSwitchRow(
                title = "Skip Silence",
                subtitle = "Automatically skip silent segments at track beginning and end",
                checked = preferences.skipSilence,
                onCheckedChange = { onPreferencesChange(preferences.copy(skipSilence = it)) }
            )

            MusicSettingSwitchRow(
                title = "Pause on Headset Unplug",
                subtitle = "Pause playback when wired or Bluetooth headphones disconnect",
                checked = preferences.pauseOnHeadsetDisconnect,
                onCheckedChange = { onPreferencesChange(preferences.copy(pauseOnHeadsetDisconnect = it)) }
            )

            // Crossfade Duration Slider
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Crossfade Duration", color = NPWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text("${preferences.crossfadeSec}s", color = NPYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = preferences.crossfadeSec.toFloat(),
                    onValueChange = { onPreferencesChange(preferences.copy(crossfadeSec = it.toInt())) },
                    valueRange = 0f..12f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = NPYellow,
                        activeTrackColor = NPYellow,
                        inactiveTrackColor = NPWhite.copy(alpha = 0.2f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = NPWhite.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(14.dp))

            // Lyrics Section
            Text("LYRICS & LRCLIB", color = NPYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            MusicSettingSwitchRow(
                title = "Fetch Lyrics Online",
                subtitle = "Automatically download synced lyrics from LRCLIB",
                checked = preferences.fetchLyricsOnline,
                onCheckedChange = { onPreferencesChange(preferences.copy(fetchLyricsOnline = it)) }
            )

            MusicSettingSwitchRow(
                title = "Wi-Fi Only for Lyrics",
                subtitle = "Download lyrics only when connected to Wi-Fi",
                checked = preferences.lyricsWifiOnly,
                onCheckedChange = { onPreferencesChange(preferences.copy(lyricsWifiOnly = it)) }
            )

            MusicSettingSwitchRow(
                title = "Keep Screen On in Lyrics View",
                subtitle = "Prevents device sleep while viewing song lyrics",
                checked = preferences.keepScreenOnInLyrics,
                onCheckedChange = { onPreferencesChange(preferences.copy(keepScreenOnInLyrics = it)) }
            )

            // Clear Lyrics Cache
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClearLyricsCache() }
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = NPWhite.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Clear Lyrics Cache", color = NPWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text("Remove all cached lyrics stored on this device", color = NPWhite.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun MusicSettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = NPWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = NPWhite.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NPBlack,
                checkedTrackColor = NPYellow,
                uncheckedThumbColor = NPWhite.copy(alpha = 0.6f),
                uncheckedTrackColor = NPWhite.copy(alpha = 0.2f)
            )
        )
    }
}
