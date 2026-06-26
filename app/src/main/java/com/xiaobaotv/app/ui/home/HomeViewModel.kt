package com.xiaobaotv.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.model.WatchHistoryItem
import com.xiaobaotv.app.domain.repository.ContentRepository
import com.xiaobaotv.app.domain.repository.WatchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val heroItems: List<VodContent> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val watchHistoryRepository: WatchHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _hotMovies = MutableStateFlow<List<VodContent>>(emptyList())
    val hotMovies: StateFlow<List<VodContent>> = _hotMovies.asStateFlow()

    private val _hotTvSeries = MutableStateFlow<List<VodContent>>(emptyList())
    val hotTvSeries: StateFlow<List<VodContent>> = _hotTvSeries.asStateFlow()

    private val _hotAnime = MutableStateFlow<List<VodContent>>(emptyList())
    val hotAnime: StateFlow<List<VodContent>> = _hotAnime.asStateFlow()

    private val _continueWatchingList = MutableStateFlow<List<WatchHistoryItem>>(emptyList())
    val continueWatchingList: StateFlow<List<WatchHistoryItem>> = _continueWatchingList.asStateFlow()

    init {
        loadHomeContent()
        observeWatchHistory()
    }

    private fun observeWatchHistory() {
        watchHistoryRepository.getWatchHistory()
            .onEach { historyList ->
                _continueWatchingList.value = historyList.take(10)
            }
            .launchIn(viewModelScope)
    }

    fun loadHomeContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val moviesDeferred = async { contentRepository.getVodList(typeId = 1, limit = 10) }
                val tvDeferred = async { contentRepository.getVodList(typeId = 2, limit = 10) }
                val animeDeferred = async { contentRepository.getVodList(typeId = 4, limit = 10) }

                val results = awaitAll(moviesDeferred, tvDeferred, animeDeferred)
                val moviesResult = results[0]
                val tvResult = results[1]
                val animeResult = results[2]

                val movies = moviesResult.getOrDefault(emptyList())
                val tvSeries = tvResult.getOrDefault(emptyList())
                val anime = animeResult.getOrDefault(emptyList())

                _uiState.update { it.copy(isLoading = false, heroItems = movies.take(5)) }
                _hotMovies.value = movies
                _hotTvSeries.value = tvSeries
                _hotAnime.value = anime
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
