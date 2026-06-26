package com.xiaobaotv.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.xiaobaotv.app.domain.model.Episode
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.ui.navigation.isExpandedLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    vodId: Int,
    onPlayClick: (Int, Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isTablet = isExpandedLayout()

    LaunchedEffect(vodId) {
        viewModel.loadDetail(vodId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.vod?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (uiState.isLoading && uiState.vod == null) {
                DetailSkeleton()
            }

            uiState.error?.let { error ->
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { viewModel.loadDetail(vodId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Text("重试")
                    }
                }
            }

            uiState.vod?.let { vod ->
                if (isTablet) {
                    TabletDetailLayout(
                        vod = vod,
                        uiState = uiState,
                        onPlayClick = onPlayClick,
                        onRetrySources = { viewModel.loadDetail(vodId) }
                    )
                } else {
                    MobileDetailLayout(
                        vod = vod,
                        uiState = uiState,
                        onPlayClick = onPlayClick
                    )
                }
            }
        }
    }
}

@Composable
private fun TabletDetailLayout(
    vod: VodContent,
    uiState: DetailUiState,
    onPlayClick: (Int, Int) -> Unit,
    onRetrySources: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left Column (Poster and Metadata Info)
        Column(
            modifier = Modifier.weight(0.4f),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = vod.pic,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 150f
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = vod.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${vod.year ?: ""} / ${vod.area ?: ""} / ${vod.typeName ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            PlayActionButton(uiState = uiState, vod = vod, onPlayClick = onPlayClick)
        }

        // Right Column (Storyline details + Episodes)
        Column(
            modifier = Modifier.weight(0.6f)
        ) {
            Text(
                text = "剧情简介",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = vod.content ?: "暂无简介",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "选集播放",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.sources.isNotEmpty()) {
                val episodes = uiState.sources.firstOrNull()?.episodes ?: emptyList()
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(90.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(episodes, key = { it.index }) { episode ->
                        OutlinedButton(
                            onClick = { onPlayClick(vod.id, episode.index) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = episode.name,
                                maxLines = 1,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else if (uiState.sourcesError != null) {
                Text(
                    text = uiState.sourcesError,
                    color = MaterialTheme.colorScheme.error
                )
                OutlinedButton(onClick = onRetrySources) {
                    Text("重试")
                }
            }
        }
    }
}

@Composable
private fun MobileDetailLayout(
    vod: VodContent,
    uiState: DetailUiState,
    onPlayClick: (Int, Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Backdrop Image
        item {
            Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                AsyncImage(
                    model = vod.pic,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                                startY = 0f,
                                endY = 1000f
                            )
                        )
                )
            }
        }

        // Content details & Play Action button
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = vod.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!vod.score.isNullOrBlank()) {
                        Text(
                            text = vod.score,
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "${vod.year ?: ""} / ${vod.area ?: ""} / ${vod.typeName ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                PlayActionButton(uiState = uiState, vod = vod, onPlayClick = onPlayClick)

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "剧情简介",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = vod.content ?: "暂无简介",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Episode Selection Layout (Mobile)
        if (uiState.sources.isNotEmpty()) {
            item {
                Text(
                    text = "选集播放",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            val episodes = uiState.sources.firstOrNull()?.episodes ?: emptyList()
            val episodeRows = episodes.chunked(4)
            items(episodeRows, key = { row -> row.firstOrNull()?.index ?: 0 }) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    row.forEach { episode ->
                        OutlinedButton(
                            onClick = { onPlayClick(vod.id, episode.index) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = episode.name,
                                maxLines = 1,
                                fontSize = 12.sp
                            )
                        }
                    }
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayActionButton(
    uiState: DetailUiState,
    vod: VodContent,
    onPlayClick: (Int, Int) -> Unit
) {
    val history = uiState.historyItem
    val buttonText = if (history != null && history.durationMs > 0) {
        val progress = (history.positionMs * 100 / history.durationMs).toInt().coerceIn(0, 100)
        "继续播放 ${history.episodeName} (已看 $progress%)"
    } else if (history != null) {
        "继续播放 ${history.episodeName}"
    } else {
        "立即播放"
    }
    val targetEpisodeNum = if (history != null) history.episodeIndex + 1 else 1

    Button(
        onClick = { onPlayClick(vod.id, targetEpisodeNum) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(buttonText)
    }
}

@Composable
private fun DetailSkeleton() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(24.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(20.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}
