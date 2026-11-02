package com.auramusic.app.domain.model

import androidx.compose.ui.graphics.Color

enum class NowPlayingStyle(val label: String) {
    SPOTIFY("Spotify Encore"),
    LIQUID_GLASS("Blur - Liquid Glass")
}

enum class PlaybackMode(val label: String) {
    NORMAL("Normal"),
    SHUFFLE("Shuffle All"),
    REPEAT_ALL("Repeat All"),
    REPEAT_ONE("Repeat One")
}

data class AuraPalette(
    val primary: Color = Color(0xFF8B5CF6),
    val secondary: Color = Color(0xFFEC4899),
    val backgroundTop: Color = Color(0xFF1E1436),
    val backgroundBottom: Color = Color(0xFF0A0B0E),
    val surfaceGlass: Color = Color(0x26FFFFFF)
)

data class AuraTrack(
    val id: String,
    val videoId: String,
    val title: String,
    val artist: String,
    val album: String = "YouTube Music",
    val durationSeconds: Int = 210,
    val artworkUrl: String = "",
    val source: String = "youtube",
    val genre: String = "Pop",
    val bitrateKbps: Int = 160,
    val codec: String = "AAC",
    val loudnessDb: Float = 0f,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null,
    val isExplicit: Boolean = false,
    val hasLyricsBadge: Boolean = true
) {
    val highResArtworkUrl: String
        get() = toHighResArtwork(artworkUrl, videoId)
}

/**
 * Normalizes artwork URLs from YouTube Music, Google User Content, and YouTube CDN
 * to fetch reliable high-definition images with zero 404 black screens.
 */
fun toHighResArtwork(url: String, videoId: String = ""): String {
    val cleanUrl = if (url.startsWith("//")) "https:$url" else url.trim()
    if (cleanUrl.isBlank() && videoId.isNotBlank()) {
        return "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
    }
    if (cleanUrl.isBlank()) return ""

    return when {
        // Google User Content / Ggpht (YouTube Music standard square CDN)
        cleanUrl.contains("googleusercontent.com") || cleanUrl.contains("ggpht.com") -> {
            // Strip any low-res signed query parameters (?sqp=...)
            val clean = cleanUrl.substringBefore("?")
            if (clean.contains("=")) {
                // Replaces =s60, =s120, =s540, =w60-h60, =w120-h120 with official YTM 544x544 square album art
                clean.replace(Regex("=.*$"), "=w544-h544-l90-rj")
            } else {
                "$clean=w544-h544-l90-rj"
            }
        }
        // Spotify CDN (already native crisp 640x640)
        cleanUrl.contains("i.scdn.co") -> cleanUrl
        // YouTube video thumbnails (img.youtube.com or i.ytimg.com)
        cleanUrl.contains("youtube.com") || cleanUrl.contains("ytimg.com") -> {
            // Keep hqdefault.jpg as it is guaranteed to exist on YouTube servers for 100% of videos
            // (hq720.jpg causes 404 and black screen for videos that were not uploaded in HD)
            cleanUrl.substringBefore("?")
        }
        else -> cleanUrl
    }
}

data class LyricWord(
    val word: String,
    val startTimeSec: Float,
    val endTimeSec: Float
)

data class LyricLine(
    val timeSec: Float,
    val endTimeSec: Float,
    val text: String,
    val words: List<LyricWord> = emptyList(),
    val romanizedText: String? = null
)

data class LyricsData(
    val videoId: String,
    val plainLyrics: String = "",
    val syncedLines: List<LyricLine> = emptyList(),
    val source: String = "SimpMusic RichSync",
    val hasRomanization: Boolean = false
)

data class SponsorSegment(
    val uuid: String,
    val startTimeSec: Float,
    val endTimeSec: Float,
    val category: String
) {
    val displayLabel: String
        get() = when (category) {
            "sponsor" -> "Sponsor"
            "selfpromo" -> "Self-Promotion"
            "interaction" -> "Interaction Reminder"
            "intro" -> "Intermission / Intro"
            "outro" -> "Endcards / Credits"
            "preview" -> "Recap / Preview"
            "music_offtopic" -> "Non-Music Section"
            else -> category.replaceFirstChar { it.uppercase() }
        }
}

data class ReturnYouTubeDislikeData(
    val likes: Long = 0L,
    val dislikes: Long = 0L,
    val viewCount: Long = 0L,
    val rating: Float = 5.0f
)

data class EqualizerPreset(
    val id: String,
    val name: String,
    val subtitle: String,
    val bandsDb: List<Float>, // 10 bands: 31Hz..16kHz (-12f..+12f)
    val preampGain: Float = 1.0f
)

val SIMPMUSIC_EQ_PRESETS = listOf(
    EqualizerPreset(
        id = "balanced",
        name = "Balanced Sound",
        subtitle = "Natural and clean audio profile",
        bandsDb = listOf(4.2f, 3.5f, 1.8f, -0.4f, -1.2f, 0.6f, 2.4f, 3.8f, 2.9f, 1.5f),
        preampGain = 0.92f
    ),
    EqualizerPreset(
        id = "bass_boost",
        name = "Bass Booster",
        subtitle = "Rich and deep low frequencies",
        bandsDb = listOf(6.5f, 5.2f, 3.4f, 1.0f, 0f, 0f, 0.5f, 1.2f, 1.5f, 1.0f),
        preampGain = 0.86f
    ),
    EqualizerPreset(
        id = "vocal_booster",
        name = "Vocal Booster",
        subtitle = "Enhanced vocals and speech clarity",
        bandsDb = listOf(-1.5f, -1.0f, 0.5f, 2.8f, 4.2f, 4.0f, 2.5f, 1.0f, 0f, -0.5f),
        preampGain = 0.94f
    ),
    EqualizerPreset(
        id = "treble_booster",
        name = "Treble Booster",
        subtitle = "Crisp, bright acoustic highs",
        bandsDb = listOf(4.5f, 3.8f, 1.2f, -1.0f, -1.5f, -0.5f, 2.0f, 4.0f, 5.2f, 4.8f),
        preampGain = 0.90f
    ),
    EqualizerPreset(
        id = "studio",
        name = "Studio Reference",
        subtitle = "Uncolored, authentic sound",
        bandsDb = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
        preampGain = 1.0f
    ),
    EqualizerPreset(
        id = "late_night",
        name = "Late Night Lounge",
        subtitle = "Soft dynamics for quiet listening",
        bandsDb = listOf(3.0f, 2.5f, 1.5f, 0.5f, 0f, -1.0f, -1.5f, -1.0f, 0.5f, 1.5f),
        preampGain = 0.88f
    )
)

data class MoodCategory(
    val title: String,
    val query: String,
    val accentHex: Long
)

data class YtmChartSection(
    val id: String,
    val title: String,
    val subtitle: String,
    val tracks: List<AuraTrack>
)

data class AuraPlaylist(
    val id: String,
    val name: String,
    val description: String,
    val coverUrl: String,
    val tracks: List<AuraTrack>
)

data class ListeningStats(
    val totalMinutes: Int = 0,
    val tracksPlayed: Int = 0,
    val topArtists: List<Pair<String, Int>> = emptyList(),
    val topGenres: List<Pair<String, Int>> = emptyList(),
    val peakHourLabel: String = "10:00 PM"
)

data class YtmTopArtist(
    val id: String,
    val name: String,
    val subscribers: String,
    val avatarUrl: String
) {
    val highResAvatarUrl: String
        get() = toHighResArtwork(avatarUrl)
}

data class YtmCommunityPlaylist(
    val id: String,
    val title: String,
    val author: String,
    val trackCount: String = "",
    val artworkUrl: String = ""
) {
    val highResArtworkUrl: String
        get() = toHighResArtwork(artworkUrl)
}

data class ArtistSong(
    val rank: Int,
    val track: AuraTrack,
    val playCount: String = ""
)

data class ArtistAlbum(
    val id: String,
    val title: String,
    val year: String = "",
    val artworkUrl: String = "",
    val type: String = "Album"
) {
    val highResArtworkUrl: String
        get() = toHighResArtwork(artworkUrl)
}

data class ArtistCarouselItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val artworkUrl: String = "",
    val endpointType: String = "album", // "album", "video", "playlist", "artist"
    val videoId: String = "",
    val browseId: String = ""
) {
    val highResArtworkUrl: String
        get() = toHighResArtwork(artworkUrl)
}

data class ArtistCarousel(
    val title: String,
    val items: List<ArtistCarouselItem>
)

data class ArtistDetail(
    val id: String,
    val name: String,
    val subscribers: String,
    val bannerUrl: String,
    val topSongs: List<ArtistSong>,
    val albums: List<ArtistAlbum> = emptyList(),
    val carousels: List<ArtistCarousel> = emptyList()
)

data class PlaylistTrackItem(
    val rank: Int,
    val track: AuraTrack,
    val viewCount: String = ""
)

data class PlaylistDetail(
    val id: String,
    val title: String,
    val author: String,
    val trackCount: String,
    val bannerUrl: String,
    val tracks: List<PlaylistTrackItem>,
    val totalViews: String = ""
)

data class ResolvedVideoStream(
    val videoId: String,
    val streamUrl: String,
    val quality: String = "720p",
    val durationSeconds: Int = 0,
    val isLive: Boolean = false,
    val audioUrl: String? = null
)


