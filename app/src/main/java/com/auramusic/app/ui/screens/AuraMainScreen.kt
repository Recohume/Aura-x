package com.auramusic.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auramusic.app.core.media.AuraPlayerManager
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.ui.AuraViewModel
import com.auramusic.app.ui.theme.AuraNeonLime
import com.auramusic.app.ui.theme.AuraPlayGreen
import com.auramusic.app.ui.theme.AuraViolet
import com.auramusic.app.ui.theme.SpotifyCard
import com.auramusic.app.ui.theme.SpotifyPitchBlack
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary

@Composable
fun AuraMainScreen(viewModel: AuraViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isPlayerExpanded by remember { mutableStateOf(false) }

    val chartSections by viewModel.chartSections.collectAsState()
    val topArtists by viewModel.topArtists.collectAsState()
    val communityPlaylists by viewModel.communityPlaylists.collectAsState()
    val selectedArtistDetail by viewModel.selectedArtistDetail.collectAsState()
    val isArtistLoading by viewModel.isArtistLoading.collectAsState()
    val selectedPlaylistDetail by viewModel.selectedPlaylistDetail.collectAsState()

    // Seamless Android system back handling
    BackHandler(enabled = isPlayerExpanded) {
        isPlayerExpanded = false
    }

    BackHandler(enabled = !isPlayerExpanded && selectedArtistDetail != null) {
        viewModel.closeArtistDetail()
    }

    BackHandler(enabled = !isPlayerExpanded && selectedPlaylistDetail != null) {
        viewModel.closePlaylistDetail()
    }

    BackHandler(enabled = !isPlayerExpanded && selectedArtistDetail == null && selectedPlaylistDetail == null && selectedTab != 0) {
        selectedTab = 0
    }
    val isPlaylistLoading by viewModel.isPlaylistLoading.collectAsState()
    val activeCategory by viewModel.activeCategory.collectAsState()
    val categoryTracks by viewModel.categoryTracks.collectAsState()
    val isCategoryLoading by viewModel.isCategoryLoading.collectAsState()
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
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val listeningStats by viewModel.listeningStats.collectAsState()
    val downloadingIds by viewModel.downloadingIds.collectAsState()

    val currentTrack by AuraPlayerManager.currentTrack.collectAsState()
    val isPlaying by AuraPlayerManager.isPlaying.collectAsState()
    val isBuffering by AuraPlayerManager.isBuffering.collectAsState()
    val positionSec by AuraPlayerManager.positionSec.collectAsState()
    val durationSec by AuraPlayerManager.durationSec.collectAsState()
    val isShuffleEnabled by AuraPlayerManager.isShuffleEnabled.collectAsState()
    val queue by AuraPlayerManager.queue.collectAsState()
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
            .background(SpotifyPitchBlack)
    ) {
        // 1. Screens Content (full screen underneath floating controls)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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
                    currentPlayingId = currentTrack?.videoId,
                    isPlaying = isPlaying,
                    recentlyPlayed = recentlyPlayed,
                    userPlaylists = playlists,
                    topArtists = topArtists,
                    communityPlaylists = communityPlaylists,
                    activeCategory = activeCategory,
                    categoryTracks = categoryTracks,
                    isCategoryLoading = isCategoryLoading,
                    onPlayTrack = { track, queue -> viewModel.playTrack(track, queue) },
                    onPlayArtist = { artist -> viewModel.playArtist(artist) },
                    onOpenArtist = { artist -> viewModel.openArtistDetail(artist) },
                    onPlayCommunityPlaylist = { playlist -> viewModel.playCommunityPlaylist(playlist) },
                    onOpenPlaylist = { playlist -> viewModel.openPlaylistDetail(playlist) },
                    onSelectCategory = { category -> viewModel.selectCategory(category) },
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
                    favorites = favorites,
                    downloads = downloads,
                    downloadingIds = downloadingIds,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onFilterChange = { viewModel.setSearchFilter(it) },
                    onPlayTrack = { track, queue -> viewModel.playTrack(track, queue) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onDownloadTrack = { viewModel.downloadTrack(it) }
                )
                2 -> LibraryScreen(
                    favorites = favorites,
                    downloads = downloads,
                    playlists = playlists,
                    stats = listeningStats,
                    downloadingIds = downloadingIds,
                    currentPlayingId = currentTrack?.videoId,
                    isPlaying = isPlaying,
                    spotifyImportStatus = spotifyImportStatus,
                    onImportSpotifyPlaylist = { viewModel.importSpotifyPlaylist(it) },
                    onPlayTrack = { track, queue -> viewModel.playTrack(track, queue) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onDownloadTrack = { viewModel.downloadTrack(it) },
                    onUpdatePlaylistCover = { id, cover -> viewModel.updatePlaylistCover(id, cover) },
                    onUpdatePlaylistTitle = { id, title -> viewModel.updatePlaylistTitle(id, title) },
                    onDeletePlaylist = { id -> viewModel.deletePlaylist(id) }
                )
                3 -> SettingsScreen(viewModel = viewModel)
            }
        }

        // 1b. Full-Screen Slide-in Artist Detail Screen
        AnimatedVisibility(
            visible = selectedArtistDetail != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            selectedArtistDetail?.let { detail ->
                ArtistDetailScreen(
                    artistDetail = detail,
                    isLoading = isArtistLoading,
                    currentPlayingId = currentTrack?.videoId,
                    onClose = { viewModel.closeArtistDetail() },
                    onPlayTrack = { track, queue -> viewModel.playTrack(track, queue) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                )
            }
        }

        // 1c. Full-Screen Slide-in Playlist Detail Screen
        AnimatedVisibility(
            visible = selectedPlaylistDetail != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            selectedPlaylistDetail?.let { detail ->
                PlaylistDetailScreen(
                    playlistDetail = detail,
                    isLoading = isPlaylistLoading,
                    currentPlayingId = currentTrack?.videoId,
                    onClose = { viewModel.closePlaylistDetail() },
                    onPlayTrack = { track, queue -> viewModel.playTrack(track, queue) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                )
            }
        }

        // 2. Dim gradient scrim behind the bottom floating controls (dimmed so background is obscured)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            SpotifyPitchBlack.copy(alpha = 0.70f),
                            SpotifyPitchBlack.copy(alpha = 0.95f),
                            SpotifyPitchBlack
                        )
                    )
                )
        )

        // 3. TRUE FLOATING OVERLAY: Reduced-Size MiniPlayer & Increased Width/Height Capsule Navigation Dock
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Reduced-size Floating MiniPlayer (closer with the menu)
            val activeTrack = currentTrack
            if (activeTrack != null && !isPlayerExpanded) {
                AuraMiniPlayer(
                    track = activeTrack,
                    isPlaying = isPlaying,
                    isBuffering = isBuffering,
                    progress = (positionSec / durationSec.coerceAtLeast(1f)).coerceIn(0f, 1f),
                    backgroundColor = palette.primary,
                    onExpand = { isPlayerExpanded = true },
                    onTogglePlayPause = { AuraPlayerManager.togglePlayPause() },
                    onSkipPrevious = { AuraPlayerManager.skipToPrevious() },
                    onSkipNext = { AuraPlayerManager.skipToNext() }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Floating Dynamic Capsule Navigation Dock with increased width and height
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(40.dp),
                    color = Color(0xFF16181F).copy(alpha = 0.98f),
                    shadowElevation = 16.dp,
                    modifier = Modifier.animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 7.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val navItems = listOf(
                            Triple(0, "Home", Icons.Rounded.Home),
                            Triple(1, "Search", Icons.Rounded.Search),
                            Triple(2, "My Music", Icons.Rounded.LibraryMusic),
                            Triple(3, "Settings", Icons.Rounded.Settings)
                        )
                        navItems.forEach { (idx, label, icon) ->
                            val isSelected = selectedTab == idx
                            val iconScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.08f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "navIconScale"
                            )

                            Surface(
                                onClick = {
                                    viewModel.closeArtistDetail()
                                    viewModel.closePlaylistDetail()
                                    selectedTab = idx
                                },
                                shape = RoundedCornerShape(28.dp),
                                color = if (isSelected) AuraNeonLime else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = if (isSelected) 18.dp else 14.dp,
                                        vertical = 13.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) Color.Black else TextSecondary,
                                        modifier = Modifier
                                            .size(23.dp)
                                            .graphicsLayer {
                                                scaleX = iconScale
                                                scaleY = iconScale
                                            }
                                    )

                                    AnimatedVisibility(
                                        visible = isSelected,
                                        enter = fadeIn(tween(160)) + expandHorizontally(
                                            animationSpec = tween(
                                                durationMillis = 200,
                                                easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
                                            )
                                        ),
                                        exit = fadeOut(tween(120)) + shrinkHorizontally(
                                            animationSpec = tween(
                                                durationMillis = 180,
                                                easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
                                            )
                                        )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Spacer(modifier = Modifier.width(7.dp))
                                            Text(
                                                text = label,
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                maxLines = 1,
                                                softWrap = false
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

        // Full-Screen Now Playing Sheet
        val activeTrack = currentTrack
        AnimatedVisibility(
            visible = isPlayerExpanded && activeTrack != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (activeTrack != null) {
                AuraNowPlayingSheet(
                    track = activeTrack,
                    isPlaying = isPlaying,
                    isBuffering = isBuffering,
                    positionSec = positionSec,
                    durationSec = durationSec,
                    palette = palette,
                    lyrics = lyrics,
                    lyricsOffsetMs = lyricsOffsetMs,
                    showRomanized = showRomanized,
                    isFavorite = favorites.any { it.videoId == activeTrack.videoId },
                    isShuffleEnabled = isShuffleEnabled,
                    queue = queue,
                    currentStyle = nowPlayingStyle,
                    onCollapse = { isPlayerExpanded = false },
                    onTogglePlayPause = { AuraPlayerManager.togglePlayPause() },
                    onSeekTo = { AuraPlayerManager.seekToSeconds(it) },
                    onSkipNext = { AuraPlayerManager.skipToNext() },
                    onSkipPrevious = { AuraPlayerManager.skipToPrevious() },
                    onToggleFavorite = { viewModel.toggleFavorite(activeTrack) },
                    onToggleShuffle = { AuraPlayerManager.toggleShuffle() },
                    onToggleStyle = {
                        viewModel.setNowPlayingStyle(
                            if (nowPlayingStyle == com.auramusic.app.domain.model.NowPlayingStyle.SPOTIFY)
                                com.auramusic.app.domain.model.NowPlayingStyle.LIQUID_GLASS
                            else
                                com.auramusic.app.domain.model.NowPlayingStyle.SPOTIFY
                        )
                    },
                    onPlayQueueTrack = { viewModel.playTrack(it, queue) },
                    onAdjustLyricsOffset = { viewModel.adjustLyricsOffset(it) },
                    onToggleRomanized = { viewModel.toggleRomanizedLyrics() }
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
    backgroundColor: Color,
    onExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit
) {
    val lighterProgressColor = remember(backgroundColor) {
        lerp(backgroundColor, Color.White, 0.65f)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor.copy(alpha = 0.96f),
        shadowElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onExpand)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(9.dp))
                    Column {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(lighterProgressColor)
                            .clickable(onClick = onTogglePlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(start = if (!isPlaying) 1.5.dp else 0.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next Track",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                color = lighterProgressColor,
                trackColor = backgroundColor.copy(alpha = 0.35f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        }
    }
}
