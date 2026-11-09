package com.npplayer.feature.settings.ui

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
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.npplayer.core.data.preferences.SubtitlePreferences
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun SubtitleSettingsDialog(
    preferences: SubtitlePreferences,
    onUpdatePreferences: (SubtitlePreferences) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Subtitles,
                    contentDescription = null,
                    tint = NPYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Subtitle Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Color Selection
                Column {
                    Text(
                        text = "TEXT COLOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = preferences.color == "YELLOW",
                            onClick = { onUpdatePreferences(preferences.copy(color = "YELLOW")) },
                            label = {
                                Text(
                                    text = "High-Contrast Yellow",
                                    fontWeight = if (preferences.color == "YELLOW") FontWeight.Bold else FontWeight.Normal,
                                    color = if (preferences.color == "YELLOW") NPBlack else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NPYellow,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        FilterChip(
                            selected = preferences.color == "WHITE",
                            onClick = { onUpdatePreferences(preferences.copy(color = "WHITE")) },
                            label = {
                                Text(
                                    text = "Crisp White",
                                    fontWeight = if (preferences.color == "WHITE") FontWeight.Bold else FontWeight.Normal,
                                    color = if (preferences.color == "WHITE") NPBlack else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NPYellow,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                // Font Size Selection
                Column {
                    Text(
                        text = "FONT SIZE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(14 to "Small", 18 to "Normal", 22 to "Large", 28 to "X-Large").forEach { (size, label) ->
                            val isSelected = preferences.sizeSp == size
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdatePreferences(preferences.copy(sizeSp = size)) },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NPBlack else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NPYellow,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }

                // Preferred Online Subtitle Languages (Multi-select)
                Column {
                    Text(
                        text = "ONLINE SUBTITLE LANGUAGES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val langOptions = listOf(
                        "en" to "English",
                        "es" to "Spanish",
                        "hi" to "Hindi",
                        "ne" to "Nepali",
                        "fr" to "French",
                        "de" to "German",
                        "ar" to "Arabic",
                        "zh" to "Chinese",
                        "ja" to "Japanese"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        langOptions.forEach { (code, name) ->
                            val isSelected = preferences.preferredLanguages.contains(code)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val updated = preferences.preferredLanguages.toMutableSet()
                                    if (isSelected) {
                                        if (updated.size > 1) updated.remove(code)
                                    } else {
                                        updated.add(code)
                                    }
                                    onUpdatePreferences(preferences.copy(preferredLanguages = updated))
                                },
                                label = {
                                    Text(
                                        text = "$name (${code.uppercase()})",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NPBlack else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NPYellow,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }

                // Auto-Detect Sibling Files Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Load Sibling Subtitles",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Scans video folder for .srt, .vtt, .ass",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = preferences.autoDetectSibling,
                            onCheckedChange = { onUpdatePreferences(preferences.copy(autoDetectSibling = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NPBlack,
                                checkedTrackColor = NPYellow,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                // Auto-Search Online When No Subtitles
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Search SubDL Online",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Auto-queries SubDL if video has 0 subtitles",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = preferences.autoSearchOnline,
                            onCheckedChange = { onUpdatePreferences(preferences.copy(autoSearchOnline = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NPBlack,
                                checkedTrackColor = NPYellow,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Done", color = NPYellow, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
