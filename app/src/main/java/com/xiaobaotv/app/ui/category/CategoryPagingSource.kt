package com.xiaobaotv.app.ui.category

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.usecase.GetVodListUseCase

class CategoryPagingSource(
    private val getVodListUseCase: GetVodListUseCase,
    private val typeId: Int
) : PagingSource<Int, VodContent>() {

    override fun getRefreshKey(state: PagingState<Int, VodContent>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, VodContent> {
        val page = params.key ?: 1
        return try {
            val result = getVodListUseCase(typeId = typeId, page = page)
            result.fold(
                onSuccess = { items ->
                    LoadResult.Page(
                        data = items,
                        prevKey = if (page > 1) page - 1 else null,
                        nextKey = if (items.isNotEmpty()) page + 1 else null
                    )
                },
                onFailure = { LoadResult.Error(it) }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
