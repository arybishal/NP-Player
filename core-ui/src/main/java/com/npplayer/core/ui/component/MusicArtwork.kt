package com.npplayer.core.ui.component

import android.content.ContentUris
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.npplayer.core.ui.R
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite

val ARTWORK_URI: Uri = Uri.parse("content://media/external/audio/albumart")

fun getAlbumArtUri(albumId: Long): Uri? {
    return if (albumId > 0) ContentUris.withAppendedId(ARTWORK_URI, albumId) else null
}

@Composable
fun MusicArtwork(
    albumId: Long,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val context = LocalContext.current
    val artworkUri = remember(albumId) {
        getAlbumArtUri(albumId)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(NPBlack)
            .border(0.5.dp, NPWhite.copy(alpha = 0.12f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (artworkUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artworkUri)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(id = R.drawable.ic_music_placeholder),
                error = painterResource(id = R.drawable.ic_music_placeholder)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_music_placeholder),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
