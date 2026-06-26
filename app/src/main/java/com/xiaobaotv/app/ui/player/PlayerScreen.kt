package com.xiaobaotv.app.ui.player

import android.view.GestureDetector
import android.view.MotionEvent
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.delay

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
    data class SkipEvent(val deltaMs: Long)
    var skipEvent by remember { mutableStateOf<SkipEvent?>(null) }

    LaunchedEffect(skipEvent) {
        if (skipEvent != null) {
            delay(500L)
            skipEvent = null
        }
    }

    val exoPlayer = remember(vodId) {
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
                        30_000,
                        120_000,
                        2_500,
                        5_000
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
            if (uiState.savedPositionMs > 0L) {
                exoPlayer.seekTo(uiState.savedPositionMs)
            }
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    // Position polling — feed position to ViewModel for periodic saves
    LaunchedEffect(exoPlayer) {
        while (true) {
            if (exoPlayer.isPlaying) {
                viewModel.updatePlaybackState(exoPlayer.currentPosition, exoPlayer.duration)
            }
            delay(1500L)
        }
    }

    // Lifecycle-aware save on pause/stop
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.saveOnPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(vodId) {
        onDispose { exoPlayer.release() }
    }

    // Full-screen toggle
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
            var playerFullScreen = false
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        setFullscreenButtonClickListener {
                            playerFullScreen = !playerFullScreen
                            fullScreenState.isActive = playerFullScreen
                        }
                        setControllerVisibilityListener(
                            PlayerControlView.VisibilityListener { visibility: Int ->
                                showControls = visibility == View.VISIBLE
                            }
                        )
                        val gestureDetector = GestureDetector(
                            ctx,
                            object : GestureDetector.SimpleOnGestureListener() {
                                override fun onDoubleTap(e: MotionEvent): Boolean {
                                    val seekAmount =
                                        if (e.x < width / 2f) -5000L else 5000L
                                    val newPos = (exoPlayer.currentPosition + seekAmount)
                                        .coerceIn(0L, exoPlayer.duration.coerceAtLeast(0))
                                    exoPlayer.seekTo(newPos)
                                    skipEvent = SkipEvent(seekAmount)
                                    // Hide controls in case the first tap triggered them
                                    post { hideController() }
                                    return true
                                }
                            }
                        )
                        setOnTouchListener { _, event ->
                            gestureDetector.onTouchEvent(event)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Skip indicator overlay
        skipEvent?.let { event ->
            Box(
                modifier = Modifier
                    .align(if (event.deltaMs > 0) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(horizontal = 48.dp)
                    .size(60.dp)
                    .background(Color.Black.copy(alpha = 0.75f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${if (event.deltaMs > 0) "+" else ""}${event.deltaMs / 1000}s",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        // Back button overlay
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
