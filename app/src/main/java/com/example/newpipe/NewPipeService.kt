package com.example.newpipe

import com.example.ui.CloudVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.kiosk.KioskInfo
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import java.util.concurrent.TimeUnit
import java.util.Locale

/**
 * One page of video results plus a cursor for loading the next page (null once there's no more),
 * used to back infinite-scroll on the Home feed and search results — the same paging model
 * NewPipeExtractor itself uses.
 */
data class VideoPage(val videos: List<CloudVideo>, val nextPage: Page?)

/**
 * A resolved, directly-playable stream for ExoPlayer.
 *
 * YouTube's "progressive" streams (video+audio already combined in one file) are simple to play
 * but capped at a fairly low resolution (usually 720p or less). Higher resolutions are only
 * available as separate video-only and audio-only streams that need to be combined at playback
 * time — this is exactly what the real NewPipe app does via ExoPlayer's MergingMediaSource, so
 * [Adaptive] is preferred here whenever it's available, for genuinely high-quality playback.
 */
sealed class ResolvedStream {
    data class Adaptive(
        val videoOnlyUrl: String,
        val audioOnlyUrl: String,
        val resolution: String,
        val availableQualities: List<String>,
        val captions: List<CaptionTrack>,
        val likeCount: Long,
        val uploaderUrl: String?
    ) : ResolvedStream()

    data class Progressive(
        val url: String,
        val resolution: String,
        val availableQualities: List<String>,
        val captions: List<CaptionTrack>,
        val likeCount: Long,
        val uploaderUrl: String?
    ) : ResolvedStream()

    /** A live broadcast — played via its HLS manifest URL instead of a regular video file. */
    data class Live(val hlsUrl: String, val likeCount: Long, val uploaderUrl: String?) : ResolvedStream()

    /** URL to use for the "Convert to audio" background playback feature, if any is available. */
    val bestAudioUrlOrNull: String?
        get() = when (this) {
            is Adaptive -> audioOnlyUrl
            is Progressive -> url // progressive already contains audio; ExoPlayer just won't render video
            is Live -> null // live audio-only extraction isn't supported by this simplified backend
        }

    val likeCountOrNull: Long?
        get() = when (this) {
            is Adaptive -> likeCount.takeIf { it >= 0 }
            is Progressive -> likeCount.takeIf { it >= 0 }
            is Live -> likeCount.takeIf { it >= 0 }
        }

    val uploaderUrlOrNull: String?
        get() = when (this) {
            is Adaptive -> uploaderUrl
            is Progressive -> uploaderUrl
            is Live -> uploaderUrl
        }
}

/** A single available subtitle/caption track for a resolved video. */
data class CaptionTrack(val url: String, val mimeType: String, val languageName: String)

/**
 * Real YouTube data + playback backend, replacing the mock [CloudVideo] list and fake player.
 *
 * This wraps NewPipeExtractor the same way NewPipe itself does: no API key, no login — it scrapes
 * public YouTube pages client-side, same as opening them in a browser. That also means it can
 * break whenever YouTube changes its page structure; NewPipeExtractor releases fixes for that
 * over time, so keeping the extractor dependency (see libs.versions.toml) up to date matters.
 */
object NewPipeService {

    private val youtube get() = ServiceList.YouTube

    @Volatile
    private var initialized = false

    /** Must be called once (e.g. from Application.onCreate) before any other function here. */
    fun init() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            NewPipe.init(NewPipeDownloader.getInstance())
            initialized = true
        }
    }

    // Kiosk (trending) URL is stable per app session — cached so loadMoreTrending() can reuse it
    // without re-deriving it from the kiosk list every time.
    private var trendingUrl: String? = null

    /** Home feed: YouTube's default "Trending" kiosk (first page), mapped into [CloudVideo]. */
    suspend fun getTrending(): VideoPage = withContext(Dispatchers.IO) {
        val kioskList = youtube.kioskList
        val kioskId = kioskList.defaultKioskId
        val url = kioskList.getListLinkHandlerFactoryByType(kioskId).fromId(kioskId).url
        trendingUrl = url
        val kioskInfo = KioskInfo.getInfo(youtube, url)
        VideoPage(
            videos = kioskInfo.relatedItems.filterIsInstance<StreamInfoItem>().map { it.toCloudVideo() },
            nextPage = kioskInfo.nextPage
        )
    }

    /** Next page of the trending feed, for infinite scroll. Returns null once there's no more. */
    suspend fun loadMoreTrending(nextPage: Page): VideoPage = withContext(Dispatchers.IO) {
        val url = trendingUrl ?: youtube.kioskList.let { kl ->
            val id = kl.defaultKioskId
            kl.getListLinkHandlerFactoryByType(id).fromId(id).url.also { trendingUrl = it }
        }
        val page = KioskInfo.getMoreItems(youtube, url, nextPage)
        VideoPage(
            videos = page.items.filterIsInstance<StreamInfoItem>().map { it.toCloudVideo() },
            nextPage = page.nextPage
        )
    }

    /** Real YouTube search (first page), used to back the app's search bar instead of only filtering locally. */
    suspend fun search(query: String): VideoPage = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext VideoPage(emptyList(), null)
        val queryHandler = youtube.searchQHFactory.fromQuery(query, emptyList(), "")
        val searchInfo = SearchInfo.getInfo(youtube, queryHandler)
        VideoPage(
            videos = searchInfo.relatedItems.filterIsInstance<StreamInfoItem>().map { it.toCloudVideo() },
            nextPage = searchInfo.nextPage
        )
    }

    /** Next page of search results, for infinite scroll. */
    suspend fun loadMoreSearch(query: String, nextPage: Page): VideoPage = withContext(Dispatchers.IO) {
        val queryHandler = youtube.searchQHFactory.fromQuery(query, emptyList(), "")
        val page = SearchInfo.getMoreItems(youtube, queryHandler, nextPage)
        VideoPage(
            videos = page.items.filterIsInstance<StreamInfoItem>().map { it.toCloudVideo() },
            nextPage = page.nextPage
        )
    }

    /**
     * Resolves a YouTube watch-page URL (as stored in [CloudVideo.fileUrl]) to the
     * highest-quality directly-playable stream available.
     *
     * Prefers combining the best video-only stream with the best audio-only stream (this is how
     * YouTube serves anything above ~720p); falls back to a progressive (already-muxed) stream
     * only if no adaptive streams are available for this video.
     */
    suspend fun resolvePlayableStream(
        watchUrl: String,
        preferredResolution: String? = null,
        minResolutionHeight: Int = 0
    ): ResolvedStream? = withContext(Dispatchers.IO) {
        val info = StreamInfo.getInfo(youtube, watchUrl)
        val likeCount = info.likeCount
        val uploaderUrl = info.uploaderUrl

        // Live broadcasts (e.g. a "LIVE" video in trending) have no regular video file to
        // download — they're only playable via a continuously-updating HLS manifest. Trying to
        // resolve them as normal video-only/audio-only/progressive streams (as below) finds
        // nothing playable, which is exactly the "stuck at 00:00, never plays" bug this fixes.
        val isLive = info.streamType == StreamType.LIVE_STREAM || info.streamType == StreamType.AUDIO_LIVE_STREAM
        if (isLive) {
            val hlsUrl = info.hlsUrl
            if (!hlsUrl.isNullOrBlank()) {
                return@withContext ResolvedStream.Live(hlsUrl, likeCount, uploaderUrl)
            }
            // No HLS manifest available for this live stream — nothing we can play.
            return@withContext null
        }

        val captions = info.subtitles.orEmpty()
            .filter { it.content != null && it.format != null }
            .map { CaptionTrack(url = it.content, mimeType = it.format!!.mimeType, languageName = it.displayLanguageName) }

        val videoOnlyByResolution = info.videoOnlyStreams.filter { it.content != null }
        val progressiveByResolution = info.videoStreams.filter { !it.isVideoOnly && it.content != null }
        val availableQualities = (videoOnlyByResolution + progressiveByResolution)
            .mapNotNull { it.getResolution() }
            .distinct()
            .sortedByDescending { parseResolutionHeight(it) }

        val bestAudio = info.audioStreams
            .filter { it.content != null }
            .maxByOrNull { it.averageBitrate }

        // Manual quality pick (from the settings sheet) takes priority; otherwise auto-pick the
        // highest resolution available, preferring MPEG-4/AVC (H.264) over WebM/VP9 or AV1 at
        // the same resolution — AVC has near-universal hardware decoder support, while VP9/AV1
        // decoder availability varies a lot by device and is the most common cause of a
        // ERROR_CODE_DECODER_INIT_FAILED crash on budget/older phones.
        fun pickBest(candidates: List<org.schabi.newpipe.extractor.stream.VideoStream>) =
            candidates
                .filter { minResolutionHeight <= 0 || parseResolutionHeight(it.getResolution()) >= minResolutionHeight }
                .ifEmpty { candidates } // nothing meets the floor — better to play something than nothing
                .sortedWith(
                    compareByDescending<org.schabi.newpipe.extractor.stream.VideoStream> { parseResolutionHeight(it.getResolution()) }
                        .thenByDescending { it.format == MediaFormat.MPEG_4 }
                ).firstOrNull()

        val chosenVideoOnly = if (preferredResolution != null) {
            videoOnlyByResolution
                .filter { it.getResolution() == preferredResolution }
                .sortedByDescending { it.format == MediaFormat.MPEG_4 }
                .firstOrNull()
        } else {
            pickBest(videoOnlyByResolution)
        }

        if (chosenVideoOnly != null && bestAudio != null) {
            return@withContext ResolvedStream.Adaptive(
                videoOnlyUrl = chosenVideoOnly.content,
                audioOnlyUrl = bestAudio.content,
                resolution = chosenVideoOnly.getResolution() ?: "unknown",
                availableQualities = availableQualities,
                captions = captions,
                likeCount = likeCount,
                uploaderUrl = uploaderUrl
            )
        }

        // Fallback: a progressive (video+audio already combined) stream — lower quality, but
        // guarantees something plays even for videos with no separate adaptive streams.
        val chosenProgressive = if (preferredResolution != null) {
            progressiveByResolution
                .filter { it.getResolution() == preferredResolution }
                .sortedByDescending { it.format == MediaFormat.MPEG_4 }
                .firstOrNull()
        } else {
            pickBest(progressiveByResolution)
        }

        chosenProgressive?.let {
            ResolvedStream.Progressive(
                url = it.content,
                resolution = it.getResolution() ?: "unknown",
                availableQualities = availableQualities,
                captions = captions,
                likeCount = likeCount,
                uploaderUrl = uploaderUrl
            )
        }
    }

    /** Parses strings like "1080p60" or "720p" into a comparable height ("1080", "720"). */
    private fun parseResolutionHeight(resolution: String?): Int =
        resolution?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0

    private fun StreamInfoItem.toCloudVideo(): CloudVideo = CloudVideo(
        id = this.url,
        title = this.name,
        duration = formatDuration(this.duration),
        creator = this.uploaderName.orEmpty(),
        imageUrl = this.thumbnails.maxByOrNull { it.height }?.url.orEmpty(),
        views = formatViewCount(this.viewCount),
        fileUrl = this.url, // YouTube watch-page URL; resolved to a real stream URL on play.
        sizeMb = 0.0 // Unknown until the stream is resolved; not used for streaming playback.
    )

    private fun formatDuration(totalSeconds: Long): String {
        if (totalSeconds <= 0) return "--:--"
        val hours = TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%d:%02d", minutes, seconds)
        }
    }

    private fun formatViewCount(viewCount: Long): String {
        if (viewCount < 0) return ""
        return when {
            viewCount >= 1_000_000_000 -> String.format(Locale.US, "%.1fB views", viewCount / 1_000_000_000.0)
            viewCount >= 1_000_000 -> String.format(Locale.US, "%.1fM views", viewCount / 1_000_000.0)
            viewCount >= 1_000 -> String.format(Locale.US, "%.1fK views", viewCount / 1_000.0)
            else -> "$viewCount views"
        }
    }

    // ===== Channel view =====

    /** Real channel details: name, avatar, subscriber count, description, plus the Videos tab. */
    suspend fun getChannel(channelUrl: String): ChannelDetails = withContext(Dispatchers.IO) {
        val info = ChannelInfo.getInfo(youtube, channelUrl)
        val videosTab = info.tabs.find { it.contentFilters.firstOrNull() == ChannelTabs.VIDEOS }
        val hasShorts = info.tabs.any { it.contentFilters.firstOrNull() == ChannelTabs.SHORTS }

        val firstVideoPage = videosTab?.let {
            val tabInfo = ChannelTabInfo.getInfo(youtube, it)
            VideoPage(
                videos = tabInfo.relatedItems.filterIsInstance<StreamInfoItem>().map { item -> item.toCloudVideo() },
                nextPage = tabInfo.nextPage
            )
        } ?: VideoPage(emptyList(), null)

        ChannelDetails(
            name = info.name,
            avatarUrl = info.avatars.maxByOrNull { it.height }?.url.orEmpty(),
            subscriberCount = info.subscriberCount,
            description = info.description.orEmpty(),
            videosLinkHandler = videosTab,
            hasShorts = hasShorts,
            firstVideoPage = firstVideoPage
        )
    }

    /** Next page of a channel's Videos tab, for infinite scroll on the channel screen. */
    suspend fun loadMoreChannelVideos(
        linkHandler: org.schabi.newpipe.extractor.linkhandler.ListLinkHandler,
        nextPage: Page
    ): VideoPage = withContext(Dispatchers.IO) {
        val page = ChannelTabInfo.getMoreItems(youtube, linkHandler, nextPage)
        VideoPage(
            videos = page.items.filterIsInstance<StreamInfoItem>().map { it.toCloudVideo() },
            nextPage = page.nextPage
        )
    }

    /** A channel's Shorts tab, if it has one. */
    suspend fun getChannelShorts(channelUrl: String): VideoPage = withContext(Dispatchers.IO) {
        val info = ChannelInfo.getInfo(youtube, channelUrl)
        val shortsTab = info.tabs.find { it.contentFilters.firstOrNull() == ChannelTabs.SHORTS }
            ?: return@withContext VideoPage(emptyList(), null)
        val tabInfo = ChannelTabInfo.getInfo(youtube, shortsTab)
        VideoPage(
            videos = tabInfo.relatedItems.filterIsInstance<StreamInfoItem>().map { it.toCloudVideo() },
            nextPage = tabInfo.nextPage
        )
    }
}

/** Real channel metadata + its first page of uploaded videos, shown on the Channel screen. */
data class ChannelDetails(
    val name: String,
    val avatarUrl: String,
    val subscriberCount: Long,
    val description: String,
    val videosLinkHandler: org.schabi.newpipe.extractor.linkhandler.ListLinkHandler?,
    val hasShorts: Boolean,
    val firstVideoPage: VideoPage
)
