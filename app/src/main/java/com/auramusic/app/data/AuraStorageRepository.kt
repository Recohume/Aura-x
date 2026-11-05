package com.auramusic.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.auramusic.app.domain.model.AuraPalette
import com.auramusic.app.domain.model.AuraPlaylist
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.LyricLine
import com.auramusic.app.domain.model.LyricsData
import com.auramusic.app.domain.model.ListeningStats
import com.auramusic.app.domain.model.NowPlayingStyle
import org.json.JSONArray
import org.json.JSONObject

class AuraStorageRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("aura_music_storage", Context.MODE_PRIVATE)

    fun loadFavorites(): List<AuraTrack> {
        val raw = prefs.getString("favorites_json", null) ?: return emptyList()
        return parseTrackArray(raw)
    }

    fun saveFavorites(tracks: List<AuraTrack>) {
        prefs.edit().putString("favorites_json", serializeTrackArray(tracks).toString()).apply()
    }

    fun loadDownloads(): List<AuraTrack> {
        val raw = prefs.getString("downloads_json", null) ?: return emptyList()
        return parseTrackArray(raw)
    }

    fun saveDownloads(tracks: List<AuraTrack>) {
        prefs.edit().putString("downloads_json", serializeTrackArray(tracks).toString()).apply()
    }

    fun loadPlaylists(): List<AuraPlaylist> {
        val raw = prefs.getString("playlists_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<AuraPlaylist>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val tracksArr = obj.optJSONArray("tracks") ?: JSONArray()
                list.add(
                    AuraPlaylist(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        description = obj.optString("description"),
                        coverUrl = obj.optString("coverUrl"),
                        tracks = parseTrackArray(tracksArr.toString())
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePlaylists(playlists: List<AuraPlaylist>) {
        val arr = JSONArray()
        playlists.forEach { pl ->
            arr.put(JSONObject().apply {
                put("id", pl.id)
                put("name", pl.name)
                put("description", pl.description)
                put("coverUrl", pl.coverUrl)
                put("tracks", serializeTrackArray(pl.tracks))
            })
        }
        prefs.edit().putString("playlists_json", arr.toString()).apply()
    }

    fun getLyricsOffsetMs(videoId: String): Int {
        return prefs.getInt("lyric_offset_$videoId", 0)
    }

    fun setLyricsOffsetMs(videoId: String, offsetMs: Int) {
        prefs.edit().putInt("lyric_offset_$videoId", offsetMs.coerceIn(-5000, 5000)).apply()
    }

    fun saveOfflineLyrics(videoId: String, lyrics: LyricsData) {
        try {
            val json = JSONObject().apply {
                put("videoId", lyrics.videoId)
                put("plainLyrics", lyrics.plainLyrics)
                put("source", lyrics.source)
                put("hasRomanization", lyrics.hasRomanization)
                val linesArr = JSONArray()
                lyrics.syncedLines.forEach { line ->
                    linesArr.put(JSONObject().apply {
                        put("timeSec", line.timeSec.toDouble())
                        put("endTimeSec", line.endTimeSec.toDouble())
                        put("text", line.text)
                        if (line.romanizedText != null) {
                            put("romanizedText", line.romanizedText)
                        }
                    })
                }
                put("syncedLines", linesArr)
            }
            prefs.edit().putString("offline_lyrics_$videoId", json.toString()).apply()
        } catch (e: Exception) {
            // Non-critical
        }
    }

    fun loadOfflineLyrics(videoId: String): LyricsData? {
        val raw = prefs.getString("offline_lyrics_$videoId", null) ?: return null
        return try {
            val obj = JSONObject(raw)
            val linesArr = obj.optJSONArray("syncedLines") ?: JSONArray()
            val lines = mutableListOf<LyricLine>()
            for (i in 0 until linesArr.length()) {
                val lObj = linesArr.optJSONObject(i) ?: continue
                lines.add(
                    LyricLine(
                        timeSec = lObj.optDouble("timeSec", 0.0).toFloat(),
                        endTimeSec = lObj.optDouble("endTimeSec", 0.0).toFloat(),
                        text = lObj.optString("text", ""),
                        romanizedText = if (lObj.has("romanizedText")) lObj.getString("romanizedText") else null
                    )
                )
            }
            LyricsData(
                videoId = obj.optString("videoId", videoId),
                plainLyrics = obj.optString("plainLyrics", ""),
                syncedLines = lines,
                source = obj.optString("source", "SimpMusic Offline"),
                hasRomanization = obj.optBoolean("hasRomanization", false)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveOfflinePalette(videoId: String, palette: AuraPalette) {
        try {
            val json = JSONObject().apply {
                put("primary", palette.primary.toArgb())
                put("secondary", palette.secondary.toArgb())
                put("backgroundTop", palette.backgroundTop.toArgb())
                put("backgroundBottom", palette.backgroundBottom.toArgb())
                put("surfaceGlass", palette.surfaceGlass.toArgb())
            }
            prefs.edit().putString("offline_palette_$videoId", json.toString()).apply()
        } catch (e: Exception) {
            // Non-critical
        }
    }

    fun loadOfflinePalette(videoId: String): AuraPalette? {
        val raw = prefs.getString("offline_palette_$videoId", null) ?: return null
        return try {
            val obj = JSONObject(raw)
            AuraPalette(
                primary = Color(obj.getInt("primary")),
                secondary = Color(obj.getInt("secondary")),
                backgroundTop = Color(obj.getInt("backgroundTop")),
                backgroundBottom = Color(obj.getInt("backgroundBottom")),
                surfaceGlass = Color(obj.getInt("surfaceGlass"))
            )
        } catch (e: Exception) {
            null
        }
    }

    fun loadNowPlayingStyle(): NowPlayingStyle {
        val name = prefs.getString("now_playing_style", NowPlayingStyle.SPOTIFY.name)
        return NowPlayingStyle.entries.firstOrNull { it.name == name } ?: NowPlayingStyle.SPOTIFY
    }

    fun saveNowPlayingStyle(style: NowPlayingStyle) {
        prefs.edit().putString("now_playing_style", style.name).apply()
    }

    fun recordRecentlyPlayed(track: AuraTrack) {
        val current = loadRecentlyPlayed().toMutableList()
        current.removeAll { it.videoId == track.videoId }
        current.add(0, track)
        val capped = current.take(30)
        prefs.edit().putString("recently_played_tracks", serializeTrackArray(capped).toString()).apply()
    }

    fun removeRecentlyPlayed(videoId: String) {
        val current = loadRecentlyPlayed().toMutableList()
        current.removeAll { it.videoId == videoId }
        prefs.edit().putString("recently_played_tracks", serializeTrackArray(current).toString()).apply()
    }

    fun loadRecentlyPlayed(): List<AuraTrack> {
        val raw = prefs.getString("recently_played_tracks", null) ?: return emptyList()
        return parseTrackArray(raw)
    }

    fun recordRecommendationSignal(track: AuraTrack, signal: String) {
        if (track.videoId.isBlank()) return
        runCatching {
            val all = JSONObject(prefs.getString("recommendation_signals", "{}") ?: "{}")
            val row = all.optJSONObject(track.videoId) ?: JSONObject()
            val key = when (signal) {
                "play" -> "plays"
                "complete" -> "completions"
                "skip" -> "skips"
                else -> return
            }
            row.put(key, row.optInt(key, 0) + 1)
            all.put(track.videoId, row)
            prefs.edit().putString("recommendation_signals", all.toString()).apply()
        }
    }

    fun loadRecommendationInteractionScores(): Map<String, Float> = runCatching {
        val all = JSONObject(prefs.getString("recommendation_signals", "{}") ?: "{}")
        all.keys().asSequence().mapNotNull { videoId ->
            val row = all.optJSONObject(videoId) ?: return@mapNotNull null
            val score = row.optInt("plays") * 0.45f +
                row.optInt("completions") * 1.4f -
                row.optInt("skips") * 2.2f
            videoId to score.coerceIn(-8f, 8f)
        }.toMap()
    }.getOrDefault(emptyMap())

    fun recordTrackPlay(track: AuraTrack): ListeningStats {
        recordRecentlyPlayed(track)
        val totalMin = prefs.getInt("stats_total_min", 48) + (track.durationSeconds / 60).coerceAtLeast(3)
        val totalPlays = prefs.getInt("stats_tracks_played", 14) + 1
        prefs.edit()
            .putInt("stats_total_min", totalMin)
            .putInt("stats_tracks_played", totalPlays)
            .apply()

        val artistsJson = JSONObject(prefs.getString("stats_artists", "{}") ?: "{}")
        val cleanArtist = track.artist.split(",", "ft.").firstOrNull()?.trim() ?: track.artist
        artistsJson.put(cleanArtist, artistsJson.optInt(cleanArtist, 0) + 1)
        prefs.edit().putString("stats_artists", artistsJson.toString()).apply()

        val genresJson = JSONObject(prefs.getString("stats_genres", "{}") ?: "{}")
        genresJson.put(track.genre, genresJson.optInt(track.genre, 0) + 1)
        prefs.edit().putString("stats_genres", genresJson.toString()).apply()

        return loadListeningStats()
    }

    fun loadListeningStats(): ListeningStats {
        val totalMin = prefs.getInt("stats_total_min", 48)
        val totalPlays = prefs.getInt("stats_tracks_played", 14)

        val artistsJson = JSONObject(prefs.getString("stats_artists", "{}") ?: "{}")
        val topArtists = artistsJson.keys().asSequence()
            .map { it to artistsJson.optInt(it, 1) }
            .sortedByDescending { it.second }
            .take(5)
            .toList()
            .ifEmpty {
                listOf("The Weeknd" to 8, "SZA" to 4, "Daft Punk" to 3)
            }

        val genresJson = JSONObject(prefs.getString("stats_genres", "{}") ?: "{}")
        val topGenres = genresJson.keys().asSequence()
            .map { it to genresJson.optInt(it, 1) }
            .sortedByDescending { it.second }
            .take(4)
            .toList()
            .ifEmpty {
                listOf("Synthwave / Pop" to 7, "R&B" to 5, "Electronic" to 3)
            }

        return ListeningStats(
            totalMinutes = totalMin,
            tracksPlayed = totalPlays,
            topArtists = topArtists,
            topGenres = topGenres,
            peakHourLabel = "10:00 PM – 1:00 AM"
        )
    }

    private fun serializeTrackArray(tracks: List<AuraTrack>): JSONArray {
        val arr = JSONArray()
        tracks.forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id)
                put("videoId", t.videoId)
                put("title", t.title)
                put("artist", t.artist)
                put("album", t.album)
                put("durationSeconds", t.durationSeconds)
                put("artworkUrl", t.artworkUrl)
                put("genre", t.genre)
                put("isDownloaded", t.isDownloaded)
                put("localFilePath", t.localFilePath ?: "")
            })
        }
        return arr
    }

    private fun parseTrackArray(rawJson: String): List<AuraTrack> {
        return try {
            val arr = JSONArray(rawJson)
            val out = mutableListOf<AuraTrack>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val vid = o.optString("videoId")
                if (vid.isBlank()) continue
                out.add(
                    AuraTrack(
                        id = o.optString("id", "yt_$vid"),
                        videoId = vid,
                        title = o.optString("title", "Unknown Track"),
                        artist = o.optString("artist", "Unknown Artist"),
                        album = o.optString("album", "YouTube Music"),
                        durationSeconds = o.optInt("durationSeconds", 210),
                        artworkUrl = o.optString("artworkUrl"),
                        genre = o.optString("genre", "Pop"),
                        isDownloaded = o.optBoolean("isDownloaded", false),
                        localFilePath = o.optString("localFilePath").takeIf { it.isNotBlank() }
                    )
                )
            }
            out
        } catch (e: Exception) {
            emptyList()
        }
    }
}
