package com.auramusic.app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auramusic.app.domain.model.AuraPlaylist
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.ListeningStats
import com.auramusic.app.ui.theme.AuraCircleButtonBg
import com.auramusic.app.ui.theme.AuraNeonLime
import com.auramusic.app.ui.theme.AuraPillInactive
import com.auramusic.app.ui.theme.AuraPillInactiveText
import com.auramusic.app.ui.theme.AuraPlayGreen
import com.auramusic.app.ui.theme.SpotifyCard
import com.auramusic.app.ui.theme.SpotifyPitchBlack
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary

@Composable
fun LibraryScreen(
    favorites: List<AuraTrack>,
    downloads: List<AuraTrack>,
    playlists: List<AuraPlaylist>,
    stats: ListeningStats,
    downloadingIds: Set<String>,
    currentPlayingId: String? = null,
    isPlaying: Boolean = false,
    spotifyImportStatus: String? = null,
    onImportSpotifyPlaylist: (String) -> Unit = {},
    onPlayTrack: (AuraTrack, List<AuraTrack>) -> Unit,
    onToggleFavorite: (AuraTrack) -> Unit,
    onDownloadTrack: (AuraTrack) -> Unit,
    onUpdatePlaylistCover: (playlistId: String, newCoverUrl: String) -> Unit = { _, _ -> },
    onUpdatePlaylistTitle: (playlistId: String, newTitle: String) -> Unit = { _, _ -> },
    onDeletePlaylist: (playlistId: String) -> Unit = {}
) {
    var selectedPlaylistId by remember { mutableStateOf<String?>(null) }
    var showImportCard by remember { mutableStateOf(false) }
    var linkInput by remember { mutableStateOf("") }

    BackHandler(enabled = selectedPlaylistId != null) {
        selectedPlaylistId = null
    }

    // If a playlist is selected, show its full detailed view
    val selectedPlaylist = playlists.firstOrNull { it.id == selectedPlaylistId }
    if (selectedPlaylist != null) {
        PlaylistDetailsScreen(
            playlist = selectedPlaylist,
            favorites = favorites,
            downloads = downloads,
            downloadingIds = downloadingIds,
            currentPlayingId = currentPlayingId,
            isPlaying = isPlaying,
            onBack = { selectedPlaylistId = null },
            onPlayTrack = onPlayTrack,
            onToggleFavorite = onToggleFavorite,
            onDownloadTrack = onDownloadTrack,
            onUpdateCover = { newCover -> onUpdatePlaylistCover(selectedPlaylist.id, newCover) },
            onUpdateTitle = { newTitle -> onUpdatePlaylistTitle(selectedPlaylist.id, newTitle) },
            onDeletePlaylist = {
                onDeletePlaylist(selectedPlaylist.id)
                selectedPlaylistId = null
            }
        )
        return
    }

    var activeTab by remember { mutableIntStateOf(0) }
    val tabLabels = listOf("All", "Playlists", "Liked Songs", "Downloaded")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyPitchBlack)
            .padding(top = 2.dp)
    ) {
        // Top Bar: Back Circular Button + "My Music" + Options Circular Button (Screen 3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AuraCircleButtonBg)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "My Music",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )

            IconButton(
                onClick = { showImportCard = !showImportCard },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (showImportCard) AuraNeonLime else AuraCircleButtonBg)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "Options",
                    tint = if (showImportCard) Color.Black else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (showImportCard) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E2026),
                border = BorderStroke(1.dp, Color(0xFF2C2F3A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Import Spotify Playlist",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste a Spotify playlist or album URL to import all tracks into your library.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = linkInput,
                            onValueChange = { linkInput = it },
                            placeholder = {
                                Text(
                                    "https://open.spotify.com/playlist/...",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AuraNeonLime,
                                unfocusedBorderColor = Color(0xFF333333)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (linkInput.isNotBlank()) {
                                    onImportSpotifyPlaylist(linkInput.trim())
                                    linkInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AuraNeonLime)
                        ) {
                            Text("Import", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (!spotifyImportStatus.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = spotifyImportStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = AuraNeonLime
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Pills: All, Playlists, Liked Songs, Downloaded (Screen 3)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabLabels.size) { idx ->
                val selected = activeTab == idx
                val label = tabLabels[idx]
                Surface(
                    shape = CircleShape,
                    color = if (selected) AuraNeonLime else AuraPillInactive,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { activeTab = idx }
                ) {
                    Text(
                        text = label,
                        color = if (selected) Color.Black else AuraPillInactiveText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (activeTab) {
            0 -> {
                val allTracks = (favorites + downloads).distinctBy { it.videoId }
                if (allTracks.isEmpty()) {
                    EmptyVaultMessage("Your music vault is empty. Favorite or download tracks to see them here.")
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
                        items(allTracks, key = { it.videoId }) { track ->
                            val isFav = favorites.any { it.videoId == track.videoId }
                            val isDown = downloads.any { it.videoId == track.videoId }
                            SpotifyTrackRowItem(
                                track = track,
                                isCurrent = currentPlayingId == track.videoId,
                                isFavorite = isFav,
                                isDownloaded = isDown,
                                isDownloading = downloadingIds.contains(track.videoId),
                                onClick = { onPlayTrack(track, allTracks) },
                                onToggleFavorite = { onToggleFavorite(track) },
                                onDownload = { onDownloadTrack(track) }
                            )
                        }
                    }
                }
            }
            1 -> {
                if (playlists.isEmpty()) {
                    EmptyVaultMessage("No playlists created or imported yet.")
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
                        items(playlists, key = { it.id }) { playlist ->
                            PlaylistItemRow(
                                playlist = playlist,
                                onClick = { selectedPlaylistId = playlist.id },
                                onPlay = {
                                    if (playlist.tracks.isNotEmpty()) {
                                        onPlayTrack(playlist.tracks.first(), playlist.tracks)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            2 -> {
                if (favorites.isEmpty()) {
                    EmptyVaultMessage("No liked songs yet. Tap the heart on any song to save it here.")
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
                        items(favorites, key = { it.videoId }) { track ->
                            SpotifyTrackRowItem(
                                track = track,
                                isCurrent = currentPlayingId == track.videoId,
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
            3 -> {
                if (downloads.isEmpty()) {
                    EmptyVaultMessage("No downloaded songs yet. Tap download on any song for offline listening.")
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
                        items(downloads, key = { it.videoId }) { track ->
                            SpotifyTrackRowItem(
                                track = track,
                                isCurrent = currentPlayingId == track.videoId,
                                isFavorite = favorites.any { it.videoId == track.videoId },
                                isDownloaded = true,
                                isDownloading = false,
                                onClick = { onPlayTrack(track, downloads) },
                                onToggleFavorite = { onToggleFavorite(track) },
                                onDownload = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistItemRow(
    playlist: AuraPlaylist,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val cover = playlist.coverUrl.ifBlank {
            playlist.tracks.firstOrNull()?.highResArtworkUrl ?: ""
        }
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AuraCircleButtonBg),
            contentAlignment = Alignment.Center
        ) {
            if (cover.isNotBlank()) {
                AsyncImage(
                    model = cover,
                    contentDescription = playlist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Playlist • ${playlist.tracks.size} songs",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(AuraCircleButtonBg)
                .clickable(onClick = onPlay),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Play Playlist",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
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
