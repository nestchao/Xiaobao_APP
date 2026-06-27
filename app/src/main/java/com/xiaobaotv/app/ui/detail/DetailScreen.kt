package com.xiaobaotv.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.xiaobaotv.app.domain.model.Episode
import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.ui.navigation.isExpandedLayout
import com.xiaobaotv.app.ui.theme.ScoreStar

private const val EPISODE_GROUP_SIZE = 100

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

    Box(modifier = Modifier.fillMaxSize()) {
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
                    onBackClick = onBackClick,
                    onRetrySources = { viewModel.loadDetail(vodId) }
                )
            } else {
                MobileDetailLayout(
                    vod = vod,
                    uiState = uiState,
                    onPlayClick = onPlayClick,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

// ─── Tablet Layout ───────────────────────────────────────────────────────

@Composable
private fun TabletDetailLayout(
    vod: VodContent,
    uiState: DetailUiState,
    onPlayClick: (Int, Int) -> Unit,
    onBackClick: () -> Unit,
    onRetrySources: () -> Unit
) {
    val scrollState = rememberScrollState()
    val episodes = uiState.sources.firstOrNull()?.episodes ?: emptyList()
    var selectedGroupIndex by remember(uiState.sources) { mutableStateOf(0) }

    val groups = remember(episodes) { episodes.chunked(EPISODE_GROUP_SIZE) }
    val visibleEpisodes = remember(groups, selectedGroupIndex) {
        groups.getOrNull(selectedGroupIndex) ?: emptyList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Backdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
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
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )
            }

            // Content row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left: poster + info
                Column(
                    modifier = Modifier.weight(0.35f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.7f)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = vod.pic,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    VodMetaInfo(vod = vod)
                    Spacer(modifier = Modifier.height(16.dp))
                    PlayActionButton(uiState = uiState, vod = vod, onPlayClick = onPlayClick)
                }

                // Right: synopsis + episodes
                Column(modifier = Modifier.weight(0.65f)) {
                    Text(
                        text = "剧情简介",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = vod.content ?: "暂无简介",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "选集播放",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.sources.isNotEmpty()) {
                        if (groups.size > 1) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                items(groups.size) { index ->
                                    val groupEpisodes = groups[index]
                                    val label = "${groupEpisodes.first().name} - ${groupEpisodes.last().name}"
                                    val isSelected = selectedGroupIndex == index
                                    if (isSelected) {
                                        Button(
                                            onClick = { selectedGroupIndex = index },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(label, fontSize = 12.sp)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { selectedGroupIndex = index },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(label, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        val episodeRows = visibleEpisodes.chunked(4)
                        episodeRows.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { episode ->
                                    OutlinedButton(
                                        onClick = { onPlayClick(vod.id, episode.index) },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(text = episode.name, maxLines = 1, fontSize = 12.sp)
                                    }
                                }
                                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
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
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Floating back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White
            )
        }
    }
}

// ─── Mobile Layout ───────────────────────────────────────────────────────

@Composable
private fun MobileDetailLayout(
    vod: VodContent,
    uiState: DetailUiState,
    onPlayClick: (Int, Int) -> Unit,
    onBackClick: () -> Unit
) {
    val episodes = uiState.sources.firstOrNull()?.episodes ?: emptyList()
    var selectedGroupIndex by remember(uiState.sources) { mutableStateOf(0) }

    val groups = remember(episodes) { episodes.chunked(EPISODE_GROUP_SIZE) }
    val visibleEpisodes = remember(groups, selectedGroupIndex) {
        groups.getOrNull(selectedGroupIndex) ?: emptyList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Full-bleed backdrop image
            item {
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                ) {
                    AsyncImage(
                        model = vod.pic,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay: transparent at top → dark at bottom
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.1f),
                                        MaterialTheme.colorScheme.background
                                    ),
                                    startY = 0f,
                                    endY = 1000f
                                )
                            )
                    )
                }
            }

            // Title and metadata
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    // Title
                    Text(
                        text = vod.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Score + metadata row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Score with star
                        if (!vod.score.isNullOrBlank() && vod.score != "0.0") {
                            Text(
                                text = "★",
                                color = ScoreStar,
                                fontSize = 14.sp
                            )
                            Text(
                                text = vod.score,
                                color = ScoreStar,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "|",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = vod.year?.take(4) ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!vod.area.isNullOrBlank()) {
                            Text(
                                text = vod.area,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Genre chip
                        if (!vod.typeName.isNullOrBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = vod.typeName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Action buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Primary play button
                    Button(
                        onClick = {
                            val history = uiState.historyItem
                            val episode = if (history != null) history.episodeIndex + 1 else 1
                            onPlayClick(vod.id, episode)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val history = uiState.historyItem
                        val buttonText = if (history != null && history.durationMs > 0) {
                            val pct = (history.positionMs * 100 / history.durationMs).toInt().coerceIn(0, 100)
                            "继续 ${history.episodeName} ($pct%)"
                        } else if (history != null) {
                            "继续 ${history.episodeName}"
                        } else {
                            "立即播放"
                        }
                        Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    // Secondary action (favorites / watchlist)
                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier.width(52.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "收藏",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Synopsis
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "剧情简介",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = vod.content ?: "暂无简介",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }

            // Episode selection
            if (uiState.sources.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "选集播放",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (groups.size > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            items(groups.size) { index ->
                                val groupEpisodes = groups[index]
                                val label = "${groupEpisodes.first().name} - ${groupEpisodes.last().name}"
                                val isSelected = selectedGroupIndex == index
                                if (isSelected) {
                                    Button(
                                        onClick = { selectedGroupIndex = index },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(label, fontSize = 12.sp)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { selectedGroupIndex = index },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(label, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                val episodeRows = visibleEpisodes.chunked(4)
                items(episodeRows, key = { row -> row.firstOrNull()?.index ?: 0 }) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { episode ->
                            Button(
                                onClick = { onPlayClick(vod.id, episode.index) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Text(
                                    text = episode.name,
                                    maxLines = 1,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        repeat(4 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Floating back button over the backdrop
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White
            )
        }
    }
}

// ─── Play Action Button ──────────────────────────────────────────────────

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

// ─── VOD Meta Info ──────────────────────────────────────────────────────

@Composable
private fun VodMetaInfo(vod: VodContent) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Score
            if (!vod.score.isNullOrBlank() && vod.score != "0.0") {
                Text(
                    text = "★",
                    color = ScoreStar,
                    fontSize = 16.sp
                )
                Text(
                    text = vod.score,
                    color = ScoreStar,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "|",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Text(
                text = "${vod.year?.take(4) ?: ""} / ${vod.area ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!vod.typeName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = vod.typeName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (!vod.actor.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "主演: ${vod.actor}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

// ─── Skeleton Loading ────────────────────────────────────────────────────

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
