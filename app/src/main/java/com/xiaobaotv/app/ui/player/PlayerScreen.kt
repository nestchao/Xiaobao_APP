package com.xiaobaotv.app.ui.player

import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.xiaobaotv.app.ui.navigation.LocalFullScreenState
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    vodId: Int,
    episodeIndex: Int = 0,
    onBackClick: () -> Unit = {},
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val fullScreenState = LocalFullScreenState.current
    var showControls by remember { mutableStateOf(true) }

    val exoPlayer = remember {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(mapOf("Referer" to "https://www.xiaobaotv.tv/"))
        val cacheDataSourceFactory = androidx.media3.datasource.cache.CacheDataSource.Factory()
            .setCache(viewModel.exoPlayerCache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setLoadControl(
                androidx.media3.exoplayer.DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        30_000,  // min buffer: 30s (up from 15s default)
                        120_000, // max buffer: 120s (up from 50s default)
                        2_500,   // buffer for playback start: 2.5s (default)
                        5_000    // buffer for rebuffer: 5s (default)
                    )
                    .build()
            )
            .build()
    }

    LaunchedEffect(vodId) {
        viewModel.loadVideoInfo(vodId, episodeIndex)
    }

    LaunchedEffect(uiState.playbackUrl) {
        uiState.playbackUrl?.let { url ->
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // Full-screen toggle (hide/show system bars)
    val activity = LocalContext.current as? ComponentActivity
    LaunchedEffect(fullScreenState.isActive) {
        if (activity == null) return@LaunchedEffect
        if (fullScreenState.isActive) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    // Restore system bars when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            if (fullScreenState.isActive && activity != null) {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
            fullScreenState.isActive = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        uiState.error?.let { error ->
            Text(text = "$error", color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
        if (uiState.playbackUrl != null) {
            // Track fullscreen state locally (PlayerView doesn't expose isFullscreen in 1.4.1)
            var playerFullScreen = false
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        // Built-in fullscreen button — appears to the right of the settings gear
                        // in the default ExoPlayer control bar
                        setFullscreenButtonClickListener {
                            playerFullScreen = !playerFullScreen
                            fullScreenState.isActive = playerFullScreen
                        }
                        // Sync back button visibility with ExoPlayer controller
                        setControllerVisibilityListener(
                            PlayerControlView.VisibilityListener { visibility: Int ->
                                showControls = visibility == View.VISIBLE
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Back button overlay (only visible when controls are shown)
        if (showControls) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
        }
    }
}
