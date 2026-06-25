package com.xiaobaotv.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                    // Backdrop & Poster Header
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

                            // Play Button
                            Button(
                                onClick = { onPlayClick(vod.id, 1) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("立即播放")
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
                        item {
                            // Use a FlowRow or similar for episodes
                            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                                val rows = episodes.chunked(4)
                                rows.forEach { row ->
                                    Row(modifier = Modifier.fillMaxWidth()) {
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
                                        // Pad empty space if row is not full
                                        repeat(4 - row.size) {
                                            Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                                        }
                                    }
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
