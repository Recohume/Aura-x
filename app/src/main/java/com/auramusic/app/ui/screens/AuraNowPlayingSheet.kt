package com.auramusic.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auramusic.app.domain.model.AuraPalette
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.EqualizerPreset
import com.auramusic.app.domain.model.LyricsData
import com.auramusic.app.domain.model.NowPlayingStyle
import com.auramusic.app.domain.model.ReturnYouTubeDislikeData
import com.auramusic.app.domain.model.SIMPMUSIC_EQ_PRESETS
import com.auramusic.app.domain.model.SponsorSegment
import com.auramusic.app.ui.theme.AuraCyan
import com.auramusic.app.ui.theme.AuraMagenta
import com.auramusic.app.ui.theme.AuraViolet
import com.auramusic.app.ui.theme.ObsidianBackground
import com.auramusic.app.ui.theme.ObsidianCard
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuraNowPlayingSheet(
    track: AuraTrack,
    isPlaying: Boolean,
    isBuffering: Boolean,
    positionSec: Float,
    durationSec: Float,
    streamSourceTier: String,
    palette: AuraPalette,
    lyrics: LyricsData?,
    lyricsOffsetMs: Int,
    showRomanized: Boolean,
    sponsorSegments: List<SponsorSegment>,
    lastSkippedSegment: String?,
    rydData: ReturnYouTubeDislikeData?,
    nowPlayingStyle: NowPlayingStyle,
    activeEqPreset: EqualizerPreset,
    eqEnabled: Boolean,
    sponsorBlockEnabled: Boolean,
    sleepTimerRemainingMin: Int?,
    isFavorite: Boolean,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleFavorite: () -> Unit,
    onChangeStyle: (NowPlayingStyle) -> Unit,
    onAdjustLyricsOffset: (Int) -> Unit,
    onToggleRomanized: () -> Unit,
    onSelectEqPreset: (EqualizerPreset) -> Unit,
    onToggleEqEnabled: (Boolean) -> Unit,
    onToggleSponsorBlock: (Boolean) -> Unit,
    onSetSleepTimer: (Int?) -> Unit
) {
    var activePanel by remember { mutableStateOf("artwork") } // "artwork", "lyrics", "eq"
    val animatedTopColor by animateColorAsState(
        targetValue = palette.backgroundTop,
        animationSpec = tween(600),
        label = "bgTop"
    )

    LaunchedEffect(nowPlayingStyle) {
        if (nowPlayingStyle == NowPlayingStyle.APPLE_MUSIC) {
            activePanel = "lyrics"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(animatedTopColor, ObsidianBackground, ObsidianBackground)
                )
            )
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        // Top Bar: Collapse + Stream Source Tier Badge + Style Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCollapse,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Collapse Player",
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = nowPlayingStyle.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AuraCyan
                )
                Text(
                    text = streamSourceTier,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val nextIdx = (NowPlayingStyle.entries.indexOf(nowPlayingStyle) + 1) % NowPlayingStyle.entries.size
                        onChangeStyle(NowPlayingStyle.entries[nextIdx])
                    }
            ) {
                Text(
                    text = "Style",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Center Content Area: Artwork / SimpMusic RichSync Karaoke Lyrics / 10-Band Equalizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (activePanel) {
                "lyrics" -> {
                    RichSyncLyricsPanel(
                        lyrics = lyrics,
                        positionSec = positionSec + (lyricsOffsetMs / 1000f),
                        lyricsOffsetMs = lyricsOffsetMs,
                        showRomanized = showRomanized,
                        accentColor = palette.primary,
                        onSeekTo = onSeekTo,
                        onAdjustOffset = onAdjustLyricsOffset,
                        onToggleRomanized = onToggleRomanized
                    )
                }
                "eq" -> {
                    EqualizerAndAcousticsPanel(
                        activePreset = activeEqPreset,
                        eqEnabled = eqEnabled,
                        sponsorBlockEnabled = sponsorBlockEnabled,
                        sleepTimerRemainingMin = sleepTimerRemainingMin,
                        onSelectPreset = onSelectEqPreset,
                        onToggleEq = onToggleEqEnabled,
                        onToggleSponsorBlock = onToggleSponsorBlock,
                        onSetSleepTimer = onSetSleepTimer
                    )
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val cornerRadius = if (nowPlayingStyle == NowPlayingStyle.EXPRESSIVE) 36.dp else 20.dp
                        AsyncImage(
                            model = track.highResArtworkUrl,
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth(0.84f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(cornerRadius))
                                .background(ObsidianCard)
                        )

                        if (rydData != null && rydData.likes > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.ThumbUp,
                                        contentDescription = "Likes",
                                        tint = AuraCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formatCompactCount(rydData.likes),
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.ThumbDown,
                                        contentDescription = "Dislikes",
                                        tint = AuraMagenta,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formatCompactCount(rydData.dislikes),
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                if (sponsorSegments.isNotEmpty()) {
                                    Text(
                                        text = "• ${sponsorSegments.size} SponsorBlock segments",
                                        fontSize = 12.sp,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Track Title, Artist, Favorite Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 22.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    color = TextSecondary,
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
                    tint = if (isFavorite) AuraMagenta else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scrubber with SponsorBlock Segment Markers
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = positionSec.coerceIn(0f, durationSec.coerceAtLeast(1f)),
                onValueChange = onSeekTo,
                valueRange = 0f..durationSec.coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = palette.primary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )

            // SponsorBlock visual timeline bar
            if (sponsorSegments.isNotEmpty() && durationSec > 1f) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    sponsorSegments.forEach { seg ->
                        val startFrac = (seg.startTimeSec / durationSec).coerceIn(0f, 1f)
                        val endFrac = (seg.endTimeSec / durationSec).coerceIn(startFrac, 1f)
                        drawRoundRect(
                            color = Color(0xFF10B981),
                            topLeft = Offset(size.width * startFrac, 0f),
                            size = Size(
                                ((endFrac - startFrac) * size.width).coerceAtLeast(6f),
                                size.height
                            ),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(positionSec.toInt()),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                if (lastSkippedSegment != null) {
                    Text(
                        text = lastSkippedSegment,
                        fontSize = 11.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = formatDuration(durationSec.toInt()),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Primary Transport Controls (Previous, Play/Pause, Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    activePanel = if (activePanel == "lyrics") "artwork" else "lyrics"
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (activePanel == "lyrics") palette.primary else Color.White.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lyrics,
                    contentDescription = "RichSync Karaoke Lyrics",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onSkipPrevious,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(palette.primary)
                    .clickable(onClick = onTogglePlayPause),
                contentAlignment = Alignment.Center
            ) {
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
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            IconButton(
                onClick = onSkipNext,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next Track",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(
                onClick = {
                    activePanel = if (activePanel == "eq") "artwork" else "eq"
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (activePanel == "eq") palette.primary else Color.White.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Equalizer,
                    contentDescription = "10-Band Equalizer & AutoEq",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RichSyncLyricsPanel(
    lyrics: LyricsData?,
    positionSec: Float,
    lyricsOffsetMs: Int,
    showRomanized: Boolean,
    accentColor: Color,
    onSeekTo: (Float) -> Unit,
    onAdjustOffset: (Int) -> Unit,
    onToggleRomanized: () -> Unit
) {
    val listState = rememberLazyListState()
    val lines = lyrics?.syncedLines ?: emptyList()

    val activeLineIndex = remember(positionSec, lines) {
        lines.indexOfLast { positionSec >= it.timeSec }.coerceAtLeast(0)
    }

    LaunchedEffect(activeLineIndex) {
        if (lines.isNotEmpty() && activeLineIndex in lines.indices) {
            listState.animateScrollToItem((activeLineIndex - 1).coerceAtLeast(0))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(ObsidianCard.copy(alpha = 0.85f))
            .padding(16.dp)
    ) {
        // Lyrics Header & Sync Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = lyrics?.source ?: "SimpMusic RichSync",
                    style = MaterialTheme.typography.labelSmall,
                    color = AuraCyan
                )
                Text(
                    text = "Offset: ${if (lyricsOffsetMs >= 0) "+$lyricsOffsetMs" else "$lyricsOffsetMs"}ms",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SmallPillButton("-250ms") { onAdjustOffset(-250) }
                SmallPillButton("+250ms") { onAdjustOffset(250) }
                if (lyrics?.hasRomanization == true) {
                    SmallPillButton(if (showRomanized) "Romaji ON" else "Romaji OFF") {
                        onToggleRomanized()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (lines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = lyrics?.plainLyrics ?: "Fetching SimpMusic RichSync lyrics…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(lines) { index, line ->
                    val isCurrentLine = index == activeLineIndex
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSeekTo(line.timeSec) }
                            .padding(vertical = 4.dp)
                    ) {
                        if (isCurrentLine && line.words.isNotEmpty()) {
                            // Apple Music / SimpMusic Word-by-Word Karaoke Highlight
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                line.words.forEach { w ->
                                    val wordReached = positionSec >= w.startTimeSec
                                    Text(
                                        text = w.word,
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (wordReached) Color.White else Color.White.copy(alpha = 0.38f)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = line.text,
                                fontSize = if (isCurrentLine) 21.sp else 17.sp,
                                fontWeight = if (isCurrentLine) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isCurrentLine) Color.White else TextSecondary.copy(alpha = 0.48f)
                            )
                        }

                        if (showRomanized && !line.romanizedText.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = line.romanizedText,
                                fontSize = 13.sp,
                                color = if (isCurrentLine) accentColor else TextSecondary.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EqualizerAndAcousticsPanel(
    activePreset: EqualizerPreset,
    eqEnabled: Boolean,
    sponsorBlockEnabled: Boolean,
    sleepTimerRemainingMin: Int?,
    onSelectPreset: (EqualizerPreset) -> Unit,
    onToggleEq: (Boolean) -> Unit,
    onToggleSponsorBlock: (Boolean) -> Unit,
    onSetSleepTimer: (Int?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(ObsidianCard.copy(alpha = 0.92f))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "10-Band Parametric EQ & AutoEq",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = activePreset.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = AuraCyan
                )
            }
            Switch(checked = eqEnabled, onCheckedChange = onToggleEq)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visual 10-Band Frequency Curve
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ObsidianBackground)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            val freqLabels = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
            activePreset.bandsDb.forEachIndexed { i, db ->
                val normalized = ((db + 12f) / 24f).coerceIn(0.15f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.height(64.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .weight(1f, fill = false)
                            .height((42 * normalized).dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (eqEnabled) AuraViolet else TextSecondary.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = freqLabels.getOrElse(i) { "" },
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Selector List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(SIMPMUSIC_EQ_PRESETS) { _, preset ->
                val isSelected = preset.id == activePreset.id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AuraViolet.copy(alpha = 0.25f) else ObsidianBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectPreset(preset) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = preset.name,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = preset.subtitle,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        if (isSelected) {
                            Text(
                                text = "ACTIVE",
                                color = AuraCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SponsorBlock Toggle & Sleep Timer Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SponsorBlock Auto-Skip",
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = sponsorBlockEnabled, onCheckedChange = onToggleSponsorBlock)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = "Sleep Timer",
                    tint = AuraCyan,
                    modifier = Modifier.size(16.dp)
                )
                listOf(null to "Off", 15 to "15m", 30 to "30m").forEach { (mins, label) ->
                    SmallPillButton(
                        label = if (sleepTimerRemainingMin == mins && mins != null) "${sleepTimerRemainingMin}m" else label
                    ) {
                        onSetSleepTimer(mins)
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallPillButton(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.1f),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

private fun formatCompactCount(value: Long): String {
    return when {
        value >= 1_000_000L -> String.format("%.1fM", value / 1_000_000.0)
        value >= 1_000L -> String.format("%.1fK", value / 1_000.0)
        else -> value.toString()
    }
}
