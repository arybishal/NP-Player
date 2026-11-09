package com.npplayer.player.ui.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.preferences.AspectRatioMode
import com.npplayer.core.data.preferences.AspectRatioPreferences
import com.npplayer.core.data.preferences.AspectRatioPreset
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.core.ui.theme.NPWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AspectRatioBottomSheet(
    currentPreferences: AspectRatioPreferences,
    isZoomed: Boolean,
    onSelectMode: (AspectRatioMode) -> Unit,
    onSelectPreset: (AspectRatioPreset) -> Unit,
    onSetCustomRatio: (Float) -> Unit,
    onToggleRememberPerVideo: (Boolean) -> Unit,
    onResetZoom: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    var customInputText by remember {
        mutableStateOf(String.format(java.util.Locale.US, "%.2f", currentPreferences.customRatioValue))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141414),
        contentColor = NPWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AspectRatio,
                    contentDescription = null,
                    tint = NPYellow,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Aspect Ratio & Zoom",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NPWhite
                )

                if (isZoomed) {
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF262626))
                            .clickable { onResetZoom() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = null,
                            tint = NPYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reset Zoom",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NPWhite
                        )
                    }
                }
            }

            // Mode rows
            AspectRatioMode.entries.forEach { mode ->
                val isSelected = currentPreferences.mode == mode
                val subtitle = when (mode) {
                    AspectRatioMode.FIT -> "Maintain aspect ratio with black borders"
                    AspectRatioMode.STRETCH -> "Fill screen completely, distorting aspect ratio"
                    AspectRatioMode.CROP -> "Fill screen, maintaining ratio by cropping edges"
                    AspectRatioMode.ORIGINAL_100 -> "Original native video pixels centered 1:1"
                    AspectRatioMode.CUSTOM -> "Scale to specific aspect ratio preset or numeric ratio"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF222222) else Color.Transparent)
                        .clickable { onSelectMode(mode) }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = if (isSelected) NPYellow else Color(0x66FFFFFF),
                                shape = CircleShape
                            )
                            .background(if (isSelected) NPYellow else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NPBlack)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.displayName,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) NPYellow else NPWhite
                        )
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = Color(0x99FFFFFF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // If CUSTOM mode is selected: show presets and custom numeric input
            if (currentPreferences.mode == AspectRatioMode.CUSTOM) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ratio Presets",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xCCFFFFFF)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatioPreset.PRESETS.forEach { preset ->
                        val isPresetSelected = currentPreferences.customPreset == preset.label
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isPresetSelected) NPYellow else Color(0xFF222222))
                                .clickable { onSelectPreset(preset) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = preset.label,
                                fontSize = 13.sp,
                                fontWeight = if (isPresetSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isPresetSelected) NPBlack else NPWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom numeric input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customInputText,
                        onValueChange = { customInputText = it },
                        label = { Text("Custom ratio (e.g. 2.39, 1.85)", color = Color(0x99FFFFFF), fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                val parsed = customInputText.toFloatOrNull()
                                if (parsed != null && parsed > 0.1f && parsed < 10f) {
                                    onSetCustomRatio(parsed)
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NPWhite,
                            unfocusedTextColor = NPWhite,
                            focusedBorderColor = NPYellow,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            cursorColor = NPYellow
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NPYellow)
                            .clickable {
                                focusManager.clearFocus()
                                val parsed = customInputText.toFloatOrNull()
                                if (parsed != null && parsed > 0.1f && parsed < 10f) {
                                    onSetCustomRatio(parsed)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "Apply",
                            color = NPBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Remember choice per video
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Remember for this video",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = NPWhite
                    )
                    Text(
                        text = "Restore this aspect ratio automatically next time",
                        fontSize = 12.sp,
                        color = Color(0x88FFFFFF)
                    )
                }

                Switch(
                    checked = currentPreferences.rememberPerVideo,
                    onCheckedChange = onToggleRememberPerVideo,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NPBlack,
                        checkedTrackColor = NPYellow,
                        uncheckedThumbColor = Color(0x88FFFFFF),
                        uncheckedTrackColor = Color(0xFF333333)
                    )
                )
            }
        }
    }
}
