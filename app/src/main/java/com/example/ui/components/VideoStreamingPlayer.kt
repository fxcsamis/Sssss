package com.example.ui.components

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.IBinder
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import com.example.newpipe.AudioPlaybackService
import com.example.newpipe.NewPipeDownloader
import com.example.newpipe.ResolvedStream
import com.example.ui.CloudVideo
import com.example.ui.CloudihubViewModel
import com.example.ui.screens.VideoCloudCard
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

/**
 * YouTube-style watch screen: fixed-height (or fullscreen) video player at the top, and a
 * scrollable info + related-videos section below it. Includes double-tap seek, fullscreen,
 * a Settings sheet (quality/speed/captions/lock), a live network-speed indicator, and a
 * "Convert to audio" background-playback mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoStreamingPlayer(
    viewModel: CloudihubViewModel,
    modifier: Modifier = Modifier
) {
    val video = viewModel.playingVideo ?: return
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    val resolvedStream by viewModel.resolvedStream.collectAsState()
    val isResolving = viewModel.isResolvingStream
    val resolveError = viewModel.streamResolveError

    var isPlaying by remember { mutableStateOf(true) }
    var progressSec by remember { mutableStateOf(0) }
    var volume by remember { mutableStateOf(1f) }
    var isMuted by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var hasAutoRetriedQuality by remember(video.id) { mutableStateOf(false) }

    // ===== New playback feature state =====
    var isFullscreen by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1f) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var captionsEnabled by remember { mutableStateOf(false) }
    var isAudioMode by remember { mutableStateOf(false) }

    // Double-tap seek visual feedback ("+10" / "-10" flash on the tapped side).
    var seekFlashSide by remember { mutableStateOf(0) } // -1 = left/back, 1 = right/forward, 0 = none
    var seekFlashTrigger by remember { mutableStateOf(0) }

    val exoPlayer = remember(context) {
        val renderersFactory = androidx.media3.exoplayer.DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
        ExoPlayer.Builder(context, renderersFactory).build().apply { playWhenReady = true }
    }

    // Shared bandwidth meter attached to every network request this player makes, so the speed
    // badge reflects the real transfer rate of the video/audio actually being streamed.
    val bandwidthMeter = remember(context) { DefaultBandwidthMeter.Builder(context).build() }

    val totalSeconds = remember(video) {
        val parts = video.duration.split(":")
        when (parts.size) {
            2 -> (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
            3 -> (parts[0].toIntOrNull() ?: 0) * 3600 + (parts[1].toIntOrNull() ?: 0) * 60 + (parts[2].toIntOrNull() ?: 0)
            else -> 300
        }
    }

    fun buildDataSourceFactory() = DefaultHttpDataSource.Factory()
        .setUserAgent(NewPipeDownloader.USER_AGENT)
        .setAllowCrossProtocolRedirects(true)
        .setTransferListener(bandwidthMeter)

    // Load the resolved stream into ExoPlayer. Adaptive (separate video-only + audio-only,
    // for high resolutions like 1080p+) streams are merged via MergingMediaSource; live streams
    // use HLS; everything else falls back to a plain progressive source.
    LaunchedEffect(resolvedStream, captionsEnabled) {
        val stream = resolvedStream ?: return@LaunchedEffect
        if (isAudioMode) return@LaunchedEffect // audio mode is driven by the background service instead
        playbackError = null
        val dataSourceFactory = buildDataSourceFactory()

        val subtitleConfigs = if (captionsEnabled) {
            val captions = when (stream) {
                is ResolvedStream.Adaptive -> stream.captions
                is ResolvedStream.Progressive -> stream.captions
                is ResolvedStream.Live -> emptyList()
            }
            captions.take(1).map {
                MediaItem.SubtitleConfiguration.Builder(android.net.Uri.parse(it.url))
                    .setMimeType(it.mimeType)
                    .setLanguage(it.languageName)
                    .build()
            }
        } else {
            emptyList()
        }

        when (stream) {
            is ResolvedStream.Adaptive -> {
                val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(stream.videoOnlyUrl))
                val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(stream.audioOnlyUrl))
                if (subtitleConfigs.isEmpty()) {
                    exoPlayer.setMediaSource(MergingMediaSource(videoSource, audioSource))
                } else {
                    val item = MediaItem.Builder().setUri(stream.videoOnlyUrl).setSubtitleConfigurations(subtitleConfigs).build()
                    val textSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(item)
                    exoPlayer.setMediaSource(MergingMediaSource(videoSource, audioSource, textSource))
                }
            }
            is ResolvedStream.Progressive -> {
                val item = if (subtitleConfigs.isEmpty()) {
                    MediaItem.fromUri(stream.url)
                } else {
                    MediaItem.Builder().setUri(stream.url).setSubtitleConfigurations(subtitleConfigs).build()
                }
                exoPlayer.setMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(item))
            }
            is ResolvedStream.Live -> {
                exoPlayer.setMediaSource(HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(stream.hlsUrl)))
            }
        }

        exoPlayer.prepare()
        exoPlayer.play()
    }

    LaunchedEffect(isPlaying, isAudioMode) { if (!isAudioMode) exoPlayer.playWhenReady = isPlaying }
    LaunchedEffect(isMuted, volume) { exoPlayer.volume = if (isMuted) 0f else volume }
    LaunchedEffect(playbackSpeed) { exoPlayer.setPlaybackSpeed(playbackSpeed) }

    LaunchedEffect(exoPlayer) {
        while (true) {
            if (exoPlayer.isPlaying) progressSec = (exoPlayer.currentPosition / 1000).toInt()
            delay(500)
        }
    }

    var bufferedPercent by remember { mutableStateOf(0) }
    LaunchedEffect(exoPlayer) {
        while (true) {
            bufferedPercent = exoPlayer.bufferedPercentage
            delay(500)
        }
    }

    var exoTotalSeconds by remember { mutableStateOf(totalSeconds) }
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (player.duration > 0 && player.duration != Long.MIN_VALUE) {
                    exoTotalSeconds = (player.duration / 1000).toInt()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val isDecoderError = error.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ||
                    error.errorCode == PlaybackException.ERROR_CODE_DECODING_FAILED ||
                    error.errorCode == PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED

                val stream = resolvedStream
                val qualities = when (stream) {
                    is ResolvedStream.Adaptive -> stream.availableQualities
                    is ResolvedStream.Progressive -> stream.availableQualities
                    else -> emptyList()
                }
                val currentResolution = when (stream) {
                    is ResolvedStream.Adaptive -> stream.resolution
                    is ResolvedStream.Progressive -> stream.resolution
                    else -> null
                }

                if (isDecoderError && !hasAutoRetriedQuality && qualities.size > 1) {
                    // A device's hardware decoder often can't handle every codec/resolution
                    // combination YouTube offers (this is the #1 real-world cause of
                    // ERROR_CODE_DECODER_INIT_FAILED) — retrying one step down in quality picks
                    // a different, usually more widely-supported encode instead of just failing.
                    hasAutoRetriedQuality = true
                    val currentIndex = qualities.indexOfFirst { it.equals(currentResolution, ignoreCase = true) }
                    val fallbackQuality = if (currentIndex in 0 until qualities.size - 1) {
                        qualities[currentIndex + 1]
                    } else {
                        qualities.last()
                    }
                    playbackError = null
                    viewModel.changeQuality(fallbackQuality)
                } else {
                    playbackError = "Playback error: ${error.errorCodeName}"
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // ===== Fullscreen: rotate to landscape + hide system bars =====
    DisposableEffect(isFullscreen) {
        val act = activity
        if (act != null) {
            act.requestedOrientation = if (isFullscreen) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            // Required before the insets controller's hide()/show() calls below actually take
            // effect — without this, the status/nav bars stay visible regardless of hide(),
            // which was exactly the "top status bar doesn't go away in fullscreen" bug.
            WindowCompat.setDecorFitsSystemWindows(act.window, !isFullscreen)
            val controller = WindowCompat.getInsetsController(act.window, act.window.decorView)
            if (isFullscreen) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            if (act != null) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                WindowCompat.setDecorFitsSystemWindows(act.window, true)
                WindowCompat.getInsetsController(act.window, act.window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // ===== Network speed indicator: real transfer-rate estimate + online/offline state =====
    var networkSpeedBps by remember { mutableStateOf(0L) }
    var isOnline by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        while (true) {
            networkSpeedBps = bandwidthMeter.bitrateEstimate
            isOnline = connectivityManager
                ?.getNetworkCapabilities(connectivityManager.activeNetwork)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            delay(1000)
        }
    }
    val isSlowNetwork = isOnline && networkSpeedBps in 1..600_000 // under ~600 kbps while a video is buffering
    val blinkAlpha by rememberInfiniteTransition(label = "offline-blink").animateFloat(
        initialValue = 1f,
        targetValue = if (isOnline) 1f else 0.25f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "offline-blink-alpha"
    )
    val speedLabel = when {
        !isOnline -> "Offline"
        networkSpeedBps >= 1_000_000 -> "%.1f Mbps".format(networkSpeedBps / 1_000_000.0)
        networkSpeedBps > 0 -> "${networkSpeedBps / 1000} Kbps"
        else -> "…"
    }
    val speedColor = when {
        !isOnline -> Color(0xFFEF4444)
        isSlowNetwork -> Color(0xFFF87171)
        else -> Color.White
    }

    // ===== "Convert to audio" background playback, via AudioPlaybackService =====
    var audioService by remember { mutableStateOf<AudioPlaybackService?>(null) }
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                audioService = (binder as? AudioPlaybackService.LocalBinder)?.getService()
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                audioService = null
            }
        }
    }

    fun enterAudioMode() {
        val audioUrl = resolvedStream?.bestAudioUrlOrNull ?: return
        exoPlayer.pause()
        val startIntent = Intent(context, AudioPlaybackService::class.java)
            .putExtra(AudioPlaybackService.EXTRA_AUDIO_URL, audioUrl)
            .putExtra(AudioPlaybackService.EXTRA_TITLE, video.title)
            .putExtra(AudioPlaybackService.EXTRA_ARTIST, video.creator)
        androidx.core.content.ContextCompat.startForegroundService(context, startIntent)
        // Bound only so the UI can observe play/pause state for the waveform animation — actual
        // playback is already started above via the start Intent, not dependent on this binding.
        context.bindService(
            Intent(context, AudioPlaybackService::class.java).setAction(AudioPlaybackService.ACTION_LOCAL_BIND),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        isAudioMode = true
    }

    fun exitAudioMode() {
        audioService?.stopAudio()
        try { context.unbindService(serviceConnection) } catch (e: Exception) { /* not bound */ }
        audioService = null
        isAudioMode = false
        exoPlayer.play()
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isAudioMode) {
                try { context.unbindService(serviceConnection) } catch (e: Exception) { /* not bound */ }
            }
        }
    }

    val progressPercent = if (exoTotalSeconds > 0) progressSec.toFloat() / exoTotalSeconds else 0f
    val qualityLabel = when (val stream = resolvedStream) {
        is ResolvedStream.Adaptive -> stream.resolution.uppercase()
        is ResolvedStream.Progressive -> stream.resolution.uppercase()
        is ResolvedStream.Live -> "LIVE"
        null -> if (isResolving) "Loading…" else "—"
    }
    val availableQualities = when (val stream = resolvedStream) {
        is ResolvedStream.Adaptive -> stream.availableQualities
        is ResolvedStream.Progressive -> stream.availableQualities
        else -> emptyList()
    }
    val availableCaptions = when (val stream = resolvedStream) {
        is ResolvedStream.Adaptive -> stream.captions
        is ResolvedStream.Progressive -> stream.captions
        else -> emptyList()
    }
    val channelUrl = resolvedStream?.uploaderUrlOrNull
    val realLikeCount = resolvedStream?.likeCountOrNull

    var isLiked by remember(video.id) { mutableStateOf(false) }
    var isDisliked by remember(video.id) { mutableStateOf(false) }
    var isSubscribed by remember(video.creator) { mutableStateOf(false) }

    val relatedVideos = remember(video.id, viewModel.videos) {
        viewModel.videos.filter { it.id != video.id }
    }

    fun seekBy(deltaSeconds: Int) {
        val t = (progressSec + deltaSeconds).coerceIn(0, exoTotalSeconds)
        progressSec = t
        exoPlayer.seekTo(t * 1000L)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // ===== VIDEO PLAYER =====
        Box(
            modifier = (if (isFullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth().aspectRatio(16f / 9f))
                .background(Color.Black)
                .pointerInput(isLocked) {
                    if (isLocked) return@pointerInput
                    detectTapGestures(
                        onTap = { showControls = !showControls },
                        onDoubleTap = { offset: Offset ->
                            if (offset.x < size.width / 2f) {
                                seekFlashSide = -1
                                seekBy(-10)
                            } else {
                                seekFlashSide = 1
                                seekBy(10)
                            }
                            seekFlashTrigger++
                        }
                    )
                }
        ) {
            Image(
                painter = rememberAsyncImagePainter(video.imageUrl),
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (resolvedStream != null && !isAudioMode) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = false } }
                )
            }

            // Audio mode: waveform animation instead of a video frame.
            if (isAudioMode) {
                AudioWaveformOverlay(isPlaying = audioService?.getPlayerOrNull()?.isPlaying == true)
            }

            if (isResolving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center).size(48.dp))
            }

            val errorText = resolveError ?: playbackError
            if (errorText != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(14.dp)
                ) {
                    Text(errorText, color = Color.White, fontSize = 13.sp)
                }
            }

            // Double-tap seek flash feedback
            LaunchedEffect(seekFlashTrigger) {
                if (seekFlashTrigger > 0) {
                    delay(500)
                    seekFlashSide = 0
                }
            }
            if (seekFlashSide != 0) {
                Box(
                    modifier = Modifier
                        .align(if (seekFlashSide < 0) Alignment.CenterStart else Alignment.CenterEnd)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (seekFlashSide < 0) Icons.Default.FastRewind else Icons.Default.FastForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text("10s", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            // Top bar: back button, quality badge, network speed badge, lock/settings/fullscreen
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (isFullscreen) isFullscreen = false else viewModel.stopVideo() },
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Close", tint = Color.White)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Live network speed badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .then(if (!isOnline) Modifier.background(Color.Transparent) else Modifier)
                    ) {
                        Text(
                            speedLabel,
                            color = speedColor.copy(alpha = if (!isOnline) blinkAlpha else 1f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isLocked) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(qualityLabel, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { showSettingsSheet = true },
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f))
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    IconButton(
                        onClick = { isLocked = !isLocked },
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Lock controls",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Minimal on-video controls, toggled by tapping the video area (hidden when locked).
            if (showControls && !isLocked && !isAudioMode) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)))

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { seekBy(-10) }) {
                        Icon(Icons.Default.Replay10, contentDescription = "Rewind 10s", tint = Color.White, modifier = Modifier.size(30.dp))
                    }

                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f))
                            .clickable { isPlaying = !isPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause", tint = Color.White, modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(onClick = { seekBy(10) }) {
                        Icon(Icons.Default.Forward10, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Slider(
                        value = progressPercent,
                        onValueChange = { percent -> val t = (percent * exoTotalSeconds).toInt(); progressSec = t; exoPlayer.seekTo(t * 1000L) },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF0000), activeTrackColor = Color(0xFFFF0000),
                            inactiveTrackColor = Color.White.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth().height(20.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatTime(progressSec), color = Color.White, fontSize = 11.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            IconButton(onClick = { isMuted = !isMuted }, modifier = Modifier.size(22.dp)) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = "Mute", tint = Color.White, modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = { isFullscreen = !isFullscreen }, modifier = Modifier.size(22.dp)) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen", tint = Color.White, modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(formatTime(exoTotalSeconds), color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }

            if (isLocked) {
                IconButton(
                    onClick = { isLocked = false },
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp).size(36.dp)
                        .clip(CircleShape).background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = "Unlock", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        // ===== SCROLLABLE INFO + RELATED VIDEOS (hidden while fullscreen) =====
        if (!isFullscreen) {
            LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = video.title, color = Color(0xFF0F0F0F), fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(video.views, color = Color(0xFF606060), fontSize = 13.sp)
                            Text("•", color = Color(0xFF606060), fontSize = 13.sp)
                            Text("Buffered $bufferedPercent%", color = Color(0xFF606060), fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().clickable(
                                enabled = channelUrl != null
                            ) {
                                channelUrl?.let { url -> viewModel.openChannel(url) }
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFD9D9D9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(video.creator.take(1).uppercase(), color = Color(0xFF606060), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                video.creator, color = Color(0xFF0F0F0F), fontSize = 14.sp,
                                fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { isSubscribed = !isSubscribed },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSubscribed) Color(0xFFE5E5E5) else Color(0xFF0F0F0F),
                                    contentColor = if (isSubscribed) Color(0xFF0F0F0F) else Color.White
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(if (isSubscribed) "Subscribed" else "Subscribe", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PillActionButton(
                                icon = Icons.Default.ThumbUp,
                                label = if (realLikeCount != null) formatCompactCount(realLikeCount) else "Like",
                                active = isLiked,
                                onClick = { isLiked = !isLiked; if (isLiked) isDisliked = false }
                            )
                            PillActionButton(Icons.Default.ThumbDown, "Dislike", isDisliked) { isDisliked = !isDisliked; if (isDisliked) isLiked = false }
                            PillActionButton(Icons.Default.Share, "Share", false) {}
                            // Background-audio-playback pill (real YouTube calls this "Background")
                            PillActionButton(
                                icon = if (isAudioMode) Icons.Default.GraphicEq else Icons.Default.Headphones,
                                label = "Background",
                                active = isAudioMode,
                                onClick = { if (isAudioMode) exitAudioMode() else enterAudioMode() }
                            )
                        }

                        if (isAudioMode) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Playing as audio — keeps playing in the background / with the screen off.",
                                color = Color(0xFF606060), fontSize = 12.sp
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE5E5E5), thickness = 8.dp)
                    Text(
                        "Up next", color = Color(0xFF0F0F0F), fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(16.dp)
                    )
                }

                items(relatedVideos) { relatedVideo ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        VideoCloudCard(video = relatedVideo, onDownloadClick = {}, onPlayClick = { viewModel.playVideo(it) })
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    // ===== Settings bottom sheet: Quality / Playback speed / Captions / Lock =====
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            scrimColor = Color.Transparent
        ) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    "Quality", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF606060), modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                if (availableQualities.isEmpty()) {
                    Text(
                        "No manual quality options for this video.", fontSize = 13.sp,
                        color = Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    availableQualities.forEach { q ->
                        SettingsRow(
                            label = q.uppercase(),
                            selected = q.uppercase() == qualityLabel,
                            onClick = { viewModel.changeQuality(q); showSettingsSheet = false }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "Playback speed", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF606060), modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                    SettingsRow(
                        label = if (speed == 1f) "Normal" else "${speed}x",
                        selected = playbackSpeed == speed,
                        onClick = { playbackSpeed = speed; showSettingsSheet = false }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "Captions", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF606060), modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                if (availableCaptions.isEmpty()) {
                    Text(
                        "No captions available for this video.", fontSize = 13.sp,
                        color = Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { captionsEnabled = !captionsEnabled }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(availableCaptions.first().languageName, fontSize = 14.sp)
                        Switch(checked = captionsEnabled, onCheckedChange = { captionsEnabled = it })
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isLocked = true; showSettingsSheet = false }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Lock screen", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
    }
}

/** Simple animated bar-waveform shown over the video area while in "Convert to audio" mode. */
@Composable
private fun AudioWaveformOverlay(isPlaying: Boolean) {
    val transition = rememberInfiniteTransition(label = "waveform")
    val bars = 5
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF111827)), contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(bars) { index ->
                val heightFraction by transition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = if (isPlaying) 1f else 0.25f,
                    animationSpec = infiniteRepeatable(
                        tween(durationMillis = 400 + index * 120, easing = LinearEasing),
                        RepeatMode.Reverse
                    ),
                    label = "bar-$index"
                )
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height((60 * heightFraction).dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF38BDF8))
                )
            }
        }
    }
}

@Composable
private fun PillActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) Color(0xFFE5E5E5) else Color(0xFFF2F2F2))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color(0xFF0F0F0F), modifier = Modifier.size(18.dp))
        Text(label, color = Color(0xFF0F0F0F), fontSize = 13.sp)
    }
}

private fun formatCompactCount(count: Long): String = when {
    count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0)
    count >= 1_000 -> "%.1fK".format(count / 1_000.0)
    else -> count.toString()
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
