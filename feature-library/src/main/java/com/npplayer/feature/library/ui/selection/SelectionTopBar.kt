package com.npplayer.feature.library.ui.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun SelectionTopBar(
    selectedCount: Int,
    totalCount: Int,
    onClose: () -> Unit,
    onPlay: () -> Unit,
    onInfo: () -> Unit,
    onSearchSubtitles: () -> Unit,
    onShare: () -> Unit,
    onLockInPrivate: () -> Unit,
    onRebuildThumbnail: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = "Close selection mode" }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "$selectedCount / $totalCount Selected",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .semantics { contentDescription = "$selectedCount of $totalCount items selected" }
            )

            // Play selected queue
            IconButton(
                onClick = onPlay,
                enabled = selectedCount > 0,
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = "Play selected videos" }
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    tint = if (selectedCount > 0) NPYellow else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                )
            }

            // Info sheet
            IconButton(
                onClick = onInfo,
                enabled = selectedCount > 0,
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = "View video info" }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = if (selectedCount > 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                )
            }

            // 3-dot overflow menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "More selection actions" }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    // Subtitles (single video only)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Search subtitles online",
                                    color = if (selectedCount == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                                if (selectedCount > 1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(1 video only)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Subtitles,
                                contentDescription = null,
                                tint = if (selectedCount == 1) NPYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        },
                        enabled = selectedCount == 1,
                        onClick = {
                            menuExpanded = false
                            onSearchSubtitles()
                        }
                    )

                    // Share
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Share",
                                color = if (selectedCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = null,
                                tint = if (selectedCount > 0) NPYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        },
                        enabled = selectedCount > 0,
                        onClick = {
                            menuExpanded = false
                            onShare()
                        }
                    )

                    // Lock in Private Folder
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Lock in Private Folder",
                                color = if (selectedCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = if (selectedCount > 0) NPYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        },
                        enabled = selectedCount > 0,
                        onClick = {
                            menuExpanded = false
                            onLockInPrivate()
                        }
                    )

                    // Rebuild thumbnail
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Rebuild thumbnail",
                                color = if (selectedCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                tint = if (selectedCount > 0) NPYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        },
                        enabled = selectedCount > 0,
                        onClick = {
                            menuExpanded = false
                            onRebuildThumbnail()
                        }
                    )
                }
            }
        }
    }
}
