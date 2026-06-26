package com.xiaobaotv.app.ui.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.xiaobaotv.app.ui.components.VodPosterCard

@Composable
fun CategoryScreen(
    onVodClick: (Int) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val onVodClickRemembered = remember(onVodClick) { { id: Int -> onVodClick(id) } }

    val categories = listOf(
        1 to "电影",
        2 to "电视剧",
        3 to "综艺",
        4 to "动漫",
        5 to "短剧"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = categories.indexOfFirst { it.first == uiState.selectedTypeId }.coerceAtLeast(0),
            edgePadding = 16.dp,
            divider = {}
        ) {
            categories.forEach { (id, name) ->
                Tab(
                    selected = uiState.selectedTypeId == id,
                    onClick = { viewModel.selectType(id) },
                    text = { Text(name) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (pagingItems.itemCount == 0 && pagingItems.loadState.refresh is LoadState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = { index ->
                        val item = pagingItems[index]
                        item?.id ?: "loading_$index"
                    },
                    contentType = { index ->
                        if (pagingItems[index] != null) "vod" else "placeholder"
                    }
                ) { index ->
                    val vod = pagingItems[index]
                    if (vod != null) {
                        VodPosterCard(vod = vod, onClick = onVodClickRemembered)
                    }
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
