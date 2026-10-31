package com.auramusic.app.ui.theme

import android.graphics.BitmapFactory
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import com.auramusic.app.core.extractor.SimpMusicExtractorEngine
import com.auramusic.app.domain.model.AuraPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

val ObsidianBackground = Color(0xFF0A0B0E)
val ObsidianSurface = Color(0xFF13151C)
val ObsidianCard = Color(0xFF1B1E28)
val AuraViolet = Color(0xFF8B5CF6)
val AuraMagenta = Color(0xFFEC4899)
val AuraCyan = Color(0xFF06B6D4)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)

private val AuraDarkScheme = darkColorScheme(
    primary = AuraViolet,
    secondary = AuraMagenta,
    tertiary = AuraCyan,
    background = ObsidianBackground,
    surface = ObsidianSurface,
    surfaceVariant = ObsidianCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

private val AuraTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = (-0.3).sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.4.sp,
        color = TextSecondary
    )
)

@Composable
fun AuraMusicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AuraDarkScheme,
        typography = AuraTypography,
        content = content
    )
}

/**
 * Extracts vibrant Material You / Apple Music ambient colors from album artwork
 * using AndroidX Palette.
 */
suspend fun extractAuraPaletteFromUrl(artworkUrl: String): AuraPalette = withContext(Dispatchers.IO) {
    if (artworkUrl.isBlank()) return@withContext AuraPalette()
    try {
        val req = Request.Builder().url(artworkUrl).build()
        SimpMusicExtractorEngine.httpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext AuraPalette()
            val bytes = resp.body?.bytes() ?: return@withContext AuraPalette()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext AuraPalette()
            val palette = Palette.from(bitmap).maximumColorCount(16).generate()

            val vibrantInt = palette.getVibrantColor(0xFF8B5CF6.toInt())
            val mutedInt = palette.getDarkVibrantColor(palette.getMutedColor(0xFFEC4899.toInt()))
            val darkInt = palette.getDarkMutedColor(0xFF1E1436.toInt())

            AuraPalette(
                primary = Color(vibrantInt),
                secondary = Color(mutedInt),
                backgroundTop = Color(darkInt).copy(alpha = 0.88f),
                backgroundBottom = ObsidianBackground
            )
        }
    } catch (e: Exception) {
        AuraPalette()
    }
}
