package com.npplayer.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.database.dao.LabelDao
import com.npplayer.core.data.database.dao.LabelWithCount
import com.npplayer.core.data.database.entity.LabelEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val labelDao: LabelDao
) : ViewModel() {

    val labelsWithCount: StateFlow<List<LabelWithCount>> = labelDao.getAllLabelsWithCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createLabel(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            labelDao.insertLabel(LabelEntity(name = trimmed))
        }
    }

    fun deleteLabel(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            labelDao.deleteLabel(id)
        }
    }
}
