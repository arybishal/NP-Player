package com.npplayer.feature.library.ui.selection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.data.database.entity.LabelEntity
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelPickerBottomSheet(
    allLabels: List<LabelEntity>,
    fullyAssignedLabelIds: Set<Long>,
    partiallyAssignedLabelIds: Set<Long> = emptySet(),
    onCreateLabel: (name: String) -> Unit,
    onApply: (assignedLabelIds: Set<Long>, removedLabelIds: Set<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newLabelText by remember { mutableStateOf("") }
    var newlyCreatedLabelName by remember { mutableStateOf<String?>(null) }

    // State map: labelId -> ToggleableState (On, Off, Indeterminate)
    val stateMap = remember { mutableStateMapOf<Long, ToggleableState>() }

    LaunchedEffect(allLabels, fullyAssignedLabelIds, partiallyAssignedLabelIds) {
        allLabels.forEach { label ->
            if (label.name == newlyCreatedLabelName) {
                stateMap[label.id] = ToggleableState.On
                newlyCreatedLabelName = null
            } else if (!stateMap.containsKey(label.id)) {
                stateMap[label.id] = when {
                    fullyAssignedLabelIds.contains(label.id) -> ToggleableState.On
                    partiallyAssignedLabelIds.contains(label.id) -> ToggleableState.Indeterminate
                    else -> ToggleableState.Off
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Label,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Manage Labels",
                        fontSize = 20.sp,
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

            Spacer(modifier = Modifier.height(16.dp))

            // Create Label Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newLabelText,
                    onValueChange = { newLabelText = it },
                    placeholder = { Text("New label name...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NPYellow,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = NPYellow
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val trimmed = newLabelText.trim()
                        if (trimmed.isNotBlank()) {
                            newlyCreatedLabelName = trimmed
                            onCreateLabel(trimmed)
                            newLabelText = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NPYellow, contentColor = NPBlack),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null, tint = NPBlack)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "ADD", fontWeight = FontWeight.Bold, color = NPBlack)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Label List
            if (allLabels.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "No labels created yet. Enter a name above to create one.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allLabels, key = { it.id }) { label ->
                        val currentState = stateMap[label.id] ?: ToggleableState.Off

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (currentState == ToggleableState.On) NPYellow.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    stateMap[label.id] = when (currentState) {
                                        ToggleableState.Off -> ToggleableState.On
                                        ToggleableState.Indeterminate -> ToggleableState.On
                                        ToggleableState.On -> ToggleableState.Off
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TriStateCheckbox(
                                    state = currentState,
                                    onClick = {
                                        stateMap[label.id] = when (currentState) {
                                            ToggleableState.Off -> ToggleableState.On
                                            ToggleableState.Indeterminate -> ToggleableState.On
                                            ToggleableState.On -> ToggleableState.Off
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = NPYellow,
                                        checkmarkColor = NPBlack,
                                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = label.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Apply Button
            Button(
                onClick = {
                    val assigned = mutableSetOf<Long>()
                    val removed = mutableSetOf<Long>()
                    stateMap.forEach { (labelId, state) ->
                        when (state) {
                            ToggleableState.On -> assigned.add(labelId)
                            ToggleableState.Off -> removed.add(labelId)
                            ToggleableState.Indeterminate -> { /* leave untouched */ }
                        }
                    }
                    onApply(assigned, removed)
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NPYellow, contentColor = NPBlack),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "APPLY LABELS", fontWeight = FontWeight.Bold, color = NPBlack, fontSize = 14.sp)
            }
        }
    }
}
