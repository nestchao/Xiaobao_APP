package com.xiaobaotv.app.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.usecase.GetVodListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val isLoading: Boolean = false,
    val selectedTypeId: Int = 1,
    val items: List<VodContent> = emptyList(),
    val page: Int = 1,
    val error: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getVodListUseCase: GetVodListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategoryContent()
    }

    fun selectType(typeId: Int) {
        if (_uiState.value.selectedTypeId == typeId) return
        _uiState.update { it.copy(selectedTypeId = typeId, items = emptyList(), page = 1) }
        loadCategoryContent()
    }

    fun loadCategoryContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = getVodListUseCase(
                typeId = _uiState.value.selectedTypeId,
                page = _uiState.value.page
            )
            result.onSuccess { newItems ->
                _uiState.update { it.copy(
                    isLoading = false,
                    items = it.items + newItems,
                    error = null
                ) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadNextPage() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(page = it.page + 1) }
        loadCategoryContent()
    }
}
