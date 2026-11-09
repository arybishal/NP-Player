package com.npplayer.player.ui.sheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.player.audio.AudioFxManager
import com.npplayer.player.audio.AudioPreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerBottomSheet(
    audioFxManager: AudioFxManager,
    onDismiss: () -> Unit
) {
    val state by audioFxManager.state.collectAsState()
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Title & Master Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Audio & Equalizer",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NPWhite
                    )
                    Text(
                        text = "Hardware 5-Band DSP & Bass Boost",
                        fontSize = 12.sp,
                        color = NPWhite.copy(alpha = 0.60f)
                    )
                }

                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = { audioFxManager.setEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NPBlack,
                        checkedTrackColor = NPYellow,
                        uncheckedThumbColor = NPWhite.copy(alpha = 0.40f),
                        uncheckedTrackColor = NPWhite.copy(alpha = 0.15f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets chips
            Text(
                text = "PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NPYellow
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AudioPreset.values().forEach { preset ->
                    val isSelected = state.selectedPreset == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = { audioFxManager.setPreset(preset) },
                        label = {
                            Text(
                                text = preset.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NPBlack else NPWhite
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = NPBlack,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = NPWhite.copy(alpha = 0.08f),
                            selectedContainerColor = NPYellow
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = NPWhite.copy(alpha = 0.15f),
                            selectedBorderColor = NPYellow
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5-Band Equalizer Sliders
            Text(
                text = "FREQUENCY BANDS (-10 dB to +10 dB)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NPWhite.copy(alpha = 0.70f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
            bandLabels.forEachIndexed { index, label ->
                val level = state.bandLevels.getOrElse(index) { 0 }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NPWhite,
                        modifier = Modifier.width(64.dp)
                    )

                    Slider(
                        value = level.toFloat(),
                        onValueChange = { audioFxManager.setBandLevel(index, it.toInt()) },
                        valueRange = -10f..10f,
                        steps = 19,
                        enabled = state.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = NPYellow,
                            activeTrackColor = NPYellow,
                            inactiveTrackColor = NPWhite.copy(alpha = 0.20f)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = if (level > 0) "+$level dB" else "$level dB",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (level != 0) NPYellow else NPWhite.copy(alpha = 0.60f),
                        modifier = Modifier.width(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bass Boost Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BASS BOOST",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NPWhite.copy(alpha = 0.70f)
                )
                Text(
                    text = "${state.bassBoostStrengthPercent}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NPWhite
                )
            }
            Slider(
                value = state.bassBoostStrengthPercent.toFloat(),
                onValueChange = { audioFxManager.setBassBoost(it.toInt()) },
                valueRange = 0f..100f,
                enabled = state.isEnabled,
                colors = SliderDefaults.colors(
                    thumbColor = NPYellow,
                    activeTrackColor = NPYellow,
                    inactiveTrackColor = NPWhite.copy(alpha = 0.20f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Delay Sync (to correct Bluetooth / AV sync)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AUDIO DELAY SYNC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NPWhite.copy(alpha = 0.70f)
                    )
                    Text(
                        text = "Calibrate Bluetooth latency or A/V sync desync",
                        fontSize = 11.sp,
                        color = NPWhite.copy(alpha = 0.50f)
                    )
                }
                if (state.audioDelayMs != 0L) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NPYellow
                    ) {
                        Text(
                            text = if (state.audioDelayMs > 0) "+${state.audioDelayMs} ms" else "${state.audioDelayMs} ms",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NPBlack,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = "0 ms",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NPWhite
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { audioFxManager.setAudioDelayMs(state.audioDelayMs - 50L) },
                    shape = RoundedCornerShape(8.dp),
                    color = NPWhite.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, NPWhite.copy(alpha = 0.20f))
                ) {
                    Text(
                        text = "-50ms",
                        color = NPWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Slider(
                    value = state.audioDelayMs.toFloat(),
                    onValueChange = { audioFxManager.setAudioDelayMs(it.toLong()) },
                    valueRange = -500f..500f,
                    colors = SliderDefaults.colors(
                        thumbColor = NPYellow,
                        activeTrackColor = NPYellow,
                        inactiveTrackColor = NPWhite.copy(alpha = 0.20f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                Surface(
                    onClick = { audioFxManager.setAudioDelayMs(state.audioDelayMs + 50L) },
                    shape = RoundedCornerShape(8.dp),
                    color = NPWhite.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, NPWhite.copy(alpha = 0.20f))
                ) {
                    Text(
                        text = "+50ms",
                        color = NPWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Night Mode / Dialogue Boost Tile
            Surface(
                onClick = { audioFxManager.toggleNightMode() },
                shape = RoundedCornerShape(12.dp),
                color = if (state.isNightMode) NPYellow.copy(alpha = 0.15f) else NPWhite.copy(alpha = 0.06f),
                border = BorderStroke(1.dp, if (state.isNightMode) NPYellow else NPWhite.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Nightlight,
                        contentDescription = null,
                        tint = if (state.isNightMode) NPYellow else NPWhite,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Night Mode / Speech Clarity",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NPWhite
                        )
                        Text(
                            text = "Tames loud explosions and boosts actor whispers",
                            fontSize = 11.sp,
                            color = NPWhite.copy(alpha = 0.60f)
                        )
                    }
                    Switch(
                        checked = state.isNightMode,
                        onCheckedChange = { audioFxManager.toggleNightMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NPBlack,
                            checkedTrackColor = NPYellow,
                            uncheckedThumbColor = NPWhite.copy(alpha = 0.40f),
                            uncheckedTrackColor = NPWhite.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    }
}
