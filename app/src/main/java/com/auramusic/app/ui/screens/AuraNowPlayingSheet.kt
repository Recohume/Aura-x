package com.auramusic.app.ui.screens

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auramusic.app.core.lyrics.SimpMusicLyricsRepository
import com.auramusic.app.core.media.AuraPlayerManager
import com.auramusic.app.domain.model.AuraPalette
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.LyricsData
import com.auramusic.app.domain.model.NowPlayingStyle
import com.auramusic.app.domain.model.PlaybackMode
import com.auramusic.app.ui.theme.AuraCircleButtonBg
import com.auramusic.app.ui.theme.AuraNeonLime
import com.auramusic.app.ui.theme.AuraPlayGreen
import com.auramusic.app.ui.theme.SpotifyCard
import com.auramusic.app.ui.theme.SpotifyPitchBlack
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary

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
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleStyle: () -> Unit,
    onPlayQueueTrack: (AuraTrack) -> Unit,
    onAdjustLyricsOffset: (Int) -> Unit,
    onToggleRomanized: () -> Unit
) {
    var isLyricsExpanded by remember { mutableStateOf(false) }
    var isQueueSheetVisible by remember { mutableStateOf(false) }

    // Fully opaque solid black root container: zero background bleed-through
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Smooth transition between Spotify Encore and Blur Style - Liquid Glass
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
                        onCollapse = onCollapse,
                        onTogglePlayPause = onTogglePlayPause,
                        onSeekTo = onSeekTo,
                        onSkipNext = onSkipNext,
                        onSkipPrevious = onSkipPrevious,
                        onToggleFavorite = onToggleFavorite,
                        onToggleShuffle = onToggleShuffle,
                        onToggleStyle = onToggleStyle,
                        onOpenLyrics = { isLyricsExpanded = true },
                        onOpenQueue = { isQueueSheetVisible = true }
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
                        onCollapse = onCollapse,
                        onTogglePlayPause = onTogglePlayPause,
                        onSeekTo = onSeekTo,
                        onSkipNext = onSkipNext,
                        onSkipPrevious = onSkipPrevious,
                        onToggleFavorite = onToggleFavorite,
                        onToggleShuffle = onToggleShuffle,
                        onToggleStyle = onToggleStyle,
                        onOpenLyrics = { isLyricsExpanded = true },
                        onOpenQueue = { isQueueSheetVisible = true }
                    )
                }
            }
        }

        // Full Screen Song Lyrics Screen (Expanded) with 100% solid background
        AnimatedVisibility(
            visible = isLyricsExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
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
                onClose = { isLyricsExpanded = false },
                onSeekTo = onSeekTo,
                onTogglePlayPause = onTogglePlayPause,
                onSkipPrevious = onSkipPrevious,
                onSkipNext = onSkipNext,
                onOpenQueue = { isQueueSheetVisible = true },
                onAdjustLyricsOffset = onAdjustLyricsOffset,
                onToggleRomanized = onToggleRomanized
            )
        }

        // Upcoming Queue Bottom Sheet
        if (isQueueSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { isQueueSheetVisible = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = SpotifyCard,
                contentColor = TextPrimary
            ) {
                QueueSheetContent(
                    currentTrack = track,
                    queue = queue,
                    onPlayTrack = {
                        onPlayQueueTrack(it)
                        isQueueSheetVisible = false
                    }
                )
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
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleStyle: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenQueue: () -> Unit
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVol = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }
    var systemVolume by remember {
        mutableFloatStateOf(
            audioManager?.let { it.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVol.coerceAtLeast(1) } ?: 0.65f
        )
    }
    var showMenu by remember { mutableStateOf(false) }

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
            // Subtle Apple-style top drag handle pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(4.5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.45f))
                )
            }

            // Top Bar: Collapse, Style Switcher Chip, More Options Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
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

                // Interactive Style Switcher Pill: Tap to transform styles
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onToggleStyle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✦ Blur Glass",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.4.sp
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(SpotifyCard)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Switch to Spotify Style", color = TextPrimary) },
                            onClick = {
                                showMenu = false
                                onToggleStyle()
                            }
                        )
                    }
                }
            }

            // 2. High Quality Artwork with increased dimensions adapting dynamically without pushing controls off-screen
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                // Dynamically maximize size while fitting within available vertical & horizontal bounds
                val artSize = minOf(maxHeight - 6.dp, maxWidth).coerceIn(200.dp, 360.dp)

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

            // 3. Lower Frosted Glass Controls Container
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.10f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
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
                                letterSpacing = 0.6.sp
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
                            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(34.dp)) {
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
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleStyle: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenQueue: () -> Unit
) {
    val animatedTopColor by animateColorAsState(
        targetValue = palette.backgroundTop.copy(alpha = 0.65f),
        animationSpec = tween(500),
        label = "bgTop"
    )

    val scrollState = rememberScrollState()
    var showMenu by remember { mutableStateOf(false) }

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

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM PLAYLIST",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = track.album.ifBlank { "Top Tracks" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More Options",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF282828))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Toggle Liquid Glass Style", color = Color.White) },
                            onClick = {
                                showMenu = false
                                onToggleStyle()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Upcoming Queue", color = Color.White) },
                            onClick = {
                                showMenu = false
                                onOpenQueue()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Full Screen Lyrics", color = Color.White) },
                            onClick = {
                                showMenu = false
                                onOpenLyrics()
                            }
                        )
                    }
                }
            }

            // Scrollable Content Column: Artwork, Metadata, Scrubber, Transport, Lyrics Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 2. Full-Width Spotify Square Album Artwork (aspectRatio 1f, subtle 8dp corner radius)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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

            Spacer(modifier = Modifier.height(24.dp))

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
                        overflow = TextOverflow.Ellipsis
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

            Spacer(modifier = Modifier.height(36.dp))
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
    onClose: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onOpenQueue: () -> Unit = {},
    onAdjustLyricsOffset: (Int) -> Unit,
    onToggleRomanized: () -> Unit
) {
    val listState = rememberLazyListState()
    val lines = lyrics?.syncedLines ?: emptyList()
    val activeIdx = lines.indexOfLast { positionSec >= it.timeSec }.coerceAtLeast(0)

    val animatedCoverColor by animateColorAsState(
        targetValue = palette.primary,
        animationSpec = tween(400),
        label = "lyricsSolidCoverBg"
    )

    LaunchedEffect(activeIdx) {
        if (lines.isNotEmpty() && activeIdx >= 0) {
            listState.animateScrollToItem((activeIdx - 2).coerceAtLeast(0))
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
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 60.dp),
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
            Surface(
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
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

/**
 * Upcoming Queue Sheet
 */
@Composable
private fun QueueSheetContent(
    currentTrack: AuraTrack,
    queue: List<AuraTrack>,
    onPlayTrack: (AuraTrack) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = "Queue",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "NOW PLAYING",
            style = MaterialTheme.typography.labelSmall,
            color = AuraPlayGreen
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = currentTrack.highResArtworkUrl,
                contentDescription = currentTrack.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = currentTrack.title,
                    fontWeight = FontWeight.Bold,
                    color = AuraPlayGreen,
                    fontSize = 14.sp
                )
                Text(
                    text = currentTrack.artist,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "UP NEXT",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        val upcoming = queue.filterNot { it.videoId == currentTrack.videoId }
        if (upcoming.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "End of queue. Radio songs will load automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(upcoming, key = { it.videoId }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPlayTrack(item) }
                            .padding(vertical = 6.dp),
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
                        Column {
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.artist} • ${formatDuration(item.durationSeconds)}",
                                color = TextSecondary,
                                fontSize = 11.sp,
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
