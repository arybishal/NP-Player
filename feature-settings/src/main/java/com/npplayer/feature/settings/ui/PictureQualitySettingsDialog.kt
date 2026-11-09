package com.npplayer.feature.settings.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.preferences.PicturePreset
import com.npplayer.core.data.preferences.PictureSettings
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun PictureQualitySettingsDialog(
    settings: PictureSettings,
    onUpdateSettings: (PictureSettings) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NPBlack,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = null,
                    tint = NPYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Picture & Quality",
                    color = NPWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DEFAULT PRESET",
                    color = NPWhite.copy(alpha = 0.50f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                PicturePreset.values().forEach { preset ->
                    val isSelected = settings.activePreset == preset
                    Surface(
                        onClick = {
                            val newSettings = PictureSettings.fromPreset(preset).copy(
                                rememberPerVideo = settings.rememberPerVideo,
                                includeFiltersInScreenshot = settings.includeFiltersInScreenshot
                            )
                            onUpdateSettings(newSettings)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NPYellow.copy(alpha = 0.15f) else NPWhite.copy(alpha = 0.04f),
                        border = if (isSelected) BorderStroke(1.dp, NPYellow) else null,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.title,
                                color = if (isSelected) NPYellow else NPWhite,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = NPYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "BEHAVIOR",
                    color = NPWhite.copy(alpha = 0.50f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Remember per video",
                            color = NPWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Save color tuning separately for each video",
                            color = NPWhite.copy(alpha = 0.50f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = settings.rememberPerVideo,
                        onCheckedChange = { checked ->
                            onUpdateSettings(settings.copy(rememberPerVideo = checked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NPBlack,
                            checkedTrackColor = NPYellow,
                            uncheckedThumbColor = NPWhite.copy(alpha = 0.6f),
                            uncheckedTrackColor = NPWhite.copy(alpha = 0.15f)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Include filters in screenshots",
                            color = NPWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Screenshots capture active GPU shader filters",
                            color = NPWhite.copy(alpha = 0.50f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = settings.includeFiltersInScreenshot,
                        onCheckedChange = { checked ->
                            onUpdateSettings(settings.copy(includeFiltersInScreenshot = checked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NPBlack,
                            checkedTrackColor = NPYellow,
                            uncheckedThumbColor = NPWhite.copy(alpha = 0.6f),
                            uncheckedTrackColor = NPWhite.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Done", color = NPYellow, fontWeight = FontWeight.Bold)
            }
        }
    )
}
