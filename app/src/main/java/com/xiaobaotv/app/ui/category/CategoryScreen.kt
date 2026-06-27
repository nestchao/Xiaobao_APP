package com.xiaobaotv.app.ui.category

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.xiaobaotv.app.ui.navigation.isExpandedLayout

@Composable
fun CategoryScreen(
    onVodClick: (Int) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val onVodClickRemembered = remember(onVodClick) { { id: Int -> onVodClick(id) } }
    val isTablet = isExpandedLayout()

    val categories = listOf(
        1 to "电影",
        2 to "电视剧",
        3 to "综艺",
        4 to "动漫",
        5 to "短剧"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Pill-style category chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            categories.forEach { (id, name) ->
                val selected = uiState.selectedTypeId == id
                Surface(
                    onClick = { viewModel.selectType(id) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (pagingItems.itemCount == 0 && pagingItems.loadState.refresh is LoadState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LazyVerticalGrid(
                columns = if (isTablet) GridCells.Adaptive(130.dp) else GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
