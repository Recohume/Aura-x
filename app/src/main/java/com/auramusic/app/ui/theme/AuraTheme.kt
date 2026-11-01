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

// Encore Modern Design Tokens (Bespoke Obsidian + Electric Lime + Pastel Lilac)
val AuraNeonLime = Color(0xFFD4F644) // Electric Chartreuse / Neon Lime
val AuraLilacCard = Color(0xFFD6C7FF) // Pastel Lavender / Lilac Hero Card
val AuraLilacDarkText = Color(0xFF1D1438) // Deep Indigo text on Lilac card
val AuraLilacButton = Color(0xFF261947) // Deep Violet Play button on Lilac card
val AuraObsidianBg = Color(0xFF121316) // Deep warm Obsidian charcoal background
val AuraDarkSurface = Color(0xFF181A20) // Floating docks, surface containers
val AuraCardDark = Color(0xFF1E2026) // Card containers & list items
val AuraCardBorder = Color(0xFF2A2D36) // Subtle 1dp container borders
val AuraCircleButtonBg = Color(0xFF22242B) // Circular header and action buttons

// Semantic compatibility aliases
val SpotifyPitchBlack = AuraObsidianBg
val SpotifySurface = AuraDarkSurface
val SpotifySurfaceLight = Color(0xFF1F2128)
val SpotifyCard = AuraCardDark
val SpotifyCardElevated = Color(0xFF252731)
val SpotifyHeroCard = AuraLilacCard

val AuraSageGreen = Color(0xFFBCE354)
val AuraPlayGreen = AuraNeonLime // Primary brand accent across player and controls
val SpotifyGreen = AuraNeonLime
val AuraViolet = Color(0xFF8B5CF6)
val AuraMagenta = Color(0xFFEC4899)
val AuraCyan = Color(0xFF06B6D4)

val PillActiveBg = AuraNeonLime // Active capsule filter pill
val PillActiveText = Color(0xFF000000)
val PillInactiveBg = Color(0xFF1E1F25)
val PillInactiveText = Color(0xFF9E9EA7)
val AuraPillInactive = PillInactiveBg
val AuraPillInactiveText = PillInactiveText

val TextPrimary = Color(0xFFFFFFFF)
val TextCream = Color(0xFFF3F4F6)
val TextSecondary = Color(0xFF8E909A)
val TextMuted = Color(0xFF6B7280)
val BadgeLyricsBg = AuraNeonLime
val BadgeExplicitBg = Color(0xFF6B7280)

val AuraDarkScheme = darkColorScheme(
    primary = AuraNeonLime,
    secondary = AuraLilacCard,
    tertiary = PillActiveBg,
    background = AuraObsidianBg,
    surface = AuraDarkSurface,
    surfaceVariant = AuraCardDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

val AuraTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = (-0.3).sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        color = AuraSageGreen
    )
)

@Composable
fun AuraMusicTheme(content: @Composable () -> Unit) {
    com.auramusic.app.ui.components.ForceWhiteSystemBarsEffect()
    MaterialTheme(
        colorScheme = AuraDarkScheme,
        typography = AuraTypography,
        content = content
    )
}

/**
 * Automatically scans the cover art album and extracts dominant, vibrant, and ambient colors
 * using AndroidX Palette.
 */
suspend fun extractAuraPaletteFromUrl(artworkUrl: String): AuraPalette = withContext(Dispatchers.IO) {
    if (artworkUrl.isBlank()) return@withContext AuraPalette()
    try {
        // Use a 400x400 scaled URL for instant download & reliable palette extraction
        val scanUrl = if (artworkUrl.contains("googleusercontent.com") || artworkUrl.contains("ggpht.com")) {
            artworkUrl.replace(Regex("=w\\d+-h\\d+[^&?]*"), "=w400-h400-l90-rj")
        } else artworkUrl

        val req = Request.Builder().url(scanUrl).build()
        SimpMusicExtractorEngine.httpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext AuraPalette()
            val bytes = resp.body?.bytes() ?: return@withContext AuraPalette()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext AuraPalette()
            val palette = Palette.from(bitmap).maximumColorCount(32).generate()

            val swatches = palette.swatches
            val vibrant = palette.vibrantSwatch
            val dominant = palette.dominantSwatch
            val darkVibrant = palette.darkVibrantSwatch
            val lightVibrant = palette.lightVibrantSwatch
            val muted = palette.mutedSwatch
            val darkMuted = palette.darkMutedSwatch

            // Filter out pitch-black (lightness < 0.12) and washed-out pure-white (lightness > 0.88)
            val colorfulSwatches = swatches.filter { s ->
                val hsl = s.hsl
                hsl[1] >= 0.18f && hsl[2] in 0.15f..0.85f
            }.sortedByDescending { it.population }

            // Rich vibrant cover art color for single-color lyrics card & full-screen lyrics background
            val rawCoverColorInt = vibrant?.takeIf { it.hsl[2] >= 0.10f }?.rgb
                ?: colorfulSwatches.firstOrNull()?.rgb
                ?: dominant?.takeIf { it.hsl[1] >= 0.15f && it.hsl[2] in 0.10f..0.85f }?.rgb
                ?: darkVibrant?.rgb
                ?: lightVibrant?.rgb
                ?: muted?.rgb
                ?: dominant?.rgb
                ?: 0xFF8B5CF6.toInt()

            // Dark tone enforcement: Guarantee rich, darker tone so white lyrics text always has AAA contrast
            val hsl = FloatArray(3)
            androidx.core.graphics.ColorUtils.colorToHSL(rawCoverColorInt, hsl)
            if (hsl[2] > 0.28f) {
                hsl[2] = 0.22f // Darken to deep luxury tone while preserving hue & saturation
            } else if (hsl[2] < 0.10f) {
                hsl[2] = 0.16f // Prevent pitch-black
            }
            if (hsl[1] < 0.25f && colorfulSwatches.isNotEmpty()) {
                hsl[1] = 0.35f
            }
            val coverColorInt = androidx.core.graphics.ColorUtils.HSLToColor(hsl)

            val ambientTopInt = darkVibrant?.rgb ?: darkMuted?.rgb ?: dominant?.rgb ?: 0xFF1E1436.toInt()
            val ambientBottomInt = darkMuted?.rgb ?: 0xFF0D0C14.toInt()

            AuraPalette(
                primary = Color(coverColorInt),
                secondary = Color(muted?.rgb ?: coverColorInt),
                backgroundTop = Color(ambientTopInt),
                backgroundBottom = Color(ambientBottomInt)
            )
        }
    } catch (e: Exception) {
        AuraPalette()
    }
}
