package com.xiaobaotv.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xiaobaotv.app.ui.components.SectionHeader
import com.xiaobaotv.app.ui.components.VodPosterCard

@Composable
fun HomeScreen(
    onVodClick: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        uiState.error?.let { error ->
            Text(text = error, modifier = Modifier.align(Alignment.Center))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Hero section placeholder
            item {
                Text(
                    text = "热门推荐",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Movie Row
            item {
                SectionHeader(title = "最新电影", onMoreClick = {})
                LazyRow(contentPadding = PaddingValues(horizontal = 12.dp)) {
                    items(uiState.hotMovies) { vod ->
                        VodPosterCard(vod = vod, onClick = { onVodClick(vod.id) })
                    }
                }
            }

            // TV Row
            item {
                SectionHeader(title = "热门剧集", onMoreClick = {})
                LazyRow(contentPadding = PaddingValues(horizontal = 12.dp)) {
                    items(uiState.hotTvSeries) { vod ->
                        VodPosterCard(vod = vod, onClick = { onVodClick(vod.id) })
                    }
                }
            }

            // Anime Row
            item {
                SectionHeader(title = "热门动漫", onMoreClick = {})
                LazyRow(contentPadding = PaddingValues(horizontal = 12.dp)) {
                    items(uiState.hotAnime) { vod ->
                        VodPosterCard(vod = vod, onClick = { onVodClick(vod.id) })
                    }
                }
            }
        }
    }
}
