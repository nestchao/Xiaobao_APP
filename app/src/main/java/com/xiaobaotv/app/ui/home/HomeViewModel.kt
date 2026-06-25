package com.xiaobaotv.app.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.usecase.GetVodListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val heroItems: List<VodContent> = emptyList(),
    val hotMovies: List<VodContent> = emptyList(),
    val hotTvSeries: List<VodContent> = emptyList(),
    val hotAnime: List<VodContent> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getVodListUseCase: GetVodListUseCase,
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeContent()
    }

    fun loadHomeContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Type IDs: 1=Movie, 2=TV, 3=Variety, 4=Anime
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

                // Preload images for the initially visible row (movies) to avoid first-frame flash.
                // Additional rows are preloaded as the user scrolls.
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
