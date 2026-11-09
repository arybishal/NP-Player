package com.npplayer.feature.library.ui.selection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun SelectionBottomBar(
    isAllSelected: Boolean,
    selectedCount: Int,
    onToggleSelectAll: () -> Unit,
    onMove: () -> Unit,
    onCopy: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onLabel: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Select / Deselect All
            SelectionActionItem(
                icon = if (isAllSelected) Icons.Outlined.Checklist else Icons.Outlined.SelectAll,
                label = if (isAllSelected) "Deselect" else "Select All",
                contentDesc = if (isAllSelected) "Deselect all videos" else "Select all videos",
                onClick = onToggleSelectAll,
                enabled = true,
                tint = NPYellow
            )

            // 2. Move
            SelectionActionItem(
                icon = Icons.Outlined.DriveFileMove,
                label = "Move",
                contentDesc = "Move selected videos",
                onClick = onMove,
                enabled = selectedCount > 0
            )

            // 3. Copy
            SelectionActionItem(
                icon = Icons.Outlined.ContentCopy,
                label = "Copy",
                contentDesc = "Copy selected videos",
                onClick = onCopy,
                enabled = selectedCount > 0
            )

            // 4. Add to Playlist
            SelectionActionItem(
                icon = Icons.Outlined.PlaylistAdd,
                label = "Playlist",
                contentDesc = "Add selected videos to playlist",
                onClick = onAddToPlaylist,
                enabled = selectedCount > 0
            )

            // 5. Label
            SelectionActionItem(
                icon = Icons.Outlined.Label,
                label = "Label",
                contentDesc = "Manage labels for selected videos",
                onClick = onLabel,
                enabled = selectedCount > 0
            )

            // 6. Rename
            SelectionActionItem(
                icon = Icons.Outlined.DriveFileRenameOutline,
                label = if (selectedCount > 1) "Batch" else "Rename",
                contentDesc = if (selectedCount > 1) "Batch rename selected videos" else "Rename video",
                onClick = onRename,
                enabled = selectedCount > 0
            )

            // 7. Delete
            SelectionActionItem(
                icon = Icons.Outlined.Delete,
                label = "Delete",
                contentDesc = "Delete selected videos",
                onClick = onDelete,
                enabled = selectedCount > 0,
                tint = if (selectedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )
        }
    }
}

@Composable
private fun SelectionActionItem(
    icon: ImageVector,
    label: String,
    contentDesc: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    val alpha = if (enabled) 1.0f else 0.35f
    val displayTint = tint.copy(alpha = if (enabled) 1.0f else 0.35f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(width = 64.dp, height = 54.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = contentDesc }
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = displayTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = displayTint,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
