package com.auramusic.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auramusic.app.core.extractor.SimpMusicExtractorEngine
import com.auramusic.app.core.lyrics.SimpMusicLyricsRepository
import com.auramusic.app.core.media.AuraPlayerManager
import com.auramusic.app.core.sponsorblock.SponsorBlockRepository
import com.auramusic.app.data.AuraStorageRepository
import com.auramusic.app.domain.model.AuraPalette
import com.auramusic.app.domain.model.AuraPlaylist
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.EqualizerPreset
import com.auramusic.app.domain.model.ListeningStats
import com.auramusic.app.domain.model.LyricsData
import com.auramusic.app.domain.model.MoodCategory
import com.auramusic.app.domain.model.NowPlayingStyle
import com.auramusic.app.domain.model.ReturnYouTubeDislikeData
import com.auramusic.app.domain.model.YtmChartSection
import com.auramusic.app.ui.theme.extractAuraPaletteFromUrl
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuraViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = AuraStorageRepository(application)

    // Home Charts & Moods
    private val _chartSections = MutableStateFlow<List<YtmChartSection>>(emptyList())
    val chartSections: StateFlow<List<YtmChartSection>> = _chartSections.asStateFlow()

    private val _moodCategories = MutableStateFlow<List<MoodCategory>>(emptyList())
    val moodCategories: StateFlow<List<MoodCategory>> = _moodCategories.asStateFlow()

    private val _isLoadingHome = MutableStateFlow(true)
    val isLoadingHome: StateFlow<Boolean> = _isLoadingHome.asStateFlow()

    // Search & Spotify Importer
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow("songs")
    val searchFilter: StateFlow<String> = _searchFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<List<AuraTrack>>(SimpMusicExtractorEngine.DEFAULT_SEED_TRACKS)
    val searchResults: StateFlow<List<AuraTrack>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _spotifyImportStatus = MutableStateFlow<String?>(null)
    val spotifyImportStatus: StateFlow<String?> = _spotifyImportStatus.asStateFlow()

    // Library (Favorites, Downloads, Playlists, Stats)
    private val _favorites = MutableStateFlow<List<AuraTrack>>(emptyList())
    val favorites: StateFlow<List<AuraTrack>> = _favorites.asStateFlow()

    private val _downloads = MutableStateFlow<List<AuraTrack>>(emptyList())
    val downloads: StateFlow<List<AuraTrack>> = _downloads.asStateFlow()

    private val _playlists = MutableStateFlow<List<AuraPlaylist>>(emptyList())
    val playlists: StateFlow<List<AuraPlaylist>> = _playlists.asStateFlow()

    private val _listeningStats = MutableStateFlow(ListeningStats())
    val listeningStats: StateFlow<ListeningStats> = _listeningStats.asStateFlow()

    private val _downloadingIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingIds: StateFlow<Set<String>> = _downloadingIds.asStateFlow()

    // Now Playing Enrichments (Palette, Lyrics, RYD, Style)
    private val _palette = MutableStateFlow(AuraPalette())
    val palette: StateFlow<AuraPalette> = _palette.asStateFlow()

    private val _lyrics = MutableStateFlow<LyricsData?>(null)
    val lyrics: StateFlow<LyricsData?> = _lyrics.asStateFlow()

    private val _lyricsOffsetMs = MutableStateFlow(0)
    val lyricsOffsetMs: StateFlow<Int> = _lyricsOffsetMs.asStateFlow()

    private val _showRomanizedLyrics = MutableStateFlow(true)
    val showRomanizedLyrics: StateFlow<Boolean> = _showRomanizedLyrics.asStateFlow()

    private val _rydData = MutableStateFlow<ReturnYouTubeDislikeData?>(null)
    val rydData: StateFlow<ReturnYouTubeDislikeData?> = _rydData.asStateFlow()

    private val _nowPlayingStyle = MutableStateFlow(NowPlayingStyle.EXPRESSIVE)
    val nowPlayingStyle: StateFlow<NowPlayingStyle> = _nowPlayingStyle.asStateFlow()

    private var searchJob: Job? = null

    init {
        AuraPlayerManager.initialize(application)
        _favorites.value = storage.loadFavorites()
        _downloads.value = storage.loadDownloads()
        _playlists.value = storage.loadPlaylists()
        _listeningStats.value = storage.loadListeningStats()
        _nowPlayingStyle.value = storage.loadNowPlayingStyle()

        AuraPlayerManager.onTrackFinishedCallback = { finishedTrack ->
            _listeningStats.value = storage.recordTrackPlay(finishedTrack)
        }

        // Observe current track changes to fetch Palette, SimpMusic RichSync Lyrics, SponsorBlock, and RYD in parallel
        viewModelScope.launch {
            AuraPlayerManager.currentTrack.collectLatest { track ->
                if (track == null) return@collectLatest
                _lyricsOffsetMs.value = storage.getLyricsOffsetMs(track.videoId)

                val paletteDef = async { extractAuraPaletteFromUrl(track.highResArtworkUrl) }
                val lyricsDef = async {
                    SimpMusicLyricsRepository.getLyrics(
                        videoId = track.videoId,
                        title = track.title,
                        artist = track.artist,
                        durationSeconds = track.durationSeconds
                    )
                }
                val sponsorDef = async { SponsorBlockRepository.getSponsorSegments(track.videoId) }
                val rydDef = async { SponsorBlockRepository.getReturnYouTubeDislike(track.videoId) }

                _palette.value = paletteDef.await()
                _lyrics.value = lyricsDef.await()
                AuraPlayerManager.setSponsorSegments(sponsorDef.await())
                _rydData.value = rydDef.await()
            }
        }

        refreshHome()
        // Pre-warm the default seed track so first tap is instant
        SimpMusicExtractorEngine.prefetchStream(SimpMusicExtractorEngine.DEFAULT_SEED_TRACKS.first().videoId)
    }

    fun refreshHome() {
        viewModelScope.launch {
            _isLoadingHome.value = true
            val (sections, moods) = SimpMusicExtractorEngine.fetchHomeChartsAndMoods()
            _chartSections.value = sections
            _moodCategories.value = moods
            _isLoadingHome.value = false
            sections.firstOrNull()?.tracks?.firstOrNull()?.let {
                SimpMusicExtractorEngine.prefetchStream(it.videoId)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = SimpMusicExtractorEngine.DEFAULT_SEED_TRACKS
            return
        }
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            val results = SimpMusicExtractorEngine.searchYouTubeMusic(query, _searchFilter.value)
            if (results.isNotEmpty()) {
                _searchResults.value = results
            }
            _isSearching.value = false
        }
    }

    fun setSearchFilter(filter: String) {
        _searchFilter.value = filter
        if (_searchQuery.value.isNotBlank()) {
            updateSearchQuery(_searchQuery.value)
        }
    }

    fun playMoodCategory(mood: MoodCategory) {
        viewModelScope.launch {
            _isSearching.value = true
            val tracks = SimpMusicExtractorEngine.searchYouTubeMusic(mood.query, "songs")
            _isSearching.value = false
            if (tracks.isNotEmpty()) {
                playTrack(tracks.first(), tracks)
            }
        }
    }

    fun playTrack(track: AuraTrack, queue: List<AuraTrack>? = null) {
        AuraPlayerManager.playTrack(track, queue)
        _listeningStats.value = storage.recordTrackPlay(track)
    }

    fun toggleFavorite(track: AuraTrack) {
        val current = _favorites.value.toMutableList()
        val existingIdx = current.indexOfFirst { it.videoId == track.videoId }
        if (existingIdx >= 0) {
            current.removeAt(existingIdx)
        } else {
            current.add(0, track)
        }
        _favorites.value = current
        storage.saveFavorites(current)
    }

    fun isFavorite(videoId: String): Boolean {
        return _favorites.value.any { it.videoId == videoId }
    }

    fun downloadTrack(track: AuraTrack) {
        if (_downloadingIds.value.contains(track.videoId)) return
        viewModelScope.launch {
            _downloadingIds.value = _downloadingIds.value + track.videoId
            val file = AuraPlayerManager.downloadTrackOffline(getApplication(), track)
            _downloadingIds.value = _downloadingIds.value - track.videoId
            if (file != null) {
                val downloadedTrack = track.copy(isDownloaded = true, localFilePath = file.absolutePath)
                val updated = listOf(downloadedTrack) + _downloads.value.filterNot { it.videoId == track.videoId }
                _downloads.value = updated
                storage.saveDownloads(updated)
            }
        }
    }

    fun importSpotifyPlaylist(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _spotifyImportStatus.value = "Connecting directly to Spotify Embed…"
            val playlist = SimpMusicExtractorEngine.fetchSpotifyPlaylistDirect(url) { done, total ->
                _spotifyImportStatus.value = "Matching on YouTube Music ($done / $total)…"
            }
            if (playlist != null && playlist.tracks.isNotEmpty()) {
                val updated = listOf(playlist) + _playlists.value.filterNot { it.id == playlist.id }
                _playlists.value = updated
                storage.savePlaylists(updated)
                _spotifyImportStatus.value = "Imported '${playlist.name}' (${playlist.tracks.size} tracks)"
                playTrack(playlist.tracks.first(), playlist.tracks)
            } else {
                _spotifyImportStatus.value = "Could not parse public Spotify playlist URL"
            }
        }
    }

    fun adjustLyricsOffset(deltaMs: Int) {
        val vid = AuraPlayerManager.currentTrack.value?.videoId ?: return
        val next = (_lyricsOffsetMs.value + deltaMs).coerceIn(-5000, 5000)
        _lyricsOffsetMs.value = next
        storage.setLyricsOffsetMs(vid, next)
    }

    fun toggleRomanizedLyrics() {
        _showRomanizedLyrics.value = !_showRomanizedLyrics.value
    }

    fun setNowPlayingStyle(style: NowPlayingStyle) {
        _nowPlayingStyle.value = style
        storage.saveNowPlayingStyle(style)
    }

    fun selectEqualizerPreset(preset: EqualizerPreset) {
        AuraPlayerManager.selectEqualizerPreset(preset)
    }
}
