package com.npplayer.feature.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LinearScale
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.npplayer.core.data.preferences.AppTheme
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToControlsEditor: () -> Unit = {},
    onNavigateToSeekBarStyles: () -> Unit = {},
    onNavigateToAdvancedSettings: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SectionHeader("Appearance")
            }

            item {
                val themeLabel = when (state.currentTheme) {
                    AppTheme.FOLLOW_DEVICE -> "Follow device"
                    AppTheme.LIGHT -> "Light"
                    AppTheme.DARK -> "Dark (AMOLED)"
                }
                SettingsActionRow(
                    icon = Icons.Outlined.ColorLens,
                    title = "App Theme",
                    subtitle = themeLabel,
                    onClick = { viewModel.showThemeDialog() }
                )
            }

            item {
                SectionHeader("Library & List")
            }

            item {
                SettingsSwitchRow(
                    icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                    title = "Floating Resume Button",
                    subtitle = "Show quick-play button on Home screen",
                    checked = state.listPreferences.floatingResumeButton,
                    onCheckedChange = { viewModel.toggleFloatingResumeButton(it) }
                )
            }

            item {
                SectionHeader("Player & Playback")
            }

            item {
                SettingsActionRow(
                    icon = Icons.Outlined.PlayCircleOutline,
                    title = "Player Controls",
                    subtitle = "Customize on-screen player control slots",
                    onClick = onNavigateToControlsEditor
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Outlined.LinearScale,
                    title = "Seek Bar Styles",
                    subtitle = "Select Classic, Chapters, Wave, or Glow style",
                    onClick = onNavigateToSeekBarStyles
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Outlined.Tune,
                    title = "Advanced & Cache",
                    subtitle = "Buffer sizes, engine options, and debug overlay",
                    onClick = onNavigateToAdvancedSettings
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Outlined.Memory,
                    title = "Hardware Decoder",
                    subtitle = "Acceleration mode: ${state.playerPreferences.hardwareAcceleration}",
                    onClick = { /* Decoder selector */ }
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                    title = "Audio & Equalizer",
                    subtitle = "Audio boost, night mode, delay sync",
                    onClick = { /* Audio settings */ }
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Outlined.Subtitles,
                    title = "Subtitles",
                    subtitle = "Font size, position, yellow/white styling",
                    onClick = { /* Subtitle settings */ }
                )
            }

            item {
                SectionHeader("General")
            }

            item {
                SettingsActionRow(
                    icon = Icons.Outlined.Language,
                    title = "App Language",
                    subtitle = "English (default)",
                    onClick = { /* Language dialog */ }
                )
            }
        }

        // Theme Dialog
        if (state.isThemeDialogVisible) {
            ThemeSelectionDialog(
                currentTheme = state.currentTheme,
                onSelectTheme = { viewModel.setTheme(it) },
                onDismiss = { viewModel.dismissThemeDialog() }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = NPYellow,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NPYellow,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NPYellow,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NPBlack,
                    checkedTrackColor = NPYellow,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onSelectTheme: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "App Theme",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                ThemeOptionRow(
                    label = "Follow device (default)",
                    selected = currentTheme == AppTheme.FOLLOW_DEVICE,
                    onClick = { onSelectTheme(AppTheme.FOLLOW_DEVICE) }
                )
                ThemeOptionRow(
                    label = "Light",
                    selected = currentTheme == AppTheme.LIGHT,
                    onClick = { onSelectTheme(AppTheme.LIGHT) }
                )
                ThemeOptionRow(
                    label = "Dark (true AMOLED)",
                    selected = currentTheme == AppTheme.DARK,
                    onClick = { onSelectTheme(AppTheme.DARK) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CANCEL", color = NPYellow, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = NPYellow,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
