package com.xiaobaotv.app.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaobaotv.app.domain.model.Episode
import com.xiaobaotv.app.domain.model.VideoSource
import com.xiaobaotv.app.domain.usecase.GetPlaybackUrlUseCase
import com.xiaobaotv.app.domain.usecase.GetVideoSourcesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val isLoading: Boolean = false,
    val vodId: Int = 0,
    val sources: List<VideoSource> = emptyList(),
    val currentSourceIndex: Int = 0,
    val currentEpisodeIndex: Int = 0,
    val playbackUrl: String? = null,
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getVideoSourcesUseCase: GetVideoSourcesUseCase,
    private val getPlaybackUrlUseCase: GetPlaybackUrlUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun loadVideoInfo(vodId: Int, episodeIndex: Int = 0) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, vodId = vodId, currentEpisodeIndex = episodeIndex) }

            getVideoSourcesUseCase(vodId).onSuccess { sources ->
                _uiState.update { it.copy(sources = sources) }
                if (sources.isNotEmpty()) {
                    loadPlaybackUrl(sources[0].episodes.getOrNull(episodeIndex))
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "No sources found") }
                }
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
            _uiState.update { it.copy(currentEpisodeIndex = episodeIndex) }
            loadPlaybackUrl(episode)
        }
    }

    private fun loadPlaybackUrl(episode: Episode?) {
        if (episode == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getPlaybackUrlUseCase(episode.url).onSuccess { url ->
                _uiState.update { it.copy(isLoading = false, playbackUrl = url) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
