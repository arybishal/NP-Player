package com.npplayer.player.ui.gesture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun VolumeHUD(
    volumePercent: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        val isBoosted = volumePercent > 100
        val accentColor = if (isBoosted) NPYellow else NPWhite

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = NPBlack.copy(alpha = 0.85f),
            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(56.dp)
                    .height(180.dp)
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )

                // Vertical Progress Gauge
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(90.dp)
                        .background(NPWhite.copy(alpha = 0.20f), shape = RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val fillFraction = (volumePercent.toFloat() / 200f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .fillMaxHeight(fillFraction)
                            .background(accentColor, shape = RoundedCornerShape(3.dp))
                    )
                }

                Text(
                    text = if (isBoosted) "+$volumePercent%" else "$volumePercent%",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BrightnessHUD(
    brightnessPercent: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = NPBlack.copy(alpha = 0.85f),
            border = androidx.compose.foundation.BorderStroke(1.dp, NPYellow.copy(alpha = 0.35f)),
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(56.dp)
                    .height(180.dp)
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Outlined.BrightnessHigh,
                    contentDescription = null,
                    tint = NPYellow,
                    modifier = Modifier.size(24.dp)
                )

                // Vertical Progress Gauge
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(90.dp)
                        .background(NPWhite.copy(alpha = 0.20f), shape = RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val fillFraction = (brightnessPercent.toFloat() / 100f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .fillMaxHeight(fillFraction)
                            .background(NPYellow, shape = RoundedCornerShape(3.dp))
                    )
                }

                Text(
                    text = "$brightnessPercent%",
                    color = NPYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SpeedBurstBadge(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(NPYellow, shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "2X Speed ▶▶",
                color = NPBlack,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun DoubleTapSeekRipple(
    isForward: Boolean,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(NPBlack.copy(alpha = 0.70f), shape = CircleShape)
                .padding(20.dp)
        ) {
            Icon(
                imageVector = if (isForward) Icons.Filled.FastForward else Icons.Filled.FastRewind,
                contentDescription = null,
                tint = NPYellow,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = if (isForward) "+10s" else "-10s",
                color = NPYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun SeekUndoSnackBar(
    visible: Boolean,
    targetPositionFormatted: String,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NPBlack.copy(alpha = 0.90f),
            border = androidx.compose.foundation.BorderStroke(1.dp, NPYellow.copy(alpha = 0.40f)),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jumped to $targetPositionFormatted",
                    color = NPWhite,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "UNDO",
                    color = NPYellow,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onUndo)
                )
            }
        }
    }
}

@Composable
fun StartOverPrompt(
    visible: Boolean,
    resumedPositionFormatted: String,
    onStartOver: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NPBlack.copy(alpha = 0.90f),
            border = androidx.compose.foundation.BorderStroke(1.dp, NPWhite.copy(alpha = 0.20f)),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resumed at $resumedPositionFormatted  •  ",
                    color = NPWhite.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
                Text(
                    text = "START OVER",
                    color = NPYellow,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onStartOver)
                )
            }
        }
    }
}
