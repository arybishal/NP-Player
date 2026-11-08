package com.npplayer.player.ui.slots

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Flip
import androidx.compose.material.icons.outlined.Hd
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.MotionPhotosOn
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.PictureInPicture
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.SettingsSystemDaydream
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.npplayer.core.data.preferences.PlayerAction
import com.npplayer.core.ui.theme.NPWhite

fun getActionIcon(action: PlayerAction): ImageVector {
    return when (action) {
        PlayerAction.INFO -> Icons.Outlined.Info
        PlayerAction.RECORD -> Icons.Outlined.FiberManualRecord
        PlayerAction.SCREENSHOT -> Icons.Outlined.CameraAlt
        PlayerAction.NIGHT_DIM -> Icons.Outlined.Nightlight
        PlayerAction.AB_REPEAT -> Icons.Outlined.Repeat
        PlayerAction.PLAYLIST -> Icons.AutoMirrored.Outlined.PlaylistPlay
        PlayerAction.LOCK -> Icons.Outlined.Lock
        PlayerAction.SUBTITLES -> Icons.Outlined.Subtitles
        PlayerAction.AUDIO_FX -> Icons.Outlined.Equalizer
        PlayerAction.SPEED -> Icons.Outlined.Speed
        PlayerAction.FRAMERATE_MEMC -> Icons.Outlined.MotionPhotosOn
        PlayerAction.BATTERY_SAVER -> Icons.Outlined.BatteryChargingFull
        PlayerAction.QUALITY_HD -> Icons.Outlined.Hd
        PlayerAction.AUDIO_TRACK -> Icons.Outlined.Translate
        PlayerAction.ASPECT_RATIO -> Icons.Outlined.AspectRatio
        PlayerAction.PIP -> Icons.Outlined.PictureInPicture
        PlayerAction.ORIENTATION -> Icons.Outlined.ScreenRotation
        PlayerAction.SLEEP_TIMER -> Icons.Outlined.Bedtime
        PlayerAction.BACKGROUND_PLAY -> Icons.Outlined.Headphones
        PlayerAction.AUDIO_ONLY -> Icons.Outlined.MusicNote
        PlayerAction.MIRROR_FLIP -> Icons.Outlined.Flip
        PlayerAction.MUTE -> Icons.Outlined.VolumeOff
        PlayerAction.MORE -> Icons.Outlined.MoreVert
        PlayerAction.DECODER_MODE -> Icons.Outlined.SettingsSystemDaydream
    }
}

@Composable
fun PlayerActionIconButton(
    action: PlayerAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = NPWhite,
    iconSize: Dp = 24.dp
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = getActionIcon(action),
            contentDescription = action.title,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
