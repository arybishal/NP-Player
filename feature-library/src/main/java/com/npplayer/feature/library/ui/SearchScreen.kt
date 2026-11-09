package com.npplayer.feature.library.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.npplayer.core.data.database.entity.VideoEntity
import com.npplayer.core.data.repository.FolderSearchResult
import com.npplayer.core.data.repository.VideoSearchResult
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.feature.library.ui.selection.SelectionBottomBar
import com.npplayer.feature.library.ui.selection.SelectionModeState
import com.npplayer.feature.library.ui.selection.SelectionTopBar
import com.npplayer.feature.library.ui.selection.VideoInfoDetailedBottomSheet
import com.npplayer.feature.library.util.FileOperationsHelper
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onFolderClick: (path: String, name: String) -> Unit,
    onPlayVideo: (path: String, title: String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsState()
    val searchResult by viewModel.searchResult.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayedVideos.collectAsState()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var selectionState by remember { mutableStateOf(SelectionModeState()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    val isSelectionActive = selectionState.isActive
    val selectedVideos = remember(searchResult.matchingVideos, selectionState.selectedIds) {
        searchResult.matchingVideos.map { it.video }.filter { selectionState.selectedIds.contains(it.id) }
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Keyboard & back handling: back dismisses selection mode if active, otherwise exits search
    BackHandler {
        if (isSelectionActive) {
            selectionState = selectionState.clear()
        } else {
            focusManager.clearFocus()
            keyboardController?.hide()
            onBack()
        }
    }

    // Auto-focus and show keyboard on first entry
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    // Track loading indicator (only visible if search exceeds 300ms)
    var showLoadingIndicator by remember { mutableStateOf(false) }
    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            showLoadingIndicator = false
            delay(300)
            showLoadingIndicator = true
        } else {
            showLoadingIndicator = false
        }
    }
    LaunchedEffect(searchResult) {
        showLoadingIndicator = false
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isSelectionActive) {
                SelectionTopBar(
                    selectedCount = selectionState.selectedCount,
                    totalCount = searchResult.matchingVideos.size,
                    onClose = { selectionState = selectionState.clear() },
                    onPlay = {
                        val first = selectedVideos.firstOrNull()
                        if (first != null) {
                            onPlayVideo(first.path, first.title)
                        }
                    },
                    onInfo = { showInfoSheet = true },
                    onSearchSubtitles = {},
                    onShare = {
                        FileOperationsHelper.shareVideos(context, selectedVideos)
                    },
                    onLockInPrivate = {},
                    onRebuildThumbnail = {}
                )
            } else {
                // Top Bar with 28dp pill search field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onBack()
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.setQuery(it) },
                        placeholder = {
                            Text(
                                text = "Search videos and folders...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = if (query.isNotBlank()) NPYellow else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (query.isNotBlank()) {
                                IconButton(onClick = { viewModel.clearQuery() }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NPYellow,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            cursorColor = NPYellow,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.onSearchSubmitted(query)
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )
                }
            }

            // Subtle loading indicator (>300ms)
            if (showLoadingIndicator) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = NPYellow,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Content Body
            Box(modifier = Modifier.fillMaxSize()) {
                if (query.isBlank()) {
                    // Empty query: Show recent searches + recently played suggestions
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Recent Searches
                        if (recentSearches.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "RECENT SEARCHES",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp
                                    )
                                    TextButton(onClick = { viewModel.clearRecentSearches() }) {
                                        Text(
                                            text = "Clear all",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NPYellow,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    recentSearches.forEach { recentItem ->
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier.clickable {
                                                viewModel.setQuery(recentItem)
                                                viewModel.onSearchSubmitted(recentItem)
                                                keyboardController?.hide()
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.History,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = recentItem,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                IconButton(
                                                    onClick = { viewModel.removeRecentSearch(recentItem) },
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Close,
                                                        contentDescription = "Remove",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Recently Played Suggestions
                        if (recentlyPlayed.isNotEmpty()) {
                            item {
                                Text(
                                    text = "RECENTLY PLAYED",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }

                            items(recentlyPlayed, key = { it.id }) { video ->
                                SuggestedVideoItem(
                                    video = video,
                                    onClick = {
                                        onPlayVideo(video.path, video.title)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Query non-empty: Show search results or Empty State
                    if (searchResult.totalMatches == 0 && !showLoadingIndicator) {
                        EmptySearchResultsState(query = query)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Folders section
                            if (searchResult.matchingFolders.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "FOLDERS (${searchResult.matchingFolders.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }

                                items(
                                    items = searchResult.matchingFolders,
                                    key = { "folder_${it.folder.path}" }
                                ) { folderItem ->
                                    FolderSearchRow(
                                        folderResult = folderItem,
                                        onClick = {
                                            viewModel.onSearchSubmitted(query)
                                            onFolderClick(folderItem.folder.path, folderItem.folder.name)
                                        }
                                    )
                                }
                            }

                            // Videos section
                            if (searchResult.matchingVideos.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "VIDEOS (${searchResult.matchingVideos.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                    )
                                }

                                items(
                                    items = searchResult.matchingVideos,
                                    key = { "video_${it.video.id}" }
                                ) { videoItem ->
                                    VideoSearchRow(
                                        videoResult = videoItem,
                                        isSelected = selectionState.isSelected(videoItem.video.id),
                                        isSelectionActive = isSelectionActive,
                                        onClick = {
                                            if (isSelectionActive) {
                                                selectionState = selectionState.toggle(videoItem.video.id)
                                            } else {
                                                viewModel.onSearchSubmitted(query)
                                                onPlayVideo(videoItem.video.path, videoItem.video.title)
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectionState = SelectionModeState(isActive = true, selectedIds = setOf(videoItem.video.id))
                                        },
                                        onThumbnailClick = {
                                            if (isSelectionActive) {
                                                selectionState = selectionState.toggle(videoItem.video.id)
                                            } else {
                                                viewModel.onSearchSubmitted(query)
                                                onPlayVideo(videoItem.video.path, videoItem.video.title)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isSelectionActive) {
                SelectionBottomBar(
                    isAllSelected = selectedVideos.isNotEmpty() && selectedVideos.size == searchResult.matchingVideos.size,
                    selectedCount = selectionState.selectedCount,
                    onToggleSelectAll = {
                        val allIds = searchResult.matchingVideos.map { it.video.id }
                        selectionState = selectionState.selectAll(allIds)
                    },
                    onMove = { Toast.makeText(context, "Move from folder view", Toast.LENGTH_SHORT).show() },
                    onCopy = { Toast.makeText(context, "Copy from folder view", Toast.LENGTH_SHORT).show() },
                    onAddToPlaylist = { Toast.makeText(context, "Add to playlist from folder view", Toast.LENGTH_SHORT).show() },
                    onLabel = { Toast.makeText(context, "Manage labels in folder view or Menu", Toast.LENGTH_SHORT).show() },
                    onRename = { Toast.makeText(context, "Rename in folder view", Toast.LENGTH_SHORT).show() },
                    onDelete = { Toast.makeText(context, "Delete from folder view", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }

    if (showInfoSheet && selectedVideos.isNotEmpty()) {
        VideoInfoDetailedBottomSheet(
            videos = selectedVideos,
            onDismiss = { showInfoSheet = false },
            onShare = { FileOperationsHelper.shareVideos(context, it) }
        )
    }
}

@Composable
private fun FolderSearchRow(
    folderResult: FolderSearchResult,
    onClick: () -> Unit
) {
    val folder = folderResult.folder
    val highlightedName = rememberHighlightedAnnotatedString(
        text = folder.name,
        ranges = folderResult.nameRanges
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = NPYellow.copy(alpha = 0.18f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlightedName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${folder.videoCount} videos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VideoSearchRow(
    videoResult: VideoSearchResult,
    isSelected: Boolean = false,
    isSelectionActive: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onThumbnailClick: () -> Unit = onClick
) {
    val video = videoResult.video
    val highlightedTitle = rememberHighlightedAnnotatedString(
        text = video.title,
        ranges = videoResult.titleRanges
    )

    val backgroundColor = if (isSelected) NPYellow.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
    val border = if (isSelected) BorderStroke(1.5.dp, NPYellow) else null

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = border,
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with duration pill and resume progress bar
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NPBlack)
                    .clickable(onClick = onThumbnailClick),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = Uri.fromFile(File(video.path)),
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

                // Duration pill (bottom-right)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = NPBlack.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = formatDuration(video.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = NPWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                // Resume progress bar (bottom edge)
                if (video.lastPlayedPositionMs > 0 && video.durationMs > 0) {
                    val progress = (video.lastPlayedPositionMs.toFloat() / video.durationMs).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .background(NPYellow)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and metadata line
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = highlightedTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${video.folderName} • ${formatFileSize(video.sizeBytes)} • ${formatDuration(video.durationMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SuggestedVideoItem(
    video: VideoEntity,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 54.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NPBlack)
            ) {
                AsyncImage(
                    model = Uri.fromFile(File(video.path)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (video.lastPlayedPositionMs > 0 && video.durationMs > 0) {
                    val progress = (video.lastPlayedPositionMs.toFloat() / video.durationMs).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .background(NPYellow)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${video.folderName} • ${formatDuration(video.durationMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptySearchResultsState(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No results for \"$query\"",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Check spelling or try a different video or folder keyword.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun rememberHighlightedAnnotatedString(
    text: String,
    ranges: List<IntRange>
): AnnotatedString {
    return remember(text, ranges) {
        buildAnnotatedString {
            if (ranges.isEmpty()) {
                append(text)
                return@buildAnnotatedString
            }
            var cursor = 0
            for (range in ranges) {
                val start = range.first.coerceIn(0, text.length)
                val end = (range.last + 1).coerceIn(0, text.length)
                if (cursor < start) {
                    append(text.substring(cursor, start))
                }
                if (start < end) {
                    withStyle(
                        SpanStyle(
                            background = NPYellow,
                            color = NPBlack,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(text.substring(start, end))
                    }
                }
                cursor = maxOf(cursor, end)
            }
            if (cursor < text.length) {
                append(text.substring(cursor))
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "00:00"
    val totalSeconds = durationMs / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

private fun formatFileSize(sizeBytes: Long): String {
    if (sizeBytes <= 0) return "0 B"
    val kb = sizeBytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        else -> String.format(Locale.US, "%.0f KB", kb)
    }
}
