package com.auramusic.app.ui

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auramusic.app.core.extractor.SimpMusicExtractorEngine
import com.auramusic.app.core.lyrics.SimpMusicLyricsRepository
import com.auramusic.app.core.media.AuraPlayerManager
import com.auramusic.app.core.notifications.AuraNotificationManager
import com.auramusic.app.core.sponsorblock.SponsorBlockRepository
import com.auramusic.app.core.update.AuraReleaseInfo
import com.auramusic.app.core.update.AuraUpdateManager
import com.auramusic.app.core.update.UpdateDownloadProgress
import com.auramusic.app.core.update.UpdateState
import com.auramusic.app.data.AuraStorageRepository
import com.auramusic.app.domain.model.ArtistCarouselItem
import com.auramusic.app.domain.model.ArtistDetail
import com.auramusic.app.domain.model.ArtistSong
import com.auramusic.app.domain.model.AuraPalette
import com.auramusic.app.domain.model.AuraPlaylist
import com.auramusic.app.domain.model.AuraTrack
import com.auramusic.app.domain.model.EqualizerPreset
import com.auramusic.app.domain.model.ListeningStats
import com.auramusic.app.domain.model.LyricsData
import com.auramusic.app.domain.model.MoodCategory
import com.auramusic.app.domain.model.NowPlayingStyle
import com.auramusic.app.domain.model.PlaylistDetail
import com.auramusic.app.domain.model.PlaylistTrackItem
import com.auramusic.app.domain.model.ReturnYouTubeDislikeData
import com.auramusic.app.domain.model.YtmChartSection
import com.auramusic.app.domain.model.YtmCommunityPlaylist
import com.auramusic.app.domain.model.YtmTopArtist
import com.auramusic.app.ui.theme.extractAuraPaletteFromUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AuraViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = AuraStorageRepository(application)

    // Home Charts & Moods
    private val _chartSections = MutableStateFlow<List<YtmChartSection>>(emptyList())
    val chartSections: StateFlow<List<YtmChartSection>> = _chartSections.asStateFlow()

    private val _topArtists = MutableStateFlow<List<YtmTopArtist>>(emptyList())
    val topArtists: StateFlow<List<YtmTopArtist>> = _topArtists.asStateFlow()

    private val _selectedArtistDetail = MutableStateFlow<ArtistDetail?>(null)
    val selectedArtistDetail: StateFlow<ArtistDetail?> = _selectedArtistDetail.asStateFlow()

    private val _isArtistLoading = MutableStateFlow(false)
    val isArtistLoading: StateFlow<Boolean> = _isArtistLoading.asStateFlow()

    private val _activeCategory = MutableStateFlow("All")
    val activeCategory: StateFlow<String> = _activeCategory.asStateFlow()

    private val _categoryTracks = MutableStateFlow<List<AuraTrack>>(emptyList())
    val categoryTracks: StateFlow<List<AuraTrack>> = _categoryTracks.asStateFlow()

    private val _isCategoryLoading = MutableStateFlow(false)
    val isCategoryLoading: StateFlow<Boolean> = _isCategoryLoading.asStateFlow()

    private val _communityPlaylists = MutableStateFlow<List<YtmCommunityPlaylist>>(emptyList())
    val communityPlaylists: StateFlow<List<YtmCommunityPlaylist>> = _communityPlaylists.asStateFlow()

    private val _selectedPlaylistDetail = MutableStateFlow<PlaylistDetail?>(null)
    val selectedPlaylistDetail: StateFlow<PlaylistDetail?> = _selectedPlaylistDetail.asStateFlow()

    private val _isPlaylistLoading = MutableStateFlow(false)
    val isPlaylistLoading: StateFlow<Boolean> = _isPlaylistLoading.asStateFlow()

    private val _moodCategories = MutableStateFlow<List<MoodCategory>>(emptyList())
    val moodCategories: StateFlow<List<MoodCategory>> = _moodCategories.asStateFlow()

    private val _isLoadingHome = MutableStateFlow(true)
    val isLoadingHome: StateFlow<Boolean> = _isLoadingHome.asStateFlow()

    // Search & Spotify Importer
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow("all")
    val searchFilter: StateFlow<String> = _searchFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<List<AuraTrack>>(SimpMusicExtractorEngine.DEFAULT_SEED_TRACKS)
    val searchResults: StateFlow<List<AuraTrack>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchMatchedArtist = MutableStateFlow<YtmTopArtist?>(null)
    val searchMatchedArtist: StateFlow<YtmTopArtist?> = _searchMatchedArtist.asStateFlow()

    private val _searchMatchedArtists = MutableStateFlow<List<YtmTopArtist>>(emptyList())
    val searchMatchedArtists: StateFlow<List<YtmTopArtist>> = _searchMatchedArtists.asStateFlow()

    private val _artistFeaturingItems = MutableStateFlow<List<ArtistCarouselItem>>(emptyList())
    val artistFeaturingItems: StateFlow<List<ArtistCarouselItem>> = _artistFeaturingItems.asStateFlow()

    private val _spotifyImportStatus = MutableStateFlow<String?>(null)
    val spotifyImportStatus: StateFlow<String?> = _spotifyImportStatus.asStateFlow()

    private val _importedPlaylistSuccess = MutableStateFlow<AuraPlaylist?>(null)
    val importedPlaylistSuccess: StateFlow<AuraPlaylist?> = _importedPlaylistSuccess.asStateFlow()

    private val _selectedUserPlaylist = MutableStateFlow<AuraPlaylist?>(null)
    val selectedUserPlaylist: StateFlow<AuraPlaylist?> = _selectedUserPlaylist.asStateFlow()

    // Library (Favorites, Downloads, Playlists, Stats)
    private val _favorites = MutableStateFlow<List<AuraTrack>>(emptyList())
    val favorites: StateFlow<List<AuraTrack>> = _favorites.asStateFlow()

    private val _downloads = MutableStateFlow<List<AuraTrack>>(emptyList())
    val downloads: StateFlow<List<AuraTrack>> = _downloads.asStateFlow()

    private val _playlists = MutableStateFlow<List<AuraPlaylist>>(emptyList())
    val playlists: StateFlow<List<AuraPlaylist>> = _playlists.asStateFlow()

    private val _recentlyPlayed = MutableStateFlow<List<AuraTrack>>(emptyList())
    val recentlyPlayed: StateFlow<List<AuraTrack>> = _recentlyPlayed.asStateFlow()

    private val _listeningStats = MutableStateFlow(ListeningStats())
    val listeningStats: StateFlow<ListeningStats> = _listeningStats.asStateFlow()

    private val _downloadingIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingIds: StateFlow<Set<String>> = _downloadingIds.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

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

    private val _nowPlayingStyle = MutableStateFlow(NowPlayingStyle.SPOTIFY)
    val nowPlayingStyle: StateFlow<NowPlayingStyle> = _nowPlayingStyle.asStateFlow()

    val isVideoMode: StateFlow<Boolean> = AuraPlayerManager.isVideoMode
    val isVideoLoading: StateFlow<Boolean> = AuraPlayerManager.isVideoLoading

    private val _livePerformances = MutableStateFlow<List<AuraTrack>>(emptyList())
    val livePerformances: StateFlow<List<AuraTrack>> = _livePerformances.asStateFlow()

    private val _isLoadingLivePerformances = MutableStateFlow(false)
    val isLoadingLivePerformances: StateFlow<Boolean> = _isLoadingLivePerformances.asStateFlow()

    // In-App Updates & Releases
    private val _currentUpdateRelease = MutableStateFlow<AuraReleaseInfo?>(null)
    val currentUpdateRelease: StateFlow<AuraReleaseInfo?> = _currentUpdateRelease.asStateFlow()

    private val _isUpdateModalVisible = MutableStateFlow(false)
    val isUpdateModalVisible: StateFlow<Boolean> = _isUpdateModalVisible.asStateFlow()

    private val _updateState = MutableStateFlow(UpdateState.IDLE)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _updateDownloadProgress = MutableStateFlow(UpdateDownloadProgress())
    val updateDownloadProgress: StateFlow<UpdateDownloadProgress> = _updateDownloadProgress.asStateFlow()

    private val _updateErrorMessage = MutableStateFlow<String?>(null)
    val updateErrorMessage: StateFlow<String?> = _updateErrorMessage.asStateFlow()

    private var downloadedApkFile: File? = null

    private var searchJob: Job? = null

    init {
        AuraPlayerManager.initialize(application)
        AuraNotificationManager.setupFcmSubscription(application)
        _favorites.value = storage.loadFavorites()
        _downloads.value = storage.loadDownloads()
        _playlists.value = storage.loadPlaylists()
        _recentlyPlayed.value = storage.loadRecentlyPlayed()
        _listeningStats.value = storage.loadListeningStats()
        _nowPlayingStyle.value = NowPlayingStyle.SPOTIFY
        storage.saveNowPlayingStyle(NowPlayingStyle.SPOTIFY)

        // Silent startup check against GitHub Releases
        checkForUpdates(force = false)

        AuraPlayerManager.onTrackFinishedCallback = { finishedTrack ->
            _listeningStats.value = storage.recordTrackPlay(finishedTrack)
            _recentlyPlayed.value = storage.loadRecentlyPlayed()
        }

        // Observe current track changes to fetch Palette, SimpMusic RichSync Lyrics, SponsorBlock, and RYD in parallel
        viewModelScope.launch {
            AuraPlayerManager.currentTrack.collectLatest { track ->
                if (track == null) return@collectLatest
                storage.recordRecentlyPlayed(track)
                _recentlyPlayed.value = storage.loadRecentlyPlayed()
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
            val chartsDeferred = async { SimpMusicExtractorEngine.fetchHomeChartsAndMoods() }
            val artistsDeferred = async { SimpMusicExtractorEngine.fetchTopArtists() }
            val playlistsDeferred = async { SimpMusicExtractorEngine.fetchCommunityPlaylists() }

            val (sections, moods) = chartsDeferred.await()
            _chartSections.value = sections
            _moodCategories.value = moods
            _topArtists.value = artistsDeferred.await()
            _communityPlaylists.value = playlistsDeferred.await()
            _isLoadingHome.value = false

            val topTracks = sections.firstOrNull()?.tracks ?: emptyList()
            if (_searchQuery.value.isBlank() && topTracks.isNotEmpty()) {
                _searchResults.value = topTracks
            }

            topTracks.firstOrNull()?.let {
                SimpMusicExtractorEngine.prefetchStream(it.videoId)
            }
        }
    }

    fun playCommunityPlaylist(playlist: YtmCommunityPlaylist) {
        viewModelScope.launch {
            val tracks = SimpMusicExtractorEngine.fetchPlaylistTracks(playlist.id)
            if (tracks.isNotEmpty()) {
                playTrack(tracks.first(), tracks)
            }
        }
    }

    fun openArtistDetail(artist: YtmTopArtist) {
        viewModelScope.launch {
            _isArtistLoading.value = true
            // Immediately set a preliminary state so transition is instant
            _selectedArtistDetail.value = ArtistDetail(
                id = artist.id,
                name = artist.name,
                subscribers = artist.subscribers,
                bannerUrl = artist.avatarUrl,
                topSongs = emptyList(),
                albums = emptyList()
            )
            val fullDetail = SimpMusicExtractorEngine.fetchArtistDetail(artist)
            _selectedArtistDetail.value = fullDetail
            _isArtistLoading.value = false
        }
    }

    fun openArtistByName(artistName: String, songTitle: String = "", fallbackArtwork: String = "") {
        if (artistName.isBlank()) return
        val existingInTop = _topArtists.value.firstOrNull { it.name == artistName }
        if (existingInTop != null && songTitle.isBlank()) {
            openArtistDetail(existingInTop)
            return
        }
        viewModelScope.launch {
            _isArtistLoading.value = true
            _selectedArtistDetail.value = ArtistDetail(
                id = "",
                name = artistName,
                subscribers = "",
                bannerUrl = fallbackArtwork,
                topSongs = emptyList(),
                albums = emptyList()
            )
            val fullDetail = SimpMusicExtractorEngine.fetchArtistByName(artistName, songTitle, fallbackArtwork)
            _selectedArtistDetail.value = fullDetail
            _isArtistLoading.value = false
        }
    }

    fun closeArtistDetail() {
        _selectedArtistDetail.value = null
    }

    fun selectUserPlaylist(playlist: AuraPlaylist?) {
        _selectedUserPlaylist.value = playlist
    }

    fun clearSelectedUserPlaylist() {
        _selectedUserPlaylist.value = null
    }

    fun dismissImportSuccessModal() {
        _importedPlaylistSuccess.value = null
    }

    fun openPlaylistDetail(playlist: YtmCommunityPlaylist) {
        viewModelScope.launch {
            _isPlaylistLoading.value = true
            _selectedPlaylistDetail.value = PlaylistDetail(
                id = playlist.id,
                title = playlist.title,
                author = playlist.author,
                trackCount = playlist.trackCount,
                bannerUrl = playlist.artworkUrl,
                tracks = emptyList()
            )
            val fullDetail = SimpMusicExtractorEngine.fetchPlaylistDetail(playlist)
            _selectedPlaylistDetail.value = fullDetail
            _isPlaylistLoading.value = false
        }
    }

    fun openAlbumOrPlaylist(id: String, title: String, author: String, artworkUrl: String) {
        openPlaylistDetail(
            YtmCommunityPlaylist(
                id = id,
                title = title,
                author = author,
                trackCount = "",
                artworkUrl = artworkUrl
            )
        )
    }

    fun closePlaylistDetail() {
        _selectedPlaylistDetail.value = null
    }

    fun playArtist(artist: YtmTopArtist) {
        viewModelScope.launch {
            val tracks = SimpMusicExtractorEngine.searchYouTubeMusic("${artist.name} official songs", "songs")
            if (tracks.isNotEmpty()) {
                playTrack(tracks.first(), tracks)
            }
        }
    }

    fun selectCategory(category: String) {
        _activeCategory.value = category
        if (category == "All") {
            _categoryTracks.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isCategoryLoading.value = true
            val query = when (category) {
                "New Release" -> "new music releases official audio 2025"
                "Trending" -> "trending viral hits official audio 2025"
                "Top Chart" -> "billboard top 50 global hits official audio"
                "Podcasts" -> "popular podcasts episodes full video"
                else -> "$category songs official audio"
            }
            val results = SimpMusicExtractorEngine.searchYouTubeMusic(query, "songs")
            _categoryTracks.value = results
            _isCategoryLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchMatchedArtist.value = null
            _searchMatchedArtists.value = emptyList()
            _artistFeaturingItems.value = emptyList()
            _searchResults.value = _chartSections.value.firstOrNull()?.tracks
                ?: SimpMusicExtractorEngine.DEFAULT_SEED_TRACKS
            return
        }
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            _searchResults.value = emptyList()
            _searchMatchedArtist.value = null
            _searchMatchedArtists.value = emptyList()
            _artistFeaturingItems.value = emptyList()

            // Parallel artist search for top artist card, artist list & featuring carousels
            val artistDeferred = async {
                if (query.trim().isNotEmpty()) {
                    try {
                        SimpMusicExtractorEngine.searchArtists(query.trim())
                    } catch (_: Exception) {
                        emptyList()
                    }
                } else emptyList()
            }
            val resultsDeferred = async {
                SimpMusicExtractorEngine.searchYouTubeMusic(query, _searchFilter.value)
            }

            val artists = artistDeferred.await()
            val matched = artists.firstOrNull()
            _searchMatchedArtists.value = artists
            _searchMatchedArtist.value = matched

            val results = resultsDeferred.await()
            if (results.isNotEmpty()) {
                _searchResults.value = results
            } else if (_searchFilter.value == "artists" && matched != null) {
                // If filter is artists and tracks are empty, prefetch top tracks for that artist
                try {
                    val detail = SimpMusicExtractorEngine.fetchArtistDetail(matched)
                    _searchResults.value = detail.topSongs.map { it.track }
                } catch (_: Exception) {}
            }

            if (matched != null) {
                try {
                    val detail = SimpMusicExtractorEngine.fetchArtistDetail(matched)
                    val feat = detail.carousels.firstOrNull {
                        it.title.contains("Featuring", ignoreCase = true) ||
                        it.title.contains("Singles", ignoreCase = true) ||
                        it.title.contains("Albums", ignoreCase = true)
                    }?.items ?: detail.albums.map {
                        ArtistCarouselItem(id = it.id, title = it.title, subtitle = it.year, artworkUrl = it.artworkUrl, browseId = it.id)
                    }
                    _artistFeaturingItems.value = feat.take(10)
                } catch (_: Exception) {
                    _artistFeaturingItems.value = emptyList()
                }
            } else {
                _artistFeaturingItems.value = emptyList()
            }

            _isSearching.value = false
        }
    }

    fun removeFromRecentlyPlayed(track: AuraTrack) {
        storage.removeRecentlyPlayed(track.videoId)
        _recentlyPlayed.value = storage.loadRecentlyPlayed()
        Toast.makeText(getApplication(), "Removed '${track.title}' from recently played", Toast.LENGTH_SHORT).show()
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

    fun playSearchTrack(track: AuraTrack, results: List<AuraTrack>) {
        if (AuraPlayerManager.isShuffleEnabled.value) {
            val shuffledQueue = results.filterNot { it.videoId == track.videoId }.shuffled()
            AuraPlayerManager.playTrack(track, listOf(track) + shuffledQueue, allowRadioQueue = true)
        } else {
            // Playing from search query should NOT auto-play other songs from search results unless shuffle is enabled
            AuraPlayerManager.playTrack(track, listOf(track), allowRadioQueue = false)
        }
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

    fun isTrackDownloaded(videoId: String): Boolean {
        return _downloads.value.any { it.videoId == videoId && it.isDownloaded }
    }

    fun downloadTrack(track: AuraTrack) {
        if (_downloadingIds.value.contains(track.videoId)) return
        val isAlreadyDownloaded = _downloads.value.any {
            it.videoId == track.videoId && it.isDownloaded && it.localFilePath?.let { p -> File(p).exists() } == true
        }
        if (isAlreadyDownloaded) {
            Toast.makeText(getApplication(), "Already downloaded", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            // Immediate feedback: add placeholder to downloads list so it appears in "Downloaded" pill right away!
            val placeholder = track.copy(isDownloaded = false)
            val currentDownloads = _downloads.value.filterNot { it.videoId == track.videoId }
            _downloads.value = listOf(placeholder) + currentDownloads

            _downloadingIds.value = _downloadingIds.value + track.videoId
            _downloadProgress.value = _downloadProgress.value + (track.videoId to 0.02f)

            // Cache lyrics and color palette for offline self-sufficiency in parallel
            val lyricsJob = async {
                try {
                    SimpMusicLyricsRepository.getLyrics(
                        videoId = track.videoId,
                        title = track.title,
                        artist = track.artist,
                        durationSeconds = track.durationSeconds
                    )?.let { storage.saveOfflineLyrics(track.videoId, it) }
                } catch (e: Exception) {
                    Log.w("AuraViewModel", "Failed to cache offline lyrics for ${track.videoId}: ${e.message}")
                }
            }

            val paletteJob = async {
                try {
                    val pal = extractAuraPaletteFromUrl(track.highResArtworkUrl)
                    storage.saveOfflinePalette(track.videoId, pal)
                } catch (e: Exception) {
                    Log.w("AuraViewModel", "Failed to cache offline palette for ${track.videoId}: ${e.message}")
                }
            }

            val file = AuraPlayerManager.downloadTrackOffline(getApplication(), track) { _, _, progress ->
                _downloadProgress.value = _downloadProgress.value + (track.videoId to progress)
            }

            lyricsJob.await()
            paletteJob.await()

            _downloadingIds.value = _downloadingIds.value - track.videoId
            _downloadProgress.value = _downloadProgress.value - track.videoId

            if (file != null && file.exists() && file.length() > 10_000L) {
                val downloadedTrack = track.copy(isDownloaded = true, localFilePath = file.absolutePath)
                val updated = listOf(downloadedTrack) + _downloads.value.filterNot { it.videoId == track.videoId }
                _downloads.value = updated
                storage.saveDownloads(updated.filter { it.isDownloaded })
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Downloaded: ${track.title}", Toast.LENGTH_SHORT).show()
                }
            } else {
                // If failed, remove placeholder
                _downloads.value = _downloads.value.filterNot { it.videoId == track.videoId && !it.isDownloaded }
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Download failed for ${track.title}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun removeDownload(track: AuraTrack) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Delete by localFilePath
                track.localFilePath?.let { p ->
                    val f = File(p)
                    if (f.exists()) f.delete()
                }
                // 2. Delete from dedicated folder
                val dedicated = AuraPlayerManager.getOfflineFile(getApplication(), track.videoId)
                if (dedicated.exists()) dedicated.delete()
            } catch (e: Exception) {
                Log.w("AuraViewModel", "Error deleting offline file: ${e.message}")
            }

            val updated = _downloads.value.filterNot { it.videoId == track.videoId }
            _downloads.value = updated
            storage.saveDownloads(updated.filter { it.isDownloaded })

            withContext(Dispatchers.Main) {
                Toast.makeText(getApplication(), "Removed '${track.title}' from downloads", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun importSpotifyPlaylist(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _spotifyImportStatus.value = "Connecting to Spotify…"
            val playlist = SimpMusicExtractorEngine.fetchSpotifyPlaylistDirect(url) { done, total ->
                _spotifyImportStatus.value = "Importing tracks ($done / $total)…"
            }
            if (playlist != null && playlist.tracks.isNotEmpty()) {
                val updated = listOf(playlist) + _playlists.value.filterNot { it.id == playlist.id }
                _playlists.value = updated
                storage.savePlaylists(updated)
                _spotifyImportStatus.value = "Imported '${playlist.name}' (${playlist.tracks.size} tracks)"
                _importedPlaylistSuccess.value = playlist
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "Imported '${playlist.name}' (${playlist.tracks.size} tracks) successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                _spotifyImportStatus.value = "Could not parse public Spotify playlist URL"
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "Could not parse Spotify playlist. Please check the link and try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun createPlaylist(name: String, description: String = "", coverUrl: String = ""): AuraPlaylist {
        val newPlaylist = AuraPlaylist(
            id = "playlist_${System.currentTimeMillis()}",
            name = name.ifBlank { "My Playlist" },
            description = description,
            coverUrl = coverUrl,
            tracks = emptyList()
        )
        val updated = listOf(newPlaylist) + _playlists.value
        _playlists.value = updated
        storage.savePlaylists(updated)
        Toast.makeText(getApplication(), "Created playlist: ${newPlaylist.name}", Toast.LENGTH_SHORT).show()
        return newPlaylist
    }

    fun addTrackToPlaylist(playlistId: String, track: AuraTrack) {
        val target = _playlists.value.firstOrNull { it.id == playlistId }
        if (target == null) return

        if (target.tracks.any { it.videoId == track.videoId }) {
            Toast.makeText(getApplication(), "'${track.title}' is already in ${target.name}", Toast.LENGTH_SHORT).show()
            return
        }

        val updated = _playlists.value.map { pl ->
            if (pl.id == playlistId) {
                pl.copy(tracks = pl.tracks + track)
            } else {
                pl
            }
        }
        _playlists.value = updated
        storage.savePlaylists(updated)
        Toast.makeText(getApplication(), "Added '${track.title}' to ${target.name}", Toast.LENGTH_SHORT).show()
    }

    fun updatePlaylistCover(playlistId: String, newCoverUrl: String) {
        val updated = _playlists.value.map { pl ->
            if (pl.id == playlistId) pl.copy(coverUrl = newCoverUrl) else pl
        }
        _playlists.value = updated
        storage.savePlaylists(updated)
    }

    fun updatePlaylistTitle(playlistId: String, newTitle: String) {
        val updated = _playlists.value.map { pl ->
            if (pl.id == playlistId) pl.copy(name = newTitle) else pl
        }
        _playlists.value = updated
        storage.savePlaylists(updated)
    }

    fun deletePlaylist(playlistId: String) {
        val target = _playlists.value.firstOrNull { it.id == playlistId }
        val updated = _playlists.value.filterNot { it.id == playlistId }
        _playlists.value = updated
        storage.savePlaylists(updated)
        Toast.makeText(getApplication(), "Deleted playlist: ${target?.name ?: "Playlist"}", Toast.LENGTH_SHORT).show()
    }

    fun removeTrackFromPlaylist(playlistId: String, videoId: String) {
        val pl = _playlists.value.firstOrNull { it.id == playlistId }
        val track = pl?.tracks?.firstOrNull { it.videoId == videoId }
        val updated = _playlists.value.map { p ->
            if (p.id == playlistId) {
                p.copy(tracks = p.tracks.filterNot { it.videoId == videoId })
            } else {
                p
            }
        }
        _playlists.value = updated
        storage.savePlaylists(updated)
        Toast.makeText(getApplication(), "Removed '${track?.title ?: "song"}' from playlist", Toast.LENGTH_SHORT).show()
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

    fun toggleVideoMode() {
        if (AuraPlayerManager.isVideoMode.value) {
            AuraPlayerManager.switchToAudioMode()
            Toast.makeText(getApplication(), "Switched to Audio Mode", Toast.LENGTH_SHORT).show()
            return
        }

        val track = AuraPlayerManager.currentTrack.value
        if (track == null) {
            Toast.makeText(getApplication(), "No track currently playing", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            AuraPlayerManager.setVideoLoading(true)
            Toast.makeText(getApplication(), "Checking for Official Music Video…", Toast.LENGTH_SHORT).show()

            val official = SimpMusicExtractorEngine.findOfficialMusicVideo(track)
            AuraPlayerManager.setVideoLoading(false)

            if (official != null) {
                AuraPlayerManager.playVideoForTrack(official, track)
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Playing Official Video: ${official.title}", Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "No official music video available for this song",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun setVideoMode(enabled: Boolean) {
        AuraPlayerManager.setVideoMode(enabled)
    }

    fun loadLivePerformances(track: AuraTrack? = AuraPlayerManager.currentTrack.value) {
        val target = track ?: return
        viewModelScope.launch {
            _isLoadingLivePerformances.value = true
            _livePerformances.value = SimpMusicExtractorEngine.fetchLivePerformancesAndVideos(target)
            _isLoadingLivePerformances.value = false
        }
    }

    fun playLivePerformance(track: AuraTrack) {
        AuraPlayerManager.setVideoMode(true)
        val currentPos = AuraPlayerManager.exoPlayer?.currentPosition ?: 0L
        AuraPlayerManager.playTrack(track, listOf(track))
        AuraPlayerManager.exoPlayer?.seekTo(currentPos)
        Toast.makeText(getApplication(), "Playing: ${track.title}", Toast.LENGTH_SHORT).show()
    }

    fun checkForUpdates(force: Boolean = false) {
        viewModelScope.launch {
            val release = AuraUpdateManager.checkForUpdates(getApplication(), force)
            if (release != null) {
                _currentUpdateRelease.value = release
                _updateState.value = UpdateState.IDLE
                _updateErrorMessage.value = null
                _isUpdateModalVisible.value = true
            } else if (force) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "You're already on the latest version of Aura!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun openUpdateModalForRelease(release: AuraReleaseInfo) {
        _currentUpdateRelease.value = release
        _updateState.value = UpdateState.IDLE
        _updateErrorMessage.value = null
        _isUpdateModalVisible.value = true
    }

    /**
     * Test trigger to preview the AuraUpdateModal UI and verify release changelog styling.
     */
    fun triggerTestUpdateModal() {
        val testRelease = AuraReleaseInfo(
            version = "1.4.2",
            tagName = "v1.4.2",
            releaseTitle = "Aura v1.4.2 • In-App Update Engine Stability & UI Polish",
            releaseNotes = """
# Aura v1.4.2

Release Tags: [release, v1.4.2, android, kotlin, jetpack-compose, in-app-update, bugfix, stability]

---

## Overview

Aura v1.4.2 resolves a crash during direct in-app update downloading, removes redundant circular mascot outlines, and cleans up release changelog formatting.

---

## What's Changed

- **In-App Update Engine Crash Fixes**:
  - Fixed `IllegalFormatFlagsException: Flags ='('` during download progress updates by isolating numeric formatting from percentage labels.
  - Eliminated duplicate version titles under "WHAT'S NEW" in the update modal.
  - Redesigned the update modal mascot logo to be flat and borderless with an enlarged 96dp footprint.
  - Optimized background update check cadence to 15 minutes.
            """.trimIndent(),
            downloadUrl = "https://github.com/jlfuertes14/Aura-Music/releases",
            fileName = "aura-music-v1.4.2-universal.apk",
            apkSize = 34_820_000L,
            publishedAt = "2026-11-02T12:00:00Z",
            htmlUrl = "https://github.com/jlfuertes14/Aura-Music/releases"
        )
        _currentUpdateRelease.value = testRelease
        _updateState.value = UpdateState.IDLE
        _updateErrorMessage.value = null
        _isUpdateModalVisible.value = true
    }

    fun dismissUpdateModal() {
        if (_updateState.value != UpdateState.DOWNLOADING) {
            _isUpdateModalVisible.value = false
        }
    }

    fun snoozeUpdate(version: String) {
        AuraUpdateManager.snoozeUpdate(getApplication(), version)
        _isUpdateModalVisible.value = false
    }

    fun downloadAndInstallUpdate(release: AuraReleaseInfo) {
        viewModelScope.launch {
            if (!release.downloadUrl.endsWith(".apk", ignoreCase = true) && !release.downloadUrl.contains("/download/")) {
                // Interactive preview mode: simulate smooth download streaming for UI evaluation
                _updateState.value = UpdateState.DOWNLOADING
                _updateErrorMessage.value = null
                for (pct in 1..100) {
                    kotlinx.coroutines.delay(35)
                    val written = (release.apkSize * (pct / 100f)).toLong()
                    _updateDownloadProgress.value = UpdateDownloadProgress(
                        writtenBytes = written,
                        totalBytes = release.apkSize,
                        fraction = pct / 100f
                    )
                }
                _updateState.value = UpdateState.READY
                return@launch
            }

            _updateState.value = UpdateState.DOWNLOADING
            _updateErrorMessage.value = null
            _updateDownloadProgress.value = UpdateDownloadProgress()

            val result = AuraUpdateManager.downloadApk(getApplication(), release) { progress ->
                _updateDownloadProgress.value = progress
            }

            result.onSuccess { apkFile ->
                downloadedApkFile = apkFile
                _updateState.value = UpdateState.READY
                AuraUpdateManager.installApk(getApplication(), apkFile)
            }.onFailure { err ->
                _updateState.value = UpdateState.ERROR
                _updateErrorMessage.value = err.message ?: "Failed to download update APK"
            }
        }
    }

    fun installDownloadedApk(release: AuraReleaseInfo) {
        downloadedApkFile?.let { apkFile ->
            AuraUpdateManager.installApk(getApplication(), apkFile)
        } ?: downloadAndInstallUpdate(release)
    }
}
