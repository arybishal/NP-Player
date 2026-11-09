package com.npplayer.feature.library.music

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TimerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.repository.SleepTimerOption
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.core.ui.util.FormatUtils

@Composable
fun MusicSleepTimerDialog(
    remainingMs: Long?,
    activeOption: SleepTimerOption?,
    onSetTimer: (SleepTimerOption) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    var showCustomSlider by remember { mutableStateOf(false) }
    var customMinutes by remember { mutableFloatStateOf(20f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = NPYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Sleep Timer",
                    color = NPWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (remainingMs != null || activeOption != null) {
                    val statusText = when {
                        remainingMs != null -> {
                            val seconds = (remainingMs / 1000) % 60
                            val minutes = (remainingMs / (1000 * 60)) % 60
                            val hours = (remainingMs / (1000 * 60 * 60))
                            val formatted = if (hours > 0) {
                                String.format("%02d:%02d:%02d", hours, minutes, seconds)
                            } else {
                                String.format("%02d:%02d", minutes, seconds)
                            }
                            "Timer active: $formatted remaining (30s fade-out)"
                        }
                        activeOption == SleepTimerOption.EndOfSong -> "Timer active: stops at end of song"
                        activeOption == SleepTimerOption.EndOfQueue -> "Timer active: stops at end of queue"
                        else -> "Timer active"
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NPYellow.copy(alpha = 0.15f))
                            .border(1.dp, NPYellow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = NPYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onCancelTimer()
                                onDismiss()
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TimerOff,
                            contentDescription = null,
                            tint = NPWhite.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Turn off timer",
                            color = NPWhite,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Or reset to new time:",
                        color = NPWhite.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Preset options
                val presets = listOf(15, 30, 45, 60)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { mins ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NPWhite.copy(alpha = 0.08f))
                                .border(1.dp, NPWhite.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .clickable {
                                    onSetTimer(SleepTimerOption.Minutes(mins))
                                    onDismiss()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}m",
                                color = NPWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // End of song / End of queue
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NPWhite.copy(alpha = 0.08f))
                            .border(1.dp, NPWhite.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .clickable {
                                onSetTimer(SleepTimerOption.EndOfSong)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "End of song",
                            color = NPWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NPWhite.copy(alpha = 0.08f))
                            .border(1.dp, NPWhite.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .clickable {
                                onSetTimer(SleepTimerOption.EndOfQueue)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "End of queue",
                            color = NPWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom minutes toggle
                if (!showCustomSlider) {
                    TextButton(
                        onClick = { showCustomSlider = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Custom time...",
                            color = NPYellow,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NPWhite.copy(alpha = 0.05f))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Custom duration:",
                                color = NPWhite.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${customMinutes.toInt()} minutes",
                                color = NPYellow,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = customMinutes,
                            onValueChange = { customMinutes = it },
                            valueRange = 5f..180f,
                            steps = 34,
                            colors = SliderDefaults.colors(
                                thumbColor = NPYellow,
                                activeTrackColor = NPYellow,
                                inactiveTrackColor = NPWhite.copy(alpha = 0.2f)
                            )
                        )

                        TextButton(
                            onClick = {
                                onSetTimer(SleepTimerOption.Minutes(customMinutes.toInt()))
                                onDismiss()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "Start (${customMinutes.toInt()}m)",
                                color = NPYellow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = NPWhite.copy(alpha = 0.7f))
            }
        },
        containerColor = NPBlack,
        shape = RoundedCornerShape(20.dp)
    )
}
