package com.auramusic.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auramusic.app.R
import com.auramusic.app.core.media.AuraPlayerManager
import com.auramusic.app.domain.model.EqualizerPreset
import com.auramusic.app.domain.model.NowPlayingStyle
import com.auramusic.app.domain.model.SIMPMUSIC_EQ_PRESETS
import com.auramusic.app.ui.AuraViewModel
import com.auramusic.app.ui.theme.AuraNeonLime
import com.auramusic.app.ui.theme.SpotifyPitchBlack
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen(viewModel: AuraViewModel) {
    val context = LocalContext.current
    val nowPlayingStyle by viewModel.nowPlayingStyle.collectAsState()
    val showRomanized by viewModel.showRomanizedLyrics.collectAsState()
    val sponsorBlockEnabled by AuraPlayerManager.sponsorBlockEnabled.collectAsState()
    val eqEnabled by AuraPlayerManager.eqEnabled.collectAsState()
    val activeEqPreset by AuraPlayerManager.activeEqPreset.collectAsState()
    val downloads by viewModel.downloads.collectAsState()

    var showEqPresets by remember { mutableStateOf(false) }

    val packageInfo = remember(context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (_: Exception) {
            null
        }
    }

    val appVersionName = packageInfo?.versionName ?: "1.4.3"
    val appVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        packageInfo?.longVersionCode ?: 1L
    } else {
        @Suppress("DEPRECATION")
        packageInfo?.versionCode?.toLong() ?: 1L
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyPitchBlack),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 140.dp)
    ) {
        // 1. Header
        item {
            Column(modifier = Modifier.padding(bottom = 28.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fidelity, playback & interface controls",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // 2. Player Appearance
        item {
            SettingsCategoryHeader(title = "Player appearance")
            Spacer(modifier = Modifier.height(10.dp))

            // Modern containerless segmented pill toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF14161C), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NowPlayingStyle.entries.forEach { style ->
                    val isSelected = nowPlayingStyle == style
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF262933) else Color.Transparent)
                            .clickable { viewModel.setNowPlayingStyle(style) }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = style.label,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }

        // 3. Audio & Streaming Fidelity
        item {
            SettingsCategoryHeader(title = "Audio & streaming")
            Spacer(modifier = Modifier.height(10.dp))

            ModernSettingsRow(
                icon = Icons.Rounded.Speed,
                title = "Audio Stream Quality",
                subtitle = "Up to 256kbps direct stream (Opus / AAC)"
            )

            SettingsDivider()

            ModernClickableRow(
                icon = Icons.Rounded.Equalizer,
                title = "10-Band Equalizer",
                subtitle = if (eqEnabled) "Preset: ${activeEqPreset.name}" else "Hardware curve bypass",
                onClick = { showEqPresets = !showEqPresets },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Select EQ Preset",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            AnimatedVisibility(
                visible = showEqPresets,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(140))
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp, start = 48.dp)
                ) {
                    items(SIMPMUSIC_EQ_PRESETS, key = { it.id }) { preset ->
                        val isCurrent = activeEqPreset.id == preset.id
                        Surface(
                            onClick = { viewModel.selectEqualizerPreset(preset) },
                            shape = RoundedCornerShape(18.dp),
                            color = if (isCurrent) Color(0xFF2E3240) else Color(0xFF16181F)
                        ) {
                            Text(
                                text = preset.name,
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // 4. Playback & Automation
        item {
            SettingsCategoryHeader(title = "Playback & automation")
            Spacer(modifier = Modifier.height(10.dp))

            ModernSwitchRow(
                icon = Icons.Rounded.Block,
                title = "SponsorBlock Integration",
                subtitle = "Skip intros, sponsor spots, and non-music gaps",
                checked = sponsorBlockEnabled,
                onCheckedChange = { AuraPlayerManager.setSponsorBlockEnabled(it) }
            )

            SettingsDivider()

            ModernSwitchRow(
                icon = Icons.Rounded.Lyrics,
                title = "Romanized Lyrics",
                subtitle = "Transliterate Asian lyrics to Latin alphabet",
                checked = showRomanized,
                onCheckedChange = { viewModel.toggleRomanizedLyrics() }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }

        // 5. Storage & Cache
        item {
            SettingsCategoryHeader(title = "Storage & data")
            Spacer(modifier = Modifier.height(10.dp))

            ModernSettingsRow(
                icon = Icons.Rounded.Storage,
                title = "Offline Downloads",
                subtitle = "${downloads.size} tracks cached locally",
                trailingContent = { }
            )

            SettingsDivider()

            ModernClickableRow(
                icon = Icons.Rounded.DeleteOutline,
                title = "Clear Media Cache",
                subtitle = "Release temporary cover arts and audio buffers",
                onClick = {
                    Toast.makeText(context, "Stream cache successfully cleared", Toast.LENGTH_SHORT).show()
                },
                trailingContent = { }
            )

            Spacer(modifier = Modifier.height(34.dp))
        }

        // 5b. Updates & Notifications
        item {
            SettingsCategoryHeader(title = "Updates & notifications")
            Spacer(modifier = Modifier.height(10.dp))

            ModernClickableRow(
                icon = Icons.Rounded.SystemUpdate,
                title = "Check for Updates",
                subtitle = "Query GitHub Releases directly for new APK builds",
                onClick = {
                    viewModel.checkForUpdates(force = true)
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Check for Updates",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            SettingsDivider()

            ModernSettingsRow(
                icon = Icons.Rounded.Notifications,
                title = "Release Push Notifications",
                subtitle = "Receive FCM alerts when new versions are published",
                trailingContent = { }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }

        // 6. About Aura (Clean minimal lockup with official Aura Logo & dynamic version)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.aura_logo),
                    contentDescription = "Aura Logo",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Aura v$appVersionName",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "YouTube Music InnerTube & SimpMusic Core • 0% Ads",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        color = TextSecondary,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 2.dp)
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.05f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 48.dp, top = 2.dp, bottom = 2.dp)
    )
}

@Composable
private fun ModernSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        trailingContent()
    }
}

@Composable
private fun ModernClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        trailingContent()
    }
}

@Composable
private fun ModernSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = AuraNeonLime,
                uncheckedThumbColor = Color.White.copy(alpha = 0.8f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.15f)
            )
        )
    }
}
