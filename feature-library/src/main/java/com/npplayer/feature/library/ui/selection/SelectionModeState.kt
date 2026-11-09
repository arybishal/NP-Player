package com.npplayer.feature.library.ui.selection

data class SelectionModeState(
    val isActive: Boolean = false,
    val selectedIds: Set<Long> = emptySet()
) {
    val selectedCount: Int get() = selectedIds.size

    fun isSelected(id: Long): Boolean = selectedIds.contains(id)

    fun toggle(id: Long): SelectionModeState {
        val newSet = if (selectedIds.contains(id)) {
            selectedIds - id
        } else {
            selectedIds + id
        }
        return if (newSet.isEmpty()) {
            copy(isActive = false, selectedIds = emptySet())
        } else {
            copy(isActive = true, selectedIds = newSet)
        }
    }

    fun selectAll(allIds: Collection<Long>): SelectionModeState {
        return if (selectedIds.containsAll(allIds) && allIds.isNotEmpty()) {
            // Deselect all
            copy(isActive = false, selectedIds = emptySet())
        } else {
            copy(isActive = true, selectedIds = allIds.toSet())
        }
    }

    fun clear(): SelectionModeState = SelectionModeState(isActive = false, selectedIds = emptySet())
}
