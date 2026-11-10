package com.auramusic.app.ui.screens

import com.auramusic.app.ui.theme.AuraViolet

import com.auramusic.app.R

import android.widget.Toast
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import com.auramusic.app.core.media.AuraPlayerManager
import com.auramusic.app.ui.components.AuraAddToPlaylistModal
import com.auramusic.app.ui.components.AuraCreatePlaylistModal
import com.auramusic.app.ui.components.AuraTrackOptionsModal
import com.auramusic.app.ui.components.auraShimmer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ArrowCircleDown
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auramusic.app.core.extractor.SimpMusicExtractorEngine
import com.auramusic.app.domain.model.AuraPalette
import com.auramusic.app.ui.components.AudioPlayingIndicator
import com.auramusic.app.domain.model.AuraPlaylist
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.MoodCategory
import com.auramusic.app.domain.model.YtmChartSection
import com.auramusic.app.domain.model.YtmCommunityPlaylist
import com.auramusic.app.domain.model.YtmMoodBrowseParams
import com.auramusic.app.domain.model.YtmTopArtist
import com.auramusic.app.ui.theme.AuraCircleButtonBg
import com.auramusic.app.ui.theme.AuraLilacButton
import com.auramusic.app.ui.theme.AuraLilacCard
import com.auramusic.app.ui.theme.AuraLilacDarkText
import com.auramusic.app.ui.theme.AuraNeonLime
import com.auramusic.app.ui.theme.AuraPillInactive
import com.auramusic.app.ui.theme.AuraPillInactiveText
import com.auramusic.app.ui.theme.AuraSageGreen
import com.auramusic.app.ui.theme.BadgeExplicitBg
import com.auramusic.app.ui.theme.BadgeLyricsBg
import com.auramusic.app.ui.theme.PillActiveBg
import com.auramusic.app.ui.theme.PillActiveText
import com.auramusic.app.ui.theme.PillInactiveBg
import com.auramusic.app.ui.theme.PillInactiveText
import com.auramusic.app.ui.theme.SpotifyCard
import com.auramusic.app.ui.theme.SpotifyHeroCard
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary
import java.util.Calendar

private val EaseOutCubic = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    chartSections: List<YtmChartSection>,
    moodCategories: List<MoodCategory>,
    isLoading: Boolean,
    palette: AuraPalette,
    favorites: List<AuraTrack>,
    downloads: List<AuraTrack>,
    downloadingIds: Set<String>,
    downloadProgressMap: Map<String, Float> = emptyMap(),
    currentPlayingId: String? = null,
    isPlaying: Boolean = false,
    recentlyPlayed: List<AuraTrack> = emptyList(),
    homeRecommendations: List<AuraTrack> = emptyList(),
    albumRecommendations: List<AuraTrack> = emptyList(),
    likedRecommendations: List<AuraTrack> = emptyList(),
    userPlaylists: List<AuraPlaylist> = emptyList(),
    topArtists: List<YtmTopArtist> = emptyList(),
    communityPlaylists: List<YtmCommunityPlaylist> = emptyList(),
    musicVideos: List<AuraTrack> = emptyList(),
    activeCategory: String = "All",
    homeCategoryBackground: String? = null,
    categoryTracks: List<AuraTrack> = emptyList(),
    isCategoryLoading: Boolean = false,
    onPlayTrack: (AuraTrack, List<AuraTrack>) -> Unit,
    onPlayMusicVideo: (AuraTrack, List<AuraTrack>) -> Unit = { track, queue -> onPlayTrack(track, queue) },
    onPlayArtist: (YtmTopArtist) -> Unit = {},
    onOpenArtist: (YtmTopArtist) -> Unit = {},
    onPlayCommunityPlaylist: (YtmCommunityPlaylist) -> Unit = {},
    onOpenPlaylist: (YtmCommunityPlaylist) -> Unit = {},
    onOpenUserPlaylist: (AuraPlaylist) -> Unit = {},
    onSelectCategory: (String) -> Unit = {},
    onPlayMood: (MoodCategory) -> Unit,
    onToggleFavorite: (AuraTrack) -> Unit,
    onDownloadTrack: (AuraTrack) -> Unit,
    onRefresh: () -> Unit,
    onOpenArtistByName: (String, String) -> Unit = { _, _ -> },
    onAddTrackToPlaylist: (String, AuraTrack) -> Unit = { _, _ -> },
    onCreatePlaylist: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    var selectedTrackForOptions by remember { mutableStateOf<AuraTrack?>(null) }
    var trackForAddToPlaylist by remember { mutableStateOf<AuraTrack?>(null) }
    var showCreatePlaylistModal by remember { mutableStateOf(false) }
    var isPullRefreshing by remember { mutableStateOf(false) }
    val isAnyLoading = isLoading || isCategoryLoading
    LaunchedEffect(isAnyLoading) {
        if (!isAnyLoading) isPullRefreshing = false
    }

    val liveTopTracks = remember(chartSections) {
        chartSections.firstOrNull()?.tracks?.ifEmpty { SimpMusicExtractorEngine.DEFAULT_SEED_TRACKS }
            ?: SimpMusicExtractorEngine.DEFAULT_SEED_TRACKS
    }
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    val heroTrack = liveTopTracks.firstOrNull()
    val playlistTracks = liveTopTracks
    val personalizedHomeTracks = homeRecommendations.ifEmpty { playlistTracks }
    val quickPickTracks = (if (activeCategory != "All") categoryTracks else personalizedHomeTracks).take(20)
    val displayedTracks = if (activeCategory != "All") categoryTracks else personalizedHomeTracks
    val visibleTopArtists = topArtists
    val visibleRecentlyPlayed = recentlyPlayed
    val visibleUserPlaylists = userPlaylists
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val pageWidth = (screenWidth - 56.dp).coerceAtLeast(280.dp)
    var visibleTrackCount by remember(displayedTracks) { androidx.compose.runtime.mutableIntStateOf(20) }

    // Pulse animation for the LOSSLESS indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "losslessPulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    val listState = rememberLazyListState()
    val moodRowState = rememberLazyListState()

    LaunchedEffect(activeCategory) {
        val preloadUrl = YtmMoodBrowseParams.DEFAULT_BACKGROUNDS[activeCategory]
        if (preloadUrl != null) {
            coil.Coil.imageLoader(context).enqueue(
                ImageRequest.Builder(context).data(preloadUrl).build()
            )
        }
        val index = YtmMoodBrowseParams.ALL_MOODS.indexOf(activeCategory)
        if (index >= 0) {
            moodRowState.animateScrollToItem(index)
        } else if (activeCategory == "All") {
            moodRowState.animateScrollToItem(0)
            if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
                listState.animateScrollToItem(0)
            }
        }
    }

    // Keep background dark (null) while skeleton UI is rendering; reveal mood background simultaneously with ready content
    val moodBgUrl = if (activeCategory != "All" && !isCategoryLoading) {
        YtmMoodBrowseParams.DEFAULT_BACKGROUNDS[activeCategory] ?: homeCategoryBackground
    } else null
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val greetingHeightDp = 36.dp
    val greetingSpacingDp = 8.dp
    val moodPillsHeightDp = 42.dp
    val headerBottomPaddingDp = 10.dp
    val brandRowHeightDp = 40.dp
    val brandRowSpacingDp = 8.dp

    val collapseRangeDp = brandRowHeightDp + brandRowSpacingDp + greetingHeightDp + greetingSpacingDp
    val uncollapsedHeaderHeight = statusBarTop + brandRowHeightDp + brandRowSpacingDp + greetingHeightDp + greetingSpacingDp + moodPillsHeightDp + headerBottomPaddingDp
    val collapsedHeaderHeight = statusBarTop + moodPillsHeightDp + headerBottomPaddingDp

    val uncollapsedHeaderHeightPx = with(LocalDensity.current) { uncollapsedHeaderHeight.toPx() }
    val collapseRangePx = with(LocalDensity.current) { collapseRangeDp.toPx() }
    val darkenDistancePx = with(LocalDensity.current) { (collapseRangeDp * 1.4f).toPx() }

    val scrollOffsetPx by remember {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            if (firstIndex == 0) {
                firstOffset.toFloat()
            } else {
                uncollapsedHeaderHeightPx + (firstIndex * 350f) + firstOffset.toFloat()
            }
        }
    }
    val collapseProgress by remember {
        derivedStateOf {
            (scrollOffsetPx / collapseRangePx.coerceAtLeast(1f)).coerceIn(0f, 1f)
        }
    }
    // Keep sticky header 100% transparent while greeting is scrolling up so it never overlays or cuts the greeting text
    val darkFraction by remember {
        derivedStateOf {
            ((scrollOffsetPx - collapseRangePx * 0.70f) / darkenDistancePx.coerceAtLeast(1f)).coerceIn(0f, 1f)
        }
    }
    val currentHeaderHeight = uncollapsedHeaderHeight - (collapseRangeDp * collapseProgress)

    PullToRefreshBox(
        isRefreshing = isPullRefreshing,
        onRefresh = {
            isPullRefreshing = true
            onRefresh()
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080A12))
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Root YouTube Music Mood Category Background Backdrop Image (extends under Android status bar)
            Crossfade(
                targetState = moodBgUrl,
                animationSpec = tween(durationMillis = 380, easing = EaseOutCubic),
                label = "rootMoodBgCrossfade"
            ) { targetUrl ->
                if (targetUrl != null) {
                    val moodAtmosphereFilter = remember {
                        ColorFilter.colorMatrix(
                            ColorMatrix().apply {
                                // Subtle night/sunset exposure curve so no mood background is ever bright
                                setToScale(0.62f, 0.60f, 0.68f, 1f)
                            }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(580.dp)
                            .graphicsLayer {
                                translationY = -scrollOffsetPx
                            }
                    ) {
                        val moodImageRequest = remember(targetUrl) {
                            ImageRequest.Builder(context)
                                .data(targetUrl)
                                .crossfade(false)
                                .build()
                        }
                        AsyncImage(
                            model = moodImageRequest,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.TopCenter,
                            colorFilter = moodAtmosphereFilter,
                            modifier = Modifier.fillMaxSize()
                        )
                        // 2. Vertical Gradient Darkening scrim on the single shared mood background:
                        // 100% transparent across status bar, greeting, mood pills, and items 1 & 2,
                        // darkening only on items 3 to 4 at the bottom of Quick Picks.
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        0.00f to Color.Transparent,
                                        0.56f to Color.Transparent,
                                        0.72f to Color(0xFF080A12).copy(alpha = 0.48f),
                                        0.88f to Color(0xFF080A12).copy(alpha = 0.88f),
                                        0.97f to Color(0xFF080A12),
                                        1.00f to Color(0xFF080A12)
                                    )
                                )
                        )
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 140.dp)
            ) {
                // Top spacer matching the uncollapsed header height
                item(key = "home_sticky_header_spacer") {
                    Spacer(modifier = Modifier.height(uncollapsedHeaderHeight))
                }

                // Quick picks: four compact rows per horizontal page, matching YouTube Music's shelf.
                if (quickPickTracks.isNotEmpty() || (activeCategory != "All" && isCategoryLoading)) {
                    item(key = "quick_picks_shelf") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp, bottom = 26.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AnimatedContent(
                                    targetState = if (activeCategory == "All") "Quick picks" else "$activeCategory picks",
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(260, easing = EaseOutCubic)) togetherWith
                                        fadeOut(animationSpec = tween(180, easing = EaseOutCubic))
                                    },
                                    label = "quickPicksTitle"
                                ) { titleText ->
                                    Text(
                                        text = titleText,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White
                                    )
                                }
                                HomePlayAllButton {
                                    quickPickTracks.firstOrNull()?.let { onPlayTrack(it, quickPickTracks) }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Crossfade(
                                targetState = Triple(isCategoryLoading, activeCategory, quickPickTracks),
                                animationSpec = tween(durationMillis = 320, easing = EaseOutCubic),
                                label = "quickPicksContentTransition"
                            ) { (loading, _, tracksForState) ->
                                if (loading && tracksForState.isEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        repeat(4) { com.auramusic.app.ui.components.TrackItemSkeleton() }
                                    }
                                } else {
                                val quickPickPages = remember(tracksForState) { tracksForState.chunked(4) }
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(quickPickPages, key = { page -> page.firstOrNull()?.videoId ?: page.hashCode() }) { page ->
                                        Column(
                                            modifier = Modifier.width(pageWidth),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            page.forEach { track ->
                                                val isCurrent = currentPlayingId == track.videoId
                                                val isTrackPlaying = isCurrent && isPlaying
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(68.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(60.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFF202127))
                                .border(1.dp, Color(0xFF68DFFF).copy(alpha = 0.28f), RoundedCornerShape(6.dp))
                                                            .clickable { onPlayTrack(track, quickPickTracks) },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        AsyncImage(
                                                            model = track.highResArtworkUrl,
                                                            contentDescription = track.title,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                        if (isTrackPlaying) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .background(Color.Black.copy(alpha = 0.55f)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                AudioPlayingIndicator(
                                                                    modifier = Modifier.fillMaxSize(0.70f),
                                                                    color = AuraNeonLime
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Column(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(start = 14.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .clickable { onPlayTrack(track, quickPickTracks) }
                                                            .padding(vertical = 5.dp)
                                                    ) {
                                                        Text(
                                                            text = track.title,
                                                            color = if (isCurrent) AuraNeonLime else Color.White,
                                                            style = MaterialTheme.typography.titleSmall,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        val albumLabel = track.album.takeIf {
                                                            it.isNotBlank() && !it.equals("YouTube Music", ignoreCase = true)
                                                        }
                                                        Text(
                                                            text = if (albumLabel != null) "${track.artist}  •  $albumLabel" else track.artist,
                                                            color = Color.White,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { selectedTrackForOptions = track },
                                                        modifier = Modifier.size(42.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.MoreVert,
                                                            contentDescription = "Options for ${track.title}",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(22.dp)
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
                }
            }

        // Wide 16:9 video cards in a horizontal shelf, with a partial next card as a swipe cue.
        if (musicVideos.isNotEmpty() || isLoading || isCategoryLoading) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 26.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AnimatedContent(
                            targetState = if (activeCategory == "All") "Music videos for you" else "$activeCategory videos",
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220, easing = EaseOutCubic)) togetherWith
                                fadeOut(animationSpec = tween(160, easing = EaseOutCubic))
                            },
                            label = "musicVideosTitle"
                        ) { titleText ->
                            Text(
                                text = titleText,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 23.sp
                            )
                        }
                        if (musicVideos.isNotEmpty()) {
                            HomePlayAllButton {
                                onPlayMusicVideo(musicVideos.first(), musicVideos)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Crossfade(
                        targetState = Triple(isCategoryLoading, activeCategory, musicVideos),
                        animationSpec = tween(durationMillis = 320, easing = EaseOutCubic),
                        label = "videosCategoryTransition"
                    ) { (loading, _, videosForState) ->
                        if (loading && videosForState.isEmpty()) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(2) {
                                    Box(
                                        Modifier
                                            .width(pageWidth)
                                            .aspectRatio(16f / 9f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .auraShimmer()
                                    )
                                }
                            }
                        } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(videosForState, key = { "home_video_${it.videoId}" }) { video ->
                                Box(
                                    modifier = Modifier
                                        .width(pageWidth)
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF202127))
                    .border(1.dp, Color(0xFF68DFFF).copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                        .clickable { onPlayMusicVideo(video, videosForState) }
                                ) {
                                    AsyncImage(
                                        model = video.highResArtworkUrl,
                                        contentDescription = "Play ${video.title}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(76.dp)
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                                                )
                                            )
                                    )
                                    Text(
                                        text = video.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(start = 14.dp, end = 54.dp, bottom = 11.dp)
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.54f),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(10.dp)
                                            .size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayArrow,
                                                contentDescription = "Play ${video.title}",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
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
        }

        if (likedRecommendations.isNotEmpty()) {
            item(key = "liked_recommendations_shelf") {
                Box(modifier = Modifier.animateItem(fadeInSpec = tween(300, easing = EaseOutCubic), fadeOutSpec = tween(200, easing = EaseOutCubic), placementSpec = tween(300, easing = EaseOutCubic))) {
                    HomeRecommendationShelf(
                        title = if (activeCategory == "All") "Liked songs for you" else "$activeCategory from your likes",
                        tracks = likedRecommendations,
                        currentPlayingId = currentPlayingId,
                        isPlaying = isPlaying,
                        onPlayTrack = onPlayTrack,
                        keyPrefix = "liked_pick"
                    )
                }
            }
        }

        // 3. Top Artists Carousel (Live YouTube Music Charts FEmusic_charts)
        if (visibleTopArtists.isNotEmpty() || (activeCategory == "All" && isLoading)) {
            item(key = "top_artists_shelf") {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp).animateItem(fadeInSpec = tween(300, easing = EaseOutCubic), fadeOutSpec = tween(200, easing = EaseOutCubic), placementSpec = tween(300, easing = EaseOutCubic))) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top Artists",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    if (visibleTopArtists.isEmpty() && isLoading) {
                        com.auramusic.app.ui.components.ArtistCarouselSkeleton()
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(visibleTopArtists, key = { "artist_${it.id}" }) { artist ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(92.dp)
                                        .clickable { onOpenArtist(artist) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E2028))
                                    ) {
                                        AsyncImage(
                                            model = artist.highResAvatarUrl,
                                            contentDescription = artist.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = artist.name,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = artist.subscribers,
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4b. Top Playlists / User-Uploaded Community Playlists
        if (activeCategory == "All" && (communityPlaylists.isNotEmpty() || isLoading)) {
            item(key = "community_playlists_shelf") {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp).animateItem(fadeInSpec = tween(300, easing = EaseOutCubic), fadeOutSpec = tween(200, easing = EaseOutCubic), placementSpec = tween(300, easing = EaseOutCubic))) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top Playlists",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Community",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    if (communityPlaylists.isEmpty() && isLoading) {
                        com.auramusic.app.ui.components.PlaylistCarouselSkeleton()
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(communityPlaylists, key = { "ytm_pl_${it.id}" }) { playlist ->
                                Column(
                                    modifier = Modifier
                                        .width(135.dp)
                                        .clickable { onOpenPlaylist(playlist) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(135.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFF1E2028))
                                    ) {
                                        AsyncImage(
                                            model = playlist.highResArtworkUrl,
                                            contentDescription = playlist.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(topStart = 8.dp),
                                            color = Color.Black.copy(alpha = 0.70f),
                                            modifier = Modifier.align(Alignment.BottomEnd)
                                        ) {
                                            Text(
                                                text = playlist.trackCount,
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = playlist.title,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = playlist.author,
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4b. Recently Played Carousel (Real dynamically tracked history)
        if (visibleRecentlyPlayed.isNotEmpty()) {
            item(key = "recently_played_shelf") {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp).animateItem(fadeInSpec = tween(300, easing = EaseOutCubic), fadeOutSpec = tween(200, easing = EaseOutCubic), placementSpec = tween(300, easing = EaseOutCubic))) {
                    Text(
                        text = "Recently Played",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(visibleRecentlyPlayed.take(15), key = { "recent_${it.videoId}" }) { track ->
                            val isTrackCurrent = currentPlayingId == track.videoId
                            Column(
                                modifier = Modifier
                                    .width(125.dp)
                                    .clickable { onPlayTrack(track, visibleRecentlyPlayed) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(125.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(AuraPillInactive)
                                ) {
                                    AsyncImage(
                                        model = track.highResArtworkUrl,
                                        contentDescription = track.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (isTrackCurrent) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.55f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isPlaying) {
                                                AudioPlayingIndicator(
                                                    modifier = Modifier.fillMaxSize(0.60f),
                                                    color = AuraNeonLime
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Rounded.PlayArrow,
                                                    contentDescription = null,
                                                    tint = AuraNeonLime,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = track.title,
                                    color = if (isTrackCurrent) AuraNeonLime else Color.White,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = track.artist,
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4c. Your Playlists Carousel (User & Imported Spotify Playlists)
        if (visibleUserPlaylists.isNotEmpty()) {
            item(key = "user_playlists_shelf") {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp).animateItem(fadeInSpec = tween(300, easing = EaseOutCubic), fadeOutSpec = tween(200, easing = EaseOutCubic), placementSpec = tween(300, easing = EaseOutCubic))) {
                    Text(
                        text = "Your Playlists",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (visibleUserPlaylists.size >= 4) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            visibleUserPlaylists.chunked(2).forEach { rowPlaylists ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowPlaylists.forEach { pl ->
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(68.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF242529))
                                                .clickable { onOpenUserPlaylist(pl) }
                                                .padding(end = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            com.auramusic.app.ui.components.AuraPlaylistCover(
                                                coverUrl = pl.coverUrl,
                                                tracks = pl.tracks,
                                                modifier = Modifier.size(68.dp),
                                                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                                            )
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(start = 9.dp)
                                            ) {
                                                Text(
                                                    text = pl.name,
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "${pl.tracks.size} songs",
                                                    color = TextSecondary,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                    if (rowPlaylists.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(visibleUserPlaylists, key = { "pl_${it.id}" }) { pl ->
                                Column(
                                    modifier = Modifier
                                        .width(125.dp)
                                        .clickable { onOpenUserPlaylist(pl) }
                                ) {
                                    com.auramusic.app.ui.components.AuraPlaylistCover(
                                        coverUrl = pl.coverUrl,
                                        tracks = pl.tracks,
                                        modifier = Modifier.size(125.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = pl.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${pl.tracks.size} songs",
                                        color = TextSecondary,
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (albumRecommendations.isNotEmpty() || (activeCategory != "All" && isCategoryLoading)) {
            item(key = "albums_shelf") {
                Box(modifier = Modifier.animateItem(fadeInSpec = tween(300, easing = EaseOutCubic), fadeOutSpec = tween(200, easing = EaseOutCubic), placementSpec = tween(300, easing = EaseOutCubic))) {
                    Crossfade(
                        targetState = Triple(isCategoryLoading, activeCategory, albumRecommendations),
                        animationSpec = tween(durationMillis = 320, easing = EaseOutCubic),
                        label = "albumsCategoryTransition"
                    ) { (loading, cat, albumsForState) ->
                        if (albumsForState.isEmpty() && loading) {
                            Column(Modifier.fillMaxWidth().padding(bottom = 22.dp)) {
                                Text(
                                    text = "$cat albums",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 21.sp,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                                Spacer(Modifier.height(14.dp))
                                com.auramusic.app.ui.components.AlbumCarouselSkeleton()
                            }
                        } else {
                            HomeRecommendationShelf(
                                title = if (cat == "All") "Albums for you" else "$cat albums",
                                tracks = albumsForState,
                                currentPlayingId = currentPlayingId,
                                isPlaying = isPlaying,
                                onPlayTrack = onPlayTrack,
                                titleForTrack = { track ->
                                    track.album.takeIf { it.isNotBlank() && !it.equals("YouTube Music", true) }
                                        ?: track.title
                                },
                                keyPrefix = "album_pick"
                            )
                        }
                    }
                }
            }
        }

        // 5. Section: Top Tracks / Category Query Results
        if (activeCategory == "All" || (!isCategoryLoading && displayedTracks.isNotEmpty())) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = if (activeCategory == "All") "Top daily playlists" else "$activeCategory Tracks",
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220, easing = EaseOutCubic)) togetherWith
                            fadeOut(animationSpec = tween(160, easing = EaseOutCubic))
                        },
                        label = "bottomSectionTitle"
                    ) { titleText ->
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 6. Tracklist Items with Badges (LYRICS / E) - Paginated to avoid rate limits
            if (activeCategory == "All" && isLoading && displayedTracks.isEmpty()) {
                items(6) {
                    com.auramusic.app.ui.components.TrackItemSkeleton()
                }
            } else {
                val currentBatch = displayedTracks.take(visibleTrackCount)
                items(currentBatch, key = { it.videoId }) { track ->
                    val isFav = favorites.any { it.videoId == track.videoId }
                    val isDown = downloads.any { it.videoId == track.videoId && it.isDownloaded }
                    val isDownloading = downloadingIds.contains(track.videoId)
                    val isCurrent = currentPlayingId == track.videoId

                    SpotifyTrackRowItem(
                        track = track,
                        isCurrent = isCurrent,
                        isFavorite = isFav,
                        isDownloaded = isDown,
                        isDownloading = isDownloading,
                        downloadProgress = downloadProgressMap[track.videoId],
                        isPlaying = isPlaying,
                        onClick = { onPlayTrack(track, displayedTracks) },
                        onToggleFavorite = { onToggleFavorite(track) },
                        onDownload = { onDownloadTrack(track) },
                        onOptionsClick = { selectedTrackForOptions = track }
                    )
                }
            }

            // Pagination "See More" Button
            if (visibleTrackCount < displayedTracks.size) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFF1E2028),
                            border = BorderStroke(1.dp, Color(0xFF2E323D)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .clickable {
                                    visibleTrackCount = (visibleTrackCount + 20).coerceAtMost(displayedTracks.size)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowDownward,
                                    contentDescription = "See More",
                                    tint = AuraNeonLime,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "See More (${displayedTracks.size - visibleTrackCount} more tracks)",
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
        }

        // 3. Unified Top Header (Pinned at Alignment.TopCenter)
        // Keeps a single persistent LazyRow instance across "All" and tapped categories for seamless transitions.
        // When no category is tapped ("All"), greeting and pills scroll away with the list and only the dark Android status bar stays sticky.
        // When a category is tapped, greeting collapses upward while mood pills stay pinned below the status bar and darken to solid black.
        val isAllCategory = activeCategory == "All"
        val headerBgAlpha by animateFloatAsState(
            targetValue = if (isAllCategory) 0f else darkFraction,
            animationSpec = tween(durationMillis = 260, easing = EaseOutCubic),
            label = "headerBgAlpha"
        )
        val effectiveHeaderHeight = if (isAllCategory) {
            (uncollapsedHeaderHeight - with(LocalDensity.current) { scrollOffsetPx.toDp() }).coerceAtLeast(0.dp)
        } else {
            currentHeaderHeight
        }

        if (effectiveHeaderHeight > 0.dp) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(effectiveHeaderHeight)
                    .background(Color(0xFF080A12).copy(alpha = headerBgAlpha))
                    .scrollable(state = listState, orientation = Orientation.Vertical)
            ) {
                // AURA X branding row
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = statusBarTop + 8.dp, start = 20.dp, end = 20.dp)
                    .height(brandRowHeightDp)
                    .graphicsLayer {
                        alpha = if (isAllCategory) (1f - collapseProgress).coerceIn(0f, 1f) else 0f
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "AURA X logo",
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AURA X",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Greeting text: full unclipped height, smoothly scrolls up into transparent status bar
                val greetingTranslationY = if (isAllCategory) -scrollOffsetPx else (-collapseProgress * collapseRangePx)
                val greetingAlpha = if (isAllCategory) {
                    (1f - (scrollOffsetPx / uncollapsedHeaderHeightPx.coerceAtLeast(1f)) * 1.4f).coerceIn(0f, 1f)
                } else {
                    (1f - collapseProgress * 1.15f).coerceIn(0f, 1f)
                }
                if (greetingAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = statusBarTop + brandRowHeightDp + brandRowSpacingDp, start = 20.dp, end = 20.dp)
                            .graphicsLayer {
                                translationY = greetingTranslationY
                                alpha = greetingAlpha
                            }
                    ) {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                    }
                }

                // Mood category pills: in "All", scrolls away with the list; when tapped, pins at statusBarTop
                val pillsTopPadding = if (isAllCategory) {
                    statusBarTop + collapseRangeDp
                } else {
                    statusBarTop + (collapseRangeDp * (1f - collapseProgress))
                }
                val pillsTranslationY = if (isAllCategory) -scrollOffsetPx else 0f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = pillsTopPadding)
                        .height(moodPillsHeightDp)
                        .graphicsLayer {
                            translationY = pillsTranslationY
                        }
                ) {
                    LazyRow(
                        state = moodRowState,
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(YtmMoodBrowseParams.ALL_MOODS) { filter ->
                            AuraMoodCategoryPill(
                                filter = filter,
                                isSelected = activeCategory == filter,
                                isLoading = isCategoryLoading && (activeCategory == filter),
                                hasMoodBackground = moodBgUrl != null,
                                isHeaderDark = isAllCategory || (moodBgUrl == null) || (darkFraction > 0.30f),
                                onClick = { onSelectCategory(filter) }
                            )
                        }
                    }
                }
            }
        }

        // Dark Android status bar sticky header when no category is tapped ("All")
        if (isAllCategory) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(statusBarTop)
                    .background(Color(0xFF080A12))
            )
        }
        }
    }

    // Track Options Modal (Play Next, Add to Queue, Liked Songs, Add to Playlist, Go to Artist)
    selectedTrackForOptions?.let { modalTrack ->
        AuraTrackOptionsModal(
            track = modalTrack,
            isFavorite = favorites.any { it.videoId == modalTrack.videoId },
            onDismiss = { selectedTrackForOptions = null },
            onPlayNext = {
                AuraPlayerManager.playNext(modalTrack)
                Toast.makeText(context, "Playing next: ${modalTrack.title}", Toast.LENGTH_SHORT).show()
                selectedTrackForOptions = null
            },
            onAddToQueue = {
                AuraPlayerManager.enqueueTrack(modalTrack)
                Toast.makeText(context, "Added to queue: ${modalTrack.title}", Toast.LENGTH_SHORT).show()
                selectedTrackForOptions = null
            },
            onToggleFavorite = {
                val wasFav = favorites.any { it.videoId == modalTrack.videoId }
                onToggleFavorite(modalTrack)
                val msg = if (wasFav) "Removed from Liked Songs" else "Added to Liked Songs"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                selectedTrackForOptions = null
            },
            onAddToPlaylist = {
                val t = modalTrack
                selectedTrackForOptions = null
                trackForAddToPlaylist = t
            },
            onGoToArtist = {
                selectedTrackForOptions = null
                onOpenArtistByName(modalTrack.artist, modalTrack.title)
            }
        )
    }

    // Modal: Add Song to Playlist
    trackForAddToPlaylist?.let { trackToAdd ->
        AuraAddToPlaylistModal(
            track = trackToAdd,
            playlists = userPlaylists,
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
 * 2x3 Quick Access Grid Card (Matching Spotify / Aura Music design)
 */
@Composable
private fun QuickAccessCard(
    track: AuraTrack,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = SpotifyCard,
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(BorderStroke(1.dp, Color(0x4068DFFF)), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = track.highResArtworkUrl,
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
            )

            Text(
                text = track.title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(AuraNeonLime),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

/**
 * Editorial Featured Release Hero Card (PARADISE)
 */
@Composable
private fun FeaturedReleaseHeroCard(
    track: AuraTrack,
    isPlaying: Boolean,
    isFavorite: Boolean,
    isDownloaded: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownload: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SpotifyHeroCard,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main Top Row: Square Artwork + Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = track.highResArtworkUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(108.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FEATURED RELEASE",
                        style = MaterialTheme.typography.labelSmall,
                        color = AuraSageGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${track.artist} • ${track.genre}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Fidelity spec: Ultra HD • 320 KBPS
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Audio Quality",
                            tint = AuraSageGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ultra HD • 320 KBPS",
                            color = AuraSageGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Actions Row: Checkmark, Download, Three Dots + Play Button FAB
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.CheckCircle else Icons.Rounded.CheckCircleOutline,
                            contentDescription = "In Library",
                            tint = if (isFavorite) AuraNeonLime else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowCircleDown,
                            contentDescription = "Download for offline",
                            tint = if (isDownloaded) AuraNeonLime else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "More options",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Signature Large Play Action Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AuraNeonLime)
                        .clickable(onClick = onPlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

/**
 * Pastel Lilac "Discover weekly" Curated & Trending Hero Card (Screen 1)
 */
@Composable
private fun DiscoverWeeklyHeroCard(
    track: AuraTrack?,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownload: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = AuraLilacCard,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(165.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .padding(start = 20.dp, top = 18.dp, bottom = 18.dp, end = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Discover weekly",
                        color = AuraLilacDarkText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The original slow instrumental best playlists.",
                        color = Color(0xFF3E3952),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AuraLilacButton)
                            .clickable(onClick = onPlay),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = AuraLilacDarkText,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(onClick = onDownload, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = "Download",
                            tint = AuraLilacDarkText,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(onClick = { }, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "More",
                            tint = AuraLilacDarkText,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Right side artwork / model portrait
            AsyncImage(
                model = track?.highResArtworkUrl
                    ?: "https://images.unsplash.com/photo-1516280440614-37939bbacd81?w=400&auto=format&fit=crop&q=80",
                contentDescription = "Discover Weekly Artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(0.85f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
            )
        }
    }
}

/**
 * Modern Encore tracklist row item with 16dp rounded artwork and circular play button (Screens 1 & 3)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpotifyTrackRowItem(
    track: AuraTrack,
    isCurrent: Boolean,
    isFavorite: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float? = null,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownload: () -> Unit,
    onOptionsClick: (() -> Unit)? = null
) {
    val isTrackPlaying = isCurrent && isPlaying
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onOptionsClick?.invoke() }
            )
            .padding(horizontal = 20.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF202127)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = track.highResArtworkUrl.ifBlank { "https://i.ytimg.com/vi/${track.videoId}/hqdefault.jpg" },
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (isTrackPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    AudioPlayingIndicator(
                        modifier = Modifier.fillMaxSize(0.70f),
                        color = AuraNeonLime
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isCurrent) AuraNeonLime else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            if (isDownloading) {
                val pct = (((downloadProgress ?: 0.02f) * 100).toInt()).coerceIn(1, 100)
                Text(
                    text = "Downloading • $pct%",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraNeonLime,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (downloadProgress ?: 0.02f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = AuraNeonLime,
                    trackColor = Color(0x33FFFFFF)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDownloaded) {
                        Icon(
                            imageVector = Icons.Rounded.DownloadDone,
                            contentDescription = "Downloaded",
                            tint = AuraNeonLime,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "By ${track.artist} • ${track.durationSeconds / 60}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = { onOptionsClick?.invoke() },
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options for ${track.title}",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HomePlayAllButton(onClick: () -> Unit) {
    Text(
        text = "Play all",
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 8.dp)
    )
}

fun formatDuration(seconds: Int): String {
    val mins = (seconds / 60).coerceAtLeast(0)
    val secs = (seconds % 60).coerceAtLeast(0)
    return "%d:%02d".format(mins, secs)
}

@Composable
private fun HomeRecommendationShelf(
    title: String,
    tracks: List<AuraTrack>,
    currentPlayingId: String?,
    isPlaying: Boolean,
    onPlayTrack: (AuraTrack, List<AuraTrack>) -> Unit,
    titleForTrack: (AuraTrack) -> String = { it.title },
    keyPrefix: String
) {
    if (tracks.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
            HomePlayAllButton { tracks.firstOrNull()?.let { onPlayTrack(it, tracks) } }
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tracks, key = { "${keyPrefix}_${it.videoId}" }) { track ->
                val isCurrent = currentPlayingId == track.videoId
                Column(
                    modifier = Modifier
                        .width(132.dp)
                        .clickable { onPlayTrack(track, tracks) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(132.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF202127))
                    ) {
                        AsyncImage(
                            model = track.highResArtworkUrl,
                            contentDescription = titleForTrack(track),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isCurrent) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = AuraNeonLime,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = titleForTrack(track),
                        color = if (isCurrent) AuraNeonLime else Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun AuraMoodCategoryPill(
    filter: String,
    isSelected: Boolean,
    isLoading: Boolean,
    hasMoodBackground: Boolean,
    isHeaderDark: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150, easing = EaseOutCubic),
        label = "pillPressScale"
    )

    // Un-tapped pill fill: Gray (#353945) when sticky header is dark; translucent dark over mood image at resting state
    val unselectedFill = if (isHeaderDark) {
        Color(0xFF171B2A) // Modern gray fill
    } else {
        if (hasMoodBackground) Color.Black.copy(alpha = 0.45f) else Color(0xFF171B2A)
    }

    val pillBgColor by animateColorAsState(
        targetValue = if (isSelected) AuraNeonLime else unselectedFill,
        animationSpec = tween(durationMillis = 240, easing = EaseOutCubic),
        label = "pillBgColor"
    )
    val pillTextColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else Color.White,
        animationSpec = tween(durationMillis = 240, easing = EaseOutCubic),
        label = "pillTextColor"
    )

    // SimpMusic Infinite rotating sweep border when loading and selected:
    val showLoadingBorder = isSelected && isLoading
    val borderAlpha by animateFloatAsState(
        targetValue = if (showLoadingBorder) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = EaseOutCubic),
        label = "borderAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "PillBorderRotation")
    val rotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PillBorderDegrees"
    )

    val borderBrush = remember {
        Brush.sweepGradient(
            listOf(
                Color.Transparent,
                AuraNeonLime,
                AuraViolet,
                Color.White,
                AuraNeonLime,
                Color.Transparent
            )
        )
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .drawBehind {
                if (borderAlpha > 0.01f) {
                    rotate(rotationDegrees) {
                        drawCircle(
                            brush = borderBrush,
                            radius = size.maxDimension / 2f,
                            alpha = borderAlpha,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
            .background(pillBgColor, CircleShape)
            .padding(horizontal = 16.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = filter,
                color = pillTextColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
            if (showLoadingBorder) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    color = AuraNeonLime,
                    strokeWidth = 1.5.dp
                )
            }
        }
    }
}
