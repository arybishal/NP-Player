package com.npplayer.feature.network.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Stream
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.npplayer.core.data.database.entity.StreamBookmarkEntity
import com.npplayer.core.ui.component.NPPrimaryButton
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.feature.network.NetworkHubViewModel

@Composable
fun NetworkHubScreen(
    onBack: () -> Unit,
    onPlayStream: (url: String, title: String) -> Unit,
    viewModel: NetworkHubViewModel = hiltViewModel()
) {
    val savedStreams by viewModel.savedStreams.collectAsState()

    var showStreamDialog by remember { mutableStateOf(false) }
    var showSharesDialog by remember { mutableStateOf(false) }
    var showIptvDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
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
                        text = "Network Hub",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                NetworkHubTile(
                    icon = Icons.Outlined.Stream,
                    title = "Network Stream",
                    subtitle = "Play direct URL (HTTP, HLS, DASH, RTSP)",
                    onClick = { showStreamDialog = true }
                )
            }
            item {
                NetworkHubTile(
                    icon = Icons.Outlined.Dns,
                    title = "Network Shares",
                    subtitle = "Connect via SMB, SFTP, FTP, WebDAV, NFS",
                    onClick = { showSharesDialog = true }
                )
            }
            item {
                NetworkHubTile(
                    icon = Icons.Outlined.LiveTv,
                    title = "Live TV (IPTV)",
                    subtitle = "Load M3U / M3U8 playlists with channel guide",
                    onClick = { showIptvDialog = true }
                )
            }
            item {
                NetworkHubTile(
                    icon = Icons.Outlined.Radio,
                    title = "Internet Radio",
                    subtitle = "Stream thousands of global stations",
                    onClick = {
                        onPlayStream("https://stream.live.vc.bbcmedia.co.uk/bbc_world_service", "BBC World Service")
                    }
                )
            }
            item {
                NetworkHubTile(
                    icon = Icons.Outlined.Podcasts,
                    title = "Podcasts",
                    subtitle = "Search, subscribe, and resume episodes",
                    onClick = { showStreamDialog = true }
                )
            }
            item {
                NetworkHubTile(
                    icon = Icons.Outlined.Wifi,
                    title = "Local DLNA Network",
                    subtitle = "Discover media servers and smart TVs",
                    onClick = { showSharesDialog = true }
                )
            }

            if (savedStreams.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SAVED STREAMS (${savedStreams.size})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                items(savedStreams, key = { it.id }) { stream ->
                    SavedStreamRow(
                        stream = stream,
                        onClick = { onPlayStream(stream.url, stream.title) },
                        onDelete = { viewModel.deleteStream(stream) }
                    )
                }
            }
        }
    }

    if (showStreamDialog) {
        NetworkStreamBottomSheet(
            onDismiss = { showStreamDialog = false },
            onPlay = { url, title, type ->
                viewModel.saveAndPlayStream(title, url, type, onPlayStream)
                showStreamDialog = false
            }
        )
    }

    if (showSharesDialog) {
        NetworkSharesBottomSheet(
            onDismiss = { showSharesDialog = false }
        )
    }

    if (showIptvDialog) {
        LiveTvBottomSheet(
            onDismiss = { showIptvDialog = false },
            onPlayChannel = { url, name ->
                onPlayStream(url, name)
                showIptvDialog = false
            }
        )
    }
}

@Composable
private fun NetworkHubTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NPYellow,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SavedStreamRow(
    stream: StreamBookmarkEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(NPYellow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    tint = NPBlack,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stream.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${stream.type} • ${stream.url}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetworkStreamBottomSheet(
    onDismiss: () -> Unit,
    onPlay: (url: String, title: String, type: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("HLS") }

    val types = listOf("HLS", "DASH", "MP4", "RTSP")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Open Network Stream",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("Stream URL (m3u8, mpd, mp4, rtsp)") },
                placeholder = { Text("https://example.com/live/index.m3u8") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = clipboard.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                val text = clip.getItemAt(0).text?.toString() ?: ""
                                if (text.isNotBlank()) {
                                    urlInput = text
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentPaste,
                            contentDescription = "Paste",
                            tint = NPYellow
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NPYellow,
                    cursorColor = NPYellow
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                label = { Text("Stream Name (Optional)") },
                placeholder = { Text("e.g. Big Buck Bunny Stream") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NPYellow,
                    cursorColor = NPYellow
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Protocol Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                types.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NPYellow,
                            selectedLabelColor = NPBlack
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            NPPrimaryButton(
                onClick = { onPlay(urlInput, titleInput, selectedType) },
                enabled = urlInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Outlined.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Play Network Stream",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetworkSharesBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var shareProtocol by remember { mutableStateOf("SMB") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("445") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val protocols = listOf("SMB", "SFTP", "FTP", "WebDAV", "NFS")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Connect Network Share",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(protocols) { p ->
                    FilterChip(
                        selected = shareProtocol == p,
                        onClick = {
                            shareProtocol = p
                            port = when (p) {
                                "SMB" -> "445"
                                "SFTP" -> "22"
                                "FTP" -> "21"
                                "WebDAV" -> "80"
                                "NFS" -> "2049"
                                else -> "445"
                            }
                        },
                        label = { Text(p, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NPYellow,
                            selectedLabelColor = NPBlack
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Server Host / IP") },
                    placeholder = { Text("192.168.1.100") },
                    modifier = Modifier.weight(0.7f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NPYellow, cursorColor = NPYellow)
                )

                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port") },
                    modifier = Modifier.weight(0.3f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NPYellow, cursorColor = NPYellow)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                placeholder = { Text("guest / admin") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NPYellow, cursorColor = NPYellow)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NPYellow, cursorColor = NPYellow)
            )

            Spacer(modifier = Modifier.height(20.dp))

            NPPrimaryButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Outlined.Dns, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Connect & Save Share", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class IptvChannel(
    val name: String,
    val category: String,
    val url: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiveTvBottomSheet(
    onDismiss: () -> Unit,
    onPlayChannel: (url: String, name: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val sampleChannels = listOf(
        IptvChannel("NASA TV HD", "Science", "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8"),
        IptvChannel("Red Bull TV Live", "Sports", "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8"),
        IptvChannel("DW English News", "News", "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8"),
        IptvChannel("France 24 English", "News", "https://static.france24.com/live/F24_EN_LO_HLS/live_tv.m3u8"),
        IptvChannel("Al Jazeera English", "News", "https://live-hls-web-aje.getaj.net/AJE/03.m3u8")
    )

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "News", "Science", "Sports")

    val filteredChannels = if (selectedCategory == "All") {
        sampleChannels
    } else {
        sampleChannels.filter { it.category == selectedCategory }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LiveTv,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live TV (IPTV)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NPYellow,
                            selectedLabelColor = NPBlack
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredChannels) { ch ->
                    Surface(
                        onClick = { onPlayChannel(ch.url, ch.name) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.background,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NPYellow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tv,
                                    contentDescription = null,
                                    tint = NPBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ch.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = ch.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = "Play",
                                tint = NPYellow,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
