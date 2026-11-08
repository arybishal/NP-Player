package com.npplayer.feature.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.npplayer.core.data.preferences.PlayerAction
import com.npplayer.core.data.preferences.PlayerControlsSlots
import com.npplayer.core.ui.component.NPOutlinedButton
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.player.ui.slots.PlayerActionIconButton
import com.npplayer.player.ui.slots.getActionIcon

@Composable
fun ControlsEditorScreen(
    onBack: () -> Unit,
    viewModel: ControlsEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

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
                        text = "Player Controls Editor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                    TextButton(onClick = { viewModel.showResetDialog() }) {
                        Text(
                            text = "RESET",
                            color = NPYellow,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Live 16:9 Scaled Player Preview Canvas
            item {
                Text(
                    text = "Live Player Preview",
                    style = MaterialTheme.typography.labelLarge,
                    color = NPYellow,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LivePlayerCanvasPreview(
                    slots = state.slots,
                    selectedTier = state.selectedTier,
                    onTierSelect = { viewModel.selectTier(it) }
                )
            }

            // 2. Slot Selection Tabs with Counter Badges
            item {
                SlotTierChips(
                    slots = state.slots,
                    selectedTier = state.selectedTier,
                    onSelect = { viewModel.selectTier(it) }
                )
            }

            // 3. Action Description Card
            item {
                val action = state.selectedAction
                if (action != null) {
                    ActionDescriptionCard(
                        action = action,
                        isAssignedToCurrentTier = isActionInTier(action, state.slots, state.selectedTier),
                        onToggleAssign = { viewModel.toggleActionInCurrentTier(action) },
                        onRemove = { viewModel.removeAction(action) }
                    )
                }
            }

            // 4. Available Actions Header
            item {
                Text(
                    text = "Available Controls (Tap to select / assign)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            // 5. 24 Actions Grid
            item {
                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(PlayerAction.values().toList(), key = { it.id }) { itemAction ->
                            val isAssigned = isActionAssignedAnywhere(itemAction, state.slots)
                            val isSelected = state.selectedAction == itemAction

                            ActionGridTile(
                                action = itemAction,
                                isAssigned = isAssigned,
                                isSelected = isSelected,
                                onClick = {
                                    viewModel.selectAction(itemAction)
                                    viewModel.toggleActionInCurrentTier(itemAction)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Reset Confirmation Dialog
        if (state.isResetDialogVisible) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissResetDialog() },
                title = { Text("Reset Player Controls?") },
                text = { Text("This will revert all 4 slots back to default layout.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetToDefaults() }) {
                        Text("RESET", color = NPYellow, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissResetDialog() }) {
                        Text("CANCEL", color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun LivePlayerCanvasPreview(
    slots: PlayerControlsSlots,
    selectedTier: SelectedSlotTier,
    onTierSelect: (SelectedSlotTier) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = NPBlack,
        border = BorderStroke(1.5.dp, NPYellow.copy(alpha = 0.50f)),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Top Bar in Preview
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Demo_Video.mp4",
                    color = NPWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Beside Title slot preview
                Row(
                    modifier = Modifier
                        .clickable { onTierSelect(SelectedSlotTier.BESIDE_TITLE) }
                        .background(
                            color = if (selectedTier == SelectedSlotTier.BESIDE_TITLE) NPYellow.copy(alpha = 0.20f) else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(2.dp)
                ) {
                    slots.besideTitle.forEach { action ->
                        Icon(
                            imageVector = getActionIcon(action),
                            contentDescription = null,
                            tint = if (selectedTier == SelectedSlotTier.BESIDE_TITLE) NPYellow else NPWhite,
                            modifier = Modifier.size(16.dp).padding(1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Top Right slot preview
                Row(
                    modifier = Modifier
                        .clickable { onTierSelect(SelectedSlotTier.TOP_RIGHT) }
                        .background(
                            color = if (selectedTier == SelectedSlotTier.TOP_RIGHT) NPYellow.copy(alpha = 0.20f) else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(2.dp)
                ) {
                    slots.topRight.forEach { action ->
                        Icon(
                            imageVector = getActionIcon(action),
                            contentDescription = null,
                            tint = if (selectedTier == SelectedSlotTier.TOP_RIGHT) NPYellow else NPWhite,
                            modifier = Modifier.size(16.dp).padding(1.dp)
                        )
                    }
                }
            }

            // Center Play Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Center)
                    .background(NPYellow, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = NPBlack,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Bottom Slots & Seekbar
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Bottom Left
                    Row(
                        modifier = Modifier
                            .clickable { onTierSelect(SelectedSlotTier.BOTTOM_LEFT) }
                            .background(
                                color = if (selectedTier == SelectedSlotTier.BOTTOM_LEFT) NPYellow.copy(alpha = 0.20f) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(2.dp)
                    ) {
                        slots.bottomLeft.forEach { action ->
                            Icon(
                                imageVector = getActionIcon(action),
                                contentDescription = null,
                                tint = if (selectedTier == SelectedSlotTier.BOTTOM_LEFT) NPYellow else NPWhite,
                                modifier = Modifier.size(16.dp).padding(1.dp)
                            )
                        }
                    }

                    // Bottom Right
                    Row(
                        modifier = Modifier
                            .clickable { onTierSelect(SelectedSlotTier.BOTTOM_RIGHT) }
                            .background(
                                color = if (selectedTier == SelectedSlotTier.BOTTOM_RIGHT) NPYellow.copy(alpha = 0.20f) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(2.dp)
                    ) {
                        slots.bottomRight.forEach { action ->
                            Icon(
                                imageVector = getActionIcon(action),
                                contentDescription = null,
                                tint = if (selectedTier == SelectedSlotTier.BOTTOM_RIGHT) NPYellow else NPWhite,
                                modifier = Modifier.size(16.dp).padding(1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Miniature Seekbar line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(NPWhite.copy(alpha = 0.20f), shape = RoundedCornerShape(1.5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(3.dp)
                            .background(NPYellow, shape = RoundedCornerShape(1.5.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun SlotTierChips(
    slots: PlayerControlsSlots,
    selectedTier: SelectedSlotTier,
    onSelect: (SelectedSlotTier) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TierChip(
            label = "Title",
            used = slots.besideTitle.size,
            max = PlayerControlsSlots.MAX_BESIDE_TITLE,
            selected = selectedTier == SelectedSlotTier.BESIDE_TITLE,
            onClick = { onSelect(SelectedSlotTier.BESIDE_TITLE) },
            modifier = Modifier.weight(1f)
        )
        TierChip(
            label = "Top R",
            used = slots.topRight.size,
            max = PlayerControlsSlots.MAX_TOP_RIGHT,
            selected = selectedTier == SelectedSlotTier.TOP_RIGHT,
            onClick = { onSelect(SelectedSlotTier.TOP_RIGHT) },
            modifier = Modifier.weight(1f)
        )
        TierChip(
            label = "Btm L",
            used = slots.bottomLeft.size,
            max = PlayerControlsSlots.MAX_BOTTOM_LEFT,
            selected = selectedTier == SelectedSlotTier.BOTTOM_LEFT,
            onClick = { onSelect(SelectedSlotTier.BOTTOM_LEFT) },
            modifier = Modifier.weight(1f)
        )
        TierChip(
            label = "Btm R",
            used = slots.bottomRight.size,
            max = PlayerControlsSlots.MAX_BOTTOM_RIGHT,
            selected = selectedTier == SelectedSlotTier.BOTTOM_RIGHT,
            onClick = { onSelect(SelectedSlotTier.BOTTOM_RIGHT) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TierChip(
    label: String,
    used: Int,
    max: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) NPYellow else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) NPBlack else MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, if (selected) NPYellow else MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "$used/$max",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ActionDescriptionCard(
    action: PlayerAction,
    isAssignedToCurrentTier: Boolean,
    onToggleAssign: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = getActionIcon(action),
                    contentDescription = null,
                    tint = NPYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = action.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NPOutlinedButton(
                    onClick = onToggleAssign,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isAssignedToCurrentTier) "Remove from Slot" else "Add to Slot",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionGridTile(
    action: PlayerAction,
    isAssigned: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) NPYellow.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.5.dp,
            if (isAssigned) NPYellow else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getActionIcon(action),
                contentDescription = action.title,
                tint = if (isAssigned) NPYellow else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = action.title,
                fontSize = 9.sp,
                fontWeight = if (isAssigned) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun isActionInTier(action: PlayerAction, slots: PlayerControlsSlots, tier: SelectedSlotTier): Boolean {
    return when (tier) {
        SelectedSlotTier.BESIDE_TITLE -> slots.besideTitle.contains(action)
        SelectedSlotTier.TOP_RIGHT -> slots.topRight.contains(action)
        SelectedSlotTier.BOTTOM_LEFT -> slots.bottomLeft.contains(action)
        SelectedSlotTier.BOTTOM_RIGHT -> slots.bottomRight.contains(action)
    }
}

private fun isActionAssignedAnywhere(action: PlayerAction, slots: PlayerControlsSlots): Boolean {
    return slots.besideTitle.contains(action) ||
            slots.topRight.contains(action) ||
            slots.bottomLeft.contains(action) ||
            slots.bottomRight.contains(action)
}
