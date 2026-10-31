package com.auramusic.app.core.extractor

import android.util.Log
import com.auramusic.app.domain.model.AuraPlaylist
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.MoodCategory
import com.auramusic.app.domain.model.YtmChartSection
import dev.maxrave.pipepipe.extractor.NewPipe as PipePipe
import dev.maxrave.pipepipe.extractor.ServiceList as PipePipeServiceList
import dev.maxrave.pipepipe.extractor.downloader.Downloader as PipePipeDownloader
import dev.maxrave.pipepipe.extractor.downloader.Request as PipePipeRequest
import dev.maxrave.pipepipe.extractor.downloader.Response as PipePipeResponse
import dev.maxrave.pipepipe.extractor.exceptions.ReCaptchaException as PipePipeReCaptchaException
import dev.maxrave.pipepipe.extractor.services.youtube.YoutubeApiDecoder
import dev.maxrave.pipepipe.extractor.services.youtube.extractor.YoutubeStreamExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.schabi.newpipe.extractor.NewPipe as BraveNewPipe
import org.schabi.newpipe.extractor.ServiceList as BraveServiceList
import org.schabi.newpipe.extractor.downloader.Downloader as BraveDownloader
import org.schabi.newpipe.extractor.downloader.Request as BraveRequest
import org.schabi.newpipe.extractor.downloader.Response as BraveResponse
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException as BraveReCaptchaException
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class ResolvedStream(
    val videoId: String,
    val streamUrl: String,
    val mimeType: String = "audio/mp4",
    val bitrateKbps: Int = 160,
    val codec: String = "AAC",
    val loudnessDb: Float = 0f,
    val durationSeconds: Int = 0,
    val sourceTier: String = "PipePipeExtractor",
    val expiresAtMs: Long = System.currentTimeMillis() + 5 * 3600 * 1000L
)

object SimpMusicExtractorEngine {
    private const val TAG = "SimpMusicExtractor"
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Tier 0 In-memory Resolved Stream Cache (videoId -> ResolvedStream)
    private val streamCache = ConcurrentHashMap<String, ResolvedStream>()

    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    private val fastVerifyClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(4, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    @Volatile
    private var isInitialized = false

    fun initExtractors() {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            try {
                // 1. Initialize SimpMusic PipePipeExtractor (uses api.pipepipe.dev remote decoder when localDecoder is null)
                YoutubeApiDecoder.setLocalDecoder(null)
                PipePipe.init(object : PipePipeDownloader() {
                    override fun execute(request: PipePipeRequest): PipePipeResponse {
                        val builder = Request.Builder()
                            .url(request.url())
                            .header("User-Agent", USER_AGENT)
                        request.headers().forEach { (key, values) ->
                            builder.removeHeader(key)
                            values.forEach { v -> builder.addHeader(key, v) }
                        }
                        val bodyBytes = request.dataToSend()
                        val reqBody = bodyBytes?.toRequestBody(null, 0, bodyBytes.size)
                        builder.method(request.httpMethod(), reqBody)

                        httpClient.newCall(builder.build()).execute().use { resp ->
                            if (resp.code == 429) {
                                throw PipePipeReCaptchaException("reCaptcha Challenge requested", request.url())
                            }
                            val bodyStr = resp.body?.string() ?: ""
                            return PipePipeResponse(
                                resp.code,
                                resp.message,
                                resp.headers.toMultimap(),
                                bodyStr,
                                resp.request.url.toString()
                            )
                        }
                    }
                })
            } catch (e: Exception) {
                Log.w(TAG, "PipePipeExtractor init warning: ${e.message}")
            }

            try {
                // 2. Initialize SmartTube / BraveNewPipe Extractor
                BraveNewPipe.init(
                    object : BraveDownloader() {
                        override fun execute(request: BraveRequest): BraveResponse {
                            val builder = Request.Builder()
                                .url(request.url())
                                .header("User-Agent", USER_AGENT)
                            request.headers().forEach { (key, values) ->
                                builder.removeHeader(key)
                                values.forEach { v -> builder.addHeader(key, v) }
                            }
                            val bodyBytes = request.dataToSend()
                            val reqBody = bodyBytes?.toRequestBody(null, 0, bodyBytes.size)
                            builder.method(request.httpMethod(), reqBody)

                            httpClient.newCall(builder.build()).execute().use { resp ->
                                if (resp.code == 429) {
                                    throw BraveReCaptchaException("reCaptcha Challenge requested", request.url())
                                }
                                val bodyStr = resp.body?.string() ?: ""
                                return BraveResponse(
                                    resp.code,
                                    resp.message,
                                    resp.headers.toMultimap(),
                                    bodyStr,
                                    resp.request.url.toString()
                                )
                            }
                        }
                    },
                    Localization.DEFAULT,
                    ContentCountry.DEFAULT
                )
            } catch (e: Exception) {
                Log.w(TAG, "BraveNewPipe init warning: ${e.message}")
            }
            isInitialized = true
        }
    }

    fun getCachedStream(videoId: String): ResolvedStream? {
        val cached = streamCache[videoId] ?: return null
        if (cached.expiresAtMs > System.currentTimeMillis() + 60_000L) {
            return cached
        }
        streamCache.remove(videoId)
        return null
    }

    fun invalidateStream(videoId: String) {
        streamCache.remove(videoId)
    }

    fun prefetchStream(videoId: String) {
        if (videoId.isBlank() || getCachedStream(videoId) != null) return
        backgroundScope.launch {
            try {
                resolveStream(videoId)
            } catch (e: Exception) {
                Log.d(TAG, "Prefetch skipped for $videoId: ${e.message}")
            }
        }
    }

    /**
     * SimpMusic is403Url equivalent: verifies the stream URL responds with HTTP 200 or 206
     * and returns the final redirected URL if any.
     */
    private fun verifyPlayableUrl(candidateUrl: String): String? {
        return try {
            val req = Request.Builder()
                .url(candidateUrl)
                .header("User-Agent", USER_AGENT)
                .header("Range", "bytes=0-1024")
                .get()
                .build()
            fastVerifyClient.newCall(req).execute().use { resp ->
                if (resp.code in 200..299) {
                    resp.request.url.toString()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Multi-tier SimpMusic stream extraction:
     * Tier 0: Memory cache (0ms)
     * Tier 1: PipePipeExtractor (SimpMusic official engine + remote/local cipher)
     * Tier 2: BraveNewPipe Extractor (SmartTube engine)
     * Tier 3: Fast pre-warmed Invidious/Piped local=true proxy stream (~230ms)
     */
    suspend fun resolveStream(videoId: String): ResolvedStream = withContext(Dispatchers.IO) {
        initExtractors()

        getCachedStream(videoId)?.let { return@withContext it }

        // Tier 1: PipePipeExtractor
        try {
            val url = "https://music.youtube.com/watch?v=$videoId"
            val extractor = PipePipeServiceList.YouTube.getStreamExtractor(url) as YoutubeStreamExtractor
            extractor.fetchPage()
            val audioStreams = extractor.audioStreams
            if (!audioStreams.isNullOrEmpty()) {
                val bestM4a = audioStreams
                    .filter { it.format?.suffix?.equals("m4a", ignoreCase = true) == true }
                    .maxByOrNull { it.averageBitrate }
                val selected = bestM4a ?: audioStreams.maxByOrNull { it.averageBitrate } ?: audioStreams.first()
                val rawUrl = selected.content
                val verifiedUrl = verifyPlayableUrl(rawUrl)
                if (verifiedUrl != null) {
                    val resolved = ResolvedStream(
                        videoId = videoId,
                        streamUrl = verifiedUrl,
                        mimeType = selected.format?.mimeType ?: "audio/mp4",
                        bitrateKbps = selected.averageBitrate.takeIf { it > 0 } ?: 160,
                        codec = if (selected.format?.suffix == "m4a") "AAC" else "OPUS",
                        durationSeconds = extractor.length.toInt().coerceAtLeast(0),
                        sourceTier = "PipePipeExtractor (SimpMusic Native)"
                    )
                    streamCache[videoId] = resolved
                    Log.i(TAG, "Tier 1 PipePipeExtractor resolved $videoId (${resolved.bitrateKbps}kbps)")
                    return@withContext resolved
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Tier 1 PipePipeExtractor notice for $videoId: ${e.message}")
        }

        // Tier 2: BraveNewPipe Extractor
        try {
            val url = "https://www.youtube.com/watch?v=$videoId"
            val streamInfo = StreamInfo.getInfo(BraveServiceList.YouTube, url)
            val audioStreams = streamInfo.audioStreams
            if (!audioStreams.isNullOrEmpty()) {
                val bestM4a = audioStreams
                    .filter { it.format?.suffix?.equals("m4a", ignoreCase = true) == true }
                    .maxByOrNull { it.averageBitrate }
                val selected = bestM4a ?: audioStreams.maxByOrNull { it.averageBitrate } ?: audioStreams.first()
                val verifiedUrl = verifyPlayableUrl(selected.content)
                if (verifiedUrl != null) {
                    val resolved = ResolvedStream(
                        videoId = videoId,
                        streamUrl = verifiedUrl,
                        mimeType = selected.format?.mimeType ?: "audio/mp4",
                        bitrateKbps = selected.averageBitrate.takeIf { it > 0 } ?: 160,
                        codec = if (selected.format?.suffix == "m4a") "AAC" else "OPUS",
                        durationSeconds = streamInfo.duration.toInt().coerceAtLeast(0),
                        sourceTier = "NewPipeExtractor (SmartTube Native)"
                    )
                    streamCache[videoId] = resolved
                    Log.i(TAG, "Tier 2 BraveNewPipe resolved $videoId (${resolved.bitrateKbps}kbps)")
                    return@withContext resolved
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Tier 2 BraveNewPipe notice for $videoId: ${e.message}")
        }

        // Tier 3: Pre-warmed direct companion proxy (follows 302 redirect once so ExoPlayer starts in ~230ms)
        val proxyCandidates = listOf(
            "https://yt.chocolatemoo53.com/latest_version?id=$videoId&itag=140&local=true",
            "https://inv.nadeko.net/latest_version?id=$videoId&itag=140&local=true",
            "https://invidious.nerdvpn.de/latest_version?id=$videoId&itag=140&local=true"
        )
        for (candidate in proxyCandidates) {
            val finalUrl = verifyPlayableUrl(candidate)
            if (finalUrl != null) {
                val resolved = ResolvedStream(
                    videoId = videoId,
                    streamUrl = finalUrl,
                    mimeType = "audio/mp4",
                    bitrateKbps = 130,
                    codec = "AAC",
                    sourceTier = "Direct Edge Stream (Pre-Warmed)"
                )
                streamCache[videoId] = resolved
                Log.i(TAG, "Tier 3 Direct Edge Stream resolved $videoId -> $finalUrl")
                return@withContext resolved
            }
        }

        throw IllegalStateException("Unable to resolve playable audio stream for $videoId")
    }

    /**
     * Direct YouTube Music WEB_REMIX search (`https://music.youtube.com/youtubei/v1/search`)
     * with NewPipeExtractor fallback.
     */
    suspend fun searchYouTubeMusic(query: String, filter: String = "songs"): List<AuraTrack> =
        withContext(Dispatchers.IO) {
            if (query.isBlank()) return@withContext emptyList()
            try {
                // Params for YouTube Music Songs filter vs Videos filter
                val params = when (filter.lowercase()) {
                    "songs" -> "EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D"
                    "videos" -> "EgWKAQIQAWoKEAkQChAFEAMQBA%3D%3D"
                    else -> null
                }
                val payload = JSONObject().apply {
                    put("context", JSONObject().apply {
                        put("client", JSONObject().apply {
                            put("clientName", "WEB_REMIX")
                            put("clientVersion", "1.20250312.01.00")
                            put("hl", "en")
                            put("gl", "US")
                        })
                    })
                    put("query", query)
                    if (params != null) {
                        put("params", params)
                    }
                }

                val req = Request.Builder()
                    .url("https://music.youtube.com/youtubei/v1/search?prettyPrint=false")
                    .header("User-Agent", USER_AGENT)
                    .header("Origin", "https://music.youtube.com")
                    .header("Referer", "https://music.youtube.com/")
                    .post(payload.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val tracks = mutableListOf<AuraTrack>()
                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val bodyStr = resp.body?.string() ?: ""
                        parseYtmSearchTracks(JSONObject(bodyStr), tracks)
                    }
                }

                if (tracks.isNotEmpty()) {
                    // Proactively pre-warm the top search result for instant 0ms playback when tapped!
                    prefetchStream(tracks.first().videoId)
                    return@withContext tracks
                }
            } catch (e: Exception) {
                Log.w(TAG, "WEB_REMIX search fallback to NewPipe: ${e.message}")
            }

            // Fallback to BraveNewPipe search
            try {
                initExtractors()
                val extractor = BraveServiceList.YouTube.getSearchExtractor(query)
                extractor.fetchPage()
                val items = extractor.initialPage?.items ?: emptyList()
                val results = items.filterIsInstance<StreamInfoItem>().take(20).mapNotNull { item ->
                    val vidId = item.url?.substringAfter("v=")?.substringBefore("&") ?: return@mapNotNull null
                    if (vidId.isBlank()) return@mapNotNull null
                    AuraTrack(
                        id = "yt_$vidId",
                        videoId = vidId,
                        title = item.name ?: "Unknown Title",
                        artist = (item.uploaderName ?: "YouTube Artist")
                            .replace(" - Topic", "")
                            .trim(),
                        album = "YouTube Music",
                        durationSeconds = item.duration.toInt().coerceAtLeast(180),
                        artworkUrl = item.thumbnails?.lastOrNull()?.url
                            ?: "https://img.youtube.com/vi/$vidId/hqdefault.jpg"
                    )
                }
                results.firstOrNull()?.let { prefetchStream(it.videoId) }
                results
            } catch (e: Exception) {
                Log.e(TAG, "Search failed: ${e.message}")
                emptyList()
            }
        }

    /**
     * Fetches SimpMusic endless Radio / Automix queue via `youtubei/v1/next` (`RDAMVM{videoId}`).
     */
    suspend fun fetchRadioQueue(videoId: String): List<AuraTrack> = withContext(Dispatchers.IO) {
        if (videoId.isBlank()) return@withContext emptyList()
        try {
            val payload = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB_REMIX")
                        put("clientVersion", "1.20250312.01.00")
                        put("hl", "en")
                        put("gl", "US")
                    })
                })
                put("videoId", videoId)
                put("playlistId", "RDAMVM$videoId")
                put("isAudioOnly", true)
            }

            val req = Request.Builder()
                .url("https://music.youtube.com/youtubei/v1/next?prettyPrint=false")
                .header("User-Agent", USER_AGENT)
                .header("Origin", "https://music.youtube.com")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val queue = mutableListOf<AuraTrack>()
            httpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val root = JSONObject(resp.body?.string() ?: "{}")
                    val tabs = root.optJSONObject("contents")
                        ?.optJSONObject("singleColumnMusicWatchNextResultsRenderer")
                        ?.optJSONObject("tabbedRenderer")
                        ?.optJSONObject("watchNextTabbedResultsRenderer")
                        ?.optJSONArray("tabs")

                    val playlistItems = tabs?.optJSONObject(0)
                        ?.optJSONObject("tabRenderer")
                        ?.optJSONObject("content")
                        ?.optJSONObject("musicQueueRenderer")
                        ?.optJSONObject("content")
                        ?.optJSONObject("playlistPanelRenderer")
                        ?.optJSONArray("contents")

                    if (playlistItems != null) {
                        for (i in 0 until playlistItems.length()) {
                            val renderer = playlistItems.optJSONObject(i)
                                ?.optJSONObject("playlistPanelVideoRenderer") ?: continue
                            val itemVidId = renderer.optString("videoId")
                            if (itemVidId.isBlank() || itemVidId == videoId) continue

                            val title = renderer.optJSONObject("title")
                                ?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: continue
                            val artist = renderer.optJSONObject("shortBylineText")
                                ?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
                                ?.replace(" - Topic", "") ?: "YouTube Artist"
                            val lengthText = renderer.optJSONObject("lengthText")
                                ?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: "3:30"
                            val thumbs = renderer.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                            val thumbUrl = thumbs?.optJSONObject(thumbs.length() - 1)?.optString("url")
                                ?: "https://img.youtube.com/vi/$itemVidId/hqdefault.jpg"

                            queue.add(
                                AuraTrack(
                                    id = "yt_$itemVidId",
                                    videoId = itemVidId,
                                    title = title,
                                    artist = artist,
                                    durationSeconds = parseDurationText(lengthText),
                                    artworkUrl = thumbUrl
                                )
                            )
                        }
                    }
                }
            }
            queue.firstOrNull()?.let { prefetchStream(it.videoId) }
            queue
        } catch (e: Exception) {
            Log.w(TAG, "Radio queue fetch warning: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetches curated YouTube Music Charts & Moods for the Home screen.
     */
    suspend fun fetchHomeChartsAndMoods(): Pair<List<YtmChartSection>, List<MoodCategory>> =
        withContext(Dispatchers.IO) {
            val moods = listOf(
                MoodCategory("Late Night Vibes", "late night R&B chill hits official audio", 0xFF8B5CF6),
                MoodCategory("Deep Focus", "ambient electronic focus study beats", 0xFF06B6D4),
                MoodCategory("Workout Rush", "high energy phonk workout gym hits", 0xFFEC4899),
                MoodCategory("Golden Hour", "indie pop sunset chill tracks", 0xFFF59E0B),
                MoodCategory("K-Pop Radar", "top kpop hits 2025 official audio", 0xFF10B981),
                MoodCategory("J-Pop & Anime", "yoasobi ado kenshi yonezu official audio", 0xFF6366F1),
                MoodCategory("90s & 00s Throwback", "2000s r&b pop classics official audio", 0xFFEF4444),
                MoodCategory("Acoustic Unplugged", "acoustic stripped cover sessions", 0xFF14B8A6)
            )

            val trendingDeferred = async { searchYouTubeMusic("Global Top 50 Pop Hits 2025", "songs") }
            val rnbDeferred = async { searchYouTubeMusic("The Weeknd SZA Drake R&B Hits", "songs") }
            val electronicDeferred = async { searchYouTubeMusic("Fred again Daft Punk Kaytranada", "songs") }

            val trending = trendingDeferred.await().ifEmpty { DEFAULT_SEED_TRACKS }
            val rnb = rnbDeferred.await()
            val electronic = electronicDeferred.await()

            val sections = buildList {
                add(
                    YtmChartSection(
                        id = "global_pulse",
                        title = "Global Pulse Charts",
                        subtitle = "Real-time YouTube Music WEB_REMIX streams",
                        tracks = trending.take(10)
                    )
                )
                if (rnb.isNotEmpty()) {
                    add(
                        YtmChartSection(
                            id = "midnight_velvet",
                            title = "Midnight R&B & Soul",
                            subtitle = "Curated for Warm Obsidian acoustics",
                            tracks = rnb.take(10)
                        )
                    )
                }
                if (electronic.isNotEmpty()) {
                    add(
                        YtmChartSection(
                            id = "club_acoustics",
                            title = "Electronic & Future Garage",
                            subtitle = "High-dynamic range spatial selections",
                            tracks = electronic.take(10)
                        )
                    )
                }
            }

            Pair(sections, moods)
        }

    /**
     * Direct Client-First Spotify Public Playlist Importer:
     * Fetches `https://open.spotify.com/embed/playlist/{playlistId}`, extracts `__NEXT_DATA__` tracklist,
     * and resolves matching YouTube Music tracks in parallel without any backend server.
     */
    suspend fun fetchSpotifyPlaylistDirect(
        playlistUrl: String,
        onProgress: (resolved: Int, total: Int) -> Unit = { _, _ -> }
    ): AuraPlaylist? = withContext(Dispatchers.IO) {
        val playlistId = Regex("playlist/([a-zA-Z0-9]+)").find(playlistUrl)?.groupValues?.getOrNull(1)
            ?: return@withContext null

        val embedUrl = "https://open.spotify.com/embed/playlist/$playlistId"
        val req = Request.Builder()
            .url(embedUrl)
            .header("User-Agent", USER_AGENT)
            .build()

        val html = httpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext null
            resp.body?.string() ?: return@withContext null
        }

        val nextDataJson = Regex("<script id=\"__NEXT_DATA__\" type=\"application/json\">(.*?)</script>", RegexOption.DOT_MATCHES_ALL)
            .find(html)?.groupValues?.getOrNull(1) ?: return@withContext null

        val root = JSONObject(nextDataJson)
        val entity = root.optJSONObject("props")
            ?.optJSONObject("pageProps")
            ?.optJSONObject("state")
            ?.optJSONObject("data")
            ?.optJSONObject("entity") ?: return@withContext null

        val playlistName = entity.optString("name", "Spotify Imported Playlist")
        val coverUrl = entity.optJSONObject("coverArt")
            ?.optJSONArray("sources")
            ?.optJSONObject(0)
            ?.optString("url") ?: ""

        val trackListArray = entity.optJSONArray("trackList") ?: JSONArray()
        val totalToImport = trackListArray.length().coerceAtMost(25)
        val rawSeeds = mutableListOf<Pair<String, String>>()

        for (i in 0 until totalToImport) {
            val item = trackListArray.optJSONObject(i) ?: continue
            val title = item.optString("title").trim()
            val subtitle = item.optString("subtitle").replace("\u00A0", " ").trim()
            if (title.isNotEmpty()) {
                rawSeeds.add(title to subtitle)
            }
        }

        val resolvedTracks = mutableListOf<AuraTrack>()
        var completed = 0
        rawSeeds.chunked(5).forEach { batch ->
            val results = batch.map { (title, artist) ->
                async {
                    val match = searchYouTubeMusic("$title $artist", "songs").firstOrNull()
                    completed++
                    onProgress(completed, rawSeeds.size)
                    match
                }
            }.awaitAll().filterNotNull()
            resolvedTracks.addAll(results)
        }

        AuraPlaylist(
            id = "spotify_$playlistId",
            name = playlistName,
            description = "Imported directly from Spotify (${resolvedTracks.size} tracks matched on YouTube Music)",
            coverUrl = coverUrl.ifBlank { resolvedTracks.firstOrNull()?.highResArtworkUrl ?: "" },
            tracks = resolvedTracks
        )
    }

    private fun parseYtmSearchTracks(root: JSONObject, out: MutableList<AuraTrack>) {
        val tabs = root.optJSONObject("contents")
            ?.optJSONObject("tabbedSearchResultsRenderer")
            ?.optJSONArray("tabs") ?: return

        for (t in 0 until tabs.length()) {
            val sectionContents = tabs.optJSONObject(t)
                ?.optJSONObject("tabRenderer")
                ?.optJSONObject("content")
                ?.optJSONObject("sectionListRenderer")
                ?.optJSONArray("contents") ?: continue

            for (s in 0 until sectionContents.length()) {
                val shelfItems = sectionContents.optJSONObject(s)
                    ?.optJSONObject("musicShelfRenderer")
                    ?.optJSONArray("contents") ?: continue

                for (i in 0 until shelfItems.length()) {
                    val item = shelfItems.optJSONObject(i)
                        ?.optJSONObject("musicResponsiveListItemRenderer") ?: continue

                    val videoId = item.optJSONObject("playlistItemData")?.optString("videoId")
                        ?: item.optJSONObject("overlay")
                            ?.optJSONObject("musicItemThumbnailOverlayRenderer")
                            ?.optJSONObject("content")
                            ?.optJSONObject("musicPlayButtonRenderer")
                            ?.optJSONObject("playNavigationEndpoint")
                            ?.optJSONObject("watchEndpoint")
                            ?.optString("videoId")
                        ?: continue

                    if (videoId.isBlank()) continue

                    val flexColumns = item.optJSONArray("flexColumns") ?: continue
                    val title = flexColumns.optJSONObject(0)
                        ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                        ?.optJSONObject("text")
                        ?.optJSONArray("runs")
                        ?.optJSONObject(0)
                        ?.optString("text") ?: continue

                    val subtitleRuns = flexColumns.optJSONObject(1)
                        ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                        ?.optJSONObject("text")
                        ?.optJSONArray("runs")

                    var artist = "YouTube Artist"
                    var album = "YouTube Music"
                    var durationSec = 210

                    if (subtitleRuns != null) {
                        val texts = mutableListOf<String>()
                        for (r in 0 until subtitleRuns.length()) {
                            val txt = subtitleRuns.optJSONObject(r)?.optString("text")?.trim() ?: ""
                            if (txt.isNotEmpty() && txt != "•" && txt != "&") {
                                texts.add(txt)
                            }
                        }
                        if (texts.isNotEmpty()) {
                            artist = if (texts[0].equals("Song", ignoreCase = true) || texts[0].equals("Video", ignoreCase = true)) {
                                texts.getOrNull(1) ?: "YouTube Artist"
                            } else {
                                texts[0]
                            }
                        }
                        texts.firstOrNull { it.matches(Regex("^\\d+:\\d{2}(:\\d{2})?$")) }?.let {
                            durationSec = parseDurationText(it)
                        }
                        if (texts.size >= 3) {
                            album = texts[2]
                        }
                    }

                    val thumbs = item.optJSONObject("thumbnail")
                        ?.optJSONObject("musicThumbnailRenderer")
                        ?.optJSONObject("thumbnail")
                        ?.optJSONArray("thumbnails")
                    val rawThumb = thumbs?.optJSONObject(thumbs.length() - 1)?.optString("url")
                        ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

                    out.add(
                        AuraTrack(
                            id = "yt_$videoId",
                            videoId = videoId,
                            title = title,
                            artist = artist.replace(" - Topic", "").trim(),
                            album = album,
                            durationSeconds = durationSec,
                            artworkUrl = rawThumb
                        )
                    )
                }
            }
        }
    }

    private fun parseDurationText(text: String): Int {
        val parts = text.trim().split(":").mapNotNull { it.toIntOrNull() }
        return when (parts.size) {
            2 -> parts[0] * 60 + parts[1]
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
            else -> 210
        }
    }

    val DEFAULT_SEED_TRACKS = listOf(
        AuraTrack(
            id = "yt_4NRXx6U8ABQ",
            videoId = "4NRXx6U8ABQ",
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            durationSeconds = 200,
            artworkUrl = "https://img.youtube.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
            genre = "Synthwave / Pop"
        ),
        AuraTrack(
            id = "yt_fHI8X4OXluQ",
            videoId = "fHI8X4OXluQ",
            title = "Save Your Tears",
            artist = "The Weeknd",
            album = "After Hours",
            durationSeconds = 215,
            artworkUrl = "https://img.youtube.com/vi/fHI8X4OXluQ/hqdefault.jpg",
            genre = "Synthpop"
        ),
        AuraTrack(
            id = "yt_34Na4j8AVgA",
            videoId = "34Na4j8AVgA",
            title = "Starboy",
            artist = "The Weeknd ft. Daft Punk",
            album = "Starboy",
            durationSeconds = 230,
            artworkUrl = "https://img.youtube.com/vi/34Na4j8AVgA/hqdefault.jpg",
            genre = "R&B / Electronic"
        ),
        AuraTrack(
            id = "yt_qFLhGq0060w",
            videoId = "qFLhGq0060w",
            title = "I Feel It Coming",
            artist = "The Weeknd ft. Daft Punk",
            album = "Starboy",
            durationSeconds = 269,
            artworkUrl = "https://img.youtube.com/vi/qFLhGq0060w/hqdefault.jpg",
            genre = "Disco / Funk"
        ),
        AuraTrack(
            id = "yt_JQbjS0_ZfJ0",
            videoId = "JQbjS0_ZfJ0",
            title = "Kill Bill",
            artist = "SZA",
            album = "SOS",
            durationSeconds = 153,
            artworkUrl = "https://img.youtube.com/vi/JQbjS0_ZfJ0/hqdefault.jpg",
            genre = "R&B"
        )
    )
}
