package com.npplayer.feature.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.npplayer.core.data.preferences.SeekBarStylePreference
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.player.ui.seekbar.NPSeekBar
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun SeekBarStylesScreen(
    onBack: () -> Unit,
    viewModel: SeekBarStyleViewModel = hiltViewModel()
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
                        text = "Seek Bar Styles",
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Live Interactive "Drag to Try" Sandbox Box
            item {
                Text(
                    text = "Live \"Drag to Try\" Sandbox",
                    style = MaterialTheme.typography.labelLarge,
                    color = NPYellow,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NPBlack,
                    border = BorderStroke(1.5.dp, NPYellow.copy(alpha = 0.50f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatDuration(state.sandboxPositionMs),
                                color = NPWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatDuration(state.sandboxDurationMs),
                                color = NPWhite.copy(alpha = 0.70f),
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        NPSeekBar(
                            style = state.selectedStyle,
                            currentPositionMs = state.sandboxPositionMs,
                            durationMs = state.sandboxDurationMs,
                            bufferedPositionMs = 600000L,
                            isPlaying = true,
                            onSeek = { viewModel.updateSandboxPosition(it) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Drag the thumb or tap along the bar to test responsiveness and magnetic snapping.",
                            color = NPWhite.copy(alpha = 0.60f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // 2. Style Options
            item {
                Text(
                    text = "Select Player Style",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                SeekBarStyleCard(
                    title = "Classic",
                    subtitle = "Thin yellow line with clean round handle. Minimalist, fluid, and zero visual friction.",
                    selected = state.selectedStyle == SeekBarStylePreference.CLASSIC,
                    onClick = { viewModel.selectStyle(SeekBarStylePreference.CLASSIC) }
                )
            }

            item {
                SeekBarStyleCard(
                    title = "Chapters",
                    subtitle = "Segmented scene markers with magnetic snapping. Current chapter segment swells in height.",
                    selected = state.selectedStyle == SeekBarStylePreference.CHAPTERS,
                    onClick = { viewModel.selectStyle(SeekBarStylePreference.CHAPTERS) }
                )
            }

            item {
                SeekBarStyleCard(
                    title = "Wave",
                    subtitle = "Dynamic sine wave soundwave oscillations that ripple along played track at playback speed.",
                    selected = state.selectedStyle == SeekBarStylePreference.WAVE,
                    onClick = { viewModel.selectStyle(SeekBarStylePreference.WAVE) }
                )
            }

            item {
                SeekBarStyleCard(
                    title = "Glow (AMOLED)",
                    subtitle = "High-tech radial illumination halo and diffused light trail tailored for deep black screens.",
                    selected = state.selectedStyle == SeekBarStylePreference.GLOW,
                    onClick = { viewModel.selectStyle(SeekBarStylePreference.GLOW) }
                )
            }
        }
    }
}

@Composable
private fun SeekBarStyleCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.5.dp,
            if (selected) NPYellow else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
