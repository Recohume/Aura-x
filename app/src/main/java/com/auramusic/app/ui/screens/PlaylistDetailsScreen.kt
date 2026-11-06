package com.auramusic.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import com.auramusic.app.ui.components.AuraPlaylistCover
import com.auramusic.app.ui.components.AuraAddToPlaylistModal
import com.auramusic.app.ui.components.AuraCreatePlaylistModal
import com.auramusic.app.ui.components.ForceWhiteSystemBarsEffect
import com.auramusic.app.ui.components.TrackOptionRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.auramusic.app.core.media.AuraPlayerManager
import com.auramusic.app.domain.model.AuraPlaylist
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.ui.components.AuraPlaylistCover
import com.auramusic.app.ui.components.AuraPlaylistOptionsModal
import com.auramusic.app.ui.components.AuraTrackOptionsModal
import com.auramusic.app.ui.theme.AuraPlayGreen
import com.auramusic.app.ui.theme.SpotifyCard
import com.auramusic.app.ui.theme.SpotifyPitchBlack
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailsScreen(
    playlist: AuraPlaylist,
    favorites: List<AuraTrack>,
    downloads: List<AuraTrack>,
    downloadingIds: Set<String>,
    downloadProgressMap: Map<String, Float> = emptyMap(),
    currentPlayingId: String?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayTrack: (AuraTrack, List<AuraTrack>) -> Unit,
    onToggleFavorite: (AuraTrack) -> Unit,
    onDownloadTrack: (AuraTrack) -> Unit,
    onRemoveDownload: ((AuraTrack) -> Unit)? = null,
    onRemoveTrackFromPlaylist: ((videoId: String) -> Unit)? = null,
    onUpdateCover: (newCoverUrl: String) -> Unit,
    onUpdateTitle: (newTitle: String) -> Unit,
    onDeletePlaylist: () -> Unit,
    playlists: List<AuraPlaylist> = emptyList(),
    onAddTrackToPlaylist: (playlistId: String, track: AuraTrack) -> Unit = { _, _ -> },
    onCreatePlaylist: (name: String, description: String, coverUrl: String) -> Unit = { _, _, _ -> },
    onOpenArtist: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedModalTrack by remember { mutableStateOf<AuraTrack?>(null) }
    var trackForAddToPlaylist by remember { mutableStateOf<AuraTrack?>(null) }
    var showCreatePlaylistModal by remember { mutableStateOf(false) }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var newTitleInput by remember { mutableStateOf(playlist.name) }

    // Media Picker Launcher for selecting an image from the user's files/gallery
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val localPath = savePlaylistCoverLocally(context, playlist.id, uri)
                withContext(Dispatchers.Main) {
                    onUpdateCover(localPath)
                    Toast.makeText(context, "Playlist cover updated!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Storage/Media Permission Launcher prompting user to allow files
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            mediaPickerLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Storage permission is required to choose a custom cover", Toast.LENGTH_SHORT).show()
        }
    }

    val onPickCoverImage: () -> Unit = {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val hasPermission = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            mediaPickerLauncher.launch("image/*")
        } else {
            permissionLauncher.launch(permission)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyPitchBlack)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Playlist Options",
                    tint = TextPrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Header section: Cover, Title, Stats, Action buttons
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Cover Image with tap-to-modify badge
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SpotifyCard)
                            .clickable { onPickCoverImage() },
                        contentAlignment = Alignment.Center
                    ) {
                        AuraPlaylistCover(
                            coverUrl = playlist.coverUrl,
                            tracks = playlist.tracks,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        // Subtle overlay badge: "Edit Cover"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clickable { onPickCoverImage() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Edit Cover",
                                    tint = AuraPlayGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit Cover",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${playlist.tracks.size} songs • Spotify Playlist",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Play and Shuffle row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SpotifyCard,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    val contextualTracks = playlist.tracks.map { it.copy(album = playlist.name) }
                                    val shuffled = contextualTracks.shuffled()
                                    shuffled.firstOrNull()?.let { first ->
                                        onPlayTrack(first, shuffled)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = AuraPlayGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Shuffle",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Prominent circular Play button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AuraPlayGreen)
                                .clickable {
                                    val contextualTracks = playlist.tracks.map { it.copy(album = playlist.name) }
                                    contextualTracks.firstOrNull()?.let { first ->
                                        onPlayTrack(first, contextualTracks)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play All",
                                tint = Color.Black,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }

            // Songs Header
            item {
                Text(
                    text = "Tracks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            // Song list items
            // Imported playlists may intentionally contain the same song more than once.
            // Include the occurrence index so repeated video IDs never collide in LazyColumn.
            itemsIndexed(
                items = playlist.tracks,
                key = { index, track -> "playlist_track_${index}_${track.videoId.ifBlank { track.id }}" }
            ) { _, track ->
                SpotifyTrackRowItem(
                    track = track,
                    isCurrent = currentPlayingId == track.videoId,
                    isFavorite = favorites.any { it.videoId == track.videoId },
                    isDownloaded = downloads.any { it.videoId == track.videoId && it.isDownloaded },
                    isDownloading = downloadingIds.contains(track.videoId),
                    downloadProgress = downloadProgressMap[track.videoId],
                    onClick = {
                        val contextualTracks = playlist.tracks.map { it.copy(album = playlist.name) }
                        onPlayTrack(track.copy(album = playlist.name), contextualTracks)
                    },
                    onToggleFavorite = { onToggleFavorite(track) },
                    onDownload = { onDownloadTrack(track) },
                    onOptionsClick = { selectedModalTrack = track }
                )
            }
        }
    }

    // Dialog to rename playlist
    if (showEditTitleDialog) {
        AlertDialog(
            onDismissRequest = { showEditTitleDialog = false },
            containerColor = SpotifyCard,
            title = {
                Text("Rename Playlist", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = newTitleInput,
                    onValueChange = { newTitleInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraPlayGreen,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitleInput.isNotBlank()) {
                            onUpdateTitle(newTitleInput.trim())
                        }
                        showEditTitleDialog = false
                    }
                ) {
                    Text("Save", color = AuraPlayGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTitleDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showMenu) {
        AuraPlaylistOptionsModal(
            playlist = playlist,
            onDismiss = { showMenu = false },
            onDownloadAll = {
                showMenu = false
                playlist.tracks.forEach { onDownloadTrack(it) }
                Toast.makeText(context, "Downloading ${playlist.tracks.size} songs for offline listening…", Toast.LENGTH_SHORT).show()
            },
            onChangeCover = {
                showMenu = false
                mediaPickerLauncher.launch("image/*")
            },
            onResetCover = if (playlist.coverUrl.isNotBlank()) {
                {
                    showMenu = false
                    onUpdateCover("")
                    Toast.makeText(context, "Cover reset to Spotify collage", Toast.LENGTH_SHORT).show()
                }
            } else null,
            onRename = {
                showMenu = false
                newTitleInput = playlist.name
                showEditTitleDialog = true
            },
            onDelete = {
                showMenu = false
                onDeletePlaylist()
            }
        )
    }

    selectedModalTrack?.let { modalTrack ->
        AuraTrackOptionsModal(
            track = modalTrack,
            isFavorite = favorites.any { it.videoId == modalTrack.videoId },
            onDismiss = { selectedModalTrack = null },
            onPlayNext = {
                AuraPlayerManager.playNext(modalTrack)
                Toast.makeText(context, "Playing next: ${modalTrack.title}", Toast.LENGTH_SHORT).show()
                selectedModalTrack = null
            },
            onAddToQueue = {
                AuraPlayerManager.enqueueTrack(modalTrack)
                Toast.makeText(context, "Added to queue: ${modalTrack.title}", Toast.LENGTH_SHORT).show()
                selectedModalTrack = null
            },
            onToggleFavorite = {
                val wasFav = favorites.any { it.videoId == modalTrack.videoId }
                onToggleFavorite(modalTrack)
                val msg = if (wasFav) "Removed from Liked Songs" else "Added to Liked Songs"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                selectedModalTrack = null
            },
            onAddToPlaylist = {
                val t = modalTrack
                selectedModalTrack = null
                trackForAddToPlaylist = t
            },
            onGoToArtist = {
                selectedModalTrack = null
                onOpenArtist(modalTrack.artist)
            },
            isDownloaded = downloads.any { it.videoId == modalTrack.videoId && it.isDownloaded },
            onDownloadTrack = {
                onDownloadTrack(modalTrack)
                Toast.makeText(context, "Downloading ${modalTrack.title}…", Toast.LENGTH_SHORT).show()
                selectedModalTrack = null
            },
            onRemoveDownload = onRemoveDownload?.let { cb ->
                {
                    cb(modalTrack)
                    selectedModalTrack = null
                }
            },
            onRemoveFromPlaylist = onRemoveTrackFromPlaylist?.let { cb ->
                {
                    cb(modalTrack.videoId)
                    selectedModalTrack = null
                }
            },
            onWatchVideo = {
                selectedModalTrack = null
                val contextualTracks = playlist.tracks.map { it.copy(album = playlist.name) }
                onPlayTrack(modalTrack.copy(album = playlist.name), contextualTracks)
                com.auramusic.app.core.media.AuraPlayerManager.setVideoMode(true)
            }
        )
    }

    // Modal: Add Song to Playlist
    trackForAddToPlaylist?.let { trackToAdd ->
        AuraAddToPlaylistModal(
            track = trackToAdd,
            playlists = playlists,
            onDismiss = { trackForAddToPlaylist = null },
            onSelectPlaylist = { targetPl ->
                onAddTrackToPlaylist(targetPl.id, trackToAdd)
                trackForAddToPlaylist = null
                Toast.makeText(context, "Added '${trackToAdd.title}' to '${targetPl.name}'", Toast.LENGTH_SHORT).show()
            },
            onCreateNewPlaylist = {
                trackForAddToPlaylist = null
                showCreatePlaylistModal = true
            }
        )
    }

    // Modal: Create Playlist on the fly
    if (showCreatePlaylistModal) {
        AuraCreatePlaylistModal(
            onDismiss = { showCreatePlaylistModal = false },
            onCreate = { title, desc, coverUri ->
                showCreatePlaylistModal = false
                onCreatePlaylist(title, desc, coverUri)
                Toast.makeText(context, "Created playlist: $title", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * Persists picked image content URI into private app storage for stable playlist cover display.
 */
private fun savePlaylistCoverLocally(context: Context, playlistId: String, uri: Uri): String {
    return try {
        val dir = File(context.filesDir, "playlist_covers").apply { mkdirs() }
        val destFile = File(dir, "cover_${playlistId}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath
    } catch (e: Exception) {
        uri.toString()
    }
}
