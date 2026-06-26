package com.xiaobaotv.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    vodId: Int,
    onPlayClick: (Int, Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Backdrop
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

                    // Content Info
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

                            // Play Button — shows "继续播放" if history exists
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

                    // Episode Selection
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
                    uiState.sourcesError?.let { error ->
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                    }
                }
            }
        }
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
