package com.npplayer.feature.library.music

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.database.entity.SongEntity
import com.npplayer.core.ui.component.MusicArtwork
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun MusicMiniPlayer(
    song: SongEntity?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    val progress = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    var dragAccumulatorX by remember { mutableFloatStateOf(0f) }
    var dragAccumulatorY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NPBlack)
            .border(1.dp, NPWhite.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .pointerInput(song.id) {
                detectDragGestures(
                    onDragEnd = {
                        if (dragAccumulatorY < -50f) {
                            onOpenNowPlaying()
                        } else if (dragAccumulatorX < -50f) {
                            onSkipNext()
                        } else if (dragAccumulatorX > 50f) {
                            onSkipPrevious()
                        }
                        dragAccumulatorX = 0f
                        dragAccumulatorY = 0f
                    },
                    onDragCancel = {
                        dragAccumulatorX = 0f
                        dragAccumulatorY = 0f
                    },
                    onDrag = { _, dragAmount ->
                        dragAccumulatorX += dragAmount.x
                        dragAccumulatorY += dragAmount.y
                    }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Yellow Progress line at the top
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = NPYellow,
                trackColor = NPWhite.copy(alpha = 0.08f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenNowPlaying() }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album artwork
                MusicArtwork(
                    albumId = song.albumId,
                    contentDescription = song.title,
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Title and Artist
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        color = NPWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${song.artist} • ${song.album}",
                        color = NPWhite.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Controls: Previous, Play/Pause, Next
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = NPWhite.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NPYellow)
                        .clickable { onTogglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = NPBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SkipNext,
                        contentDescription = "Next Track",
                        tint = NPWhite.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
