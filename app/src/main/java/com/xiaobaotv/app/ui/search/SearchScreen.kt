package com.xiaobaotv.app.ui.search

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xiaobaotv.app.ui.components.VodPosterCard
import com.xiaobaotv.app.ui.navigation.isExpandedLayout

private val hotTags = listOf("庆余年", "墨雨云间", "玫瑰的故事", "三体", "繁花", "赘婿", "狂飙", "漫长的季节")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onVodClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onVodClickRemembered = remember(onVodClick) { { id: Int -> onVodClick(id) } }
    val isTablet = isExpandedLayout()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        TextField(
            value = uiState.query,
            onValueChange = { viewModel.onQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = {
                Text("搜索你想看的", color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "清除",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Box(modifier = Modifier.weight(1f)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (uiState.query.isEmpty() && !uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (uiState.recentSearches.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "最近搜索",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { viewModel.clearRecentSearches() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "清除全部历史",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            uiState.recentSearches.forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.animateContentSize()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = item.query,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.clickable { viewModel.onQueryChange(item.query) }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { viewModel.deleteRecentSearch(item.query) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "删除单个",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "热门搜索",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Tags in a wrapping flow
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        hotTags.forEach { tag ->
                            Surface(
                                onClick = { viewModel.onQueryChange(tag) },
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.results.isEmpty() && !uiState.isLoading && uiState.query.isNotEmpty()) {
                Text(
                    text = "没有找到相关内容",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyVerticalGrid(
                columns = if (isTablet) GridCells.Adaptive(130.dp) else GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.results, key = { it.id }) { vod ->
                    VodPosterCard(vod = vod, onClick = onVodClickRemembered)
                }
            }
        }
    }
}
