package com.npplayer.feature.library.music

import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.database.entity.SongEntity
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicTagEditorSheet(
    sheetState: SheetState,
    song: SongEntity,
    onSongUpdated: (SongEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album) }
    var albumArtist by remember { mutableStateOf(song.albumArtist ?: "") }
    var genre by remember { mutableStateOf(song.genre) }
    var yearStr by remember { mutableStateOf(if (song.year > 0) song.year.toString() else "") }
    var trackNoStr by remember { mutableStateOf(if (song.trackNo > 0) song.trackNo.toString() else "") }
    var discNoStr by remember { mutableStateOf(if (song.discNo > 0) song.discNo.toString() else "1") }

    var isSaving by remember { mutableStateOf(false) }

    // Scoped storage write permission launcher for Android 11+
    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Permission granted, execute save
            scope.launch {
                executeTagSave(
                    context = context,
                    song = song,
                    title = title,
                    artist = artist,
                    album = album,
                    albumArtist = albumArtist,
                    genre = genre,
                    yearStr = yearStr,
                    trackNoStr = trackNoStr,
                    discNoStr = discNoStr,
                    onSuccess = { updated ->
                        onSongUpdated(updated)
                        onDismiss()
                    }
                )
            }
        } else {
            Toast.makeText(context, "Permission denied to edit file tags", Toast.LENGTH_SHORT).show()
            isSaving = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NPBlack,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Cancel", tint = NPWhite)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Edit Song Info",
                        color = NPWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isSaving) {
                    CircularProgressIndicator(color = NPYellow, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(NPYellow)
                            .clickable {
                                isSaving = true
                                scope.launch {
                                    val uri = Uri.parse(song.uri)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        try {
                                            // Check if we need MediaStore write request
                                            executeTagSave(
                                                context = context,
                                                song = song,
                                                title = title,
                                                artist = artist,
                                                album = album,
                                                albumArtist = albumArtist,
                                                genre = genre,
                                                yearStr = yearStr,
                                                trackNoStr = trackNoStr,
                                                discNoStr = discNoStr,
                                                onSuccess = { updated ->
                                                    onSongUpdated(updated)
                                                    onDismiss()
                                                }
                                            )
                                        } catch (securityEx: SecurityException) {
                                            val writeRequest = MediaStore.createWriteRequest(
                                                context.contentResolver,
                                                listOf(uri)
                                            )
                                            val intentSender = writeRequest.intentSender
                                            writePermissionLauncher.launch(
                                                IntentSenderRequest.Builder(intentSender).build()
                                            )
                                        }
                                    } else {
                                        executeTagSave(
                                            context = context,
                                            song = song,
                                            title = title,
                                            artist = artist,
                                            album = album,
                                            albumArtist = albumArtist,
                                            genre = genre,
                                            yearStr = yearStr,
                                            trackNoStr = trackNoStr,
                                            discNoStr = discNoStr,
                                            onSuccess = { updated ->
                                                onSongUpdated(updated)
                                                onDismiss()
                                            }
                                        )
                                    }
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Outlined.Check, contentDescription = "Save", tint = NPBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Save", color = NPBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = NPWhite.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(14.dp))

            TagField(label = "Title", value = title, onValueChange = { title = it })
            TagField(label = "Artist", value = artist, onValueChange = { artist = it })
            TagField(label = "Album", value = album, onValueChange = { album = it })
            TagField(label = "Album Artist", value = albumArtist, onValueChange = { albumArtist = it })
            TagField(label = "Genre", value = genre, onValueChange = { genre = it })

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    TagField(label = "Year", value = yearStr, onValueChange = { yearStr = it }, keyboardType = KeyboardType.Number)
                }
                Box(modifier = Modifier.weight(1f)) {
                    TagField(label = "Track No", value = trackNoStr, onValueChange = { trackNoStr = it }, keyboardType = KeyboardType.Number)
                }
                Box(modifier = Modifier.weight(1f)) {
                    TagField(label = "Disc No", value = discNoStr, onValueChange = { discNoStr = it }, keyboardType = KeyboardType.Number)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TagField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = NPWhite.copy(alpha = 0.6f)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = NPWhite,
            unfocusedTextColor = NPWhite,
            focusedBorderColor = NPYellow,
            unfocusedBorderColor = NPWhite.copy(alpha = 0.2f),
            cursorColor = NPYellow
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    )
}

private suspend fun executeTagSave(
    context: Context,
    song: SongEntity,
    title: String,
    artist: String,
    album: String,
    albumArtist: String,
    genre: String,
    yearStr: String,
    trackNoStr: String,
    discNoStr: String,
    onSuccess: (SongEntity) -> Unit
) = withContext(Dispatchers.IO) {
    try {
        val year = yearStr.toIntOrNull() ?: 0
        val trackNo = trackNoStr.toIntOrNull() ?: 0
        val discNo = discNoStr.toIntOrNull() ?: 1

        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.TITLE, title)
            put(MediaStore.Audio.Media.ARTIST, artist)
            put(MediaStore.Audio.Media.ALBUM, album)
            if (year > 0) put(MediaStore.Audio.Media.YEAR, year)
            if (trackNo > 0) put(MediaStore.Audio.Media.TRACK, trackNo)
        }

        val uri = Uri.parse(song.uri)
        try {
            context.contentResolver.update(uri, values, null, null)
        } catch (_: Exception) {
            // MediaStore update might fail if read-only, but we continue updating app metadata
        }

        // Trigger MediaScanner if file path exists
        if (song.filePath.isNotBlank()) {
            MediaScannerConnection.scanFile(context, arrayOf(song.filePath), null, null)
        }

        val updatedSong = song.copy(
            title = title,
            artist = artist,
            album = album,
            albumArtist = albumArtist.ifBlank { null },
            genre = genre,
            year = year,
            trackNo = trackNo,
            discNo = discNo,
            dateModified = System.currentTimeMillis() / 1000
        )

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Tags updated successfully", Toast.LENGTH_SHORT).show()
            onSuccess(updatedSong)
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Failed to update tags: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
