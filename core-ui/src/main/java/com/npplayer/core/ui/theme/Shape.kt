package com.npplayer.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Strict rounded corners adhering to 16–28dp
val NPShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // For compact badges
    small = RoundedCornerShape(12.dp),       // For mini cards/thumbnails
    medium = RoundedCornerShape(16.dp),      // For folder and video item cards
    large = RoundedCornerShape(20.dp),       // For dialogs and bottom sheets
    extraLarge = RoundedCornerShape(28.dp)   // For buttons, FAB, search bar, chips
)
