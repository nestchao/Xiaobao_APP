package com.xiaobaotv.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.usecase.GetVodListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val getVodListUseCase: GetVodListUseCase
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

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        heroItems = moviesResult.getOrDefault(emptyList()).take(5),
                        hotMovies = moviesResult.getOrDefault(emptyList()),
                        hotTvSeries = tvResult.getOrDefault(emptyList()),
                        hotAnime = animeResult.getOrDefault(emptyList())
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
