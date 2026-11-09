package com.npplayer.player.ui.sheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.preferences.PicturePreset
import com.npplayer.core.data.preferences.PictureSettings
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.player.effects.PictureEffectManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PictureQualityBottomSheet(
    pictureEffectManager: PictureEffectManager,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settings by pictureEffectManager.currentSettings.collectAsState()
    val isSupported by pictureEffectManager.isEffectsSupported.collectAsState()

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
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Picture Quality",
                        color = NPWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Reset Button
                Surface(
                    onClick = { pictureEffectManager.reset() },
                    shape = RoundedCornerShape(14.dp),
                    color = NPWhite.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, NPWhite.copy(alpha = 0.16f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = "Reset",
                            tint = NPYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reset",
                            color = NPWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (!isSupported) {
                // Unsupported Fallback Notice
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NPWhite.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, NPYellow.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = NPYellow,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Video effects pipeline is not supported on this decoder or hardware configuration. Native video playback is active.",
                            color = NPWhite.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                // Presets Horizontal Row
                Text(
                    text = "PRESETS",
                    color = NPWhite.copy(alpha = 0.50f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PicturePreset.values()) { preset ->
                        val isSelected = settings.activePreset == preset
                        Surface(
                            onClick = { pictureEffectManager.selectPreset(preset) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) NPYellow else NPWhite.copy(alpha = 0.08f),
                            border = if (isSelected) null else BorderStroke(1.dp, NPWhite.copy(alpha = 0.16f)),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.title,
                                    color = if (isSelected) NPBlack else NPWhite,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Adjustments Section
                Text(
                    text = "ADJUSTMENTS",
                    color = NPWhite.copy(alpha = 0.50f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // 1. Brightness (-100 to +100)
                PictureSliderRow(
                    label = "Brightness",
                    value = settings.brightnessUi,
                    displayValue = if (settings.brightnessUi > 0) "+${settings.brightnessUi}" else "${settings.brightnessUi}",
                    min = -100,
                    max = 100,
                    onLiveChange = { v ->
                        pictureEffectManager.updateLive(
                            settings.copy(brightness = v / 100f, activePreset = PicturePreset.DEFAULT)
                        )
                    },
                    onCommit = { v ->
                        pictureEffectManager.commit(
                            settings.copy(brightness = v / 100f, activePreset = PicturePreset.DEFAULT)
                        )
                    }
                )

                // 2. Contrast (-100 to +100)
                PictureSliderRow(
                    label = "Contrast",
                    value = settings.contrastUi,
                    displayValue = if (settings.contrastUi > 0) "+${settings.contrastUi}" else "${settings.contrastUi}",
                    min = -100,
                    max = 100,
                    onLiveChange = { v ->
                        pictureEffectManager.updateLive(
                            settings.copy(contrast = 1.0f + (v / 100f), activePreset = PicturePreset.DEFAULT)
                        )
                    },
                    onCommit = { v ->
                        pictureEffectManager.commit(
                            settings.copy(contrast = 1.0f + (v / 100f), activePreset = PicturePreset.DEFAULT)
                        )
                    }
                )

                // 3. Saturation (-100 to +100)
                PictureSliderRow(
                    label = "Saturation",
                    value = settings.saturationUi,
                    displayValue = if (settings.saturationUi > 0) "+${settings.saturationUi}" else "${settings.saturationUi}",
                    min = -100,
                    max = 100,
                    onLiveChange = { v ->
                        pictureEffectManager.updateLive(
                            settings.copy(saturation = 1.0f + (v / 100f), activePreset = PicturePreset.DEFAULT)
                        )
                    },
                    onCommit = { v ->
                        pictureEffectManager.commit(
                            settings.copy(saturation = 1.0f + (v / 100f), activePreset = PicturePreset.DEFAULT)
                        )
                    }
                )

                // 4. Gamma (0.5 to 2.5)
                PictureFloatSliderRow(
                    label = "Gamma",
                    value = settings.gammaUi,
                    displayValue = "${settings.gammaUi}x",
                    min = 0.5f,
                    max = 2.5f,
                    onLiveChange = { v ->
                        pictureEffectManager.updateLive(
                            settings.copy(gamma = v, activePreset = PicturePreset.DEFAULT)
                        )
                    },
                    onCommit = { v ->
                        pictureEffectManager.commit(
                            settings.copy(gamma = v, activePreset = PicturePreset.DEFAULT)
                        )
                    }
                )

                // 5. Sharpness (0 to 100)
                PictureSliderRow(
                    label = "Sharpness",
                    value = settings.sharpnessUi,
                    displayValue = "${settings.sharpnessUi}%",
                    min = 0,
                    max = 100,
                    onLiveChange = { v ->
                        pictureEffectManager.updateLive(
                            settings.copy(sharpness = v / 100f, activePreset = PicturePreset.DEFAULT)
                        )
                    },
                    onCommit = { v ->
                        pictureEffectManager.commit(
                            settings.copy(sharpness = v / 100f, activePreset = PicturePreset.DEFAULT)
                        )
                    }
                )

                // 6. Warm / Night-light filter (0 to 100%)
                PictureSliderRow(
                    label = "Warm Night Filter",
                    value = settings.warmthUi,
                    displayValue = "${settings.warmthUi}%",
                    min = 0,
                    max = 100,
                    onLiveChange = { v ->
                        pictureEffectManager.updateLive(
                            settings.copy(warmth = v / 100f, activePreset = PicturePreset.DEFAULT)
                        )
                    },
                    onCommit = { v ->
                        pictureEffectManager.commit(
                            settings.copy(warmth = v / 100f, activePreset = PicturePreset.DEFAULT)
                        )
                    }
                )

                // 7. Hue (-180 to +180)
                PictureSliderRow(
                    label = "Hue Tint",
                    value = settings.hueUi,
                    displayValue = "${settings.hueUi}°",
                    min = -180,
                    max = 180,
                    onLiveChange = { v ->
                        pictureEffectManager.updateLive(
                            settings.copy(hue = v.toFloat(), activePreset = PicturePreset.DEFAULT)
                        )
                    },
                    onCommit = { v ->
                        pictureEffectManager.commit(
                            settings.copy(hue = v.toFloat(), activePreset = PicturePreset.DEFAULT)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Persistence Toggles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
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
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = settings.rememberPerVideo,
                        onCheckedChange = { checked ->
                            pictureEffectManager.commit(settings.copy(rememberPerVideo = checked))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
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
                            text = "Captured screenshot includes active GPU color filters",
                            color = NPWhite.copy(alpha = 0.50f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = settings.includeFiltersInScreenshot,
                        onCheckedChange = { checked ->
                            pictureEffectManager.commit(settings.copy(includeFiltersInScreenshot = checked))
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
        }
    }
}

@Composable
private fun PictureSliderRow(
    label: String,
    value: Int,
    displayValue: String,
    min: Int,
    max: Int,
    onLiveChange: (Int) -> Unit,
    onCommit: (Int) -> Unit
) {
    var localValue by remember(value) { mutableIntStateOf(value) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = NPWhite.copy(alpha = 0.90f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = displayValue,
                color = NPYellow,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = localValue.toFloat(),
            onValueChange = { newVal ->
                val intVal = newVal.toInt().coerceIn(min, max)
                localValue = intVal
                onLiveChange(intVal)
            },
            onValueChangeFinished = {
                onCommit(localValue)
            },
            valueRange = min.toFloat()..max.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = NPYellow,
                activeTrackColor = NPYellow,
                inactiveTrackColor = NPWhite.copy(alpha = 0.20f)
            ),
            modifier = Modifier.fillMaxWidth().height(32.dp)
        )
    }
}

@Composable
private fun PictureFloatSliderRow(
    label: String,
    value: Float,
    displayValue: String,
    min: Float,
    max: Float,
    onLiveChange: (Float) -> Unit,
    onCommit: (Float) -> Unit
) {
    var localValue by remember(value) { mutableFloatStateOf(value) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = NPWhite.copy(alpha = 0.90f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = displayValue,
                color = NPYellow,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = localValue,
            onValueChange = { newVal ->
                val rounded = (newVal * 10).toInt() / 10f
                localValue = rounded
                onLiveChange(rounded)
            },
            onValueChangeFinished = {
                onCommit(localValue)
            },
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = NPYellow,
                activeTrackColor = NPYellow,
                inactiveTrackColor = NPWhite.copy(alpha = 0.20f)
            ),
            modifier = Modifier.fillMaxWidth().height(32.dp)
        )
    }
}
