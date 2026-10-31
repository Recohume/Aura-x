package com.auramusic.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auramusic.app.domain.model.AuraPalette
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.MoodCategory
import com.auramusic.app.domain.model.YtmChartSection
import com.auramusic.app.ui.theme.AuraCyan
import com.auramusic.app.ui.theme.AuraViolet
import com.auramusic.app.ui.theme.ObsidianBackground
import com.auramusic.app.ui.theme.ObsidianCard
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    chartSections: List<YtmChartSection>,
    moodCategories: List<MoodCategory>,
    isLoading: Boolean,
    palette: AuraPalette,
    favorites: List<AuraTrack>,
    downloads: List<AuraTrack>,
    downloadingIds: Set<String>,
    onPlayTrack: (AuraTrack, List<AuraTrack>) -> Unit,
    onPlayMood: (MoodCategory) -> Unit,
    onToggleFavorite: (AuraTrack) -> Unit,
    onDownloadTrack: (AuraTrack) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.backgroundTop.copy(alpha = 0.45f),
                        ObsidianBackground,
                        ObsidianBackground
                    )
                )
            ),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = "Zero-Wait Engine",
                            tint = AuraCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SIMPMUSIC PIPEPIPE + MEDIA3 ENGINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = AuraCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aura Music",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ObsidianCard)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh Charts",
                        tint = TextPrimary
                    )
                }
            }
        }

        // Mood & Genre Chips
        if (moodCategories.isNotEmpty()) {
            item {
                Text(
                    text = "Sonic Moods & Atmospheres",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(moodCategories, key = { it.title }) { mood ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(mood.accentHex).copy(alpha = 0.16f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onPlayMood(mood) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(mood.accentHex))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = mood.title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        if (isLoading && chartSections.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AuraViolet)
                }
            }
        }

        // Chart Sections
        items(chartSections, key = { it.id }) { section ->
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = section.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                section.tracks.take(6).forEach { track ->
                    val isFav = favorites.any { it.videoId == track.videoId }
                    val isDown = downloads.any { it.videoId == track.videoId }
                    val isDownloading = downloadingIds.contains(track.videoId)

                    TrackRowItem(
                        track = track,
                        isFavorite = isFav,
                        isDownloaded = isDown,
                        isDownloading = isDownloading,
                        onClick = { onPlayTrack(track, section.tracks) },
                        onToggleFavorite = { onToggleFavorite(track) },
                        onDownload = { onDownloadTrack(track) }
                    )
                }
            }
        }
    }
}

@Composable
fun TrackRowItem(
    track: AuraTrack,
    isFavorite: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownload: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianCard),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = track.highResArtworkUrl,
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${track.artist} • ${formatDuration(track.durationSeconds)}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onDownload,
            modifier = Modifier.size(40.dp)
        ) {
            when {
                isDownloading -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = AuraCyan
                )
                isDownloaded -> Icon(
                    imageVector = Icons.Rounded.DownloadDone,
                    contentDescription = "Downloaded Offline",
                    tint = AuraCyan,
                    modifier = Modifier.size(20.dp)
                )
                else -> Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = "Download Track Offline",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = "Toggle Favorite",
                tint = if (isFavorite) Color(0xFFEC4899) else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun formatDuration(seconds: Int): String {
    val mins = (seconds / 60).coerceAtLeast(0)
    val secs = (seconds % 60).coerceAtLeast(0)
    return "%d:%02d".format(mins, secs)
}
