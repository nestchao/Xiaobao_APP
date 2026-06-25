package com.xiaobaotv.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaobaotv.app.data.cache.DetailPageCache
import com.xiaobaotv.app.data.cache.PlaybackUrlCache
import com.xiaobaotv.app.domain.model.VideoSource
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.usecase.GetPlaybackUrlUseCase
import com.xiaobaotv.app.domain.usecase.GetVideoSourcesUseCase
import com.xiaobaotv.app.domain.usecase.GetVodDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val isLoading: Boolean = false,
    val vod: VodContent? = null,
    val sources: List<VideoSource> = emptyList(),
    val error: String? = null,
    val sourcesError: String? = null,
    val isFromCache: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getVodDetailUseCase: GetVodDetailUseCase,
    private val getVideoSourcesUseCase: GetVideoSourcesUseCase,
    private val getPlaybackUrlUseCase: GetPlaybackUrlUseCase,
    private val playbackUrlCache: PlaybackUrlCache,
    private val detailPageCache: DetailPageCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadDetail(vodId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, sourcesError = null) }

            // Launch detail and sources in parallel — DetailPageCache.getOrFetch
            // deduplicates the underlying HTML fetch so only one network call is made.
            val detailDeferred = async { getVodDetailUseCase(vodId) }
            val sourcesDeferred = async { getVideoSourcesUseCase(vodId) }

            // Await both concurrently
            detailDeferred.await().onSuccess { vod ->
                _uiState.update { it.copy(vod = vod) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }

            sourcesDeferred.await().onSuccess { sources ->
                applySources(vodId, sources)
            }.onFailure { e ->
                // Automatic retry: clear the shared HTML cache so the next
                // attempt fetches a fresh page (the cached HTML may have been
                // incomplete or formatted differently).
                detailPageCache.remove(vodId)
                delay(1500L)
                getVideoSourcesUseCase(vodId).onSuccess { sources ->
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

        // Pre-fetch the first episode's playback URL
        val firstEpisode = sources.firstOrNull()?.episodes?.firstOrNull()
        if (firstEpisode != null) {
            viewModelScope.launch {
                getPlaybackUrlUseCase(firstEpisode.url).onSuccess { url ->
                    playbackUrlCache.put(vodId, url)
                }
            }
        }
    }
}
