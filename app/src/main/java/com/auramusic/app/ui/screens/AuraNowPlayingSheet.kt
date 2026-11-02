package com.auramusic.app.ui.screens

import android.content.Context
import android.media.AudioManager
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import java.util.Collections
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import coil.compose.AsyncImage
import com.auramusic.app.core.lyrics.SimpMusicLyricsRepository
import com.auramusic.app.core.media.AuraPlayerManager
import com.auramusic.app.domain.model.AuraPalette
import com.auramusic.app.domain.model.AuraPlaylist
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.LyricsData
import com.auramusic.app.domain.model.NowPlayingStyle
import com.auramusic.app.domain.model.PlaybackMode
import com.auramusic.app.ui.components.AuraAddToPlaylistModal
import com.auramusic.app.ui.components.AuraCreatePlaylistModal
import com.auramusic.app.ui.theme.AuraCircleButtonBg
import com.auramusic.app.ui.theme.AuraNeonLime
import com.auramusic.app.ui.theme.AuraPlayGreen
import com.auramusic.app.ui.theme.SpotifyCard
import com.auramusic.app.ui.theme.SpotifyPitchBlack
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary

private inline fun lerpFloat(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraNowPlayingSheet(
    track: AuraTrack,
    isPlaying: Boolean,
    isBuffering: Boolean,
    positionSec: Float,
    durationSec: Float,
    palette: AuraPalette,
    lyrics: LyricsData?,
    lyricsOffsetMs: Int,
    showRomanized: Boolean,
    isFavorite: Boolean,
    isShuffleEnabled: Boolean,
    queue: List<AuraTrack>,
    currentStyle: NowPlayingStyle,
    isVideoMode: Boolean = false,
    isVideoLoading: Boolean = false,
    livePerformances: List<AuraTrack> = emptyList(),
    isLoadingLivePerformances: Boolean = false,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleStyle: () -> Unit,
    onToggleVideoMode: () -> Unit = {},
    onLoadLivePerformances: () -> Unit = {},
    onPlayLivePerformance: (AuraTrack) -> Unit = {},
    onOpenArtist: (String) -> Unit = {},
    onPlayQueueTrack: (AuraTrack) -> Unit,
    onAdjustLyricsOffset: (Int) -> Unit,
    onToggleRomanized: () -> Unit,
    isDownloaded: Boolean = false,
    isDownloading: Boolean = false,
    onDownloadTrack: ((AuraTrack) -> Unit)? = null,
    onRemoveDownload: ((AuraTrack) -> Unit)? = null,
    playlists: List<AuraPlaylist> = emptyList(),
    onAddTrackToPlaylist: (String, AuraTrack) -> Unit = { _, _ -> },
    onCreatePlaylist: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var isLyricsExpanded by remember { mutableStateOf(false) }
    var lyricsCardBounds by remember { mutableStateOf<Rect?>(null) }
    var isQueueSheetVisible by remember { mutableStateOf(false) }
    var isOptionsModalVisible by remember { mutableStateOf(false) }
    var isLivePerformancesModalVisible by remember { mutableStateOf(false) }
    var isAddToPlaylistModalVisible by remember { mutableStateOf(false) }
    var isCreatePlaylistModalVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Always dismiss any lingering search input focus / soft keyboard when opening player or its modals
    LaunchedEffect(Unit, isQueueSheetVisible, isOptionsModalVisible, isLyricsExpanded, isLivePerformancesModalVisible, isAddToPlaylistModalVisible, isCreatePlaylistModalVisible) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    // Intercept back gesture / button to smoothly close open sheets / modals
    BackHandler(enabled = isLyricsExpanded || isLivePerformancesModalVisible || isOptionsModalVisible || isQueueSheetVisible || isAddToPlaylistModalVisible || isCreatePlaylistModalVisible) {
        when {
            isCreatePlaylistModalVisible -> isCreatePlaylistModalVisible = false
            isAddToPlaylistModalVisible -> isAddToPlaylistModalVisible = false
            isLivePerformancesModalVisible -> isLivePerformancesModalVisible = false
            isOptionsModalVisible -> isOptionsModalVisible = false
            isQueueSheetVisible -> isQueueSheetVisible = false
            isLyricsExpanded -> isLyricsExpanded = false
            else -> onCollapse()
        }
    }

    // Root container matching sheet bounds - 100% solid background so underlying screens never bleed through
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14151B))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // Intercepts all clicks so background screens (search field, home items) are never touched
            )
    ) {
        val fullHeightPx = constraints.maxHeight.toFloat()

        // Lyrics expansion progress: 0f = collapsed on card, 1f = fully expanded
        val lyricsExpansionProgress by animateFloatAsState(
            targetValue = if (isLyricsExpanded) 1f else 0f,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "lyricsExpansionProgress"
        )

        // Player view wrapper: stays anchored in place, subtly scaling without alpha transparency
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val scale = 0.94f + (0.06f * (1f - lyricsExpansionProgress))
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            Crossfade(
                targetState = currentStyle,
                label = "NowPlayingStyleCrossfade",
                animationSpec = tween(350)
            ) { style ->
                when (style) {
                    NowPlayingStyle.SPOTIFY -> {
                        SpotifyEncorePlayerView(
                            track = track,
                            isPlaying = isPlaying,
                            isBuffering = isBuffering,
                            positionSec = positionSec,
                            durationSec = durationSec,
                            palette = palette,
                            lyrics = lyrics,
                            lyricsOffsetMs = lyricsOffsetMs,
                            isFavorite = isFavorite,
                            isShuffleEnabled = isShuffleEnabled,
                            lyricsExpansionProgress = lyricsExpansionProgress,
                            isVideoMode = isVideoMode,
                            isVideoLoading = isVideoLoading,
                            onCollapse = onCollapse,
                            onTogglePlayPause = onTogglePlayPause,
                            onSeekTo = onSeekTo,
                            onSkipNext = onSkipNext,
                            onSkipPrevious = onSkipPrevious,
                            onToggleFavorite = onToggleFavorite,
                            onToggleShuffle = onToggleShuffle,
                            onToggleVideoMode = onToggleVideoMode,
                            onOpenArtist = {
                                onCollapse()
                                onOpenArtist(track.artist)
                            },
                            onOptionsClick = { isOptionsModalVisible = true },
                            onOpenLyrics = { isLyricsExpanded = true },
                            onOpenQueue = { isQueueSheetVisible = true },
                            onCardPositioned = { lyricsCardBounds = it }
                        )
                    }
                    NowPlayingStyle.LIQUID_GLASS -> {
                        LiquidGlassPlayerView(
                            track = track,
                            isPlaying = isPlaying,
                            isBuffering = isBuffering,
                            positionSec = positionSec,
                            durationSec = durationSec,
                            isFavorite = isFavorite,
                            isShuffleEnabled = isShuffleEnabled,
                            isVideoMode = isVideoMode,
                            isVideoLoading = isVideoLoading,
                            onCollapse = onCollapse,
                            onTogglePlayPause = onTogglePlayPause,
                            onSeekTo = onSeekTo,
                            onSkipNext = onSkipNext,
                            onSkipPrevious = onSkipPrevious,
                            onToggleFavorite = onToggleFavorite,
                            onToggleShuffle = onToggleShuffle,
                            onToggleVideoMode = onToggleVideoMode,
                            onOpenArtist = {
                                onCollapse()
                                onOpenArtist(track.artist)
                            },
                            onOptionsClick = { isOptionsModalVisible = true },
                            onOpenLyrics = { isLyricsExpanded = true },
                            onOpenQueue = { isQueueSheetVisible = true },
                            onCardPositioned = { lyricsCardBounds = it }
                        )
                    }
                }
            }
        }

        // Dark dimming scrim over player view when lyrics is expanding/expanded
        if (lyricsExpansionProgress > 0.001f) {
            val scrimAlpha = (0.50f * lyricsExpansionProgress).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
            )
        }

        // Full Screen Song Lyrics Screen:
        // Shared Element Transition (Hero animation / FLIP morphing):
        // Automatically tracks the exact physical position, width, and height of the lyrics card.
        // Opens by expanding fluidly outwards from the card's exact geometry.
        // Closes by morphing and physically traveling back into its original slot on the screen!
        if (lyricsExpansionProgress > 0.001f) {
            val fullWidthPx = constraints.maxWidth.toFloat()
            val density = LocalDensity.current

            val fallbackMargin = with(density) { 20.dp.toPx() }
            val fallbackWidth = fullWidthPx - (fallbackMargin * 2f)
            val fallbackHeight = with(density) { 280.dp.toPx() }
            val fallbackTop = fullHeightPx - with(density) { 340.dp.toPx() }

            val startLeft = lyricsCardBounds?.left ?: fallbackMargin
            val startTop = lyricsCardBounds?.top ?: fallbackTop
            val startWidth = lyricsCardBounds?.width?.takeIf { it > 10f } ?: fallbackWidth
            val startHeight = lyricsCardBounds?.height?.takeIf { it > 10f } ?: fallbackHeight

            // Spring overshoots past 1.0f on arrival; clamp progress strictly for geometry and corner radii
            val clampedProgress = lyricsExpansionProgress.coerceIn(0f, 1f)

            val currentLeft = lerpFloat(startLeft, 0f, clampedProgress)
            val currentTop = lerpFloat(startTop, 0f, clampedProgress)
            val currentWidth = lerpFloat(startWidth, fullWidthPx, clampedProgress).coerceAtLeast(10f)
            val currentHeight = lerpFloat(startHeight, fullHeightPx, clampedProgress).coerceAtLeast(10f)

            val currentLeftDp = with(density) { currentLeft.toDp() }
            val currentTopDp = with(density) { currentTop.toDp() }
            val currentWidthDp = with(density) { currentWidth.toDp() }
            val currentHeightDp = with(density) { currentHeight.toDp() }
            val currentCornerRadius = lerpFloat(20f, 0f, clampedProgress).coerceIn(0f, 20f)
            val currentElevation = (16.dp * (1f - clampedProgress)).coerceAtLeast(0.dp)

            Box(
                modifier = Modifier
                    .offset(x = currentLeftDp, y = currentTopDp)
                    .size(width = currentWidthDp, height = currentHeightDp)
                    .clip(RoundedCornerShape(currentCornerRadius.dp))
                    .background(palette.primary)
                    .shadow(
                        elevation = currentElevation,
                        shape = RoundedCornerShape(currentCornerRadius.dp)
                    )
            ) {
                // When expanding or collapsing, FullScreenSongLyricsView renders inside the clipped container
                // with 100% natural font size (scaleX = 1f, scaleY = 1f, ZERO squashing or text compression!)
                // Player controls are completely hidden when shrinking back to the lyrics card slot
                FullScreenSongLyricsView(
                    track = track,
                    lyrics = lyrics,
                    positionSec = positionSec + (lyricsOffsetMs / 1000f),
                    durationSec = durationSec,
                    isPlaying = isPlaying,
                    isBuffering = isBuffering,
                    lyricsOffsetMs = lyricsOffsetMs,
                    showRomanized = showRomanized,
                    palette = palette,
                    showPlayerControls = isLyricsExpanded && lyricsExpansionProgress >= 0.90f,
                    lyricsExpansionProgress = lyricsExpansionProgress,
                    onClose = { isLyricsExpanded = false },
                    onSeekTo = onSeekTo,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipPrevious = onSkipPrevious,
                    onSkipNext = onSkipNext,
                    onOpenQueue = { isQueueSheetVisible = true },
                    onAdjustLyricsOffset = onAdjustLyricsOffset,
                    onToggleRomanized = onToggleRomanized
                )

                // Smooth seamless card preview blend when close to the bottom slot
                // Matches the card's exact 16.sp font size and layout without any jump
                if (lyricsExpansionProgress < 0.35f) {
                    val previewFadeAlpha = (1f - (lyricsExpansionProgress / 0.35f)).coerceIn(0f, 1f)
                    Surface(
                        color = palette.primary,
                        shape = RoundedCornerShape(currentCornerRadius.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = previewFadeAlpha }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lyrics",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.35f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.OpenInFull,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val lines = lyrics?.syncedLines ?: emptyList()
                            val effectiveSec = positionSec + (lyricsOffsetMs / 1000f)
                            val activeIdx = lines.indexOfLast { effectiveSec >= it.timeSec }.coerceAtLeast(0)

                            if (lines.isEmpty()) {
                                Text(
                                    text = SimpMusicLyricsRepository.unescapeLyricsHtml(
                                        lyrics?.plainLyrics?.take(180)?.ifBlank { "Tap to view lyrics" } ?: "Tap to view lyrics"
                                    ),
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                val current = lines.getOrNull(activeIdx)
                                if (current != null) {
                                    Text(
                                        text = SimpMusicLyricsRepository.unescapeLyricsHtml(current.text),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        lineHeight = 26.sp
                                    )
                                }
                                val next1 = lines.getOrNull(activeIdx + 1)
                                if (next1 != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = SimpMusicLyricsRepository.unescapeLyricsHtml(next1.text),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.60f),
                                        lineHeight = 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Harmonized dark container color subtly tinted with dominant lyrics card palette color
        val modalContainerColor = remember(palette.primary) {
            lerp(Color(0xFF14151B), palette.primary, 0.18f)
        }

        // 3-Dot Options Bottom Sheet Modal
        if (isOptionsModalVisible) {
            NowPlayingOptionsModal(
                track = track,
                currentStyle = currentStyle,
                isFavorite = isFavorite,
                isVideoMode = isVideoMode,
                modalContainerColor = modalContainerColor,
                onDismiss = { isOptionsModalVisible = false },
                onToggleVideoMode = {
                    isOptionsModalVisible = false
                    onToggleVideoMode()
                },
                onOpenLivePerformances = {
                    isOptionsModalVisible = false
                    onLoadLivePerformances()
                    isLivePerformancesModalVisible = true
                },
                onToggleTheme = {
                    isOptionsModalVisible = false
                    onToggleStyle()
                },
                onGoToArtist = {
                    isOptionsModalVisible = false
                    onCollapse()
                    onOpenArtist(track.artist)
                },
                onToggleFavorite = {
                    onToggleFavorite()
                    isOptionsModalVisible = false
                },
                onAddToPlaylist = {
                    isOptionsModalVisible = false
                    isAddToPlaylistModalVisible = true
                },
                onPlayNext = {
                    AuraPlayerManager.playNext(track)
                    isOptionsModalVisible = false
                },
                onAddToQueue = {
                    AuraPlayerManager.enqueueTrack(track)
                    isOptionsModalVisible = false
                },
                onOpenQueue = {
                    isOptionsModalVisible = false
                    isQueueSheetVisible = true
                },
                onOpenLyrics = {
                    isOptionsModalVisible = false
                    isLyricsExpanded = true
                },
                isDownloaded = isDownloaded,
                isDownloading = isDownloading,
                onDownloadTrack = onDownloadTrack?.let { callback -> { isOptionsModalVisible = false; callback(track) } },
                onRemoveDownload = onRemoveDownload?.let { callback -> { isOptionsModalVisible = false; callback(track) } }
            )
        }

        // Modal: Add Current Song to Playlist
        if (isAddToPlaylistModalVisible) {
            AuraAddToPlaylistModal(
                track = track,
                playlists = playlists,
                onDismiss = { isAddToPlaylistModalVisible = false },
                onSelectPlaylist = { targetPl ->
                    onAddTrackToPlaylist(targetPl.id, track)
                    isAddToPlaylistModalVisible = false
                    Toast.makeText(context, "Added '${track.title}' to '${targetPl.name}'", Toast.LENGTH_SHORT).show()
                },
                onCreateNewPlaylist = {
                    isAddToPlaylistModalVisible = false
                    isCreatePlaylistModalVisible = true
                }
            )
        }

        // Modal: Create Playlist from Now Playing Sheet
        if (isCreatePlaylistModalVisible) {
            AuraCreatePlaylistModal(
                onDismiss = { isCreatePlaylistModalVisible = false },
                onCreate = { title, desc, coverUri ->
                    isCreatePlaylistModalVisible = false
                    onCreatePlaylist(title, desc, coverUri)
                    Toast.makeText(context, "Created playlist: $title", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Live Performances & Alternate Versions Bottom Sheet Modal
        if (isLivePerformancesModalVisible) {
            LivePerformancesModal(
                track = track,
                liveTracks = livePerformances,
                isLoading = isLoadingLivePerformances,
                modalContainerColor = modalContainerColor,
                onDismiss = { isLivePerformancesModalVisible = false },
                onSelectTrack = {
                    isLivePerformancesModalVisible = false
                    onPlayLivePerformance(it)
                }
            )
        }

        // Upcoming Queue Mobile Bottom Sheet Modal
        if (isQueueSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { isQueueSheetVisible = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = modalContainerColor,
                contentColor = TextPrimary,
                dragHandle = {
                    Surface(
                        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Spacer(modifier = Modifier.size(width = 36.dp, height = 4.dp))
                    }
                }
            ) {
                com.auramusic.app.ui.components.ForceWhiteSystemBarsEffect()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.55f)
                ) {
                    QueueSheetContent(
                        currentTrack = track,
                        queue = queue,
                        modalContainerColor = modalContainerColor,
                        onPlayTrack = {
                            onPlayQueueTrack(it)
                            isQueueSheetVisible = false
                        },
                        onReorderQueue = { newUpcoming ->
                            AuraPlayerManager.updateUpcomingQueue(newUpcoming)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 3-Dot Options Bottom Sheet Modal on Full Music Player
 * Unified options with theme toggling, go to artist, liked songs, queue actions, and lyrics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingOptionsModal(
    track: AuraTrack,
    currentStyle: NowPlayingStyle,
    isFavorite: Boolean,
    isVideoMode: Boolean = false,
    modalContainerColor: Color,
    onDismiss: () -> Unit,
    onToggleVideoMode: () -> Unit,
    onOpenLivePerformances: () -> Unit,
    onToggleTheme: () -> Unit,
    onGoToArtist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit = {},
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onOpenQueue: () -> Unit = {},
    onOpenLyrics: () -> Unit = {},
    isDownloaded: Boolean = false,
    isDownloading: Boolean = false,
    onDownloadTrack: (() -> Unit)? = null,
    onRemoveDownload: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = modalContainerColor,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 10.dp),
                color = Color.White.copy(alpha = 0.20f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Spacer(modifier = Modifier.size(width = 36.dp, height = 4.dp))
            }
        }
    ) {
        com.auramusic.app.ui.components.ForceWhiteSystemBarsEffect()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 6.dp)
        ) {
            // Header preview
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                AsyncImage(
                    model = track.highResArtworkUrl,
                    contentDescription = track.title,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Video Player Mode Toggle
            NowPlayingModalOptionRow(
                icon = if (isVideoMode) Icons.Rounded.Audiotrack else Icons.Rounded.Videocam,
                label = if (isVideoMode) "Switch to Audio Mode" else "Watch Official Video",
                onClick = onToggleVideoMode
            )

            // 2. Download / Remove Download
            if (isDownloaded && onRemoveDownload != null) {
                NowPlayingModalOptionRow(
                    icon = Icons.Rounded.DeleteOutline,
                    label = "Remove from Downloads",
                    onClick = onRemoveDownload
                )
            } else if (onDownloadTrack != null) {
                NowPlayingModalOptionRow(
                    icon = if (isDownloaded) Icons.Rounded.DeleteOutline else Icons.Rounded.Download,
                    label = if (isDownloading) "Downloading…" else if (isDownloaded) "Remove from Downloads" else "Download Song",
                    onClick = {
                        if (isDownloaded && onRemoveDownload != null) {
                            onRemoveDownload()
                        } else {
                            onDownloadTrack()
                        }
                    }
                )
            }

            // 2. Live Performances & Alternate Videos
            NowPlayingModalOptionRow(
                icon = Icons.Rounded.LiveTv,
                label = "Live Performances & Alternate Versions",
                onClick = onOpenLivePerformances
            )

            // 3. Toggle Theme Option
            val nextThemeLabel = if (currentStyle == NowPlayingStyle.SPOTIFY) "Blur - Liquid Glass" else "Spotify Encore"
            NowPlayingModalOptionRow(
                icon = Icons.Rounded.Palette,
                label = "Switch Theme to $nextThemeLabel",
                onClick = onToggleTheme
            )

            // 4. Go to Artist Screen
            NowPlayingModalOptionRow(
                icon = Icons.Rounded.Person,
                label = "View Artist",
                onClick = onGoToArtist
            )

            // 5. Favorite / Liked Songs
            NowPlayingModalOptionRow(
                icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                label = if (isFavorite) "Remove from Liked Songs" else "Add to Liked Songs",
                onClick = onToggleFavorite
            )

            // 5b. Add to Playlist
            NowPlayingModalOptionRow(
                icon = Icons.Rounded.PlaylistAdd,
                label = "Add to Playlist",
                onClick = onAddToPlaylist
            )

            // 6. Play Next
            NowPlayingModalOptionRow(
                icon = Icons.AutoMirrored.Rounded.QueueMusic,
                label = "Play Next",
                onClick = onPlayNext
            )

            // 7. Add to Queue
            NowPlayingModalOptionRow(
                icon = Icons.Rounded.PlaylistAdd,
                label = "Add to Queue",
                onClick = onAddToQueue
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NowPlayingModalOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

/**
 * High-performance ExoPlayer video surface maximized to the sides when in Video Mode.
 */
@Composable
fun AuraVideoPlayerSurface(
    modifier: Modifier = Modifier,
    track: AuraTrack,
    isLoading: Boolean,
    onSwitchToAudio: () -> Unit
) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // High quality poster artwork underneath video view to eliminate cold black screens
        AsyncImage(
            model = track.highResArtworkUrl.ifBlank { "https://i.ytimg.com/vi/${track.videoId}/hqdefault.jpg" },
            contentDescription = track.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = com.auramusic.app.core.media.AuraPlayerManager.exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setKeepContentOnPlayerReset(true)
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                if (view.player != com.auramusic.app.core.media.AuraPlayerManager.exoPlayer) {
                    view.player = com.auramusic.app.core.media.AuraPlayerManager.exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator overlay while resolving video stream
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Loading Video…",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Mobile Bottom Sheet Modal presenting Live Concert Performances, Acoustic Sessions,
 * and Official Music Videos for the current track.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LivePerformancesModal(
    track: AuraTrack,
    liveTracks: List<AuraTrack>,
    isLoading: Boolean,
    modalContainerColor: Color,
    onDismiss: () -> Unit,
    onSelectTrack: (AuraTrack) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = modalContainerColor,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 10.dp),
                color = Color.White.copy(alpha = 0.20f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Spacer(modifier = Modifier.size(width = 36.dp, height = 4.dp))
            }
        }
    ) {
        com.auramusic.app.ui.components.ForceWhiteSystemBarsEffect()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 6.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LiveTv,
                    contentDescription = null,
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Live & Alternate Versions",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "${track.title} • ${track.artist}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = AuraNeonLime, modifier = Modifier.size(32.dp))
                        Text(
                            text = "Searching live concert stages & performances…",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (liveTracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No alternate live performances found for this song.",
                        color = TextSecondary,
                        fontSize = 13.5.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(liveTracks) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1B1E28))
                                .clickable {
                                    onDismiss()
                                    onSelectTrack(item)
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 84.dp, height = 52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF242735)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = item.highResArtworkUrl.ifBlank { "https://i.ytimg.com/vi/${item.videoId}/hqdefault.jpg" },
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = item.artist,
                                    color = TextSecondary,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                imageVector = Icons.Rounded.PlayCircle,
                                contentDescription = "Play",
                                tint = AuraNeonLime,
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sleek, slim music player timeline scrubber with a continuous active/inactive line
 * and a perfectly centered circular point thumb (no thick pill gaps, no stop indicator dots).
 */
@Composable
private fun CompactPointSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    pointColor: Color = Color.White,
    activeTrackColor: Color = Color.White,
    inactiveTrackColor: Color = Color.White.copy(alpha = 0.22f),
    trackHeight: Dp = 2.5.dp,
    thumbRadius: Dp = 5.dp
) {
    val totalRange = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.001f)
    val currentFraction = ((value - valueRange.start) / totalRange).coerceIn(0f, 1f)

    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val displayFraction = if (isDragging) dragFraction else currentFraction

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    val availableWidth = (size.width - 2 * thumbRadius.toPx()).coerceAtLeast(1f)
                    val newFraction = ((offset.x - thumbRadius.toPx()) / availableWidth).coerceIn(0f, 1f)
                    val newValue = valueRange.start + newFraction * totalRange
                    onValueChange(newValue)
                }
            }
            .pointerInput(valueRange) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val availableWidth = (size.width - 2 * thumbRadius.toPx()).coerceAtLeast(1f)
                        dragFraction = ((offset.x - thumbRadius.toPx()) / availableWidth).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isDragging = false
                        val finalValue = valueRange.start + dragFraction * totalRange
                        onValueChange(finalValue)
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val availableWidth = (size.width - 2 * thumbRadius.toPx()).coerceAtLeast(1f)
                        val newFraction = ((change.position.x - thumbRadius.toPx()) / availableWidth).coerceIn(0f, 1f)
                        dragFraction = newFraction
                        val intermediateValue = valueRange.start + newFraction * totalRange
                        onValueChange(intermediateValue)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        ) {
            val width = size.width
            val centerY = size.height / 2f
            val trackHeightPx = trackHeight.toPx()
            val thumbRadiusPx = thumbRadius.toPx()

            val startX = thumbRadiusPx
            val endX = width - thumbRadiusPx
            val trackSpan = (endX - startX).coerceAtLeast(1f)
            val thumbX = startX + displayFraction * trackSpan

            // 1. Inactive full-width background slim line
            drawLine(
                color = inactiveTrackColor,
                start = Offset(startX, centerY),
                end = Offset(endX, centerY),
                strokeWidth = trackHeightPx,
                cap = StrokeCap.Round
            )

            // 2. Active played slim line (continuous, no gap!)
            if (thumbX > startX) {
                drawLine(
                    color = activeTrackColor,
                    start = Offset(startX, centerY),
                    end = Offset(thumbX, centerY),
                    strokeWidth = trackHeightPx,
                    cap = StrokeCap.Round
                )
            }

            // 3. Circular point thumb (dead-center on the line, perfectly bisecting it)
            drawCircle(
                color = pointColor,
                radius = if (isDragging) thumbRadiusPx * 1.35f else thumbRadiusPx,
                center = Offset(thumbX, centerY)
            )
        }
    }
}

/**
 * 1. Blur Style - Liquid Glass Full Screen Player
 * Directly matches the reference image:
 * Maximized high-resolution artwork, ambient backdrop blur, frosted glass controls container,
 * solid Apple playback glyphs, AAC audio badge, and tactile horizontal point volume slider.
 */
@Composable
private fun LiquidGlassPlayerView(
    track: AuraTrack,
    isPlaying: Boolean,
    isBuffering: Boolean,
    positionSec: Float,
    durationSec: Float,
    isFavorite: Boolean,
    isShuffleEnabled: Boolean,
    lyricsExpansionProgress: Float = 0f,
    isVideoMode: Boolean = false,
    isVideoLoading: Boolean = false,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleVideoMode: () -> Unit = {},
    onOpenArtist: () -> Unit,
    onOptionsClick: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenQueue: () -> Unit,
    onCardPositioned: (Rect) -> Unit = {}
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVol = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }
    var systemVolume by remember {
        mutableFloatStateOf(
            audioManager?.let { it.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVol.coerceAtLeast(1) } ?: 0.65f
        )
    }

    // Fully opaque solid black background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Full-screen Ambient Backdrop Blur Layer using high-res artwork
        AsyncImage(
            model = track.highResArtworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 50.dp)
        )

        // Dark ambient overlay for contrast and legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.22f),
                            Color.Black.copy(alpha = 0.40f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar: Collapse, Playing from Header, and More Options Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    val cleanAlbum = track.album.trim()
                    val isValidSource = cleanAlbum.isNotBlank() &&
                        cleanAlbum != track.title &&
                        !cleanAlbum.matches(Regex("^\\d+:\\d{2}(:\\d{2})?$")) &&
                        !cleanAlbum.endsWith("views", ignoreCase = true)
                    val playingFrom = if (isValidSource) {
                        "Playing from $cleanAlbum"
                    } else {
                        "Playing from Playlist"
                    }
                    Text(
                        text = playingFrom,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onOptionsClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isVideoMode) {
                // Maximized edge-to-edge video player spanning the entire width preserving ambient blur backdrop
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AuraVideoPlayerSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp)),
                        track = track,
                        isLoading = isVideoLoading,
                        onSwitchToAudio = onToggleVideoMode
                    )
                }
            } else {
                // 2. High Quality Artwork with generous, balanced breathing room
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dynamically maximize size while fitting within available vertical & horizontal bounds
                    val artSize = minOf(maxHeight, maxWidth)

                    Box(
                        modifier = Modifier
                            .size(artSize)
                            .clip(RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = track.highResArtworkUrl,
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Soft bottom gradient mask on top of artwork for liquid blend into controls
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.20f),
                                            Color.Black.copy(alpha = 0.65f)
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            // 3. Lower Frosted Glass Controls Container
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.10f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    // Track Title & 3 Action Icons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = track.title.uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = track.artist.uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                letterSpacing = 0.6.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable(onClick = onOpenArtist)
                            )
                        }

                        // Right 3 Actions: (+) Add, (★) Star Favorite, (⋮) More
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(onClick = onOpenQueue, modifier = Modifier.size(34.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.AddCircleOutline,
                                    contentDescription = "Add to Queue",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(34.dp)) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) Color(0xFFFFB74D) else Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(onClick = onOptionsClick, modifier = Modifier.size(34.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "More",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Horizontal Point Scrubber Slider
                    CompactPointSlider(
                        value = positionSec.coerceIn(0f, durationSec.coerceAtLeast(1f)),
                        onValueChange = onSeekTo,
                        valueRange = 0f..durationSec.coerceAtLeast(1f),
                        pointColor = Color.White,
                        activeTrackColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Timestamps & Audio Codec Badge Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDuration(positionSec.toInt()),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.65f)
                        )

                        // Audio Codec Glass Pill: [ •||• AAC ]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                                ) {
                                    Box(modifier = Modifier.width(2.dp).height(7.dp).background(Color.White, RoundedCornerShape(1.dp)))
                                    Box(modifier = Modifier.width(2.dp).height(11.dp).background(Color.White, RoundedCornerShape(1.dp)))
                                    Box(modifier = Modifier.width(2.dp).height(5.dp).background(Color.White, RoundedCornerShape(1.dp)))
                                }
                                Text(
                                    text = track.codec.ifBlank { "AAC" },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Text(
                            text = "-" + formatDuration((durationSec - positionSec).toInt().coerceAtLeast(0)),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Solid Apple-style Playback Controls Row: Prev, Play/Pause, Next
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onSkipPrevious, modifier = Modifier.size(54.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous Track",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        IconButton(onClick = onTogglePlayPause, modifier = Modifier.size(62.dp)) {
                            if (isBuffering) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(32.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(46.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        IconButton(onClick = onSkipNext, modifier = Modifier.size(54.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Next Track",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Horizontal Point Volume Slider Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeDown,
                            contentDescription = "Volume Down",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        CompactPointSlider(
                            value = systemVolume,
                            onValueChange = { newVol ->
                                systemVolume = newVol
                                audioManager?.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    (newVol * maxVol).toInt(),
                                    0
                                )
                            },
                            valueRange = 0f..1f,
                            pointColor = Color.White,
                            activeTrackColor = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Volume Up",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Bottom Row: Lyrics, Shuffle, Queue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onOpenLyrics, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Comment,
                                contentDescription = "Lyrics",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(onClick = onToggleShuffle, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffleEnabled) AuraPlayGreen else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(onClick = onOpenQueue, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                contentDescription = "Queue",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. Spotify Encore Full Screen Player View
 */
/**
 * 2. Authentic Spotify Full Screen Music Player View
 * Signature Spotify layout:
 * - Dynamic artwork gradient transitioning into deep obsidian charcoal
 * - Top Bar: Chevron collapse, PLAYING FROM PLAYLIST, More options
 * - Full-width square album artwork with subtle rounded corners
 * - Left-aligned Title (bold 22sp) and Artist (15sp) with Heart on the right
 * - Slim timeline scrubber with dead-center circular point thumb
 * - Signature playback controls: Shuffle cycle, Skip Previous, large white Play/Pause FAB, Skip Next, Queue
 * - Connected Audio / Fidelity indicator
 * - Synced lyrics card preview with expand button directly below controls
 */
@Composable
private fun SpotifyEncorePlayerView(
    track: AuraTrack,
    isPlaying: Boolean,
    isBuffering: Boolean,
    positionSec: Float,
    durationSec: Float,
    palette: AuraPalette,
    lyrics: LyricsData?,
    lyricsOffsetMs: Int,
    isFavorite: Boolean,
    isShuffleEnabled: Boolean,
    lyricsExpansionProgress: Float = 0f,
    isVideoMode: Boolean = false,
    isVideoLoading: Boolean = false,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleVideoMode: () -> Unit = {},
    onOpenArtist: () -> Unit,
    onOptionsClick: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenQueue: () -> Unit,
    onCardPositioned: (Rect) -> Unit = {}
) {
    val animatedTopColor by animateColorAsState(
        targetValue = palette.backgroundTop.copy(alpha = 0.65f),
        animationSpec = tween(500),
        label = "bgTop"
    )

    val scrollState = rememberScrollState()

    // Authentic Spotify dynamic gradient background (ambient artwork glow into pure deep obsidian black)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        animatedTopColor,
                        Color(0xFF181818),
                        Color(0xFF121212)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Pinned Top Bar: Chevron Collapse, "PLAYING FROM PLAYLIST", More Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    val cleanAlbum = track.album.trim()
                    val isValidSource = cleanAlbum.isNotBlank() &&
                        cleanAlbum != track.title &&
                        !cleanAlbum.matches(Regex("^\\d+:\\d{2}(:\\d{2})?$")) &&
                        !cleanAlbum.endsWith("views", ignoreCase = true)
                    val playingFrom = if (isValidSource) {
                        "PLAYING FROM ${cleanAlbum.uppercase()}"
                    } else {
                        "PLAYING FROM PLAYLIST"
                    }
                    Text(
                        text = playingFrom,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 0.8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = track.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onOptionsClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More Options",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Scrollable Content Column: Artwork / Video, Metadata, Scrubber, Transport, Lyrics Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isVideoMode) {
                    // Video Player maximized edge-to-edge across the sides (no horizontal padding!)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AuraVideoPlayerSurface(
                            modifier = Modifier.fillMaxSize(),
                            track = track,
                            isLoading = isVideoLoading,
                            onSwitchToAudio = onToggleVideoMode
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Full-Width Spotify Square Album Artwork (aspectRatio 1f, subtle 8dp corner radius)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF242424)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = track.highResArtworkUrl,
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Controls, Metadata, and Lyrics Container with standard 24dp horizontal margins
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(if (isVideoMode) 16.dp else 24.dp))

                    // 3. Track Title, Artist & Favorite Heart Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onOpenArtist)
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) AuraPlayGreen else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Slim Timeline Scrubber & Timestamps
            Column(modifier = Modifier.fillMaxWidth()) {
                CompactPointSlider(
                    value = positionSec.coerceIn(0f, durationSec.coerceAtLeast(1f)),
                    onValueChange = onSeekTo,
                    valueRange = 0f..durationSec.coerceAtLeast(1f),
                    pointColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.22f),
                    trackHeight = 2.5.dp,
                    thumbRadius = 5.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(positionSec.toInt()),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    Text(
                        text = formatDuration(durationSec.toInt()),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Primary Spotify Transport Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val playbackMode by AuraPlayerManager.playbackMode.collectAsState()
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.size(44.dp)
                ) {
                    val (icon, tint) = when (playbackMode) {
                        PlaybackMode.NORMAL -> Icons.Rounded.Shuffle to Color.White.copy(alpha = 0.6f)
                        PlaybackMode.SHUFFLE -> Icons.Rounded.Shuffle to AuraPlayGreen
                        PlaybackMode.REPEAT_ALL -> Icons.Rounded.Repeat to AuraPlayGreen
                        PlaybackMode.REPEAT_ONE -> Icons.Rounded.RepeatOne to AuraPlayGreen
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = playbackMode.label,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Large Solid White Circular Play/Pause Button (Signature Spotify styling)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onTogglePlayPause),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next Track",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(
                    onClick = onOpenQueue,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = "Upcoming Queue",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Signature Spotify Synced Lyrics Card (Automatically uses scanned cover art color, no borders, covers 5 lines)
            val coverColor = palette.primary

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = coverColor,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .onGloballyPositioned { coordinates ->
                        onCardPositioned(coordinates.boundsInRoot())
                    }
                    .graphicsLayer {
                        // When lyrics is expanding or in flight, smoothly hide static card underneath
                        // so the flying morph surface appears as the single continuous shared element
                        alpha = if (lyricsExpansionProgress > 0.05f) 0f else (1f - (lyricsExpansionProgress / 0.05f)).coerceIn(0f, 1f)
                    }
                    .clickable(onClick = onOpenLyrics)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lyrics",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.35f),
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.OpenInFull,
                                    contentDescription = "Expand Lyrics",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val previewTextAlpha = if (lyricsExpansionProgress > 0.05f) 0f else (1f - (lyricsExpansionProgress / 0.05f)).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp)
                            .graphicsLayer { alpha = previewTextAlpha },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            val lines = lyrics?.syncedLines ?: emptyList()
                            val effectiveSec = positionSec + (lyricsOffsetMs / 1000f)
                            val activeIdx = lines.indexOfLast { effectiveSec >= it.timeSec }.coerceAtLeast(0)

                            if (lines.isEmpty()) {
                                Text(
                                    text = SimpMusicLyricsRepository.unescapeLyricsHtml(
                                        lyrics?.plainLyrics?.take(180)?.ifBlank { "Tap to view lyrics" }
                                            ?: "Tap to view lyrics"
                                    ),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.85f),
                                    lineHeight = 24.sp,
                                    maxLines = 6
                                )
                            } else {
                                val prevPrev = lines.getOrNull(activeIdx - 2)
                                val prev = lines.getOrNull(activeIdx - 1)
                                val current = lines.getOrNull(activeIdx)
                                val next1 = lines.getOrNull(activeIdx + 1)
                                val next2 = lines.getOrNull(activeIdx + 2)

                                if (prevPrev != null) {
                                    Text(
                                        text = SimpMusicLyricsRepository.unescapeLyricsHtml(prevPrev.text),
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.35f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                if (prev != null) {
                                    Text(
                                        text = SimpMusicLyricsRepository.unescapeLyricsHtml(prev.text),
                                        fontSize = 15.sp,
                                        color = Color.White.copy(alpha = 0.50f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                if (current != null) {
                                    Text(
                                        text = SimpMusicLyricsRepository.unescapeLyricsHtml(current.text),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        lineHeight = 28.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (next1 != null) {
                                    Text(
                                        text = SimpMusicLyricsRepository.unescapeLyricsHtml(next1.text),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.55f),
                                        lineHeight = 22.sp
                                    )
                                }

                                if (next2 != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = SimpMusicLyricsRepository.unescapeLyricsHtml(next2.text),
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.35f),
                                        lineHeight = 20.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
}
}

/**
 * Full Screen Song Lyrics View with track controls at bottom,
 * 100% solid, fully opaque background (no transparency).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FullScreenSongLyricsView(
    track: AuraTrack,
    lyrics: LyricsData?,
    positionSec: Float,
    durationSec: Float,
    isPlaying: Boolean,
    isBuffering: Boolean,
    lyricsOffsetMs: Int,
    showRomanized: Boolean,
    palette: AuraPalette,
    showPlayerControls: Boolean = true,
    lyricsExpansionProgress: Float = 1f,
    onClose: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onOpenQueue: () -> Unit = {},
    onAdjustLyricsOffset: (Int) -> Unit,
    onToggleRomanized: () -> Unit
) {
    val lines = lyrics?.syncedLines ?: emptyList()
    val activeIdx = lines.indexOfLast { positionSec >= it.timeSec }.coerceAtLeast(0)
    // Pre-calculate initial index on frame 1 so the active line is already placed in the center immediately
    val initialIdx = remember { (activeIdx - 1).coerceAtLeast(0) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIdx)

    val animatedCoverColor by animateColorAsState(
        targetValue = palette.primary,
        animationSpec = tween(400),
        label = "lyricsSolidCoverBg"
    )

    LaunchedEffect(activeIdx) {
        if (lines.isNotEmpty() && activeIdx >= 0) {
            listState.animateScrollToItem((activeIdx - 1).coerceAtLeast(0))
        }
    }

    // 100% solid single color background automatically using the scanned cover art color (NO gradient)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedCoverColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar with tap-to-close & swipe-down-to-close gestures
            // Smoothly crossfades with Card preview header below 0.35f progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = if (lyricsExpansionProgress < 0.35f) 0f else ((lyricsExpansionProgress - 0.35f) / 0.35f).coerceIn(0f, 1f)
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 16f) {
                                onClose()
                            }
                        }
                    }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Lyrics",
                        tint = Color.White
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onToggleRomanized) {
                    Text(
                        text = if (showRomanized) "ROM" else "ORIG",
                        color = if (showRomanized) AuraPlayGreen else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Synced Lyrics List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (lines.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = SimpMusicLyricsRepository.unescapeLyricsHtml(
                                lyrics?.plainLyrics?.ifBlank { "No synchronized lyrics available for this track." }
                                    ?: "Loading lyrics…"
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 28.sp
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 140.dp, bottom = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(lines) { idx, line ->
                            val isCurrentLine = idx == activeIdx
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSeekTo(line.timeSec) }
                                    .padding(vertical = 4.dp)
                            ) {
                                if (isCurrentLine && line.words.isNotEmpty()) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        line.words.forEach { w ->
                                            val wordReached = positionSec >= w.startTimeSec
                                            Text(
                                                text = SimpMusicLyricsRepository.unescapeLyricsHtml(w.word),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (wordReached) Color.White else Color.White.copy(alpha = 0.4f)
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = SimpMusicLyricsRepository.unescapeLyricsHtml(line.text),
                                        fontSize = if (isCurrentLine) 24.sp else 19.sp,
                                        fontWeight = if (isCurrentLine) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (isCurrentLine) Color.White else Color.White.copy(alpha = 0.4f)
                                    )
                                }

                                if (showRomanized && !line.romanizedText.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = SimpMusicLyricsRepository.unescapeLyricsHtml(line.romanizedText),
                                        fontSize = 14.sp,
                                        color = if (isCurrentLine) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.35f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Compact Track UI Controls inside Full Lyrics View with CompactPointSlider
            // 100% seamless: uses the EXACT same color as the lyrics background (no gray transparent overlay)
            // Completely hidden during shrinking back to the lyrics card so only lyrics are present!
            if (showPlayerControls) {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = ((lyricsExpansionProgress - 0.90f) / 0.10f).coerceIn(0f, 1f)
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        CompactPointSlider(
                            value = positionSec.coerceIn(0f, durationSec.coerceAtLeast(1f)),
                            onValueChange = onSeekTo,
                            valueRange = 0f..durationSec.coerceAtLeast(1f),
                            pointColor = Color.White,
                            activeTrackColor = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatDuration(positionSec.toInt()),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = formatDuration(durationSec.toInt()),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val playbackMode by AuraPlayerManager.playbackMode.collectAsState()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { AuraPlayerManager.cyclePlaybackMode() },
                                modifier = Modifier.size(46.dp)
                            ) {
                                val (icon, tint) = when (playbackMode) {
                                    PlaybackMode.NORMAL -> Icons.Rounded.Shuffle to Color.White.copy(alpha = 0.5f)
                                    PlaybackMode.SHUFFLE -> Icons.Rounded.Shuffle to AuraPlayGreen
                                    PlaybackMode.REPEAT_ALL -> Icons.Rounded.Repeat to AuraPlayGreen
                                    PlaybackMode.REPEAT_ONE -> Icons.Rounded.RepeatOne to AuraPlayGreen
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = playbackMode.label,
                                    tint = tint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            IconButton(
                                onClick = onSkipPrevious,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SkipPrevious,
                                    contentDescription = "Previous Track",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable(onClick = onTogglePlayPause),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isBuffering) {
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = Color.Black,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onSkipNext,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SkipNext,
                                    contentDescription = "Next Track",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            IconButton(
                                onClick = onOpenQueue,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                    contentDescription = "Upcoming Queue",
                                    tint = Color.White.copy(alpha = 0.85f),
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

/**
 * Upcoming Queue Sheet matching Spotify flat minimalist UX:
 * - Real Mobile Bottom Sheet Modal anchored to the bottom with full dimming scrim
 * - Flat transparent list items with zero borders
 * - Drag to reorder via holding ANY tile OR dragging the handle icon
 * - Damped spring reorder animation with tactile haptic feedback
 * - Auto-scroll when dragging near viewport boundaries
 */
@Composable
private fun QueueSheetContent(
    currentTrack: AuraTrack,
    queue: List<AuraTrack>,
    modalContainerColor: Color,
    onPlayTrack: (AuraTrack) -> Unit,
    onReorderQueue: (List<AuraTrack>) -> Unit
) {
    val curIdx = queue.indexOfFirst { it.videoId == currentTrack.videoId }
    val upcomingTracks = remember(queue, currentTrack.videoId) {
        if (curIdx >= 0 && curIdx < queue.lastIndex) {
            queue.subList(curIdx + 1, queue.size)
        } else {
            queue.filterNot { it.videoId == currentTrack.videoId }
        }
    }

    var upcomingList by remember(upcomingTracks) { mutableStateOf(upcomingTracks) }

    // Decoupled UI & Data: List data remains frozen during drag so LazyColumn never re-layouts or scrolls unexpectedly
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var targetDropIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val tileHeightPx = with(density) { 56.dp.toPx() }

    fun startDrag(itemId: String) {
        val currentIdx = upcomingList.indexOfFirst { (it.videoId.ifBlank { it.id }) == itemId }
        if (currentIdx < 0) return
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        draggedIndex = currentIdx
        targetDropIndex = currentIdx
        dragOffsetY = 0f
    }

    fun handleDragDelta(deltaY: Float) {
        val current = draggedIndex ?: return
        dragOffsetY += deltaY

        val deltaSlots = (dragOffsetY / tileHeightPx).roundToInt()
        val newTarget = (current + deltaSlots).coerceIn(0, upcomingList.lastIndex)
        if (newTarget != targetDropIndex) {
            targetDropIndex = newTarget
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }

        // Auto-scroll when dragged near top or bottom of viewport
        val layoutInfo = lazyListState.layoutInfo
        val visible = layoutInfo.visibleItemsInfo
        val itemInfo = visible.firstOrNull { it.index == current }
        if (itemInfo != null) {
            val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
            val topInViewport = itemInfo.offset + dragOffsetY
            val bottomInViewport = topInViewport + itemInfo.size
            if (topInViewport < 48f) {
                coroutineScope.launch {
                    lazyListState.scrollBy(-18f)
                }
            } else if (bottomInViewport > viewportHeight - 48f) {
                coroutineScope.launch {
                    lazyListState.scrollBy(18f)
                }
            }
        }
    }

    fun commitDrop() {
        val current = draggedIndex
        val target = targetDropIndex
        if (current != null && target != null && current != target &&
            current in upcomingList.indices && target in upcomingList.indices
        ) {
            val mutable = upcomingList.toMutableList()
            val moved = mutable.removeAt(current)
            mutable.add(target, moved)
            upcomingList = mutable
            onReorderQueue(mutable)
        }
        draggedIndex = null
        targetDropIndex = null
        dragOffsetY = 0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Text(
            text = "Queue",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "NOW PLAYING",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AuraNeonLime,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Flat "Now Playing" Tile (Zero Border, Flat Transparent Design)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = currentTrack.highResArtworkUrl,
                contentDescription = currentTrack.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentTrack.title,
                    fontWeight = FontWeight.Bold,
                    color = AuraNeonLime,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentTrack.artist,
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AuraNeonLime.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = AuraNeonLime,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Playing",
                    color = AuraNeonLime,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UP NEXT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            if (upcomingList.isNotEmpty()) {
                Text(
                    text = "${upcomingList.size} upcoming",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (upcomingList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "End of queue. Radio songs will load automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(
                    items = upcomingList,
                    key = { _, item -> item.videoId.ifBlank { item.id } }
                ) { index, item ->
                    val itemKey = item.videoId.ifBlank { item.id }
                    val isDraggingThis = draggedIndex == index
                    val currentDrag = draggedIndex
                    val currentTarget = targetDropIndex

                    // Physics engine spring displacement: surrounding items glide out of the way smoothly
                    val itemDisplacementY by animateFloatAsState(
                        targetValue = when {
                            currentDrag == null || currentTarget == null -> 0f
                            index == currentDrag -> 0f
                            // Dragging UP: items between currentTarget and currentDrag glide DOWN (+tileHeightPx)
                            currentDrag > currentTarget && index in currentTarget until currentDrag -> tileHeightPx
                            // Dragging DOWN: items between currentDrag and currentTarget glide UP (-tileHeightPx)
                            currentDrag < currentTarget && index in (currentDrag + 1)..currentTarget -> -tileHeightPx
                            else -> 0f
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "disp_$index"
                    )

                    val animatedElevation by animateDpAsState(
                        targetValue = if (isDraggingThis) 12.dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "elev_$index"
                    )
                    val animatedScale by animateFloatAsState(
                        targetValue = if (isDraggingThis) 1.03f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "scale_$index"
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        // Dragged tile uses SOLID light gray color (not transparent)
                        color = if (isDraggingThis) Color(0xFF3A3D46) else Color.Transparent,
                        border = null,
                        shadowElevation = animatedElevation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDraggingThis) 25f else 1f)
                            .graphicsLayer {
                                translationY = if (isDraggingThis) dragOffsetY else itemDisplacementY
                                scaleX = animatedScale
                                scaleY = animatedScale
                            }
                            // Holding anywhere on the song tile activates drag mode
                            .pointerInput(itemKey) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { startDrag(itemKey) },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        handleDragDelta(dragAmount.y)
                                    },
                                    onDragEnd = { commitDrop() },
                                    onDragCancel = { commitDrop() }
                                )
                            }
                            .clickable {
                                if (draggedIndex == null) {
                                    onPlayTrack(item)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = item.highResArtworkUrl,
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDraggingThis) AuraNeonLime else Color.White,
                                    fontSize = 13.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.artist,
                                    color = TextSecondary,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            // Drag Handle Icon on the right: Immediate touch & drag without waiting for long-press
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .pointerInput(itemKey) {
                                        detectDragGestures(
                                            onDragStart = { startDrag(itemKey) },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                handleDragDelta(dragAmount.y)
                                            },
                                            onDragEnd = { commitDrop() },
                                            onDragCancel = { commitDrop() }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DragHandle,
                                    contentDescription = "Drag to reorder",
                                    tint = if (isDraggingThis) AuraNeonLime else Color.White.copy(alpha = 0.45f),
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
