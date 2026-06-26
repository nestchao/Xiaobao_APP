package com.xiaobaotv.app.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class CategoryUiState(
    val isLoading: Boolean = false,
    val selectedTypeId: Int = 1,
    val error: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private val _typeIdFlow = MutableStateFlow(1)
    val pagingDataFlow: Flow<PagingData<VodContent>> = _typeIdFlow
        .flatMapLatest { typeId ->
            Pager(PagingConfig(pageSize = 20, prefetchDistance = 4)) {
                CategoryPagingSource(contentRepository, typeId)
            }.flow
        }
        .cachedIn(viewModelScope)

    fun selectType(typeId: Int) {
        if (_typeIdFlow.value == typeId) return
        _typeIdFlow.value = typeId
        _uiState.update { it.copy(selectedTypeId = typeId, error = null) }
    }
}
