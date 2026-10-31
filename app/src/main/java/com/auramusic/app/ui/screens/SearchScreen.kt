package com.auramusic.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.ui.theme.AuraNeonLime
import com.auramusic.app.ui.theme.AuraPillInactive
import com.auramusic.app.ui.theme.AuraPillInactiveText
import com.auramusic.app.ui.theme.AuraPlayGreen
import com.auramusic.app.ui.theme.SpotifyCard
import com.auramusic.app.ui.theme.SpotifyPitchBlack
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary

@Composable
fun SearchScreen(
    query: String,
    filter: String,
    results: List<AuraTrack>,
    isSearching: Boolean,
    favorites: List<AuraTrack>,
    downloads: List<AuraTrack>,
    downloadingIds: Set<String>,
    onQueryChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onPlayTrack: (AuraTrack, List<AuraTrack>) -> Unit,
    onToggleFavorite: (AuraTrack) -> Unit,
    onDownloadTrack: (AuraTrack) -> Unit
) {
    val listState = rememberLazyListState()
    val searchEasing = remember { CubicBezierEasing(0.23f, 1f, 0.32f, 1f) }

    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20
        }
    }

    LaunchedEffect(query, filter) {
        if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
            listState.scrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyPitchBlack)
            .padding(top = 2.dp)
    ) {
        // Collapsible "Search" Headline: Pushes up and hides when scrolling songs
        AnimatedVisibility(
            visible = !isScrolled,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 220, easing = searchEasing)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 180)
            ) + slideInVertically(
                animationSpec = tween(durationMillis = 220, easing = searchEasing),
                initialOffsetY = { -it }
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 200, easing = searchEasing)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 150)
            ) + slideOutVertically(
                animationSpec = tween(durationMillis = 200, easing = searchEasing),
                targetOffsetY = { -it }
            )
        ) {
            Column {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Search Input Bar (Slim, compact 40dp height with pill shape)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E2026))
                .border(
                    width = 1.dp,
                    color = if (query.isNotEmpty()) AuraNeonLime else Color(0xFF2C2F3A),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "What do you want to listen to?",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontSize = 13.sp
                        ),
                        cursorBrush = SolidColor(AuraPlayGreen),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Clear Search",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Pills matching Home & Library
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("songs" to "Songs", "videos" to "Videos").forEach { (key, label) ->
                val selected = filter == key
                Surface(
                    shape = CircleShape,
                    color = if (selected) AuraNeonLime else AuraPillInactive,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onFilterChange(key) }
                ) {
                    Text(
                        text = label,
                        color = if (selected) Color.Black else AuraPillInactiveText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp)
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
                CircularProgressIndicator(color = AuraPlayGreen, modifier = Modifier.size(28.dp))
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            items(results, key = { it.videoId }) { track ->
                SpotifyTrackRowItem(
                    track = track,
                    isCurrent = false,
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
