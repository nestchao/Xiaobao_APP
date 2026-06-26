package com.xiaobaotv.app.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.datasource.cache.Cache
import com.xiaobaotv.app.data.cache.PlaybackUrlCache
import com.xiaobaotv.app.domain.model.Episode
import com.xiaobaotv.app.domain.model.VideoSource
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.model.WatchHistoryItem
import com.xiaobaotv.app.domain.repository.ContentRepository
import com.xiaobaotv.app.domain.repository.VideoRepository
import com.xiaobaotv.app.domain.repository.WatchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val isLoading: Boolean = false,
    val vodId: Int = 0,
    val sources: List<VideoSource> = emptyList(),
    val currentSourceIndex: Int = 0,
    val currentEpisodeIndex: Int = 0,
    val playbackUrl: String? = null,
    val savedPositionMs: Long = 0L,
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val contentRepository: ContentRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val playbackUrlCache: PlaybackUrlCache,
    val exoPlayerCache: Cache
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    private val _durationMs = MutableStateFlow(0L)

    private var vodContent: VodContent? = null
    private var saveJob: Job? = null

    fun loadVideoInfo(vodId: Int, episodeIndex: Int = 0) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, vodId = vodId) }

            // Check pre-fetched cache using both vodId and the target episode index
            playbackUrlCache.get(vodId, episodeIndex)?.let { url ->
                _uiState.update { it.copy(playbackUrl = url) }
            }

            // Load vod detail for name/pic
            contentRepository.getVodDetail(vodId).onSuccess { content ->
                vodContent = content
            }

            // Check history for saved position and source index
            val history = watchHistoryRepository.getWatchHistoryItem(vodId)
            val finalSourceIndex = history?.sourceIndex ?: 0
            val finalEpisodeIndex = episodeIndex

            videoRepository.getVideoSources(vodId).onSuccess { sources ->
                _uiState.update {
                    it.copy(
                        sources = sources,
                        currentSourceIndex = finalSourceIndex,
                        currentEpisodeIndex = finalEpisodeIndex
                    )
                }

                // Restore position if same episode and significant progress
                if (history != null && history.episodeIndex == finalEpisodeIndex && history.positionMs > 5000L) {
                    _uiState.update { it.copy(savedPositionMs = history.positionMs) }
                }

                if (sources.isNotEmpty() && _uiState.value.playbackUrl == null) {
                    val targetEpisode = sources.getOrNull(finalSourceIndex)?.episodes?.getOrNull(finalEpisodeIndex)
                    loadPlaybackUrl(targetEpisode)
                } else if (sources.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "No sources found") }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }

                startProgressSavingLoop()

            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectEpisode(episodeIndex: Int) {
        val currentState = _uiState.value
        val source = currentState.sources.getOrNull(currentState.currentSourceIndex)
        val episode = source?.episodes?.getOrNull(episodeIndex)

        if (episode != null) {
            _uiState.update { it.copy(currentEpisodeIndex = episodeIndex, savedPositionMs = 0L) }
            loadPlaybackUrl(episode)
        }
    }

    private fun loadPlaybackUrl(episode: Episode?) {
        if (episode == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            videoRepository.getPlaybackUrl(episode.url).onSuccess { url ->
                _uiState.update { it.copy(isLoading = false, playbackUrl = url) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updatePlaybackState(positionMs: Long, durationMs: Long) {
        _currentPositionMs.value = positionMs
        if (durationMs > 0) {
            _durationMs.value = durationMs
        }
    }

    private fun startProgressSavingLoop() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            while (true) {
                delay(10000L)
                if (_currentPositionMs.value > 0) {
                    saveProgressInternal()
                }
            }
        }
    }

    fun saveOnPause() {
        viewModelScope.launch(Dispatchers.IO) {
            saveProgressInternal()
        }
    }

    private suspend fun saveProgressInternal() {
        val vod = vodContent ?: return
        val state = _uiState.value
        val source = state.sources.getOrNull(state.currentSourceIndex) ?: return
        val episode = source.episodes.getOrNull(state.currentEpisodeIndex) ?: return
        val position = _currentPositionMs.value
        val duration = _durationMs.value

        if (position <= 0) return

        val historyItem = WatchHistoryItem(
            vodId = vod.id,
            name = vod.name,
            pic = vod.pic,
            sourceIndex = state.currentSourceIndex,
            sourceName = source.name,
            episodeIndex = state.currentEpisodeIndex,
            episodeName = episode.name,
            positionMs = position,
            durationMs = duration,
            lastWatchedAt = System.currentTimeMillis()
        )
        watchHistoryRepository.saveWatchHistory(historyItem)
    }

    override fun onCleared() {
        super.onCleared()
        saveJob?.cancel()
        // Save one final time — use runBlocking since viewModelScope is already cancelled
        kotlinx.coroutines.runBlocking(Dispatchers.IO) {
            saveProgressInternal()
        }
    }
}
