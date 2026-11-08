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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun MenuScreen(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPrivate: () -> Unit,
    onNavigateToNetwork: () -> Unit
) {
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
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
                    onClick = {}
                )
            }
            item {
                MenuTile(
                    icon = Icons.Outlined.Share,
                    title = "File Transfer",
                    subtitle = "Fast Wi-Fi direct phone-to-phone / PC",
                    onClick = {}
                )
            }
            item {
                MenuTile(
                    icon = Icons.Outlined.Storage,
                    title = "Media Manager",
                    subtitle = "Storage analyzer, duplicates & large files",
                    onClick = {}
                )
            }
            item {
                MenuTile(
                    icon = Icons.Outlined.Cloud,
                    title = "Cloud Drive",
                    subtitle = "Google Drive, OneDrive, Dropbox",
                    onClick = {}
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
                    onClick = {}
                )
            }
            item {
                MenuTile(
                    icon = Icons.Outlined.Info,
                    title = "Legal & About",
                    subtitle = "Version 1.0.0 • Open-source licenses",
                    onClick = {}
                )
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
