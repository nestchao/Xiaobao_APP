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
import com.xiaobaotv.app.ui.components.VodPosterCard

@Composable
fun CategoryScreen(
    onVodClick: (Int) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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

        Box(modifier = Modifier.fillWeight(1f)) {
            if (uiState.items.isEmpty() && uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.items) { vod ->
                    VodPosterCard(vod = vod, onClick = { onVodClick(vod.id) })
                }

                // Simple infinite scroll trigger
                item {
                    LaunchedEffect(Unit) {
                        viewModel.loadNextPage()
                    }
                }
            }
        }
    }
}

private fun Modifier.fillWeight(weight: Float) = this.then(Modifier.fillMaxHeight().fillMaxWidth())
