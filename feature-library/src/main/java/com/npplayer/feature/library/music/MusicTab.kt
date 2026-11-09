package com.npplayer.feature.library.music

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class MusicTab(val label: String, val icon: ImageVector) {
    SONGS("Songs", Icons.Outlined.MusicNote),
    ALBUMS("Albums", Icons.Outlined.Album),
    ARTISTS("Artists", Icons.Outlined.Person),
    PLAYLISTS("Playlists", Icons.AutoMirrored.Outlined.PlaylistPlay),
    GENRES("Genres", Icons.Outlined.Category),
    FOLDERS("Folders", Icons.Outlined.Folder),
    FAVORITES("Favorites", Icons.Outlined.Favorite),
    RECENTLY_ADDED("Recently added", Icons.Outlined.History)
}
