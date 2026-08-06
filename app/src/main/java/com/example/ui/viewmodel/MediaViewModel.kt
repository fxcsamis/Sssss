package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.SampleData
import com.example.model.MusicTrack
import com.example.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NavTab {
    YOUTUBE, MUSIC, BROWSER
}

data class UiState(
    val currentTab: NavTab = NavTab.YOUTUBE,
    // Search bar state
    val searchQuery: String = "",
    val isSearchExpanded: Boolean = false,
    val selectedCategory: String = "All",
    
    // YouTube Video State
    val selectedVideo: VideoItem? = null,
    val isVideoFullscreen: Boolean = false,
    val isVideoMiniPlayer: Boolean = false,
    val isVideoPlaying: Boolean = false,
    val videoProgress: Float = 0.25f,
    val isVideoLiked: Boolean = false,
    
    // Music Player State
    val selectedTrack: MusicTrack? = SampleData.sampleTracks[0],
    val isMusicFullscreen: Boolean = false,
    val isMusicPlaying: Boolean = false,
    val musicProgress: Float = 0.35f,
    val currentLyricIndex: Int = 1,
    
    // Browser State
    val currentUrl: String = "https://youtube.com/feed/trending",
    val browserInput: String = "https://youtube.com/feed/trending",
    val browserTitle: String = "Orbit Browser - Trending Media",
    val isWebLoading: Boolean = false
)

class MediaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun selectTab(tab: NavTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSearchExpanded(expanded: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchExpanded = expanded)
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    // Video Actions
    fun openVideo(video: VideoItem) {
        _uiState.value = _uiState.value.copy(
            selectedVideo = video,
            isVideoFullscreen = true,
            isVideoMiniPlayer = false,
            isVideoPlaying = true,
            videoProgress = 0f
        )
    }

    fun toggleVideoPlay() {
        val current = _uiState.value.isVideoPlaying
        _uiState.value = _uiState.value.copy(isVideoPlaying = !current)
    }

    fun updateVideoProgress(progress: Float) {
        _uiState.value = _uiState.value.copy(videoProgress = progress.coerceIn(0f, 1f))
    }

    fun minimizeVideoToMiniPlayer() {
        _uiState.value = _uiState.value.copy(
            isVideoFullscreen = false,
            isVideoMiniPlayer = true
        )
    }

    fun expandVideoFromMiniPlayer() {
        _uiState.value = _uiState.value.copy(
            isVideoFullscreen = true,
            isVideoMiniPlayer = false
        )
    }

    fun dismissVideoPlayer() {
        _uiState.value = _uiState.value.copy(
            selectedVideo = null,
            isVideoFullscreen = false,
            isVideoMiniPlayer = false,
            isVideoPlaying = false
        )
    }

    fun toggleVideoLike() {
        _uiState.value = _uiState.value.copy(isVideoLiked = !_uiState.value.isVideoLiked)
    }

    // Music Actions
    fun playTrack(track: MusicTrack, openFullscreen: Boolean = true) {
        _uiState.value = _uiState.value.copy(
            selectedTrack = track,
            isMusicPlaying = true,
            isMusicFullscreen = openFullscreen,
            musicProgress = 0f
        )
    }

    fun toggleMusicPlay() {
        _uiState.value = _uiState.value.copy(isMusicPlaying = !_uiState.value.isMusicPlaying)
    }

    fun setMusicFullscreen(fullscreen: Boolean) {
        _uiState.value = _uiState.value.copy(isMusicFullscreen = fullscreen)
    }

    fun updateMusicProgress(progress: Float) {
        _uiState.value = _uiState.value.copy(musicProgress = progress.coerceIn(0f, 1f))
    }

    fun playNextTrack() {
        val tracks = SampleData.sampleTracks
        val currentId = _uiState.value.selectedTrack?.id
        val currentIndex = tracks.indexOfFirst { it.id == currentId }
        val nextIndex = if (currentIndex != -1) (currentIndex + 1) % tracks.size else 0
        playTrack(tracks[nextIndex], openFullscreen = _uiState.value.isMusicFullscreen)
    }

    fun playPreviousTrack() {
        val tracks = SampleData.sampleTracks
        val currentId = _uiState.value.selectedTrack?.id
        val currentIndex = tracks.indexOfFirst { it.id == currentId }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else tracks.size - 1
        playTrack(tracks[prevIndex], openFullscreen = _uiState.value.isMusicFullscreen)
    }

    // Browser Actions
    fun navigateToUrl(url: String) {
        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else url
        
        _uiState.value = _uiState.value.copy(
            currentUrl = formattedUrl,
            browserInput = formattedUrl,
            browserTitle = formattedUrl.replace("https://", "").replace("http://", "").take(25) + "..."
        )
    }

    fun updateBrowserInput(input: String) {
        _uiState.value = _uiState.value.copy(browserInput = input)
    }
}
