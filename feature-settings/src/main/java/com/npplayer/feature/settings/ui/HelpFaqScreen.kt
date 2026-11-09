package com.npplayer.feature.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.ui.component.NpLogo
import com.npplayer.core.ui.component.NpLogoVariant
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPYellow

data class FaqItem(
    val title: String,
    val description: String
)

@Composable
fun HelpFaqScreen(
    onBack: () -> Unit
) {
    val faqList = remember {
        listOf(
            FaqItem(
                title = "Player Gestures Guide",
                description = "• Swipe right edge vertically: Adjust volume (supports boost up to 200%).\n• Swipe left edge vertically: Adjust screen brightness.\n• Double tap right / left: Quick forward or rewind by 10s (configurable).\n• Press and hold screen: Activates instant 2x speed burst.\n• Seek Undo: Tapping a large seek jump displays a 5-second undo button."
            ),
            FaqItem(
                title = "Customizing On-Screen Controls",
                description = "Navigate to Settings > Player Controls to access the interactive 16:9 canvas. You can assign up to 24 actions across 4 slots:\n• Beside Title (max 3 actions)\n• Top Right (max 6 actions)\n• Bottom Left (max 6 actions)\n• Bottom Right (max 7 actions)"
            ),
            FaqItem(
                title = "Private Media Vault & Protection",
                description = "The Private Vault keeps your videos hidden from Android gallery and file indexers using .nomedia. It is secured by a 4-digit PIN and Biometric authentication. When playing vault videos, FLAG_SECURE is engaged to prevent screenshots and task switcher previews."
            ),
            FaqItem(
                title = "Codec & Hardware Decoder Support",
                description = "NP Player leverages MediaCodec hardware acceleration by default. If a stream contains codecs unsupported by device hardware (such as DTS, Dolby Digital AC3/E-AC3, TrueHD, or HEVC), the player automatically activates seamless software decoder fallback."
            ),
            FaqItem(
                title = "Network Streaming & IPTV",
                description = "From the Network Hub, open direct stream URLs (HLS .m3u8, DASH .mpd, HTTP/HTTPS MP4, and RTSP). You can also connect to local network shares (SMB, SFTP, FTP, WebDAV) and load IPTV playlists."
            ),
            FaqItem(
                title = "Audio FX & Equalizer",
                description = "Open the Equalizer in Settings or directly during playback via the Audio FX slot. Choose from 8 presets (Bass Boost, Rock, Vocal, Movie, etc.) or manually shape 5 frequency bands (60Hz to 14kHz)."
            )
        )
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
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
                        text = "Help & FAQ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NpLogo(
                                variant = NpLogoVariant.HORIZONTAL,
                                size = 28.dp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NPYellow
                            ) {
                                Text(
                                    text = "v1.0.0",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NPBlack,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Learn how to get the most out of NP Player's offline engine, gestures, codecs, and online subtitle search.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(faqList) { faq ->
                FaqCard(faq = faq)
            }
        }
    }
}

@Composable
private fun FaqCard(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (expanded) NPYellow.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = faq.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (expanded) NPYellow else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = if (expanded) NPYellow else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = faq.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
