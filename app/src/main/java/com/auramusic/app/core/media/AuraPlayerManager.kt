package com.auramusic.app.core.media

import android.content.Context
import android.media.audiofx.Equalizer
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.auramusic.app.core.extractor.SimpMusicExtractorEngine
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.EqualizerPreset
import com.auramusic.app.domain.model.SIMPMUSIC_EQ_PRESETS
import com.auramusic.app.domain.model.SponsorSegment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File

@OptIn(UnstableApi::class)
object AuraPlayerManager {
    private const val TAG = "AuraPlayerManager"
    private const val MAX_CACHE_BYTES = 512L * 1024L * 1024L // 512 MB SimpMusic SimpleCache

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    private var appContext: Context? = null
    private var simpleCache: SimpleCache? = null
    private var hardwareEq: Equalizer? = null

    var exoPlayer: ExoPlayer? = null
        private set

    // Reactive Player StateFlows
    private val _currentTrack = MutableStateFlow<AuraTrack?>(null)
    val currentTrack: StateFlow<AuraTrack?> = _currentTrack.asStateFlow()

    private val _queue = MutableStateFlow<List<AuraTrack>>(emptyList())
    val queue: StateFlow<List<AuraTrack>> = _queue.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _positionSec = MutableStateFlow(0f)
    val positionSec: StateFlow<Float> = _positionSec.asStateFlow()

    private val _durationSec = MutableStateFlow(210f)
    val durationSec: StateFlow<Float> = _durationSec.asStateFlow()

    private val _streamSourceTier = MutableStateFlow("SimpMusic PipePipeExtractor")
    val streamSourceTier: StateFlow<String> = _streamSourceTier.asStateFlow()

    private val _sponsorSegments = MutableStateFlow<List<SponsorSegment>>(emptyList())
    val sponsorSegments: StateFlow<List<SponsorSegment>> = _sponsorSegments.asStateFlow()

    private val _lastSkippedSegment = MutableStateFlow<String?>(null)
    val lastSkippedSegment: StateFlow<String?> = _lastSkippedSegment.asStateFlow()

    private val _activeEqPreset = MutableStateFlow(SIMPMUSIC_EQ_PRESETS.first())
    val activeEqPreset: StateFlow<EqualizerPreset> = _activeEqPreset.asStateFlow()

    private val _eqEnabled = MutableStateFlow(true)
    val eqEnabled: StateFlow<Boolean> = _eqEnabled.asStateFlow()

    private val _loudnessNormalization = MutableStateFlow(true)
    val loudnessNormalization: StateFlow<Boolean> = _loudnessNormalization.asStateFlow()

    private val _sponsorBlockEnabled = MutableStateFlow(true)
    val sponsorBlockEnabled: StateFlow<Boolean> = _sponsorBlockEnabled.asStateFlow()

    private val _crossfadeSeconds = MutableStateFlow(4)
    val crossfadeSeconds: StateFlow<Int> = _crossfadeSeconds.asStateFlow()

    private val _sleepTimerRemainingMin = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingMin: StateFlow<Int?> = _sleepTimerRemainingMin.asStateFlow()

    var onTrackFinishedCallback: ((AuraTrack) -> Unit)? = null

    fun initialize(context: Context) {
        if (exoPlayer != null) return
        val ctx = context.applicationContext
        appContext = ctx
        SimpMusicExtractorEngine.initExtractors()

        // 1. SimpMusic SimpleCache (512 MB disk cache for 0ms replays & instant scrubbing)
        val cacheDir = File(ctx.cacheDir, "aura_media3_cache")
        val dbProvider = StandaloneDatabaseProvider(ctx)
        val cache = SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES), dbProvider)
        simpleCache = cache

        // 2. OkHttpDataSource + DefaultDataSource (handles both HTTP streams & file:// offline tracks)
        val okHttpFactory = OkHttpDataSource.Factory(SimpMusicExtractorEngine.httpClient)
            .setDefaultRequestProperties(
                mapOf(
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
                )
            )
        val upstreamFactory = DefaultDataSource.Factory(ctx, okHttpFactory)

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        // 3. SimpMusic ResolvingDataSource: intercepts aura://track/{videoId} on ExoPlayer's loader thread
        val resolvingFactory = ResolvingDataSource.Factory(cacheDataSourceFactory) { dataSpec: DataSpec ->
            val uri = dataSpec.uri
            if (uri.scheme == "aura" && uri.host == "track") {
                val videoId = uri.lastPathSegment ?: throw PlaybackException(
                    "Missing videoId",
                    null,
                    PlaybackException.ERROR_CODE_IO_UNSPECIFIED
                )

                // Check offline downloaded file first
                val offlineFile = getOfflineFile(ctx, videoId)
                if (offlineFile.exists() && offlineFile.length() > 10_000L) {
                    _streamSourceTier.value = "Offline Local Storage (0ms)"
                    return@Factory dataSpec.buildUpon()
                        .setUri(Uri.fromFile(offlineFile))
                        .setKey(videoId)
                        .build()
                }

                // Otherwise resolve via SimpMusicExtractorEngine (Tier 0 memory cache -> Tier 1 PipePipe -> Tier 2 NewPipe -> Tier 3 Edge)
                val resolved = runBlocking(Dispatchers.IO) {
                    SimpMusicExtractorEngine.resolveStream(videoId)
                }
                _streamSourceTier.value = "${resolved.sourceTier} (${resolved.codec} ${resolved.bitrateKbps}kbps)"

                return@Factory dataSpec.buildUpon()
                    .setUri(Uri.parse(resolved.streamUrl))
                    .setKey(videoId)
                    .build()
            }
            dataSpec
        }

        // 4. SimpMusic Zero-Wait LoadControl (bufferForPlaybackMs = 0, bufferForPlaybackAfterRebufferMs = 0)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS * 2,
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS * 2,
                0,
                0
            )
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val renderersFactory = DefaultRenderersFactory(ctx)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

        val player = ExoPlayer.Builder(ctx, renderersFactory)
            .setMediaSourceFactory(DefaultMediaSourceFactory(resolvingFactory))
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _isBuffering.value = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    val durMs = player.duration
                    if (durMs > 0 && durMs != C.TIME_UNSET) {
                        _durationSec.value = (durMs / 1000f).coerceAtLeast(1f)
                    }
                    bindHardwareEqualizer(player.audioSessionId)
                } else if (playbackState == Player.STATE_ENDED) {
                    _currentTrack.value?.let { finished ->
                        onTrackFinishedCallback?.invoke(finished)
                    }
                    skipToNext()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val mediaId = mediaItem?.mediaId ?: return
                val matched = _queue.value.firstOrNull { it.videoId == mediaId }
                if (matched != null) {
                    _currentTrack.value = matched
                    _durationSec.value = matched.durationSeconds.toFloat().coerceAtLeast(1f)
                    prefetchUpcomingTracks(matched.videoId)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "ExoPlayer error: ${error.message}", error)
                val failedVid = _currentTrack.value?.videoId ?: return
                SimpMusicExtractorEngine.invalidateStream(failedVid)
                // Automatic SimpMusic stream recovery
                scope.launch {
                    delay(350)
                    player.prepare()
                    player.play()
                }
            }
        })

        exoPlayer = player
        startProgressTicker()
    }

    fun playTrack(track: AuraTrack, newQueue: List<AuraTrack>? = null) {
        val player = exoPlayer ?: return
        val updatedQueue = when {
            !newQueue.isNullOrEmpty() -> newQueue
            _queue.value.any { it.videoId == track.videoId } -> _queue.value
            else -> listOf(track) + _queue.value
        }
        _queue.value = updatedQueue
        _currentTrack.value = track
        _positionSec.value = 0f
        _durationSec.value = track.durationSeconds.toFloat().coerceAtLeast(1f)
        _isBuffering.value = true

        val mediaItems = updatedQueue.map { item -> buildMediaItem(item) }
        val startIndex = updatedQueue.indexOfFirst { it.videoId == track.videoId }.coerceAtLeast(0)

        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.play()

        // Proactively pre-warm the next track & fetch radio queue if queue is short
        prefetchUpcomingTracks(track.videoId)
        if (updatedQueue.size <= 3) {
            scope.launch {
                val radio = SimpMusicExtractorEngine.fetchRadioQueue(track.videoId)
                if (radio.isNotEmpty()) {
                    val existingIds = _queue.value.map { it.videoId }.toSet()
                    val fresh = radio.filterNot { existingIds.contains(it.videoId) }
                    if (fresh.isNotEmpty()) {
                        _queue.value = _queue.value + fresh
                        player.addMediaItems(fresh.map { buildMediaItem(it) })
                        fresh.firstOrNull()?.let { SimpMusicExtractorEngine.prefetchStream(it.videoId) }
                    }
                }
            }
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE) {
                player.prepare()
            }
            player.play()
        }
    }

    fun seekToSeconds(seconds: Float) {
        val player = exoPlayer ?: return
        val targetMs = (seconds.coerceAtLeast(0f) * 1000f).toLong()
        player.seekTo(targetMs)
        _positionSec.value = seconds.coerceAtLeast(0f)
    }

    fun skipToNext() {
        val player = exoPlayer ?: return
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            player.play()
        } else {
            val list = _queue.value
            val currentId = _currentTrack.value?.videoId
            val idx = list.indexOfFirst { it.videoId == currentId }
            if (idx in 0 until list.lastIndex) {
                playTrack(list[idx + 1], list)
            } else if (list.isNotEmpty()) {
                playTrack(list.first(), list)
            }
        }
    }

    fun skipToPrevious() {
        val player = exoPlayer ?: return
        if (_positionSec.value > 4f) {
            seekToSeconds(0f)
            return
        }
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            player.play()
        } else {
            val list = _queue.value
            val currentId = _currentTrack.value?.videoId
            val idx = list.indexOfFirst { it.videoId == currentId }
            if (idx > 0) {
                playTrack(list[idx - 1], list)
            } else {
                seekToSeconds(0f)
            }
        }
    }

    fun setSponsorSegments(segments: List<SponsorSegment>) {
        _sponsorSegments.value = segments
    }

    fun setSponsorBlockEnabled(enabled: Boolean) {
        _sponsorBlockEnabled.value = enabled
    }

    fun selectEqualizerPreset(preset: EqualizerPreset) {
        _activeEqPreset.value = preset
        applyEqualizerCurve()
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        _eqEnabled.value = enabled
        applyEqualizerCurve()
    }

    fun setLoudnessNormalization(enabled: Boolean) {
        _loudnessNormalization.value = enabled
        applyEqualizerCurve()
    }

    fun setCrossfadeSeconds(seconds: Int) {
        _crossfadeSeconds.value = seconds.coerceIn(0, 12)
    }

    fun setSleepTimerMinutes(minutes: Int?) {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingMin.value = minutes
        if (minutes != null && minutes > 0) {
            sleepTimerJob = scope.launch {
                var rem = minutes
                while (rem > 0 && isActive) {
                    delay(60_000L)
                    rem--
                    _sleepTimerRemainingMin.value = rem
                }
                if (rem <= 0) {
                    exoPlayer?.pause()
                    _sleepTimerRemainingMin.value = null
                }
            }
        }
    }

    fun getOfflineFile(context: Context, videoId: String): File {
        val dir = File(context.filesDir, "aura_offline_tracks")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "$videoId.m4a")
    }

    suspend fun downloadTrackOffline(context: Context, track: AuraTrack): File? = withContext(Dispatchers.IO) {
        try {
            val resolved = SimpMusicExtractorEngine.resolveStream(track.videoId)
            val targetFile = getOfflineFile(context, track.videoId)
            val req = Request.Builder()
                .url(resolved.streamUrl)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
                )
                .build()

            SimpMusicExtractorEngine.httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body ?: return@withContext null
                targetFile.outputStream().use { out ->
                    body.byteStream().use { input ->
                        input.copyTo(out)
                    }
                }
            }
            if (targetFile.exists() && targetFile.length() > 10_000L) targetFile else null
        } catch (e: Exception) {
            Log.e(TAG, "Offline download failed for ${track.videoId}: ${e.message}")
            null
        }
    }

    private fun buildMediaItem(track: AuraTrack): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setArtworkUri(Uri.parse(track.highResArtworkUrl))
            .build()

        return MediaItem.Builder()
            .setMediaId(track.videoId)
            .setUri(Uri.parse("aura://track/${track.videoId}"))
            .setMediaMetadata(metadata)
            .build()
    }

    private fun prefetchUpcomingTracks(currentVideoId: String) {
        val list = _queue.value
        val idx = list.indexOfFirst { it.videoId == currentVideoId }
        if (idx >= 0) {
            list.getOrNull(idx + 1)?.let { SimpMusicExtractorEngine.prefetchStream(it.videoId) }
            list.getOrNull(idx + 2)?.let { SimpMusicExtractorEngine.prefetchStream(it.videoId) }
        }
    }

    private fun bindHardwareEqualizer(audioSessionId: Int) {
        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET || audioSessionId <= 0) return
        try {
            hardwareEq?.release()
            hardwareEq = Equalizer(0, audioSessionId).apply {
                enabled = _eqEnabled.value
            }
            applyEqualizerCurve()
        } catch (e: Exception) {
            Log.w(TAG, "Hardware Equalizer binding notice: ${e.message}")
        }
    }

    private fun applyEqualizerCurve() {
        val player = exoPlayer ?: return
        val preset = _activeEqPreset.value
        val enabled = _eqEnabled.value

        try {
            val eq = hardwareEq
            if (eq != null) {
                eq.enabled = enabled
                val numBands = eq.numberOfBands.toInt()
                val range = eq.bandLevelRange // millibels, e.g. [-1500, 1500]
                val minMb = range[0].toInt()
                val maxMb = range[1].toInt()

                for (b in 0 until numBands) {
                    val mappedIdx = ((b.toFloat() / numBands.coerceAtLeast(1)) * preset.bandsDb.size)
                        .toInt()
                        .coerceIn(0, preset.bandsDb.lastIndex)
                    val targetMb = if (enabled) (preset.bandsDb[mappedIdx] * 100f).toInt() else 0
                    eq.setBandLevel(b.toShort(), targetMb.coerceIn(minMb, maxMb).toShort())
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Equalizer curve apply notice: ${e.message}")
        }

        val gain = if (enabled) preset.preampGain else 1.0f
        player.volume = gain.coerceIn(0.2f, 1.0f)
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val player = exoPlayer
                if (player != null && player.isPlaying) {
                    val posSec = (player.currentPosition / 1000f).coerceAtLeast(0f)
                    _positionSec.value = posSec

                    val durMs = player.duration
                    if (durMs > 0 && durMs != C.TIME_UNSET) {
                        _durationSec.value = (durMs / 1000f).coerceAtLeast(1f)
                    }

                    // SponsorBlock real-time auto-skip check
                    if (_sponsorBlockEnabled.value) {
                        val segments = _sponsorSegments.value
                        val hit = segments.firstOrNull { seg ->
                            posSec >= seg.startTimeSec && posSec < seg.endTimeSec - 0.35f
                        }
                        if (hit != null) {
                            seekToSeconds(hit.endTimeSec)
                            _lastSkippedSegment.value = "Skipped ${hit.displayLabel}"
                        }
                    }
                }
                delay(100L)
            }
        }
    }
}
