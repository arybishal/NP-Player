package com.npplayer.feature.settings.ui

import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.npplayer.core.data.preferences.AppTheme
import com.npplayer.core.data.preferences.AudioPreferences
import com.npplayer.core.data.preferences.ResumePromptPreference
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.player.ui.sheet.EqualizerBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToControlsEditor: () -> Unit = {},
    onNavigateToSeekBarStyles: () -> Unit = {},
    onNavigateToAdvancedSettings: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().height(64.dp),
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
                    icon = Icons.Outlined.Tune,
                    title = "Picture Quality & Presets",
                    subtitle = "Preset: ${state.pictureSettings.activePreset.title} • GPU video effects",
                    onClick = { viewModel.showPictureDialog() }
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Outlined.Memory,
                    title = "Hardware Decoder",
                    subtitle = "Mode: ${state.playerPreferences.hardwareAcceleration} • Codecs & Fallback",
                    onClick = { viewModel.showDecoderDialog() }
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                    title = "Audio & Equalizer",
                    subtitle = "5-band EQ, bass boost, night mode, A/V sync",
                    onClick = { viewModel.showEqualizerSheet() }
                )
            }

            item {
                val audioSummary = if (state.audioPreferences.rememberAudioTrack) "Remember track • Apply to folder" else "Track memory disabled"
                SettingsActionRow(
                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                    title = "Audio Tracks & Memory",
                    subtitle = audioSummary,
                    onClick = { viewModel.showAudioPreferencesDialog() }
                )
            }

            item {
                val musicSummary = "Gapless, crossfade (${state.musicPreferences.crossfadeSec}s), LRCLIB lyrics & headset"
                SettingsActionRow(
                    icon = Icons.Outlined.MusicNote,
                    title = "Music & Playback",
                    subtitle = musicSummary,
                    onClick = { viewModel.showMusicSettings() }
                )
            }

            item {
                val subText = "${if (state.subtitlePreferences.color == "YELLOW") "Yellow" else "White"} text • ${state.subtitlePreferences.sizeSp}sp"
                SettingsActionRow(
                    icon = Icons.Outlined.Subtitles,
                    title = "Subtitles",
                    subtitle = subText,
                    onClick = { viewModel.showSubtitleDialog() }
                )
            }

            item {
                SectionHeader("General")
            }

            item {
                val resumeLabel = when (state.resumePromptPreference) {
                    ResumePromptPreference.ASK -> "Ask me (default)"
                    ResumePromptPreference.ALWAYS_RESUME -> "Always resume"
                    ResumePromptPreference.ALWAYS_START_OVER -> "Always start from the beginning"
                }
                SettingsActionRow(
                    icon = Icons.Outlined.Restore,
                    title = "When a video has a saved position",
                    subtitle = resumeLabel,
                    onClick = { viewModel.showResumePromptDialog() }
                )
            }

            item {
                val currentLangName = SupportedLanguages.firstOrNull { it.code.equals(state.appLanguage, ignoreCase = true) }?.nativeName ?: "English"
                SettingsActionRow(
                    icon = Icons.Outlined.Language,
                    title = "App Language",
                    subtitle = currentLangName,
                    onClick = { viewModel.showLanguageDialog() }
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

        // Resume Prompt Dialog
        if (state.isResumePromptDialogVisible) {
            ResumePromptSettingDialog(
                currentPreference = state.resumePromptPreference,
                onSelectPreference = { viewModel.setResumePromptPreference(it) },
                onDismiss = { viewModel.dismissResumePromptDialog() }
            )
        }

        // Hardware Decoder Dialog
        if (state.isDecoderDialogVisible) {
            HardwareDecoderDialog(
                currentMode = state.playerPreferences.hardwareAcceleration,
                onSelectMode = { viewModel.setHardwareAcceleration(it) },
                onDismiss = { viewModel.dismissDecoderDialog() }
            )
        }

        // Equalizer Bottom Sheet
        if (state.isEqualizerSheetVisible) {
            EqualizerBottomSheet(
                audioFxManager = viewModel.audioFxManager,
                onDismiss = { viewModel.dismissEqualizerSheet() }
            )
        }

        // Subtitle Settings Dialog
        if (state.isSubtitleDialogVisible) {
            SubtitleSettingsDialog(
                preferences = state.subtitlePreferences,
                onUpdatePreferences = { viewModel.updateSubtitlePreferences(it) },
                onDismiss = { viewModel.dismissSubtitleDialog() }
            )
        }

        // Language Selection Dialog
        if (state.isLanguageDialogVisible) {
            LanguageSelectionDialog(
                currentLanguageCode = state.appLanguage,
                onSelectLanguage = { viewModel.setAppLanguage(it) },
                onDismiss = { viewModel.dismissLanguageDialog() }
            )
        }

        // Picture Quality Settings Dialog
        if (state.isPictureDialogVisible) {
            PictureQualitySettingsDialog(
                settings = state.pictureSettings,
                onUpdateSettings = { viewModel.updatePictureSettings(it) },
                onDismiss = { viewModel.dismissPictureDialog() }
            )
        }

        // Audio Preferences Dialog
        if (state.isAudioPreferencesDialogVisible) {
            AudioPreferencesDialog(
                preferences = state.audioPreferences,
                onToggleRemember = { viewModel.toggleRememberAudioTrack(it) },
                onToggleApplyToFolder = { viewModel.toggleApplyAudioToFolder(it) },
                onResetRemembered = {
                    viewModel.resetRememberedAudioTracks {
                        Toast.makeText(context, "Remembered audio tracks reset", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = { viewModel.dismissAudioPreferencesDialog() }
            )
        }

        // Music Settings Sheet
        if (state.isMusicSettingsVisible) {
            val musicSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            MusicSettingsSheet(
                sheetState = musicSheetState,
                preferences = state.musicPreferences,
                onPreferencesChange = { viewModel.updateMusicPreferences(it) },
                onClearLyricsCache = {
                    Toast.makeText(context, "Lyrics cache cleared", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { viewModel.dismissMusicSettings() }
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

@Composable
private fun ResumePromptSettingDialog(
    currentPreference: ResumePromptPreference,
    onSelectPreference: (ResumePromptPreference) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "When a video has a saved position",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                ThemeOptionRow(
                    label = "Ask me (default)",
                    selected = currentPreference == ResumePromptPreference.ASK,
                    onClick = { onSelectPreference(ResumePromptPreference.ASK) }
                )
                ThemeOptionRow(
                    label = "Always resume",
                    selected = currentPreference == ResumePromptPreference.ALWAYS_RESUME,
                    onClick = { onSelectPreference(ResumePromptPreference.ALWAYS_RESUME) }
                )
                ThemeOptionRow(
                    label = "Always start from the beginning",
                    selected = currentPreference == ResumePromptPreference.ALWAYS_START_OVER,
                    onClick = { onSelectPreference(ResumePromptPreference.ALWAYS_START_OVER) }
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
private fun AudioPreferencesDialog(
    preferences: AudioPreferences,
    onToggleRemember: (Boolean) -> Unit,
    onToggleApplyToFolder: (Boolean) -> Unit,
    onResetRemembered: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Audio Tracks & Memory",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Remember audio track",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Remembers chosen audio track for each video file",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = preferences.rememberAudioTrack,
                        onCheckedChange = onToggleRemember,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NPBlack,
                            checkedTrackColor = NPYellow,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Apply to whole folder / series",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (preferences.rememberAudioTrack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "Automatically select this audio language for subsequent episodes in the folder",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (preferences.rememberAudioTrack) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                    Switch(
                        checked = preferences.applyToFolderSeries && preferences.rememberAudioTrack,
                        onCheckedChange = onToggleApplyToFolder,
                        enabled = preferences.rememberAudioTrack,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NPBlack,
                            checkedTrackColor = NPYellow,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Surface(
                    onClick = onResetRemembered,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Restore,
                            contentDescription = null,
                            tint = NPYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reset remembered audio tracks",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "DONE", color = NPYellow, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
