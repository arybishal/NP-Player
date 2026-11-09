package com.npplayer.feature.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.npplayer.core.data.database.dao.LabelWithCount
import com.npplayer.core.ui.component.NpLogo
import com.npplayer.core.ui.component.NpLogoVariant
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun MenuScreen(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPrivate: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onNavigateToPlaylists: () -> Unit = {},
    onNavigateToMediaManager: () -> Unit = {},
    onNavigateToHelpFaq: () -> Unit = {},
    onNavigateToLegalAbout: () -> Unit = {},
    viewModel: MenuViewModel = hiltViewModel()
) {
    val labelsWithCount by viewModel.labelsWithCount.collectAsState()
    var showLabelsSheet by remember { mutableStateOf(false) }

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
                        text = "Menu",
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
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NpLogo(
                            variant = NpLogoVariant.HORIZONTAL,
                            size = 36.dp,
                            showTagline = true
                        )
                    }
                }
            }
            item {
                MenuTile(
                    icon = Icons.Outlined.Lock,
                    title = "Private Vault",
                    subtitle = "Biometric & PIN secured hidden videos",
                    onClick = onNavigateToPrivate
                )
            }
            item {
                MenuTile(
                    icon = Icons.Outlined.Public,
                    title = "Network Hub",
                    subtitle = "Shares (SMB/SFTP), Streams, Live TV & DLNA",
                    onClick = onNavigateToNetwork
                )
            }
            item {
                MenuTile(
                    icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                    title = "Video Playlists",
                    subtitle = "Custom queues and playlists",
                    onClick = onNavigateToPlaylists
                )
            }
            item {
                MenuTile(
                    icon = Icons.AutoMirrored.Outlined.Label,
                    title = "Labels & Tags",
                    subtitle = "Organize videos with custom tags (${labelsWithCount.size} active)",
                    onClick = { showLabelsSheet = true }
                )
            }
            item {
                MenuTile(
                    icon = Icons.Outlined.Storage,
                    title = "Media Manager",
                    subtitle = "Storage analyzer, duplicates & large files",
                    onClick = onNavigateToMediaManager
                )
            }
            item {
                MenuTile(
                    icon = Icons.Outlined.Settings,
                    title = "Settings",
                    subtitle = "Player, decoder, themes & preferences",
                    onClick = onNavigateToSettings
                )
            }
            item {
                MenuTile(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    title = "Help & FAQ",
                    subtitle = "Usage guides and tips",
                    onClick = onNavigateToHelpFaq
                )
            }
            item {
                MenuTile(
                    icon = Icons.Outlined.Info,
                    title = "Legal & About",
                    subtitle = "Version 1.0.0 • Open-source licenses",
                    onClick = onNavigateToLegalAbout
                )
            }
        }
    }

    if (showLabelsSheet) {
        LabelsManagerBottomSheet(
            labels = labelsWithCount,
            onCreateLabel = { viewModel.createLabel(it) },
            onDeleteLabel = { viewModel.deleteLabel(it) },
            onDismiss = { showLabelsSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabelsManagerBottomSheet(
    labels: List<LabelWithCount>,
    onCreateLabel: (String) -> Unit,
    onDeleteLabel: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newLabelText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NPBlack,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Label,
                    contentDescription = null,
                    tint = NPYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Labels & Tags",
                    color = NPWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Create Label Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newLabelText,
                    onValueChange = { newLabelText = it },
                    placeholder = {
                        Text(
                            text = "New label name...",
                            fontSize = 14.sp,
                            color = NPWhite.copy(alpha = 0.5f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NPYellow,
                        unfocusedBorderColor = NPWhite.copy(alpha = 0.25f),
                        cursorColor = NPYellow,
                        focusedTextColor = NPWhite,
                        unfocusedTextColor = NPWhite
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (newLabelText.isNotBlank()) {
                            onCreateLabel(newLabelText)
                            newLabelText = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NPYellow,
                        contentColor = NPBlack
                    ),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD", fontWeight = FontWeight.Bold)
                }
            }

            // Labels List
            if (labels.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NPWhite.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "No custom labels yet. Type a name above and tap ADD to create your first tag.",
                        color = NPWhite.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(labels, key = { it.id }) { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NPWhite.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, NPWhite.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Label,
                                    contentDescription = null,
                                    tint = NPYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = NPWhite
                                    )
                                    Text(
                                        text = "${item.videoCount} videos tagged",
                                        fontSize = 11.sp,
                                        color = NPWhite.copy(alpha = 0.6f)
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteLabel(item.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete label",
                                        tint = NPWhite.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuTile(
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
                modifier = Modifier.size(24.dp)
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
