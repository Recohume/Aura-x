package com.auramusic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auramusic.app.domain.model.AuraPlaylist
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.ui.theme.AuraNeonLime
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary

/**
 * Mobile Sheet Modal for adding a track to an existing playlist,
 * or initiating creation of a new playlist on the fly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraAddToPlaylistModal(
    track: AuraTrack,
    playlists: List<AuraPlaylist>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (AuraPlaylist) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF14161E),
        scrimColor = Color.Black.copy(alpha = 0.75f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.20f))
            )
        }
    ) {
        ForceWhiteSystemBarsEffect()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 6.dp)
                .padding(bottom = 36.dp)
        ) {
            // Track Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                AsyncImage(
                    model = track.highResArtworkUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Add to playlist",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${track.title} • ${track.artist}",
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Top action: + New Playlist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onCreateNewPlaylist)
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlaylistAdd,
                        contentDescription = "New Playlist",
                        tint = AuraNeonLime,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "New Playlist",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Playlist list
            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No playlists yet. Tap 'New Playlist' above to create one!",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(playlists, key = { it.id }) { pl ->
                        val isAlreadyIn = pl.tracks.any { it.videoId == track.videoId }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectPlaylist(pl) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AuraPlaylistCover(
                                coverUrl = pl.coverUrl,
                                tracks = pl.tracks,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pl.name,
                                    color = TextPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${pl.tracks.size} songs",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            // Right section: Plus button or Checkmark
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isAlreadyIn) AuraNeonLime.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAlreadyIn) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Added",
                                        tint = AuraNeonLime,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Add to Playlist",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
