package com.xiaobaotv.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.xiaobaotv.app.domain.model.WatchHistoryItem
import com.xiaobaotv.app.ui.components.SectionHeader
import com.xiaobaotv.app.ui.components.VodPosterCard
import com.xiaobaotv.app.ui.navigation.isExpandedLayout

@Composable
fun HomeScreen(
    onVodClick: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onVodClickRemembered = remember(onVodClick) { { id: Int -> onVodClick(id) } }
    val isTablet = isExpandedLayout()

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
            // Hero Banner Carousel
            if (uiState.heroItems.isNotEmpty()) {
                item {
                    val pagerState = rememberPagerState(pageCount = { uiState.heroItems.size })
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isTablet) 320.dp else 220.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        HorizontalPager(state = pagerState) { page ->
                            val vod = uiState.heroItems[page]
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onVodClickRemembered(vod.id) }
                            ) {
                                AsyncImage(
                                    model = vod.pic,
                                    contentDescription = vod.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                                startY = 100f
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = vod.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    vod.remarks?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Continue Watching Row
            if (uiState.continueWatchingList.isNotEmpty()) {
                item { SectionHeader(title = "继续观看") }
                item {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.continueWatchingList,
                            key = { it.vodId },
                            contentType = { "history" }
                        ) { history ->
                            HistoryPosterCard(
                                history = history,
                                onClick = onVodClickRemembered,
                                modifier = Modifier.width(if (isTablet) 140.dp else 120.dp)
                            )
                        }
                    }
                }
            }

            // Movie Row
            if (uiState.hotMovies.isNotEmpty()) {
                item { SectionHeader(title = "最新电影") }
                item {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.hotMovies,
                            key = { it.id },
                            contentType = { "vod" }
                        ) { vod ->
                            VodPosterCard(
                                vod = vod,
                                onClick = onVodClickRemembered,
                                modifier = Modifier.width(if (isTablet) 140.dp else 120.dp)
                            )
                        }
                    }
                }
            }

            // TV Row
            if (uiState.hotTvSeries.isNotEmpty()) {
                item { SectionHeader(title = "热门剧集") }
                item {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.hotTvSeries,
                            key = { it.id },
                            contentType = { "vod" }
                        ) { vod ->
                            VodPosterCard(
                                vod = vod,
                                onClick = onVodClickRemembered,
                                modifier = Modifier.width(if (isTablet) 140.dp else 120.dp)
                            )
                        }
                    }
                }
            }

            // Anime Row
            if (uiState.hotAnime.isNotEmpty()) {
                item { SectionHeader(title = "热门动漫") }
                item {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.hotAnime,
                            key = { it.id },
                            contentType = { "vod" }
                        ) { vod ->
                            VodPosterCard(
                                vod = vod,
                                onClick = onVodClickRemembered,
                                modifier = Modifier.width(if (isTablet) 140.dp else 120.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryPosterCard(
    history: WatchHistoryItem,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick(history.vodId) }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(0.75f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = history.pic,
                contentDescription = history.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
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
