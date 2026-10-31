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
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.ui.theme.AuraCyan
import com.auramusic.app.ui.theme.AuraViolet
import com.auramusic.app.ui.theme.ObsidianBackground
import com.auramusic.app.ui.theme.ObsidianCard
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary

@Composable
fun SearchScreen(
    query: String,
    filter: String,
    results: List<AuraTrack>,
    isSearching: Boolean,
    spotifyImportStatus: String?,
    favorites: List<AuraTrack>,
    downloads: List<AuraTrack>,
    downloadingIds: Set<String>,
    onQueryChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onPlayTrack: (AuraTrack, List<AuraTrack>) -> Unit,
    onToggleFavorite: (AuraTrack) -> Unit,
    onDownloadTrack: (AuraTrack) -> Unit,
    onImportSpotifyPlaylist: (String) -> Unit
) {
    var spotifyUrlInput by remember { mutableStateOf("") }
    var showSpotifyImporter by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(top = 18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Discover & Search",
                style = MaterialTheme.typography.headlineLarge
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1DB954).copy(alpha = 0.16f),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showSpotifyImporter = !showSpotifyImporter }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LibraryMusic,
                        contentDescription = "Spotify Playlist Importer",
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Spotify Import",
                        color = Color(0xFF1DB954),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Direct Client-First Spotify Playlist Importer Box
        if (showSpotifyImporter) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ObsidianCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Direct Spotify Public Playlist Importer",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1DB954)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scrapes Spotify Embed directly on-device & matches tracks on YouTube Music.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = spotifyUrlInput,
                        onValueChange = { spotifyUrlInput = it },
                        placeholder = { Text("https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1DB954),
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = spotifyImportStatus ?: "Zero cloud backend required",
                            style = MaterialTheme.typography.labelSmall,
                            color = AuraCyan
                        )
                        Button(
                            onClick = { onImportSpotifyPlaylist(spotifyUrlInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                        ) {
                            Text("Import Playlist", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // YouTube Music Search Input
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search songs, artists, albums on YouTube Music…") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = TextSecondary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Clear Search",
                            tint = TextSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ObsidianCard,
                unfocusedContainerColor = ObsidianCard,
                focusedBorderColor = AuraViolet,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips (Songs vs Videos)
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("songs" to "Official Songs", "videos" to "Music Videos & Live").forEach { (key, label) ->
                val selected = filter == key
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) AuraViolet else ObsidianCard,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onFilterChange(key) }
                ) {
                    Text(
                        text = label,
                        color = if (selected) Color.White else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AuraViolet, modifier = Modifier.size(28.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            items(results, key = { it.videoId }) { track ->
                TrackRowItem(
                    track = track,
                    isFavorite = favorites.any { it.videoId == track.videoId },
                    isDownloaded = downloads.any { it.videoId == track.videoId },
                    isDownloading = downloadingIds.contains(track.videoId),
                    onClick = { onPlayTrack(track, results) },
                    onToggleFavorite = { onToggleFavorite(track) },
                    onDownload = { onDownloadTrack(track) }
                )
            }
        }
    }
}
