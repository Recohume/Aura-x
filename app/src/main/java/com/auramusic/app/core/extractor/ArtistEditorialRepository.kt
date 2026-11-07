package com.auramusic.app.core.extractor

import android.content.Context
import android.util.Base64
import com.auramusic.app.domain.model.ArtistMotion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/** Apple Music editorial artist video lookup, matching SimpMusic's public catalog flow. */
object ArtistEditorialRepository {
    private const val PREFS = "artist_editorial_cache"
    private const val MAX_AGE_MS = 14L * 24 * 60 * 60 * 1000
    private val tokenLock = Any()
    @Volatile private var cachedToken: String? = null
    private val memoryCache = ConcurrentHashMap<String, ArtistMotion>()
    private val negativeCache = ConcurrentHashMap<String, Long>()

    suspend fun fetch(context: Context, artistName: String): ArtistMotion? = withContext(Dispatchers.IO) {
        val normalized = artistName.trim().lowercase()
        if (normalized.isBlank()) return@withContext null
        negativeCache[normalized]?.let { savedAt ->
            if (System.currentTimeMillis() - savedAt < MAX_AGE_MS) return@withContext null
            negativeCache.remove(normalized)
        }
        memoryCache[normalized]?.let { return@withContext it }
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val cacheKey = "artist_${normalized.hashCode()}"
        val cached = prefs.getString(cacheKey, null)
        if (cached != null) {
            runCatching {
                val row = JSONObject(cached)
                if (System.currentTimeMillis() - row.optLong("savedAt") < MAX_AGE_MS) {
                    val value = if (row.optBoolean("found")) row.toMotion() else null
                    if (value == null) negativeCache[normalized] = row.optLong("savedAt") else memoryCache[normalized] = value
                    return@withContext value
                }
            }
        }

        try {
            val token = token()
            val searchUrl = "https://amp-api-edge.music.apple.com/v1/catalog/us/search".toHttpUrl().newBuilder()
                .addQueryParameter("term", artistName)
                .addQueryParameter("types", "artists")
                .addQueryParameter("fields[artists]", "url,name,artwork")
                .addQueryParameter("art[url]", "f")
                .addQueryParameter("format[resources]", "map")
                .addQueryParameter("extend", "artistUrl")
                .addQueryParameter("l", "en-US")
                .addQueryParameter("limit", "1")
                .addQueryParameter("platform", "web")
                .build()
            val search = requestJson(searchUrl.toString(), token) ?: return@withContext null
            val ranked = search.optJSONObject("results")?.optJSONObject("artists")?.optJSONArray("data")
            val artistId = ranked?.optJSONObject(0)?.optString("id")?.takeIf { it.isNotBlank() }
                ?: search.optJSONObject("resources")?.optJSONObject("artists")?.keys()?.asSequence()?.firstOrNull()
                ?: return@withContext remember(context, cacheKey, normalized, null)

            val detailUrl = "https://amp-api.music.apple.com/v1/catalog/us/artists/$artistId".toHttpUrl().newBuilder()
                .addQueryParameter("art[url]", "c,f")
                .addQueryParameter("extend", "editorialArtwork,editorialVideo,hero,keyColor")
                .addQueryParameter("format[resources]", "map")
                .addQueryParameter("l", "en-US")
                .addQueryParameter("platform", "web")
                .build()
            val detail = requestJson(detailUrl.toString(), token) ?: return@withContext null
            val artist = detail.optJSONObject("resources")?.optJSONObject("artists")
                ?.optJSONObject(artistId)?.optJSONObject("attributes")
            val editorial = artist?.optJSONObject("editorialVideo")
                ?: return@withContext remember(context, cacheKey, normalized, null)
            val square = editorial.optJSONObject("motionArtistSquare1x1")
            val wide = editorial.optJSONObject("motionArtistWide16x9")
                ?: editorial.optJSONObject("motionArtistFullscreen16x9")
            val squareMaster = square?.optString("video")?.takeIf(String::isNotBlank)
            val wideMaster = wide?.optString("video")?.takeIf(String::isNotBlank)
            if (squareMaster == null && wideMaster == null) {
                return@withContext remember(context, cacheKey, normalized, null)
            }
            val motion = ArtistMotion(
                squareVideoUrl = squareMaster?.let(::selectH264Rendition) ?: squareMaster,
                squarePreviewUrl = previewUrl(square?.optJSONObject("previewFrame"), 640, 640),
                wideVideoUrl = wideMaster?.let(::selectH264Rendition) ?: wideMaster,
                widePreviewUrl = previewUrl(wide?.optJSONObject("previewFrame"), 640, 360)
            )
            remember(context, cacheKey, normalized, motion)
        } catch (_: Exception) {
            // Network errors are deliberately not cached; a later screen visit can retry.
            null
        }
    }

    private fun remember(context: Context, key: String, artist: String, motion: ArtistMotion?): ArtistMotion? {
        if (motion == null) negativeCache[artist] = System.currentTimeMillis() else memoryCache[artist] = motion
        val row = JSONObject().put("savedAt", System.currentTimeMillis()).put("found", motion != null)
        if (motion != null) {
            row.put("squareVideo", motion.squareVideoUrl.orEmpty())
                .put("squarePreview", motion.squarePreviewUrl.orEmpty())
                .put("wideVideo", motion.wideVideoUrl.orEmpty())
                .put("widePreview", motion.widePreviewUrl.orEmpty())
        }
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(key, row.toString()).apply()
        return motion
    }

    private fun JSONObject.toMotion() = ArtistMotion(
        squareVideoUrl = optString("squareVideo").takeIf(String::isNotBlank),
        squarePreviewUrl = optString("squarePreview").takeIf(String::isNotBlank),
        wideVideoUrl = optString("wideVideo").takeIf(String::isNotBlank),
        widePreviewUrl = optString("widePreview").takeIf(String::isNotBlank)
    )

    private fun previewUrl(art: JSONObject?, width: Int, height: Int): String? {
        val template = art?.optString("url")?.takeIf(String::isNotBlank) ?: return null
        return template.replace("{w}", width.toString()).replace("{h}", height.toString()).replace("{c}", "").replace("{f}", "jpg")
    }

    private fun requestJson(url: String, initialToken: String): JSONObject? {
        fun get(token: String) = Request.Builder().url(url)
            .header("Authorization", "Bearer $token")
            .header("Origin", "https://music.apple.com")
            .header("Referer", "https://music.apple.com/")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/131.0.0.0 Safari/537.36")
            .header("Accept", "application/json").build()
        var response = SimpMusicExtractorEngine.httpClient.newCall(get(initialToken)).execute()
        if (response.code == 401) {
            response.close()
            synchronized(tokenLock) { cachedToken = null }
            response = SimpMusicExtractorEngine.httpClient.newCall(get(token(forceRefresh = true))).execute()
        }
        return response.use { if (it.isSuccessful) JSONObject(it.body?.string() ?: "{}") else null }
    }

    private fun token(forceRefresh: Boolean = false): String {
        if (!forceRefresh) cachedToken?.let { return it }
        synchronized(tokenLock) { cachedToken?.takeIf { !forceRefresh }?.let { return it } }
        val homepage = Request.Builder().url("https://music.apple.com/").get().build()
        val html = SimpMusicExtractorEngine.httpClient.newCall(homepage).execute().use {
            if (!it.isSuccessful) error("Apple Music home request failed") else it.body?.string().orEmpty()
        }
        val bundlePath = Regex("/assets/index~[^/]+\\.js").find(html)?.value
            ?: error("Apple Music web bundle was not found")
        val bundleUrl = "https://music.apple.com$bundlePath"
        val bundle = SimpMusicExtractorEngine.httpClient.newCall(Request.Builder().url(bundleUrl).get().build()).execute().use {
            if (!it.isSuccessful) error("Apple Music web bundle request failed") else it.body?.string().orEmpty()
        }
        val candidates = Regex("eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+").findAll(bundle).map { it.value }
        val value = candidates.firstOrNull { jwt ->
            runCatching {
                val payload = jwt.split('.')[1].let { it + "=".repeat((4 - it.length % 4) % 4) }
                String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)).contains("\"iss\":\"AMPWebPlay\"")
            }.getOrDefault(false)
        } ?: candidates.firstOrNull() ?: error("Apple Music bearer token not found")
        synchronized(tokenLock) { cachedToken = value }
        return value
    }

    private fun selectH264Rendition(masterUrl: String): String? = try {
        SimpMusicExtractorEngine.httpClient.newCall(Request.Builder().url(masterUrl).get().build()).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string().orEmpty()
            val variants = Regex("#EXT-X-STREAM-INF:([^\\r\\n]*)\\r?\\n([^\\r\\n]+)").findAll(body).mapNotNull { match ->
                val attrs = match.groupValues[1]
                val resolution = Regex("RESOLUTION=(\\d+)x(\\d+)").find(attrs) ?: return@mapNotNull null
                val codecs = Regex("CODECS=\\\"([^\\\"]+)\\\"").find(attrs)?.groupValues?.get(1).orEmpty()
                val width = resolution.groupValues[1].toIntOrNull() ?: return@mapNotNull null
                val bandwidth = Regex("(?:^|,)BANDWIDTH=(\\d+)").find(attrs)?.groupValues?.get(1)?.toIntOrNull() ?: Int.MAX_VALUE
                Triple(width, bandwidth, match.groupValues[2].trim()).takeIf { codecs.startsWith("avc1") }
            }.toList()
            val h264 = variants.ifEmpty {
                Regex("#EXT-X-STREAM-INF:([^\\r\\n]*)\\r?\\n([^\\r\\n]+)").findAll(body).mapNotNull { m ->
                    val width = Regex("RESOLUTION=(\\d+)x").find(m.groupValues[1])?.groupValues?.get(1)?.toIntOrNull() ?: return@mapNotNull null
                    val bitrate = Regex("(?:^|,)BANDWIDTH=(\\d+)").find(m.groupValues[1])?.groupValues?.get(1)?.toIntOrNull() ?: Int.MAX_VALUE
                    Triple(width, bitrate, m.groupValues[2].trim())
                }.toList()
            }
            val chosen = h264.filter { it.first >= 640 }.minByOrNull { it.first } ?: h264.maxByOrNull { it.first }
            chosen?.third?.let { path ->
                when {
                    path.startsWith("http") -> path
                    path.startsWith("/") -> "https://${masterUrl.substringAfter("https://").substringBefore('/')}$path"
                    else -> masterUrl.substringBeforeLast('/') + "/" + path
                }
            }
        }
    } catch (_: Exception) { null }
}
