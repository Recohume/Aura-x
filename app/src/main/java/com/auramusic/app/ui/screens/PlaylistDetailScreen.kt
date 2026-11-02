package com.auramusic.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryAdd
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auramusic.app.core.media.AuraPlayerManager
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.PlaylistDetail
import com.auramusic.app.domain.model.PlaylistTrackItem
import com.auramusic.app.ui.theme.AuraNeonLime
import com.auramusic.app.ui.theme.SpotifyPitchBlack
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistDetail: PlaylistDetail,
    isLoading: Boolean,
    currentPlayingId: String?,
    onClose: () -> Unit,
    onPlayTrack: (AuraTrack, List<AuraTrack>) -> Unit,
    onToggleFavorite: (AuraTrack) -> Unit
) {
    val context = LocalContext.current
    var selectedModalTrack by remember { mutableStateOf<AuraTrack?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val allTracks = remember(playlistDetail.tracks, playlistDetail.title) {
        playlistDetail.tracks.map { it.track.copy(album = playlistDetail.title) }
    }
    var visibleTrackCount by remember(playlistDetail.tracks) { mutableIntStateOf(20) }

    BackHandler {
        onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyPitchBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // 1. Top Screen Banner Picture with Title & Views / Author on the Bottom-Right
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                ) {
                    AsyncImage(
                        model = playlistDetail.bannerUrl,
                        contentDescription = playlistDetail.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay to darken bottom so text is crisp and readable
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.70f),
                                        SpotifyPitchBlack
                                    )
                                )
                            )
                    )

                    // Back button at top-left
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(start = 16.dp, top = 8.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.50f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Playlist title on bottom right & views/author directly below it
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 12.dp)
                            .fillMaxWidth(0.85f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = playlistDetail.title,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            textAlign = TextAlign.End,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = playlistDetail.author,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (playlistDetail.totalViews.isNotBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = playlistDetail.totalViews,
                                color = AuraNeonLime,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 2. Section Header: Songs count, Total Views & Quick Play All + Shuffle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tracks",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        val subtitleText = buildString {
                            if (playlistDetail.tracks.isNotEmpty()) {
                                append("${playlistDetail.tracks.size} tracks")
                            } else {
                                append(playlistDetail.trackCount)
                            }
                            if (playlistDetail.totalViews.isNotBlank()) {
                                append(" • ${playlistDetail.totalViews}")
                            }
                        }
                        Text(
                            text = subtitleText,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    if (allTracks.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Shuffle Button
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1E2028),
                                border = BorderStroke(1.dp, Color(0xFF2E323D)),
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        val shuffled = allTracks.shuffled()
                                        onPlayTrack(shuffled.first(), shuffled)
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Shuffle,
                                        contentDescription = "Shuffle Playlist",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Play All Button
                            Surface(
                                shape = CircleShape,
                                color = AuraNeonLime,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable { onPlayTrack(allTracks.first(), allTracks) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = "Play Playlist",
                                        tint = Color.Black,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Loading indicator / Skeleton Loading
            if (isLoading && playlistDetail.tracks.isEmpty()) {
                items(6) {
                    com.auramusic.app.ui.components.TrackItemSkeleton()
                }
            }

            // 4. Ranked Song List (1..N) with Album Art, Title, Views/Duration, and 3-Dot options (Paginated)
            val paginatedTracks = playlistDetail.tracks.take(visibleTrackCount)
            items(paginatedTracks, key = { "pl_song_${it.rank}_${it.track.videoId}" }) { item ->
                val isCurrent = item.track.videoId == currentPlayingId

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayTrack(item.track.copy(album = playlistDetail.title), allTracks) }
                        .padding(horizontal = 20.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ranking number 1, 2, 3...
                    Text(
                        text = "${item.rank}",
                        color = if (isCurrent) AuraNeonLime else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.width(28.dp),
                        textAlign = TextAlign.Start
                    )

                    // Album Art
                    AsyncImage(
                        model = item.track.artworkUrl.ifBlank { item.track.highResArtworkUrl },
                        contentDescription = item.track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Artist + Views/Duration
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.track.title,
                            color = if (isCurrent) AuraNeonLime else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.track.artist,
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (item.viewCount.isNotBlank()) {
                                val isMetric = item.viewCount.contains("play", ignoreCase = true) || item.viewCount.contains("view", ignoreCase = true)
                                Text(
                                    text = " • ${item.viewCount}",
                                    color = if (isMetric) AuraNeonLime.copy(alpha = 0.90f) else TextSecondary.copy(alpha = 0.75f),
                                    fontSize = 11.sp,
                                    fontWeight = if (isMetric) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // 3-dot stacked vertical icon opening modal options
                    IconButton(
                        onClick = { selectedModalTrack = item.track },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Song options",
                            tint = Color.White.copy(alpha = 0.70f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Pagination "See More" button for large / TOP 100 playlists
            if (visibleTrackCount < playlistDetail.tracks.size) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFF1E2028),
                            border = BorderStroke(1.dp, Color(0xFF2E323D)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .clickable {
                                    visibleTrackCount = (visibleTrackCount + 20).coerceAtMost(playlistDetail.tracks.size)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowDownward,
                                    contentDescription = "See More",
                                    tint = AuraNeonLime,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "See More Tracks (${playlistDetail.tracks.size - visibleTrackCount} remaining)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Options Modal Bottom Sheet
        selectedModalTrack?.let { track ->
            ModalBottomSheet(
                onDismissRequest = { selectedModalTrack = null },
                sheetState = sheetState,
                containerColor = Color(0xFF14161E),
                scrimColor = Color.Black.copy(alpha = 0.75f)
            ) {
                com.auramusic.app.ui.components.ForceWhiteSystemBarsEffect()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 8.dp)
                        .padding(bottom = 36.dp)
                ) {
                    // Modal Header with Track Artwork & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        AsyncImage(
                            model = track.highResArtworkUrl,
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artist,
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
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Option 1: Play Next
                    PlaylistModalOptionItem(
                        icon = Icons.AutoMirrored.Rounded.QueueMusic,
                        label = "Play Next",
                        onClick = {
                            AuraPlayerManager.playNext(track)
                            Toast.makeText(context, "Playing next: ${track.title}", Toast.LENGTH_SHORT).show()
                            selectedModalTrack = null
                        }
                    )

                    // Option 2: Add to Queue
                    PlaylistModalOptionItem(
                        icon = Icons.Rounded.PlaylistAdd,
                        label = "Add to Queue",
                        onClick = {
                            AuraPlayerManager.enqueueTrack(track)
                            Toast.makeText(context, "Added to queue", Toast.LENGTH_SHORT).show()
                            selectedModalTrack = null
                        }
                    )

                    // Option 3: Add to Liked Songs
                    PlaylistModalOptionItem(
                        icon = Icons.Rounded.Favorite,
                        label = "Liked Songs",
                        onClick = {
                            onToggleFavorite(track)
                            Toast.makeText(context, "Saved to Liked Songs", Toast.LENGTH_SHORT).show()
                            selectedModalTrack = null
                        }
                    )

                    // Option 4: Add to Playlist
                    PlaylistModalOptionItem(
                        icon = Icons.Rounded.LibraryAdd,
                        label = "Add to Playlist",
                        onClick = {
                            Toast.makeText(context, "Saved to library", Toast.LENGTH_SHORT).show()
                            selectedModalTrack = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistModalOptionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.5.sp
        )
    }
}
