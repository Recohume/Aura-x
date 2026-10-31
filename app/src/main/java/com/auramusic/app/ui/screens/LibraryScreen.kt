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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auramusic.app.domain.model.AuraPlaylist
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.ListeningStats
import com.auramusic.app.ui.theme.AuraCyan
import com.auramusic.app.ui.theme.AuraMagenta
import com.auramusic.app.ui.theme.AuraViolet
import com.auramusic.app.ui.theme.ObsidianBackground
import com.auramusic.app.ui.theme.ObsidianCard
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary

@Composable
fun LibraryScreen(
    favorites: List<AuraTrack>,
    downloads: List<AuraTrack>,
    playlists: List<AuraPlaylist>,
    stats: ListeningStats,
    downloadingIds: Set<String>,
    onPlayTrack: (AuraTrack, List<AuraTrack>) -> Unit,
    onToggleFavorite: (AuraTrack) -> Unit,
    onDownloadTrack: (AuraTrack) -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Favorites (${favorites.size})" to Icons.Rounded.Favorite,
        "Offline (${downloads.size})" to Icons.Rounded.DownloadDone,
        "Playlists (${playlists.size})" to Icons.Rounded.QueueMusic,
        "Aura Receipt" to Icons.Rounded.AutoGraph
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(top = 18.dp)
    ) {
        Text(
            text = "Your Vault & Analytics",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { idx, (label, icon) ->
                val selected = activeTab == idx
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) AuraViolet else ObsidianCard,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { activeTab = idx }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (selected) Color.White else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            color = if (selected) Color.White else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (activeTab) {
            0 -> {
                if (favorites.isEmpty()) {
                    EmptyVaultMessage("No favorite tracks yet. Tap the heart icon on any track to save it here.")
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
                        items(favorites, key = { it.videoId }) { track ->
                            TrackRowItem(
                                track = track,
                                isFavorite = true,
                                isDownloaded = downloads.any { it.videoId == track.videoId },
                                isDownloading = downloadingIds.contains(track.videoId),
                                onClick = { onPlayTrack(track, favorites) },
                                onToggleFavorite = { onToggleFavorite(track) },
                                onDownload = { onDownloadTrack(track) }
                            )
                        }
                    }
                }
            }
            1 -> {
                if (downloads.isEmpty()) {
                    EmptyVaultMessage("No offline tracks downloaded yet. Tap the download icon on any track for 0ms offline playback.")
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
                        items(downloads, key = { it.videoId }) { track ->
                            TrackRowItem(
                                track = track,
                                isFavorite = favorites.any { it.videoId == track.videoId },
                                isDownloaded = true,
                                isDownloading = false,
                                onClick = { onPlayTrack(track, downloads) },
                                onToggleFavorite = { onToggleFavorite(track) },
                                onDownload = {}
                            )
                        }
                    }
                }
            }
            2 -> {
                if (playlists.isEmpty()) {
                    EmptyVaultMessage("No imported playlists yet. Use the 'Spotify Import' button in Search to import any public Spotify playlist.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(playlists, key = { it.id }) { pl ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ObsidianCard,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        pl.tracks.firstOrNull()?.let { first ->
                                            onPlayTrack(first, pl.tracks)
                                        }
                                    }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = pl.name,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = pl.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AuraCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                AuraAnalyticsReceiptCard(stats)
            }
        }
    }
}

@Composable
private fun EmptyVaultMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun AuraAnalyticsReceiptCard(stats: ListeningStats) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianCard,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = "AURA ACOUSTIC RECEIPT",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AuraCyan
            )
            Text(
                text = "CLIENT-FIRST TELEMETRY • ZERO CLOUD TRACKING",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatMetricBox("MINUTES STREAMED", "${stats.totalMinutes} MIN", AuraViolet)
                StatMetricBox("TRACKS PLAYED", "${stats.tracksPlayed}", AuraMagenta)
                StatMetricBox("PEAK SONIC HOUR", stats.peakHourLabel, AuraCyan)
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "TOP ARTISTS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            stats.topArtists.forEachIndexed { idx, (artist, plays) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "0${idx + 1}. $artist",
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "$plays plays",
                        fontFamily = FontFamily.Monospace,
                        color = AuraCyan,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatMetricBox(label: String, value: String, accent: Color) {
    Column {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = accent
        )
    }
}
