package com.npplayer.feature.library.ui.selection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.database.entity.FolderEntity
import com.npplayer.core.data.database.entity.PlaylistEntity
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.core.ui.util.FormatUtils

@Composable
fun FolderPickerDialog(
    title: String,
    folders: List<FolderEntity>,
    onSelectFolder: (targetFolderPath: String) -> Unit,
    onCreateFolder: (newFolderName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Folder", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NPYellow,
                        cursorColor = NPYellow
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newFolderName.trim()
                        if (name.isNotBlank()) {
                            onCreateFolder(name)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NPYellow, contentColor = NPBlack)
                ) {
                    Text("CREATE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { showCreateDialog = true }) {
                    Icon(imageVector = Icons.Outlined.CreateNewFolder, contentDescription = null, tint = NPYellow, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Folder", color = NPYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            if (folders.isEmpty()) {
                Text("No folders found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(folders, key = { it.path }) { folder ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectFolder(folder.path) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Outlined.Folder, contentDescription = null, tint = NPYellow, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = folder.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = "${folder.videoCount} videos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = NPYellow, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun AddToPlaylistDialog(
    playlists: List<PlaylistEntity>,
    onSelectPlaylist: (playlistId: Long) -> Unit,
    onCreatePlaylist: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Playlist", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NPYellow,
                        cursorColor = NPYellow
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newPlaylistName.trim()
                        if (name.isNotBlank()) {
                            onCreatePlaylist(name)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NPYellow, contentColor = NPBlack)
                ) {
                    Text("CREATE & ADD", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Add to Playlist", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { showCreateDialog = true }) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null, tint = NPYellow, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Playlist", color = NPYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            if (playlists.isEmpty()) {
                Text("No playlists found. Create one using the button above.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(playlists, key = { it.id }) { pl ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectPlaylist(pl.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Outlined.PlaylistPlay, contentDescription = null, tint = NPYellow, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = pl.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = "${pl.videoCount} videos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = NPYellow, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun RenameDialog(
    initialName: String,
    extension: String,
    onRename: (newName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameText by remember { mutableStateOf(initialName) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Video", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = {
                        nameText = it
                        errorText = if (it.isBlank()) {
                            "Name cannot be empty"
                        } else if (it.any { c -> c in listOf('/', '\\', ':', '*', '?', '"', '<', '>', '|') }) {
                            "Invalid characters in filename"
                        } else null
                    },
                    label = { Text("File Name") },
                    suffix = { Text(".$extension", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    isError = errorText != null,
                    supportingText = { errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NPYellow,
                        cursorColor = NPYellow
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = nameText.trim()
                    if (trimmed.isNotBlank() && errorText == null) {
                        onRename(trimmed)
                    }
                },
                enabled = nameText.isNotBlank() && errorText == null,
                colors = ButtonDefaults.buttonColors(containerColor = NPYellow, contentColor = NPBlack)
            ) {
                Text("RENAME", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun BatchRenameDialog(
    selectedCount: Int,
    onBatchRename: (pattern: String, startNumber: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var patternText by remember { mutableStateOf("Video {n}") }
    var startNumberText by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Batch Rename ($selectedCount Videos)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Use {n} as counter placeholder, e.g. 'Episode {n}' -> 'Episode 1', 'Episode 2'...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = patternText,
                    onValueChange = { patternText = it },
                    label = { Text("Naming Pattern") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NPYellow,
                        cursorColor = NPYellow
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = startNumberText,
                    onValueChange = { startNumberText = it.filter { c -> c.isDigit() } },
                    label = { Text("Start Number") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NPYellow,
                        cursorColor = NPYellow
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                val startNum = startNumberText.toIntOrNull() ?: 1
                val preview1 = patternText.replace("{n}", "$startNum")
                val preview2 = patternText.replace("{n}", "${startNum + 1}")
                Text(
                    text = "Preview: $preview1, $preview2...",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = NPYellow
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pattern = patternText.trim()
                    val start = startNumberText.toIntOrNull() ?: 1
                    if (pattern.isNotBlank()) {
                        onBatchRename(pattern, start)
                    }
                },
                enabled = patternText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NPYellow, contentColor = NPBlack)
            ) {
                Text("APPLY BATCH RENAME", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun DeleteConfirmDialog(
    count: Int,
    totalSizeBytes: Long,
    onConfirm: (deleteMatchingSubtitles: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var deleteSubs by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (count == 1) "Delete Video?" else "Delete $count Videos?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "This will permanently remove the video file(s) (${FormatUtils.formatFileSize(totalSizeBytes)}). This cannot be undone.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { deleteSubs = !deleteSubs }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = deleteSubs,
                        onCheckedChange = { deleteSubs = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NPYellow,
                            checkmarkColor = NPBlack
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Also delete matching subtitle files (.srt, .vtt, etc.)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(deleteSubs) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("DELETE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ProgressOperationDialog(
    title: String,
    progressText: String,
    onCancel: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = { /* prevent dismiss while executing */ },
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(color = NPYellow, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = progressText, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        confirmButton = {
            if (onCancel != null) {
                TextButton(onClick = onCancel) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
