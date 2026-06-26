package com.xiaobaotv.app.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.model.WatchHistoryItem
import com.xiaobaotv.app.domain.usecase.GetVodListUseCase
import com.xiaobaotv.app.domain.usecase.GetWatchHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val hotMovies: List<VodContent> = emptyList(),
    val hotTvSeries: List<VodContent> = emptyList(),
    val hotAnime: List<VodContent> = emptyList(),
    val continueWatchingList: List<WatchHistoryItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getVodListUseCase: GetVodListUseCase,
    private val getWatchHistoryUseCase: GetWatchHistoryUseCase,
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeContent()
        observeWatchHistory()
    }

    private fun observeWatchHistory() {
        getWatchHistoryUseCase()
            .onEach { historyList ->
                _uiState.update { it.copy(continueWatchingList = historyList.take(10)) }
            }
            .launchIn(viewModelScope)
    }

    fun loadHomeContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val moviesDeferred = async { getVodListUseCase(typeId = 1, limit = 10) }
                val tvDeferred = async { getVodListUseCase(typeId = 2, limit = 10) }
                val animeDeferred = async { getVodListUseCase(typeId = 4, limit = 10) }

                val results = awaitAll(moviesDeferred, tvDeferred, animeDeferred)
                val moviesResult = results[0]
                val tvResult = results[1]
                val animeResult = results[2]

                val movies = moviesResult.getOrDefault(emptyList())
                val tvSeries = tvResult.getOrDefault(emptyList())
                val anime = animeResult.getOrDefault(emptyList())

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        heroItems = movies.take(5),
                        hotMovies = movies,
                        hotTvSeries = tvSeries,
                        hotAnime = anime
                    )
                }

                preloadImages(movies)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun preloadImages(items: List<VodContent>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            val imageLoader = Coil.imageLoader(context)
            items.forEach { vod ->
                imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(vod.pic)
                        .size(360)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()
                )
            }
        }
    }
}
