package com.npplayer.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.ui.R
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPPlayerTheme
import com.npplayer.core.ui.theme.NPYellow

enum class NpLogoVariant {
    ICON,
    MARK,
    HORIZONTAL
}

@Composable
fun NpLogo(
    modifier: Modifier = Modifier,
    variant: NpLogoVariant = NpLogoVariant.ICON,
    size: Dp = 32.dp,
    markTint: Color = MaterialTheme.colorScheme.onSurface,
    showTagline: Boolean = false
) {
    when (variant) {
        NpLogoVariant.ICON -> {
            Image(
                painter = painterResource(id = R.drawable.ic_np_logo_icon),
                contentDescription = "NP Player",
                modifier = modifier.size(size)
            )
        }
        NpLogoVariant.MARK -> {
            Icon(
                painter = painterResource(id = R.drawable.ic_np_logo_mark),
                contentDescription = "NP Player Mark",
                tint = markTint,
                modifier = modifier.size(size)
            )
        }
        NpLogoVariant.HORIZONTAL -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_np_logo_icon),
                    contentDescription = null,
                    modifier = Modifier.size(size)
                )
                Spacer(modifier = Modifier.width(size * 0.35f))
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "NP ",
                            fontSize = (size.value * 0.58f).sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Player",
                            fontSize = (size.value * 0.58f).sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.5).sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(size * 0.85f)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(NPYellow)
                    )
                    if (showTagline) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "PURE PERFORMANCE",
                            fontSize = (size.value * 0.22f).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "NP Logo Variants - Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun NpLogoDarkPreview() {
    NPPlayerTheme(forceDark = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            NpLogo(variant = NpLogoVariant.HORIZONTAL, size = 36.dp)
            Spacer(modifier = Modifier.height(16.dp))
            NpLogo(variant = NpLogoVariant.ICON, size = 48.dp)
            Spacer(modifier = Modifier.height(16.dp))
            NpLogo(variant = NpLogoVariant.MARK, size = 32.dp, markTint = NPYellow)
        }
    }
}

@Preview(name = "NP Logo Variants - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun NpLogoLightPreview() {
    NPPlayerTheme(forceDark = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            NpLogo(variant = NpLogoVariant.HORIZONTAL, size = 36.dp)
            Spacer(modifier = Modifier.height(16.dp))
            NpLogo(variant = NpLogoVariant.ICON, size = 48.dp)
            Spacer(modifier = Modifier.height(16.dp))
            NpLogo(variant = NpLogoVariant.MARK, size = 32.dp, markTint = NPBlack)
        }
    }
}
