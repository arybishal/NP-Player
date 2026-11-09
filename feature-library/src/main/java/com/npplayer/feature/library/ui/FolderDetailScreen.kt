package com.npplayer.feature.library.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.npplayer.core.data.database.entity.VideoEntity
import com.npplayer.core.ui.component.NPFilterChip
import com.npplayer.core.ui.component.NPLabelChip
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.core.ui.util.FormatUtils
import com.npplayer.feature.library.ui.selection.AddToPlaylistDialog
import com.npplayer.feature.library.ui.selection.BatchRenameDialog
import com.npplayer.feature.library.ui.selection.DeleteConfirmDialog
import com.npplayer.feature.library.ui.selection.FolderPickerDialog
import com.npplayer.feature.library.ui.selection.LabelPickerBottomSheet
import com.npplayer.feature.library.ui.selection.ProgressOperationDialog
import com.npplayer.feature.library.ui.selection.RenameDialog
import com.npplayer.feature.library.ui.selection.SelectionBottomBar
import com.npplayer.feature.library.ui.selection.SelectionTopBar
import com.npplayer.feature.library.ui.selection.VideoInfoDetailedBottomSheet
import com.npplayer.feature.library.util.FileOperationsHelper
import com.npplayer.player.ui.sheet.OnlineSubtitlesBottomSheet
import com.npplayer.player.ui.sheet.OnlineSubtitlesUiState
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderDetailScreen(
    onBack: () -> Unit,
    onVideoClick: (videoPath: String, videoTitle: String) -> Unit,
    onSearchClick: () -> Unit = {},
    viewModel: FolderDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val isSelectionActive = state.selectionState.isActive
    val selectedVideos = remember(state.filteredVideos, state.selectionState.selectedIds) {
        state.filteredVideos.filter { state.selectionState.selectedIds.contains(it.id) }
    }
    val selectedCount = selectedVideos.size

    // Dialog states
    var showMoveDialog by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showBatchRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showLabelSheet by remember { mutableStateOf(false) }
    var showOnlineSubtitlesSheet by remember { mutableStateOf(false) }

    // Intercept Back button during selection mode
    BackHandler(enabled = isSelectionActive) {
        viewModel.clearSelection()
    }

    Scaffold(
        topBar = {
            if (isSelectionActive) {
                SelectionTopBar(
                    selectedCount = selectedCount,
                    totalCount = state.filteredVideos.size,
                    onClose = { viewModel.clearSelection() },
                    onPlay = {
                        val first = selectedVideos.firstOrNull()
                        if (first != null) {
                            onVideoClick(first.path, first.title)
                        }
                    },
                    onInfo = { showInfoSheet = true },
                    onSearchSubtitles = {
                        val single = selectedVideos.firstOrNull()
                        if (single != null) {
                            viewModel.searchOnlineSubtitles(single)
                            showOnlineSubtitlesSheet = true
                        }
                    },
                    onShare = {
                        viewModel.shareSelected(context)
                    },
                    onLockInPrivate = {
                        viewModel.lockSelectedInVault { count ->
                            Toast.makeText(context, "$count video(s) locked in Private Folder", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRebuildThumbnail = {
                        viewModel.rebuildThumbnails(context) {
                            Toast.makeText(context, "Thumbnails rebuilt", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                FolderDetailTopBar(
                    title = state.folderName,
                    onSearchClick = onSearchClick,
                    onBack = onBack
                )
            }
        },
        bottomBar = {
            if (isSelectionActive) {
                val isAllSelected = selectedCount == state.filteredVideos.size && state.filteredVideos.isNotEmpty()
                SelectionBottomBar(
                    isAllSelected = isAllSelected,
                    selectedCount = selectedCount,
                    onToggleSelectAll = { viewModel.toggleSelectAll() },
                    onMove = { showMoveDialog = true },
                    onCopy = { showCopyDialog = true },
                    onAddToPlaylist = { showPlaylistDialog = true },
                    onLabel = { showLabelSheet = true },
                    onRename = {
                        if (selectedCount == 1) {
                            showRenameDialog = true
                        } else if (selectedCount > 1) {
                            showBatchRenameDialog = true
                        }
                    },
                    onDelete = { showDeleteDialog = true }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Label filter chip row
            if (state.allLabels.isNotEmpty() && !isSelectionActive) {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NPFilterChip(
                        selected = state.selectedLabelFilterId == null,
                        onClick = { viewModel.selectLabelFilter(null) },
                        label = "All"
                    )
                    state.allLabels.forEach { label ->
                        NPFilterChip(
                            selected = state.selectedLabelFilterId == label.id,
                            onClick = {
                                if (state.selectedLabelFilterId == label.id) {
                                    viewModel.selectLabelFilter(null)
                                } else {
                                    viewModel.selectLabelFilter(label.id)
                                }
                            },
                            label = label.name
                        )
                    }
                }
            }

            if (state.filteredVideos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No videos found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.filteredVideos, key = { it.id }) { video ->
                        val isSelected = state.selectionState.isSelected(video.id)
                        val labelsForVideo = state.videoLabelsMap[video.id] ?: emptyList()

                        SelectableVideoListItem(
                            video = video,
                            isSelected = isSelected,
                            isSelectionActive = isSelectionActive,
                            labels = labelsForVideo,
                            onClick = {
                                if (isSelectionActive) {
                                    viewModel.toggleSelection(video.id)
                                } else {
                                    onVideoClick(video.path, video.title)
                                }
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isSelectionActive) {
                                    viewModel.toggleSelection(video.id)
                                } else {
                                    viewModel.startSelection(video.id)
                                }
                            },
                            onThumbnailClick = {
                                if (isSelectionActive) {
                                    viewModel.toggleSelection(video.id)
                                } else {
                                    onVideoClick(video.path, video.title)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // --- Action Dialogs ---

    if (showMoveDialog) {
        FolderPickerDialog(
            title = "Move to Folder",
            folders = state.allFolders.filter { it.path != state.folderPath },
            onSelectFolder = { targetPath ->
                showMoveDialog = false
                viewModel.moveSelected(targetPath) { moved ->
                    Toast.makeText(context, "Moved $moved video(s)", Toast.LENGTH_SHORT).show()
                }
            },
            onCreateFolder = { folderName ->
                val parent = File(state.folderPath).parentFile ?: File(state.folderPath)
                val newDir = File(parent, folderName)
                newDir.mkdirs()
                showMoveDialog = false
                viewModel.moveSelected(newDir.absolutePath) { moved ->
                    Toast.makeText(context, "Moved $moved video(s)", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showMoveDialog = false }
        )
    }

    if (showCopyDialog) {
        FolderPickerDialog(
            title = "Copy to Folder",
            folders = state.allFolders,
            onSelectFolder = { targetPath ->
                showCopyDialog = false
                viewModel.copySelected(targetPath) { copied ->
                    Toast.makeText(context, "Copied $copied video(s)", Toast.LENGTH_SHORT).show()
                }
            },
            onCreateFolder = { folderName ->
                val parent = File(state.folderPath).parentFile ?: File(state.folderPath)
                val newDir = File(parent, folderName)
                newDir.mkdirs()
                showCopyDialog = false
                viewModel.copySelected(newDir.absolutePath) { copied ->
                    Toast.makeText(context, "Copied $copied video(s)", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showCopyDialog = false }
        )
    }

    if (showPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = state.allPlaylists,
            onSelectPlaylist = { plId ->
                showPlaylistDialog = false
                viewModel.addToPlaylist(plId) {
                    Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show()
                }
            },
            onCreatePlaylist = { plName ->
                showPlaylistDialog = false
                viewModel.createPlaylistAndAdd(plName) {
                    Toast.makeText(context, "Playlist created & videos added", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showPlaylistDialog = false }
        )
    }

    if (showRenameDialog && selectedVideos.size == 1) {
        val single = selectedVideos.first()
        val file = File(single.path)
        RenameDialog(
            initialName = file.nameWithoutExtension,
            extension = file.extension,
            onRename = { newName ->
                showRenameDialog = false
                viewModel.renameSingle(single.id, newName) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showBatchRenameDialog && selectedVideos.size > 1) {
        BatchRenameDialog(
            selectedCount = selectedVideos.size,
            onBatchRename = { pattern, startNum ->
                showBatchRenameDialog = false
                viewModel.batchRename(pattern, startNum) { count ->
                    Toast.makeText(context, "Renamed $count video(s)", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showBatchRenameDialog = false }
        )
    }

    if (showDeleteDialog) {
        val totalSize = selectedVideos.sumOf { it.sizeBytes }
        DeleteConfirmDialog(
            count = selectedVideos.size,
            totalSizeBytes = totalSize,
            onConfirm = { deleteSubs ->
                showDeleteDialog = false
                viewModel.deleteSelected(deleteSubs) { deleted ->
                    Toast.makeText(context, "Deleted $deleted video(s)", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showInfoSheet && selectedVideos.isNotEmpty()) {
        val singleLabels = if (selectedVideos.size == 1) state.videoLabelsMap[selectedVideos.first().id] ?: emptyList() else emptyList()
        VideoInfoDetailedBottomSheet(
            videos = selectedVideos,
            labels = singleLabels,
            onDismiss = { showInfoSheet = false },
            onShare = {
                showInfoSheet = false
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "video/*"
                    putExtra(android.content.Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(selectedVideos.first().path)))
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(intent, "Share Video"))
            }
        )
    }

    if (showLabelSheet && selectedVideos.isNotEmpty()) {
        val selectedVideoIds = selectedVideos.map { it.id }.toSet()
        val fullyAssigned = state.allLabels.filter { label ->
            selectedVideoIds.all { vId -> state.videoLabelsMap[vId]?.contains(label.name) == true }
        }.map { it.id }.toSet()

        val partiallyAssigned = state.allLabels.filter { label ->
            val count = selectedVideoIds.count { vId -> state.videoLabelsMap[vId]?.contains(label.name) == true }
            count > 0 && count < selectedVideoIds.size
        }.map { it.id }.toSet()

        LabelPickerBottomSheet(
            allLabels = state.allLabels,
            fullyAssignedLabelIds = fullyAssigned,
            partiallyAssignedLabelIds = partiallyAssigned,
            onCreateLabel = { name -> viewModel.createLabel(name) },
            onApply = { assigned, removed ->
                viewModel.applyLabels(assigned, removed)
                showLabelSheet = false
            },
            onDismiss = { showLabelSheet = false }
        )
    }

    if (showOnlineSubtitlesSheet && selectedVideos.size == 1) {
        val single = selectedVideos.first()
        OnlineSubtitlesBottomSheet(
            uiState = state.onlineSubtitlesState,
            initialQuery = single.title,
            preferredLanguages = setOf("en"),
            onSearch = { query, langs -> viewModel.searchOnlineSubtitles(single, query) },
            onDownloadSubtitle = { item ->
                viewModel.downloadSubtitle(item, single.path) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = {
                showOnlineSubtitlesSheet = false
                viewModel.dismissOnlineSubtitles()
            }
        )
    }

    if (state.isOperationInProgress) {
        ProgressOperationDialog(
            title = "Processing Videos",
            progressText = state.operationProgress ?: "Working..."
        )
    }
}

@Composable
private fun FolderDetailTopBar(
    title: String,
    onSearchClick: () -> Unit,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier
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
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SelectableVideoListItem(
    video: VideoEntity,
    isSelected: Boolean,
    isSelectionActive: Boolean,
    labels: List<String>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onThumbnailClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        NPYellow.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val border = if (isSelected) {
        BorderStroke(1.5.dp, NPYellow)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = border,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .semantics {
                contentDescription = if (isSelected) "Selected, ${video.title}" else video.title
            }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Video Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 88.dp, height = 56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onThumbnailClick),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = File(video.path),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Selection Check Badge (yellow circle with black check)
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(28.dp)
                            .background(NPYellow, CircleShape)
                            .border(1.5.dp, NPBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = NPBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Duration badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(
                            color = NPBlack.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = FormatUtils.formatDuration(video.durationMs),
                        color = NPWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = FormatUtils.formatFileSize(video.sizeBytes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (video.width > 0 && video.height > 0) {
                        Text(
                            text = " • ${video.width}x${video.height}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (labels.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        labels.take(3).forEach { labelText ->
                            NPLabelChip(text = labelText)
                        }
                    }
                }
            }
        }
    }
}
