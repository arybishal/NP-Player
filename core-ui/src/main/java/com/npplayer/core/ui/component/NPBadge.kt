package com.npplayer.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun NPNewBadge(
    count: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = NPYellow,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (count > 0) "NEW $count" else "NEW",
            color = NPBlack,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun NPSubtitleBadge(
    languageCode: String = "SRT",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = NPYellow,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = languageCode.uppercase(),
            color = NPYellow,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
    }
}
