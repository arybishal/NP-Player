package com.npplayer.feature.library.music

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

val ALPHABET_INDEX = listOf("#") + ('A'..'Z').map { it.toString() }

@Composable
fun AlphabetFastScroller(
    modifier: Modifier = Modifier,
    onLetterSelected: (String) -> Unit
) {
    var componentHeight by remember { mutableStateOf(0) }
    var activeLetter by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .width(28.dp)
            .padding(vertical = 12.dp)
            .onGloballyPositioned { coordinates ->
                componentHeight = coordinates.size.height
            }
            .pointerInput(componentHeight) {
                if (componentHeight <= 0) return@pointerInput
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        val itemHeight = componentHeight.toFloat() / ALPHABET_INDEX.size
                        val index = (offset.y / itemHeight).toInt().coerceIn(0, ALPHABET_INDEX.size - 1)
                        val letter = ALPHABET_INDEX[index]
                        activeLetter = letter
                        onLetterSelected(letter)
                    },
                    onDragEnd = {
                        activeLetter = null
                    },
                    onDragCancel = {
                        activeLetter = null
                    },
                    onVerticalDrag = { change, _ ->
                        change.consume()
                        val itemHeight = componentHeight.toFloat() / ALPHABET_INDEX.size
                        val index = (change.position.y / itemHeight).toInt().coerceIn(0, ALPHABET_INDEX.size - 1)
                        val letter = ALPHABET_INDEX[index]
                        if (activeLetter != letter) {
                            activeLetter = letter
                            onLetterSelected(letter)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ALPHABET_INDEX.forEach { char ->
                val isSelected = activeLetter == char
                Text(
                    text = char,
                    color = if (isSelected) NPYellow else NPWhite.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 0.5.dp)
                )
            }
        }

        // Active letter popup bubble
        activeLetter?.let { letter ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NPYellow),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    color = NPBlack,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
