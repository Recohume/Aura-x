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
        get() = when {
            artworkUrl.isNotBlank() && (artworkUrl.contains("googleusercontent.com") || artworkUrl.contains("ggpht.com")) ->
                artworkUrl.replace(Regex("=w\\d+-h\\d+[^&?]*"), "=w1200-h1200-l90-rj")
            artworkUrl.isNotBlank() && artworkUrl.contains("i.scdn.co") ->
                artworkUrl
            artworkUrl.isNotBlank() && artworkUrl.contains("youtube.com") ->
                artworkUrl.replace("hqdefault.jpg", "maxresdefault.jpg")
            artworkUrl.isNotBlank() -> artworkUrl
            videoId.isNotBlank() -> "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
            else -> ""
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
)

data class YtmCommunityPlaylist(
    val id: String,
    val title: String,
    val author: String,
    val trackCount: String,
    val artworkUrl: String
)

data class ArtistSong(
    val rank: Int,
    val track: AuraTrack,
    val playCount: String = ""
)

data class ArtistDetail(
    val id: String,
    val name: String,
    val subscribers: String,
    val bannerUrl: String,
    val topSongs: List<ArtistSong>
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
    val tracks: List<PlaylistTrackItem>
)

