package com.auramusic.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.auramusic.app.ui.components.AuraAddToPlaylistModal
import com.auramusic.app.ui.components.AuraCreateOrImportModal
import com.auramusic.app.ui.components.AuraCreatePlaylistModal
import com.auramusic.app.ui.components.AuraPlaylistOptionsModal
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.auramusic.app.core.media.AuraPlayerManager
import com.auramusic.app.domain.model.AuraPlaylist
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.ListeningStats
import com.auramusic.app.ui.components.AuraTrackOptionsModal
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
    downloadProgressMap: Map<String, Float> = emptyMap(),
    currentPlayingId: String? = null,
    isPlaying: Boolean = false,
    spotifyImportStatus: String? = null,
    importedPlaylistSuccess: AuraPlaylist? = null,
    initialPlaylistId: String? = null,
    openedFromHomeScreen: Boolean = false,
    onNavigateToHome: () -> Unit = {},
    onClearInitialPlaylist: () -> Unit = {},
    onImportSpotifyPlaylist: (String) -> Unit = {},
    onDismissImportSuccess: () -> Unit = {},
    onPlayTrack: (AuraTrack, List<AuraTrack>) -> Unit,
    onToggleFavorite: (AuraTrack) -> Unit,
    onDownloadTrack: (AuraTrack) -> Unit,
    onRemoveDownload: (AuraTrack) -> Unit = {},
    onUpdatePlaylistCover: (playlistId: String, newCoverUrl: String) -> Unit = { _, _ -> },
    onUpdatePlaylistTitle: (playlistId: String, newTitle: String) -> Unit = { _, _ -> },
    onDeletePlaylist: (playlistId: String) -> Unit = {},
    onRemoveTrackFromPlaylist: (playlistId: String, videoId: String) -> Unit = { _, _ -> },
    onCreatePlaylist: (name: String, description: String, coverUrl: String) -> Unit = { _, _, _ -> },
    onAddTrackToPlaylist: (playlistId: String, track: AuraTrack) -> Unit = { _, _ -> },
    onOpenArtist: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var activeTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedModalTrack by remember { mutableStateOf<AuraTrack?>(null) }
    var selectedPlaylistId by rememberSaveable { mutableStateOf<String?>(initialPlaylistId) }
    var selectedPlaylistForOptions by remember { mutableStateOf<AuraPlaylist?>(null) }
    var showCreateOrImportSheet by remember { mutableStateOf(false) }
    var showCreatePlaylistModal by remember { mutableStateOf(false) }
    var trackForAddToPlaylist by remember { mutableStateOf<AuraTrack?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renamePlaylistTarget by remember { mutableStateOf<AuraPlaylist?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var showImportCard by remember { mutableStateOf(false) }
    var linkInput by remember { mutableStateOf("") }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val pl = selectedPlaylistForOptions
        if (uri != null && pl != null) {
            onUpdatePlaylistCover(pl.id, uri.toString())
            Toast.makeText(context, "Cover updated", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(initialPlaylistId) {
        if (initialPlaylistId != null) {
            selectedPlaylistId = initialPlaylistId
        }
    }

    // Auto-close import section upon successful import
    LaunchedEffect(spotifyImportStatus) {
        if (spotifyImportStatus?.startsWith("Imported", ignoreCase = true) == true) {
            kotlinx.coroutines.delay(1000)
            showImportCard = false
        }
    }

    LaunchedEffect(importedPlaylistSuccess) {
        if (importedPlaylistSuccess != null) {
            showImportCard = false
            onDismissImportSuccess()
        }
    }

    BackHandler(enabled = selectedPlaylistId != null) {
        selectedPlaylistId = null
        onClearInitialPlaylist()
        if (openedFromHomeScreen) {
            onNavigateToHome()
        }
    }

    // If a playlist is selected, show its full detailed view
    val selectedPlaylist = playlists.firstOrNull { it.id == selectedPlaylistId }
    if (selectedPlaylist != null) {
        PlaylistDetailsScreen(
            playlist = selectedPlaylist,
            favorites = favorites,
            downloads = downloads,
            downloadingIds = downloadingIds,
            downloadProgressMap = downloadProgressMap,
            currentPlayingId = currentPlayingId,
            isPlaying = isPlaying,
            onBack = {
                selectedPlaylistId = null
                onClearInitialPlaylist()
                if (openedFromHomeScreen) {
                    onNavigateToHome()
                }
            },
            onPlayTrack = onPlayTrack,
            onToggleFavorite = onToggleFavorite,
            onDownloadTrack = onDownloadTrack,
            onRemoveDownload = onRemoveDownload,
            onRemoveTrackFromPlaylist = { vId -> onRemoveTrackFromPlaylist(selectedPlaylist.id, vId) },
            onUpdateCover = { newCover -> onUpdatePlaylistCover(selectedPlaylist.id, newCover) },
            onUpdateTitle = { newTitle -> onUpdatePlaylistTitle(selectedPlaylist.id, newTitle) },
            onDeletePlaylist = {
                onDeletePlaylist(selectedPlaylist.id)
                selectedPlaylistId = null
                onClearInitialPlaylist()
                if (openedFromHomeScreen) {
                    onNavigateToHome()
                }
            },
            playlists = playlists,
            onAddTrackToPlaylist = onAddTrackToPlaylist,
            onCreatePlaylist = onCreatePlaylist,
            onOpenArtist = onOpenArtist
        )
        return
    }

    val tabLabels = listOf("All", "Playlists", "Liked Songs", "Downloaded")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyPitchBlack)
            .padding(top = 2.dp)
    ) {
        // Top Bar: "My Music" + Flat Options Button (no arrow back, no circle fill container)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Music",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 20.sp
            )

            IconButton(
                onClick = {
                    if (showImportCard) {
                        showImportCard = false
                    } else {
                        showCreateOrImportSheet = true
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (showImportCard) Icons.Rounded.Close else Icons.Rounded.Add,
                    contentDescription = if (showImportCard) "Close Import" else "Create or Import Playlist",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Flat Designed Spotify Link & Playlist Import Section
        AnimatedVisibility(
            visible = showImportCard,
            enter = fadeIn(tween(200)) + expandVertically(
                animationSpec = tween(280, easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f))
            ),
            exit = fadeOut(tween(160)) + shrinkVertically(
                animationSpec = tween(240, easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f))
            )
        ) {
            val isImporting = spotifyImportStatus?.contains("…") == true

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF14161E),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Link,
                            contentDescription = null,
                            tint = AuraNeonLime,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Import Spotify Playlist",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lower height link input field (no paste button, clean compact row)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0D0E13))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = linkInput,
                            onValueChange = { linkInput = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 12.5.sp
                            ),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (linkInput.isEmpty()) {
                                    Text(
                                        text = "Paste Spotify playlist or album URL…",
                                        color = TextSecondary.copy(alpha = 0.7f),
                                        fontSize = 12.5.sp
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (linkInput.isNotBlank()) {
                            IconButton(
                                onClick = { linkInput = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Status and Import button on the SAME row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Imported status or progress on the left
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp)
                        ) {
                            if (!spotifyImportStatus.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isImporting) {
                                        CircularProgressIndicator(
                                            color = AuraNeonLime,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = spotifyImportStatus,
                                        color = if (spotifyImportStatus.contains("Could not", true) || spotifyImportStatus.contains("Failed", true))
                                            Color(0xFFFF5252)
                                        else
                                            AuraNeonLime,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Compact "Import" button (no "Playlist" text)
                        Button(
                            onClick = {
                                if (linkInput.isNotBlank()) {
                                    onImportSpotifyPlaylist(linkInput.trim())
                                    linkInput = ""
                                }
                            },
                            enabled = linkInput.isNotBlank() && !isImporting,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuraNeonLime,
                                disabledContainerColor = Color(0xFF222530)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Importing…", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    tint = if (linkInput.isNotBlank()) Color.Black else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    "Import",
                                    color = if (linkInput.isNotBlank()) Color.Black else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
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
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (activeTab) {
            0 -> {
                val allTracks = (favorites + downloads).distinctBy { it.videoId }
                if (allTracks.isEmpty()) {
                    EmptyVaultMessage("Your music vault is empty. Favorite or download tracks to see them here.")
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
                        items(allTracks, key = { it.videoId }) { track ->
                            val isFav = favorites.any { it.videoId == track.videoId }
                            val isDown = downloads.any { it.videoId == track.videoId && it.isDownloaded }
                            val isDownProgress = downloadingIds.contains(track.videoId)
                            SpotifyTrackRowItem(
                                track = track,
                                isCurrent = currentPlayingId == track.videoId,
                                isFavorite = isFav,
                                isDownloaded = isDown,
                                isDownloading = isDownProgress,
                                downloadProgress = downloadProgressMap[track.videoId],
                                onClick = { onPlayTrack(track, allTracks) },
                                onToggleFavorite = { onToggleFavorite(track) },
                                onDownload = { onDownloadTrack(track) },
                                onOptionsClick = { selectedModalTrack = track }
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
                                onLongClick = { selectedPlaylistForOptions = playlist }
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
                                isDownloaded = downloads.any { it.videoId == track.videoId && it.isDownloaded },
                                isDownloading = downloadingIds.contains(track.videoId),
                                downloadProgress = downloadProgressMap[track.videoId],
                                onClick = { onPlayTrack(track, favorites) },
                                onToggleFavorite = { onToggleFavorite(track) },
                                onDownload = { onDownloadTrack(track) },
                                onOptionsClick = { selectedModalTrack = track }
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
                            val isDown = track.isDownloaded
                            val isDownProgress = downloadingIds.contains(track.videoId)
                            SpotifyTrackRowItem(
                                track = track,
                                isCurrent = currentPlayingId == track.videoId,
                                isFavorite = favorites.any { it.videoId == track.videoId },
                                isDownloaded = isDown,
                                isDownloading = isDownProgress,
                                downloadProgress = downloadProgressMap[track.videoId],
                                onClick = { onPlayTrack(track, downloads) },
                                onToggleFavorite = { onToggleFavorite(track) },
                                onDownload = { onDownloadTrack(track) },
                                onOptionsClick = { selectedModalTrack = track }
                            )
                        }
                    }
                }
            }
        }
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
            onRemoveDownload = {
                onRemoveDownload(modalTrack)
                selectedModalTrack = null
            },
            onWatchVideo = {
                selectedModalTrack = null
                onPlayTrack(modalTrack, listOf(modalTrack))
                com.auramusic.app.core.media.AuraPlayerManager.setVideoMode(true)
            }
        )
    }

    selectedPlaylistForOptions?.let { pl ->
        AuraPlaylistOptionsModal(
            playlist = pl,
            onDismiss = { selectedPlaylistForOptions = null },
            onDownloadAll = {
                selectedPlaylistForOptions = null
                pl.tracks.forEach { onDownloadTrack(it) }
                Toast.makeText(context, "Downloading ${pl.tracks.size} songs for offline listening…", Toast.LENGTH_SHORT).show()
            },
            onChangeCover = {
                selectedPlaylistForOptions = null
                mediaPickerLauncher.launch("image/*")
            },
            onResetCover = if (pl.coverUrl.isNotBlank()) {
                {
                    selectedPlaylistForOptions = null
                    onUpdatePlaylistCover(pl.id, "")
                    Toast.makeText(context, "Cover reset to Spotify collage", Toast.LENGTH_SHORT).show()
                }
            } else null,
            onRename = {
                selectedPlaylistForOptions = null
                renamePlaylistTarget = pl
                renameInput = pl.name
                showRenameDialog = true
            },
            onDelete = {
                selectedPlaylistForOptions = null
                onDeletePlaylist(pl.id)
            }
        )
    }

    if (showRenameDialog && renamePlaylistTarget != null) {
        val target = renamePlaylistTarget!!
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = Color(0xFF1B1D26),
            title = { Text("Rename Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraNeonLime,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = renameInput.trim()
                        if (trimmed.isNotBlank()) {
                            onUpdatePlaylistTitle(target.id, trimmed)
                            Toast.makeText(context, "Renamed to '$trimmed'", Toast.LENGTH_SHORT).show()
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("Save", color = AuraNeonLime, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    // Modal 1: Choice between "Create a playlist" and "Import Playlist from Spotify"
    if (showCreateOrImportSheet) {
        AuraCreateOrImportModal(
            onDismiss = { showCreateOrImportSheet = false },
            onCreatePlaylist = {
                showCreateOrImportSheet = false
                showCreatePlaylistModal = true
            },
            onImportSpotify = {
                showCreateOrImportSheet = false
                showImportCard = true
            }
        )
    }

    // Modal 2: Create Playlist with Cover Image, Title, Description
    if (showCreatePlaylistModal) {
        AuraCreatePlaylistModal(
            onDismiss = { showCreatePlaylistModal = false },
            onCreate = { title, desc, coverUri ->
                showCreatePlaylistModal = false
                onCreatePlaylist(title, desc, coverUri)
                activeTab = 1
                Toast.makeText(context, "Created playlist: $title", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal 3: Add Song to Playlist
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistItemRow(
    playlist: AuraPlaylist,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 20.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.auramusic.app.ui.components.AuraPlaylistCover(
            coverUrl = playlist.coverUrl,
            tracks = playlist.tracks,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp)
        )

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


