package com.npplayer.feature.library.music

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lyrics
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import com.npplayer.core.data.lyrics.LyricsRepository
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.database.entity.SongEntity
import com.npplayer.core.data.preferences.SeekBarStylePreference
import com.npplayer.core.data.repository.MusicRepeatMode
import com.npplayer.core.data.repository.SleepTimerOption
import com.npplayer.core.ui.component.MusicArtwork
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.core.ui.util.FormatUtils
import com.npplayer.player.audio.AudioFxManager
import com.npplayer.player.ui.seekbar.NPSeekBar
import com.npplayer.player.ui.sheet.EqualizerBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSheet(
    sheetState: SheetState,
    song: SongEntity?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    bufferedPositionMs: Long,
    shuffleMode: Boolean,
    repeatMode: MusicRepeatMode,
    isFavorite: Boolean,
    playbackSpeed: Float,
    sleepTimerRemainingMs: Long?,
    sleepTimerOption: SleepTimerOption?,
    seekBarStyle: SeekBarStylePreference,
    audioFxManager: AudioFxManager,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onSetSleepTimer: (SleepTimerOption) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onOpenQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    lyricsRepository: LyricsRepository? = null,
    onDismiss: () -> Unit
) {
    if (song == null) return

    val context = LocalContext.current
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    val lyricsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var artworkDragAccumulator by remember { mutableFloatStateOf(0f) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NPBlack,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Collapse, Title/Album, Favorite & Queue
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = NPWhite,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "NOW PLAYING",
                        color = NPWhite.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = song.album,
                        color = NPWhite.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Favorited" else "Favorite",
                            tint = if (isFavorite) NPYellow else NPWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = onOpenQueue) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.QueueMusic,
                            contentDescription = "Queue",
                            tint = NPWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Artwork with horizontal swipe gesture for Next / Previous track
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, NPWhite.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                    .pointerInput(song.id) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (artworkDragAccumulator < -70f) {
                                    onSkipNext()
                                } else if (artworkDragAccumulator > 70f) {
                                    onSkipPrevious()
                                }
                                artworkDragAccumulator = 0f
                            },
                            onDragCancel = {
                                artworkDragAccumulator = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                artworkDragAccumulator += dragAmount
                            }
                        )
                    }
            ) {
                MusicArtwork(
                    albumId = song.albumId,
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Track Details: Title, Artist, Album & Year
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = song.title,
                    color = NPWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = song.artist,
                    color = NPWhite.copy(alpha = 0.75f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (song.year > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${song.genre.ifBlank { "Audio" }} • ${song.year}",
                        color = NPWhite.copy(alpha = 0.45f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Seek Bar using NPSeekBar with selected style preference
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                NPSeekBar(
                    style = seekBarStyle,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    bufferedPositionMs = bufferedPositionMs,
                    isPlaying = isPlaying,
                    onSeek = onSeekTo,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Timestamps: Position & Duration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = FormatUtils.formatDuration(currentPositionMs),
                    color = NPWhite.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Text(
                    text = FormatUtils.formatDuration(durationMs),
                    color = NPWhite.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback Controls Row: Shuffle, Prev, Play/Pause, Next, Repeat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle button
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleMode) NPYellow else NPWhite.copy(alpha = 0.45f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous button
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SkipPrevious,
                        contentDescription = "Previous",
                        tint = NPWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play / Pause Large Button (64dp, Yellow background)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(NPYellow)
                        .clickable { onTogglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = NPBlack,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Next button
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SkipNext,
                        contentDescription = "Next",
                        tint = NPWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Repeat button
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier.size(48.dp)
                ) {
                    val repeatIcon = if (repeatMode == MusicRepeatMode.ONE) Icons.Outlined.RepeatOne else Icons.Outlined.Repeat
                    val isRepeatActive = repeatMode != MusicRepeatMode.OFF
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = if (isRepeatActive) NPYellow else NPWhite.copy(alpha = 0.45f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions Row: Sleep Timer, Equalizer, Speed, Lyrics, Share, Add to playlist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickActionItem(
                    icon = Icons.Outlined.Timer,
                    label = if (sleepTimerRemainingMs != null) {
                        val mins = (sleepTimerRemainingMs / 60000).coerceAtLeast(1)
                        "${mins}m"
                    } else "Timer",
                    isActive = sleepTimerRemainingMs != null || sleepTimerOption != null,
                    onClick = { showSleepTimerDialog = true }
                )

                QuickActionItem(
                    icon = Icons.Outlined.Equalizer,
                    label = "Equalizer",
                    isActive = false,
                    onClick = { showEqualizerSheet = true }
                )

                QuickActionItem(
                    icon = Icons.Outlined.Speed,
                    label = if (playbackSpeed != 1.0f) "${playbackSpeed}x" else "Speed",
                    isActive = playbackSpeed != 1.0f,
                    onClick = { showSpeedDialog = true }
                )

                QuickActionItem(
                    icon = Icons.Outlined.Lyrics,
                    label = "Lyrics",
                    isActive = false,
                    onClick = { showLyricsSheet = true }
                )

                QuickActionItem(
                    icon = Icons.Outlined.Share,
                    label = "Share",
                    isActive = false,
                    onClick = {
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "audio/*"
                                putExtra(Intent.EXTRA_STREAM, Uri.parse(song.uri))
                                putExtra(Intent.EXTRA_TEXT, "Listening to ${song.title} by ${song.artist}")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Song"))
                        } catch (_: Exception) {}
                    }
                )

                QuickActionItem(
                    icon = Icons.Outlined.PlaylistAdd,
                    label = "Playlist",
                    isActive = false,
                    onClick = onAddToPlaylist
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showSleepTimerDialog) {
        MusicSleepTimerDialog(
            remainingMs = sleepTimerRemainingMs,
            activeOption = sleepTimerOption,
            onSetTimer = onSetSleepTimer,
            onCancelTimer = onCancelSleepTimer,
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    if (showSpeedDialog) {
        MusicSpeedDialog(
            currentSpeed = playbackSpeed,
            onSpeedSelected = onSetPlaybackSpeed,
            onDismiss = { showSpeedDialog = false }
        )
    }

    if (showEqualizerSheet) {
        EqualizerBottomSheet(
            audioFxManager = audioFxManager,
            onDismiss = { showEqualizerSheet = false }
        )
    }

    if (showLyricsSheet) {
        if (lyricsRepository != null) {
            LyricsSheet(
                sheetState = lyricsSheetState,
                song = song,
                currentPositionMs = currentPositionMs,
                lyricsRepository = lyricsRepository,
                onSeekTo = onSeekTo,
                onDismiss = { showLyricsSheet = false }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showLyricsSheet = false },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Lyrics,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Lyrics",
                        color = NPWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Text(
                        text = "Lyrics engine initializing...",
                        color = NPWhite.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showLyricsSheet = false }) {
                        Text(text = "Close", color = NPYellow, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = NPBlack,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isActive) NPYellow.copy(alpha = 0.2f) else NPWhite.copy(alpha = 0.08f))
                .border(
                    1.dp,
                    if (isActive) NPYellow else NPWhite.copy(alpha = 0.12f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) NPYellow else NPWhite,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isActive) NPYellow else NPWhite.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
