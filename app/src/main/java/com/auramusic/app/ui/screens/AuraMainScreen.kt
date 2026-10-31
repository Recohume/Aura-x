package com.auramusic.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auramusic.app.core.media.AuraPlayerManager
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.ui.AuraViewModel
import com.auramusic.app.ui.theme.AuraViolet
import com.auramusic.app.ui.theme.ObsidianBackground
import com.auramusic.app.ui.theme.ObsidianCard
import com.auramusic.app.ui.theme.ObsidianSurface
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary

@Composable
fun AuraMainScreen(viewModel: AuraViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isPlayerExpanded by remember { mutableStateOf(false) }

    val chartSections by viewModel.chartSections.collectAsState()
    val moodCategories by viewModel.moodCategories.collectAsState()
    val isLoadingHome by viewModel.isLoadingHome.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val spotifyImportStatus by viewModel.spotifyImportStatus.collectAsState()

    val favorites by viewModel.favorites.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val listeningStats by viewModel.listeningStats.collectAsState()
    val downloadingIds by viewModel.downloadingIds.collectAsState()

    val currentTrack by AuraPlayerManager.currentTrack.collectAsState()
    val isPlaying by AuraPlayerManager.isPlaying.collectAsState()
    val isBuffering by AuraPlayerManager.isBuffering.collectAsState()
    val positionSec by AuraPlayerManager.positionSec.collectAsState()
    val durationSec by AuraPlayerManager.durationSec.collectAsState()
    val streamSourceTier by AuraPlayerManager.streamSourceTier.collectAsState()
    val sponsorSegments by AuraPlayerManager.sponsorSegments.collectAsState()
    val lastSkippedSegment by AuraPlayerManager.lastSkippedSegment.collectAsState()
    val activeEqPreset by AuraPlayerManager.activeEqPreset.collectAsState()
    val eqEnabled by AuraPlayerManager.eqEnabled.collectAsState()
    val sponsorBlockEnabled by AuraPlayerManager.sponsorBlockEnabled.collectAsState()
    val sleepTimerRemainingMin by AuraPlayerManager.sleepTimerRemainingMin.collectAsState()

    val palette by viewModel.palette.collectAsState()
    val lyrics by viewModel.lyrics.collectAsState()
    val lyricsOffsetMs by viewModel.lyricsOffsetMs.collectAsState()
    val showRomanized by viewModel.showRomanizedLyrics.collectAsState()
    val rydData by viewModel.rydData.collectAsState()
    val nowPlayingStyle by viewModel.nowPlayingStyle.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Scaffold(
            containerColor = ObsidianBackground,
            bottomBar = {
                Column {
                    // Floating MiniPlayer Dock
                    val activeTrack = currentTrack
                    if (activeTrack != null && !isPlayerExpanded) {
                        AuraMiniPlayer(
                            track = activeTrack,
                            isPlaying = isPlaying,
                            isBuffering = isBuffering,
                            progress = (positionSec / durationSec.coerceAtLeast(1f)).coerceIn(0f, 1f),
                            accentColor = palette.primary,
                            onExpand = { isPlayerExpanded = true },
                            onTogglePlayPause = { AuraPlayerManager.togglePlayPause() },
                            onSkipNext = { AuraPlayerManager.skipToNext() }
                        )
                    }

                    NavigationBar(
                        containerColor = ObsidianSurface,
                        tonalElevation = 0.dp
                    ) {
                        val navItems = listOf(
                            Triple(0, "Home", Icons.Rounded.Home),
                            Triple(1, "Search", Icons.Rounded.Search),
                            Triple(2, "Library", Icons.Rounded.LibraryMusic)
                        )
                        navItems.forEach { (idx, label, icon) ->
                            NavigationBarItem(
                                selected = selectedTab == idx,
                                onClick = { selectedTab = idx },
                                icon = {
                                    Icon(imageVector = icon, contentDescription = label)
                                },
                                label = {
                                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    indicatorColor = AuraViolet,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        chartSections = chartSections,
                        moodCategories = moodCategories,
                        isLoading = isLoadingHome,
                        palette = palette,
                        favorites = favorites,
                        downloads = downloads,
                        downloadingIds = downloadingIds,
                        onPlayTrack = { track, queue -> viewModel.playTrack(track, queue) },
                        onPlayMood = { mood -> viewModel.playMoodCategory(mood) },
                        onToggleFavorite = { track -> viewModel.toggleFavorite(track) },
                        onDownloadTrack = { track -> viewModel.downloadTrack(track) },
                        onRefresh = { viewModel.refreshHome() }
                    )
                    1 -> SearchScreen(
                        query = searchQuery,
                        filter = searchFilter,
                        results = searchResults,
                        isSearching = isSearching,
                        spotifyImportStatus = spotifyImportStatus,
                        favorites = favorites,
                        downloads = downloads,
                        downloadingIds = downloadingIds,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        onFilterChange = { viewModel.setSearchFilter(it) },
                        onPlayTrack = { track, queue -> viewModel.playTrack(track, queue) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDownloadTrack = { viewModel.downloadTrack(it) },
                        onImportSpotifyPlaylist = { viewModel.importSpotifyPlaylist(it) }
                    )
                    2 -> LibraryScreen(
                        favorites = favorites,
                        downloads = downloads,
                        playlists = playlists,
                        stats = listeningStats,
                        downloadingIds = downloadingIds,
                        onPlayTrack = { track, queue -> viewModel.playTrack(track, queue) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDownloadTrack = { viewModel.downloadTrack(it) }
                    )
                }
            }
        }

        // Full-Screen Now Playing Sheet
        val activeTrack = currentTrack
        AnimatedVisibility(
            visible = isPlayerExpanded && activeTrack != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            if (activeTrack != null) {
                AuraNowPlayingSheet(
                    track = activeTrack,
                    isPlaying = isPlaying,
                    isBuffering = isBuffering,
                    positionSec = positionSec,
                    durationSec = durationSec,
                    streamSourceTier = streamSourceTier,
                    palette = palette,
                    lyrics = lyrics,
                    lyricsOffsetMs = lyricsOffsetMs,
                    showRomanized = showRomanized,
                    sponsorSegments = sponsorSegments,
                    lastSkippedSegment = lastSkippedSegment,
                    rydData = rydData,
                    nowPlayingStyle = nowPlayingStyle,
                    activeEqPreset = activeEqPreset,
                    eqEnabled = eqEnabled,
                    sponsorBlockEnabled = sponsorBlockEnabled,
                    sleepTimerRemainingMin = sleepTimerRemainingMin,
                    isFavorite = favorites.any { it.videoId == activeTrack.videoId },
                    onCollapse = { isPlayerExpanded = false },
                    onTogglePlayPause = { AuraPlayerManager.togglePlayPause() },
                    onSeekTo = { AuraPlayerManager.seekToSeconds(it) },
                    onSkipNext = { AuraPlayerManager.skipToNext() },
                    onSkipPrevious = { AuraPlayerManager.skipToPrevious() },
                    onToggleFavorite = { viewModel.toggleFavorite(activeTrack) },
                    onChangeStyle = { viewModel.setNowPlayingStyle(it) },
                    onAdjustLyricsOffset = { viewModel.adjustLyricsOffset(it) },
                    onToggleRomanized = { viewModel.toggleRomanizedLyrics() },
                    onSelectEqPreset = { viewModel.selectEqualizerPreset(it) },
                    onToggleEqEnabled = { AuraPlayerManager.setEqualizerEnabled(it) },
                    onToggleSponsorBlock = { AuraPlayerManager.setSponsorBlockEnabled(it) },
                    onSetSleepTimer = { AuraPlayerManager.setSleepTimerMinutes(it) }
                )
            }
        }
    }
}

@Composable
private fun AuraMiniPlayer(
    track: AuraTrack,
    isPlaying: Boolean,
    isBuffering: Boolean,
    progress: Float,
    accentColor: Color,
    onExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = ObsidianCard,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onExpand)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = track.highResArtworkUrl,
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = track.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    ) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = onSkipNext) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next Track",
                            tint = Color.White
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                color = accentColor,
                trackColor = Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
            )
        }
    }
}
