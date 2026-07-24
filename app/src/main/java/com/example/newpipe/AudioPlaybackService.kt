package com.example.newpipe

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Backs the "Convert to audio" feature: when a person taps it, playback switches from the
 * on-screen video ExoPlayer to a separate ExoPlayer instance owned by this foreground service,
 * so audio keeps playing after the app is backgrounded or the screen turns off — the same way a
 * real music/podcast player works, complete with lock-screen/notification playback controls via
 * MediaSession. Without tapping "Convert to audio", the regular video player (owned by the
 * Composable, not this service) is released as soon as the screen closes, so nothing plays in
 * the background — that's the intended, expected difference.
 */
class AudioPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    /** Lets the UI grab this running service instance directly (see [ACTION_LOCAL_BIND]). */
    inner class LocalBinder : Binder() {
        fun getService(): AudioPlaybackService = this@AudioPlaybackService
    }

    private val localBinder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        val renderersFactory = androidx.media3.exoplayer.DefaultRenderersFactory(this)
            .setEnableDecoderFallback(true)
        val exoPlayer = ExoPlayer.Builder(this, renderersFactory).build()
        player = exoPlayer
        mediaSession = MediaSession.Builder(this, exoPlayer).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onBind(intent: Intent?): IBinder? =
        if (intent?.action == ACTION_LOCAL_BIND) localBinder else super.onBind(intent)

    /**
     * Starts playback as soon as the service itself starts, directly from the Intent that
     * launched it — rather than waiting for the UI to bind and call [playAudioUrl] separately.
     * That earlier bind-then-call approach had a real race condition (binding is asynchronous,
     * so playback could be delayed or silently never start if the connection callback was slow),
     * which matched exactly the "converts to audio but nothing actually plays in the background"
     * bug report.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val audioUrl = intent?.getStringExtra(EXTRA_AUDIO_URL)
        if (audioUrl != null) {
            val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
            val artist = intent.getStringExtra(EXTRA_ARTIST).orEmpty()
            playAudioUrl(audioUrl, title, artist)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /** Starts (or replaces) audio-only playback with the given resolved audio stream URL. */
    fun playAudioUrl(audioUrl: String, title: String, artist: String) {
        val exoPlayer = player ?: return
        val mediaItem = MediaItem.Builder()
            .setUri(audioUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .build()
            )
            .build()

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(NewPipeDownloader.USER_AGENT)
            .setAllowCrossProtocolRedirects(true)

        val source = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)

        exoPlayer.setMediaSource(source)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun stopAudio() {
        player?.stop()
        stopSelf()
    }

    fun getPlayerOrNull(): Player? = player

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        player = null
        super.onDestroy()
    }

    /**
     * MediaSessionService's default behavior is to keep running in the background only while
     * media is actively playing — this override makes that explicit: if playback isn't actually
     * happening when the task is swiped away, the service stops cleanly instead of lingering.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val exoPlayer = player
        if (exoPlayer == null || !exoPlayer.playWhenReady || exoPlayer.mediaItemCount == 0) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        const val ACTION_LOCAL_BIND = "com.example.newpipe.ACTION_LOCAL_BIND"
        const val EXTRA_AUDIO_URL = "extra_audio_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
    }
}

