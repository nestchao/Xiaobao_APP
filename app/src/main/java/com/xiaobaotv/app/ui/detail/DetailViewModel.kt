package com.xiaobaotv.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaobaotv.app.domain.model.VideoSource
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.usecase.GetVideoSourcesUseCase
import com.xiaobaotv.app.domain.usecase.GetVodDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val isLoading: Boolean = false,
    val vod: VodContent? = null,
    val sources: List<VideoSource> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getVodDetailUseCase: GetVodDetailUseCase,
    private val getVideoSourcesUseCase: GetVideoSourcesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadDetail(vodId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Fetch detail and sources in parallel
            val detailJob = launch {
                getVodDetailUseCase(vodId).onSuccess { vod ->
                    _uiState.update { it.copy(vod = vod) }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            }

            val sourcesJob = launch {
                getVideoSourcesUseCase(vodId).onSuccess { sources ->
                    _uiState.update { it.copy(sources = sources) }
                }.onFailure { e ->
                    // Sources error might be acceptable if detail is still loading
                }
            }

            detailJob.join()
            sourcesJob.join()
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
