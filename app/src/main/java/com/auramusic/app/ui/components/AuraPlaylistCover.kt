package com.auramusic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.auramusic.app.domain.model.AuraTrack

/**
 * Universal Playlist Artwork Renderer matching the authentic Spotify standard:
 * - If user customized/modified coverUrl: displays the custom image
 * - If unmodified and tracks >= 4: displays a 2x2 collage grid of the first 4 track covers (NO single song cover)
 * - If unmodified and tracks 1..3: displays the first track's cover
 * - If empty: displays modern playlist placeholder icon
 */
@Composable
fun AuraPlaylistCover(
    coverUrl: String,
    tracks: List<AuraTrack>,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF1E1F25)),
        contentAlignment = Alignment.Center
    ) {
        if (coverUrl.isNotBlank()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = "Playlist Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (tracks.size >= 4) {
            // Authentic Spotify 2x2 quadrant collage
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f).fillMaxSize()) {
                    AsyncImage(
                        model = tracks[0].highResArtworkUrl,
                        contentDescription = tracks[0].title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).fillMaxSize()
                    )
                    AsyncImage(
                        model = tracks[1].highResArtworkUrl,
                        contentDescription = tracks[1].title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).fillMaxSize()
                    )
                }
                Row(modifier = Modifier.weight(1f).fillMaxSize()) {
                    AsyncImage(
                        model = tracks[2].highResArtworkUrl,
                        contentDescription = tracks[2].title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).fillMaxSize()
                    )
                    AsyncImage(
                        model = tracks[3].highResArtworkUrl,
                        contentDescription = tracks[3].title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).fillMaxSize()
                    )
                }
            }
        } else if (tracks.isNotEmpty()) {
            AsyncImage(
                model = tracks[0].highResArtworkUrl,
                contentDescription = tracks[0].title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.LibraryMusic,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
