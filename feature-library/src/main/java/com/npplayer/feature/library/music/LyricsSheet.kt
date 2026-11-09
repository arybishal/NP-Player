package com.npplayer.feature.library.music

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.database.entity.MusicLyricsEntity
import com.npplayer.core.data.database.entity.SongEntity
import com.npplayer.core.data.lyrics.LrcParser
import com.npplayer.core.data.lyrics.LyricsRepository
import com.npplayer.core.data.lyrics.ParsedLyrics
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsSheet(
    sheetState: SheetState,
    song: SongEntity,
    currentPositionMs: Long,
    lyricsRepository: LyricsRepository,
    onSeekTo: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val lyricsFlow = remember(song.id) { lyricsRepository.getLyricsFlow(song.id) }
    val cachedLyrics by lyricsFlow.collectAsState(initial = null)

    var isLoading by remember { mutableStateOf(false) }
    var parsedLyrics by remember { mutableStateOf<ParsedLyrics?>(null) }
    var effectiveOffsetMs by remember { mutableLongStateOf(0L) }

    // Dialog state for searching again
    var showEditSearchDialog by remember { mutableStateOf(false) }
    var searchTitle by remember(song.title) { mutableStateOf(song.title) }
    var searchArtist by remember(song.artist) { mutableStateOf(song.artist) }

    // Dialog state for pasting lyrics
    var showPasteLyricsDialog by remember { mutableStateOf(false) }
    var pastedText by remember { mutableStateOf("") }

    // Initial fetch if needed
    LaunchedEffect(song.id) {
        if (cachedLyrics == null) {
            isLoading = true
            lyricsRepository.loadLyricsForSong(song, forceOnline = false)
            isLoading = false
        }
    }

    // Process cached lyrics entity into ParsedLyrics
    LaunchedEffect(cachedLyrics) {
        val entity = cachedLyrics
        if (entity != null) {
            effectiveOffsetMs = entity.offsetMs
            val synced = entity.syncedLyrics
            if (!synced.isNullOrBlank()) {
                parsedLyrics = LrcParser.parse(synced)
            } else {
                parsedLyrics = null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NPBlack,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // Header: Title, offset & close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lyrics",
                            color = NPWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${song.title} • ${song.artist}",
                            color = NPWhite.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    lyricsRepository.loadLyricsForSong(song, forceOnline = true)
                                    isLoading = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Refresh",
                                tint = NPWhite.copy(alpha = 0.8f)
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close",
                                tint = NPWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = NPWhite.copy(alpha = 0.1f))

                // Offset adjust bar (shown when synced lyrics exist)
                if (parsedLyrics != null && parsedLyrics!!.lines.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sync Offset: ${if (effectiveOffsetMs >= 0) "+$effectiveOffsetMs" else "$effectiveOffsetMs"} ms",
                            color = NPWhite.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OffsetButton(label = "-0.5s") {
                                effectiveOffsetMs -= 500
                                scope.launch { lyricsRepository.updateOffset(song.id, effectiveOffsetMs) }
                            }
                            OffsetButton(label = "-0.1s") {
                                effectiveOffsetMs -= 100
                                scope.launch { lyricsRepository.updateOffset(song.id, effectiveOffsetMs) }
                            }
                            OffsetButton(label = "Reset") {
                                effectiveOffsetMs = 0L
                                scope.launch { lyricsRepository.updateOffset(song.id, 0L) }
                            }
                            OffsetButton(label = "+0.1s") {
                                effectiveOffsetMs += 100
                                scope.launch { lyricsRepository.updateOffset(song.id, effectiveOffsetMs) }
                            }
                            OffsetButton(label = "+0.5s") {
                                effectiveOffsetMs += 500
                                scope.launch { lyricsRepository.updateOffset(song.id, effectiveOffsetMs) }
                            }
                        }
                    }
                    HorizontalDivider(color = NPWhite.copy(alpha = 0.08f))
                }

                // Body Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = NPYellow, modifier = Modifier.size(36.dp))
                    } else if (cachedLyrics?.isInstrumental == true) {
                        // Instrumental
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.MusicNote,
                                contentDescription = null,
                                tint = NPYellow,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Instrumental Track",
                                color = NPWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No lyrics for this song",
                                color = NPWhite.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }
                    } else if (parsedLyrics != null && parsedLyrics!!.lines.isNotEmpty()) {
                        // Synced Lyrics View
                        SyncedLyricsView(
                            parsedLyrics = parsedLyrics!!,
                            currentPositionMs = currentPositionMs,
                            offsetMs = effectiveOffsetMs,
                            onSeekTo = onSeekTo
                        )
                    } else if (!cachedLyrics?.plainLyrics.isNullOrBlank()) {
                        // Plain Text Lyrics View
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NPWhite.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Plain Lyrics (Not Synced)",
                                    color = NPWhite.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = cachedLyrics!!.plainLyrics!!,
                                color = NPWhite,
                                fontSize = 17.sp,
                                lineHeight = 28.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Empty State / Not Found
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "No lyrics found",
                                color = NPWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Lyrics could not be found for \"${song.title}\"",
                                color = NPWhite.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                ActionChip(label = "Search Again", icon = Icons.Outlined.Edit) {
                                    showEditSearchDialog = true
                                }
                                ActionChip(label = "Paste Lyrics", icon = Icons.Outlined.Edit) {
                                    showPasteLyricsDialog = true
                                }
                            }
                        }
                    }
                }

                // Footer: Source attribution & Save as .lrc
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val attributionText = when (cachedLyrics?.source) {
                        "lrclib" -> "Lyrics from LRCLIB"
                        "embedded" -> "Embedded in file tags"
                        "sidecar" -> "Loaded from .lrc file"
                        "custom" -> "Custom lyrics"
                        else -> "LRCLIB Open Database"
                    }

                    Text(
                        text = attributionText,
                        color = NPYellow.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://lrclib.net"))
                            context.startActivity(intent)
                        }
                    )

                    if (!cachedLyrics?.syncedLyrics.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NPWhite.copy(alpha = 0.12f))
                                .clickable {
                                    scope.launch {
                                        val saved = lyricsRepository.saveLyricsAsLrcFile(
                                            song,
                                            cachedLyrics!!.syncedLyrics!!
                                        )
                                        if (saved) {
                                            Toast.makeText(context, "Saved .lrc next to song", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Could not save file (scoped storage)", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = null,
                                tint = NPWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Save .lrc",
                                color = NPWhite,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: Search again with custom title/artist
    if (showEditSearchDialog) {
        AlertDialog(
            onDismissRequest = { showEditSearchDialog = false },
            containerColor = NPBlack,
            title = { Text(text = "Search Lyrics on LRCLIB", color = NPWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchTitle,
                        onValueChange = { searchTitle = it },
                        label = { Text("Song Title", color = NPWhite.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NPWhite,
                            unfocusedTextColor = NPWhite,
                            focusedBorderColor = NPYellow,
                            unfocusedBorderColor = NPWhite.copy(alpha = 0.2f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchArtist,
                        onValueChange = { searchArtist = it },
                        label = { Text("Artist", color = NPWhite.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NPWhite,
                            unfocusedTextColor = NPWhite,
                            focusedBorderColor = NPYellow,
                            unfocusedBorderColor = NPWhite.copy(alpha = 0.2f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEditSearchDialog = false
                        scope.launch {
                            isLoading = true
                            val querySong = song.copy(title = searchTitle, artist = searchArtist)
                            lyricsRepository.loadLyricsForSong(querySong, forceOnline = true)
                            isLoading = false
                        }
                    }
                ) {
                    Text("Search", color = NPYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditSearchDialog = false }) {
                    Text("Cancel", color = NPWhite.copy(alpha = 0.6f))
                }
            }
        )
    }

    // Dialog: Paste lyrics
    if (showPasteLyricsDialog) {
        AlertDialog(
            onDismissRequest = { showPasteLyricsDialog = false },
            containerColor = NPBlack,
            title = { Text(text = "Paste Lyrics", color = NPWhite, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = pastedText,
                    onValueChange = { pastedText = it },
                    placeholder = { Text("Paste plain text or [00:12.34] synced LRC text...", color = NPWhite.copy(alpha = 0.4f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NPWhite,
                        unfocusedTextColor = NPWhite,
                        focusedBorderColor = NPYellow,
                        unfocusedBorderColor = NPWhite.copy(alpha = 0.2f)
                    ),
                    maxLines = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPasteLyricsDialog = false
                        if (pastedText.isNotBlank()) {
                            scope.launch {
                                val isSynced = pastedText.contains("[")
                                lyricsRepository.saveCustomLyrics(
                                    songId = song.id,
                                    plainText = pastedText,
                                    syncedText = if (isSynced) pastedText else null
                                )
                            }
                        }
                    }
                ) {
                    Text("Save", color = NPYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteLyricsDialog = false }) {
                    Text("Cancel", color = NPWhite.copy(alpha = 0.6f))
                }
            }
        )
    }
}

@Composable
private fun SyncedLyricsView(
    parsedLyrics: ParsedLyrics,
    currentPositionMs: Long,
    offsetMs: Long,
    onSeekTo: (Long) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isUserScrolling by remember { mutableStateOf(false) }

    val activeIndex = remember(currentPositionMs, offsetMs, parsedLyrics) {
        LrcParser.findActiveLineIndex(parsedLyrics.lines, currentPositionMs - offsetMs)
    }

    // Track user dragging
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (scrolling) {
                    isUserScrolling = true
                }
            }
    }

    // Auto-scroll when activeIndex changes unless user is manually scrolling
    LaunchedEffect(activeIndex, isUserScrolling) {
        if (!isUserScrolling && activeIndex >= 0 && activeIndex < parsedLyrics.lines.size) {
            val target = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 80.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(parsedLyrics.lines) { index, line ->
                val isActive = index == activeIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val seekTarget = (line.timestampMs + offsetMs).coerceAtLeast(0L)
                            onSeekTo(seekTarget)
                            isUserScrolling = false
                        }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = line.text,
                        color = if (isActive) NPYellow else NPWhite.copy(alpha = 0.35f),
                        fontSize = if (isActive) 22.sp else 16.sp,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = if (isActive) 32.sp else 24.sp
                    )
                }
            }
        }

        // Floating Sync Button when user scrolls away
        AnimatedVisibility(
            visible = isUserScrolling,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(NPYellow)
                    .clickable {
                        isUserScrolling = false
                        scope.launch {
                            val target = (activeIndex - 2).coerceAtLeast(0)
                            listState.animateScrollToItem(target)
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sync,
                    contentDescription = "Sync",
                    tint = NPBlack,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sync",
                    color = NPBlack,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OffsetButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NPWhite.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = NPWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(NPWhite.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NPYellow,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = NPWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
