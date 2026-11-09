package com.npplayer.player.ui.sheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Hearing
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.subtitles.SubtitleItem
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

sealed interface OnlineSubtitlesUiState {
    object Idle : OnlineSubtitlesUiState
    object Loading : OnlineSubtitlesUiState
    object Empty : OnlineSubtitlesUiState
    data class Success(val items: List<SubtitleItem>) : OnlineSubtitlesUiState
    data class Error(val message: String) : OnlineSubtitlesUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineSubtitlesBottomSheet(
    uiState: OnlineSubtitlesUiState,
    initialQuery: String,
    preferredLanguages: Set<String>,
    onSearch: (query: String, languages: List<String>) -> Unit,
    onDownloadSubtitle: (SubtitleItem) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var queryText by remember(initialQuery) { mutableStateOf(initialQuery) }
    var selectedLanguages by remember(preferredLanguages) {
        mutableStateOf(preferredLanguages.ifEmpty { setOf("en") }.toMutableSet())
    }
    var downloadingId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (uiState is OnlineSubtitlesUiState.Idle) {
            onSearch(initialQuery, selectedLanguages.toList())
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NPBlack,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Subtitles,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Online Subtitles",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NPWhite
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NPYellow
                    ) {
                        Text(
                            text = "SubDL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NPBlack,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = NPWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = queryText,
                onValueChange = {
                    queryText = it
                },
                placeholder = {
                    Text(
                        text = "Search film, show, or SxxExx...",
                        fontSize = 13.sp,
                        color = NPWhite.copy(alpha = 0.40f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = NPWhite.copy(alpha = 0.70f)
                    )
                },
                trailingIcon = {
                    if (queryText.isNotEmpty()) {
                        IconButton(onClick = {
                            queryText = ""
                            onSearch("", selectedLanguages.toList())
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Clear",
                                tint = NPWhite.copy(alpha = 0.70f)
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            onSearch(queryText, selectedLanguages.toList())
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Search",
                                tint = NPYellow
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NPYellow,
                    unfocusedBorderColor = NPWhite.copy(alpha = 0.20f),
                    focusedTextColor = NPWhite,
                    unfocusedTextColor = NPWhite,
                    cursorColor = NPYellow
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Language Filter Chips Row
            val availableLanguages = listOf(
                "en" to "English",
                "es" to "Spanish",
                "hi" to "Hindi",
                "ne" to "Nepali",
                "fr" to "French",
                "de" to "German",
                "ar" to "Arabic",
                "zh" to "Chinese",
                "ja" to "Japanese"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableLanguages.forEach { (code, label) ->
                    val isSelected = selectedLanguages.contains(code)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updated = selectedLanguages.toMutableSet()
                            if (isSelected) {
                                if (updated.size > 1) updated.remove(code)
                            } else {
                                updated.add(code)
                            }
                            selectedLanguages = updated
                            onSearch(queryText, updated.toList())
                        },
                        label = {
                            Text(
                                text = code.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) NPBlack else NPWhite
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = NPWhite.copy(alpha = 0.08f),
                            selectedContainerColor = NPYellow
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = NPWhite.copy(alpha = 0.15f),
                            selectedBorderColor = NPYellow
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 400.dp),
                contentAlignment = Alignment.Center
            ) {
                when (uiState) {
                    is OnlineSubtitlesUiState.Loading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = NPYellow,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Searching SubDL subtitles...",
                                fontSize = 13.sp,
                                color = NPWhite.copy(alpha = 0.70f)
                            )
                        }
                    }

                    is OnlineSubtitlesUiState.Empty -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Subtitles,
                                contentDescription = null,
                                tint = NPWhite.copy(alpha = 0.30f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No subtitles found on SubDL",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NPWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try modifying the title or season/episode above.",
                                fontSize = 12.sp,
                                color = NPWhite.copy(alpha = 0.50f)
                            )
                        }
                    }

                    is OnlineSubtitlesUiState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Unable to fetch subtitles",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NPWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = uiState.message,
                                fontSize = 12.sp,
                                color = NPWhite.copy(alpha = 0.60f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                onClick = { onSearch(queryText, selectedLanguages.toList()) },
                                shape = RoundedCornerShape(8.dp),
                                color = NPYellow
                            ) {
                                Text(
                                    text = "Retry Search",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NPBlack,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    is OnlineSubtitlesUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.items, key = { it.id }) { item ->
                                val isDownloading = downloadingId == item.id
                                SubtitleResultCard(
                                    item = item,
                                    isDownloading = isDownloading,
                                    onDownloadClick = {
                                        downloadingId = item.id
                                        onDownloadSubtitle(item)
                                    }
                                )
                            }
                        }
                    }

                    OnlineSubtitlesUiState.Idle -> {
                        // Waiting for initial query
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SubtitleResultCard(
    item: SubtitleItem,
    isDownloading: Boolean,
    onDownloadClick: () -> Unit
) {
    Surface(
        onClick = onDownloadClick,
        shape = RoundedCornerShape(12.dp),
        color = NPWhite.copy(alpha = 0.05f),
        border = BorderStroke(
            1.dp,
            if (item.matchScore >= 60) NPYellow.copy(alpha = 0.40f) else NPWhite.copy(alpha = 0.10f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Top Row: Language badge + Best Match Badge + Hearing Impaired
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NPYellow
                    ) {
                        Text(
                            text = item.languageCode.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NPBlack,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }

                    if (item.matchScore >= 60) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NPYellow.copy(alpha = 0.20f),
                            border = BorderStroke(1.dp, NPYellow)
                        ) {
                            Text(
                                text = "BEST MATCH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NPYellow,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (item.hearingImpaired) {
                        Icon(
                            imageVector = Icons.Outlined.Hearing,
                            contentDescription = "Hearing Impaired",
                            tint = NPWhite.copy(alpha = 0.70f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = item.language,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NPWhite.copy(alpha = 0.70f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Release Name
                Text(
                    text = item.releaseName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NPWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Author & Downloads
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "By ${item.author}",
                        fontSize = 11.sp,
                        color = NPWhite.copy(alpha = 0.50f)
                    )
                    if (item.downloadCount > 0) {
                        Text(
                            text = "${item.downloadCount} dl",
                            fontSize = 11.sp,
                            color = NPWhite.copy(alpha = 0.50f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Action / Download Icon
            if (isDownloading) {
                CircularProgressIndicator(
                    color = NPYellow,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NPYellow,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Download Subtitle",
                            tint = NPBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
