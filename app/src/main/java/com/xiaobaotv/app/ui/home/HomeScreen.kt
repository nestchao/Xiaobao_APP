package com.xiaobaotv.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.xiaobaotv.app.domain.model.WatchHistoryItem
import com.xiaobaotv.app.ui.components.SectionHeader
import com.xiaobaotv.app.ui.components.VodPosterCard

@Composable
fun HomeScreen(
    onVodClick: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnVodClick by rememberUpdatedState(onVodClick)

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
            item {
                Text(
                    text = "热门推荐",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Continue Watching Row
            if (uiState.continueWatchingList.isNotEmpty()) {
                item {
                    SectionHeader(title = "继续观看", onMoreClick = {})
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp)
                    ) {
                        uiState.continueWatchingList.forEach { history ->
                            HistoryPosterCard(
                                history = history,
                                onClick = { currentOnVodClick(history.vodId) }
                            )
                        }
                    }
                }
            }

            // Movie Row
            item {
                SectionHeader(title = "最新电影", onMoreClick = {})
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp)
                ) {
                    uiState.hotMovies.forEach { vod ->
                        VodPosterCard(vod = vod, onClick = { currentOnVodClick(vod.id) })
                    }
                }
            }

            // TV Row
            item {
                SectionHeader(title = "热门剧集", onMoreClick = {})
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp)
                ) {
                    uiState.hotTvSeries.forEach { vod ->
                        VodPosterCard(vod = vod, onClick = { currentOnVodClick(vod.id) })
                    }
                }
            }

            // Anime Row
            item {
                SectionHeader(title = "热门动漫", onMoreClick = {})
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp)
                ) {
                    uiState.hotAnime.forEach { vod ->
                        VodPosterCard(vod = vod, onClick = { currentOnVodClick(vod.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryPosterCard(
    history: WatchHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(120.dp)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            SubcomposeAsyncImage(
                model = history.pic,
                contentDescription = history.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            )

            // Episode badge
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(bottomEnd = 8.dp))
            ) {
                Text(
                    text = history.episodeName,
                    color = Color.White,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Progress bar
            if (history.durationMs > 0) {
                val progress = history.positionMs.toFloat() / history.durationMs
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Black.copy(alpha = 0.4f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = history.name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
