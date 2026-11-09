package com.npplayer.feature.library.ui.selection

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.npplayer.core.data.database.entity.VideoEntity
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.core.ui.util.FormatUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExtractedVideoMetadata(
    val resolution: String = "",
    val videoCodec: String = "",
    val frameRate: String = "",
    val bitrate: String = "",
    val hdrProfile: String = "",
    val containerFormat: String = "",
    val audioTracksSummary: String = "",
    val subtitleTracksSummary: String = "",
    val dateModifiedFormatted: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoInfoDetailedBottomSheet(
    videos: List<VideoEntity>,
    labels: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onShare: (List<VideoEntity>) -> Unit,
    onOpenFolder: (folderPath: String) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

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
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (videos.size == 1) "Video Details" else "Selection Summary",
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

            if (videos.size == 1) {
                val video = videos.first()
                SingleVideoInfoContent(
                    video = video,
                    labels = labels,
                    context = context,
                    onShare = { onShare(listOf(video)) },
                    onOpenFolder = { onOpenFolder(video.folderPath) }
                )
            } else {
                MultiVideoInfoContent(
                    videos = videos,
                    onShare = { onShare(videos) },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun SingleVideoInfoContent(
    video: VideoEntity,
    labels: List<String>,
    context: Context,
    onShare: () -> Unit,
    onOpenFolder: () -> Unit
) {
    var metadata by remember { mutableStateOf<ExtractedVideoMetadata?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(video.path) {
        isLoading = true
        metadata = withContext(Dispatchers.IO) {
            extractMetadata(video)
        }
        isLoading = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Thumbnail & Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = File(video.path),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${FormatUtils.formatDuration(video.durationMs)} • ${FormatUtils.formatFileSize(video.sizeBytes)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Video Path", video.path))
                        Toast.makeText(context, "Path copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = null, tint = NPYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Copy Path", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                OutlinedButton(
                    onClick = onOpenFolder,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(imageVector = Icons.Outlined.Folder, contentDescription = null, tint = NPYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Open Folder", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NPYellow, contentColor = NPBlack)
                ) {
                    Icon(imageVector = Icons.Outlined.Share, contentDescription = null, tint = NPBlack, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Share", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NPBlack)
                }
            }
        }

        // Metadata rows
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoKeyValueRow("Full Path", video.path)
                    InfoKeyValueRow("Folder", video.folderName)
                    InfoKeyValueRow("Size", FormatUtils.formatFileSize(video.sizeBytes))
                    InfoKeyValueRow("Duration", FormatUtils.formatDuration(video.durationMs))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = NPYellow, modifier = Modifier.size(24.dp))
                        }
                    } else {
                        metadata?.let { meta ->
                            if (meta.resolution.isNotBlank()) InfoKeyValueRow("Resolution", meta.resolution)
                            if (meta.videoCodec.isNotBlank()) InfoKeyValueRow("Video Codec", meta.videoCodec)
                            if (meta.frameRate.isNotBlank()) InfoKeyValueRow("Frame Rate", meta.frameRate)
                            if (meta.bitrate.isNotBlank()) InfoKeyValueRow("Bitrate", meta.bitrate)
                            if (meta.containerFormat.isNotBlank()) InfoKeyValueRow("Container", meta.containerFormat)
                            if (meta.audioTracksSummary.isNotBlank()) InfoKeyValueRow("Audio Tracks", meta.audioTracksSummary)
                            if (meta.subtitleTracksSummary.isNotBlank()) InfoKeyValueRow("Subtitles", meta.subtitleTracksSummary)
                            if (meta.dateModifiedFormatted.isNotBlank()) InfoKeyValueRow("Modified", meta.dateModifiedFormatted)
                        }
                    }

                    if (video.lastPlayedPositionMs > 0) {
                        InfoKeyValueRow("Resume Position", FormatUtils.formatDuration(video.lastPlayedPositionMs))
                    }

                    if (labels.isNotEmpty()) {
                        InfoKeyValueRow("Labels", labels.joinToString(", "))
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiVideoInfoContent(
    videos: List<VideoEntity>,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    val totalSize = remember(videos) { videos.sumOf { it.sizeBytes } }
    val totalDuration = remember(videos) { videos.sumOf { it.durationMs } }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoKeyValueRow("Total Videos", "${videos.size} items")
                InfoKeyValueRow("Total Size", FormatUtils.formatFileSize(totalSize))
                InfoKeyValueRow("Total Duration", FormatUtils.formatDuration(totalDuration))
            }
        }

        Text(
            text = "SELECTED VIDEOS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(videos, key = { it.id }) { v ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = v.title,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = FormatUtils.formatFileSize(v.sizeBytes),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Close", color = MaterialTheme.colorScheme.onSurface)
            }

            Button(
                onClick = onShare,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NPYellow, contentColor = NPBlack),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Outlined.Share, contentDescription = null, tint = NPBlack, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Share All", fontWeight = FontWeight.Bold, color = NPBlack)
            }
        }
    }
}

@Composable
private fun InfoKeyValueRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = key,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun extractMetadata(video: VideoEntity): ExtractedVideoMetadata {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(video.path)

        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) ?: "${video.width}"
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) ?: "${video.height}"
        val resolution = if (width.isNotBlank() && height.isNotBlank() && width != "0") "${width}x${height}" else ""

        val mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: ""
        val bitrateRaw = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: 0L
        val bitrate = if (bitrateRaw > 0) "${bitrateRaw / 1000} kbps" else ""

        val fpsRaw = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull()
        val fps = if (fpsRaw != null && fpsRaw > 0) String.format(Locale.US, "%.1f fps", fpsRaw) else ""

        val ext = File(video.path).extension.uppercase()
        val dateMod = if (video.dateModified > 0) {
            val date = Date(if (video.dateModified < 10000000000L) video.dateModified * 1000L else video.dateModified)
            SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(date)
        } else ""

        // MediaExtractor for audio/subtracks
        var audioCount = 0
        val audioList = mutableListOf<String>()
        var subCount = 0

        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(video.path)
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val trackMime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (trackMime.startsWith("audio/")) {
                    audioCount++
                    val lang = format.getString(MediaFormat.KEY_LANGUAGE) ?: "und"
                    val channels = if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) "${format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)}ch" else ""
                    audioList.add("${lang.uppercase()} $channels")
                } else if (trackMime.startsWith("text/") || trackMime.startsWith("subrip") || trackMime.contains("subtitle")) {
                    subCount++
                }
            }
            extractor.release()
        } catch (_: Exception) {}

        // Check external subtitles
        val parent = File(video.path).parentFile
        val baseName = File(video.path).nameWithoutExtension
        val extSubs = parent?.listFiles { f ->
            f.isFile && f.name.startsWith(baseName) && (f.name.endsWith(".srt") || f.name.endsWith(".vtt") || f.name.endsWith(".ass"))
        }?.size ?: 0

        val subSummary = when {
            subCount > 0 && extSubs > 0 -> "$subCount embedded, $extSubs external (.srt/.vtt)"
            subCount > 0 -> "$subCount embedded stream(s)"
            extSubs > 0 -> "$extSubs external subtitle file(s)"
            else -> "None"
        }

        val audioSummary = if (audioList.isNotEmpty()) {
            audioList.joinToString(" • ")
        } else if (audioCount > 0) {
            "$audioCount audio track(s)"
        } else "Default audio"

        ExtractedVideoMetadata(
            resolution = resolution,
            videoCodec = mime.removePrefix("video/").uppercase(),
            frameRate = fps,
            bitrate = bitrate,
            containerFormat = ext,
            audioTracksSummary = audioSummary,
            subtitleTracksSummary = subSummary,
            dateModifiedFormatted = dateMod
        )
    } catch (e: Exception) {
        ExtractedVideoMetadata()
    } finally {
        try { retriever.release() } catch (_: Exception) {}
    }
}
