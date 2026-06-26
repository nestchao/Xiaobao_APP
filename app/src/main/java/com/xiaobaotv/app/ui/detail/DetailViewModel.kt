package com.xiaobaotv.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaobaotv.app.data.cache.DetailPageCache
import com.xiaobaotv.app.data.cache.PlaybackUrlCache
import com.xiaobaotv.app.domain.model.VideoSource
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.model.WatchHistoryItem
import com.xiaobaotv.app.domain.repository.ContentRepository
import com.xiaobaotv.app.domain.repository.VideoRepository
import com.xiaobaotv.app.domain.repository.WatchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val isLoading: Boolean = false,
    val vod: VodContent? = null,
    val sources: List<VideoSource> = emptyList(),
    val historyItem: WatchHistoryItem? = null,
    val error: String? = null,
    val sourcesError: String? = null,
    val isFromCache: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val videoRepository: VideoRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val playbackUrlCache: PlaybackUrlCache,
    private val detailPageCache: DetailPageCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadDetail(vodId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, sourcesError = null) }

            val detailDeferred = async { contentRepository.getVodDetail(vodId) }
            val sourcesDeferred = async { videoRepository.getVideoSources(vodId) }
            val historyDeferred = async { watchHistoryRepository.getWatchHistoryItem(vodId) }

            detailDeferred.await().onSuccess { vod ->
                _uiState.update { it.copy(vod = vod) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }

            val history = historyDeferred.await()
            _uiState.update { it.copy(historyItem = history) }

            sourcesDeferred.await().onSuccess { sources ->
                applySources(vodId, sources)
            }.onFailure { e ->
                detailPageCache.remove(vodId)
                videoRepository.getVideoSources(vodId).onSuccess { sources ->
                    applySources(vodId, sources)
                }.onFailure { retryError ->
                    _uiState.update {
                        it.copy(sourcesError = retryError.message ?: "Failed to load episodes")
                    }
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun applySources(vodId: Int, sources: List<VideoSource>) {
        _uiState.update { it.copy(sources = sources) }

        val firstEpisode = sources.firstOrNull()?.episodes?.firstOrNull()
        if (firstEpisode != null) {
            viewModelScope.launch {
                videoRepository.getPlaybackUrl(firstEpisode.url).onSuccess { url ->
                    playbackUrlCache.put(vodId, 0, url)
                }
            }
        }
    }
}
