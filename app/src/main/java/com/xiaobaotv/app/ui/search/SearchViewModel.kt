package com.xiaobaotv.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.usecase.GetVodListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<VodContent> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getVodListUseCase: GetVodListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val searchFlow = MutableSharedFlow<String>()

    init {
        setupSearch()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearch() {
        viewModelScope.launch {
            searchFlow
                .debounce(500)
                .distinctUntilChanged()
                .filter { it.length >= 1 }
                .onEach { q ->
                    performSearch(q)
                }
                .collect()
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        viewModelScope.launch {
            searchFlow.emit(newQuery)
        }
    }

    private suspend fun performSearch(q: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        getVodListUseCase(query = q).onSuccess { list ->
            _uiState.update { it.copy(isLoading = false, results = list) }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(query = "", results = emptyList()) }
    }
}
