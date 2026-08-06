package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AnimatedBottomNavBar
import com.example.ui.components.FullMusicPlayerSheet
import com.example.ui.components.InteractiveVideoPlayer
import com.example.ui.components.IosBouncingSearchBar
import com.example.ui.components.MiniPlayerBar
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.MusicScreen
import com.example.ui.screens.YoutubeFeedScreen
import com.example.ui.theme.OrbitMediaTheme
import com.example.ui.viewmodel.MediaViewModel
import com.example.ui.viewmodel.NavTab

class MainActivity : ComponentActivity() {

    private val viewModel: MediaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OrbitMediaTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Animated Bottom Nav Bar
                        AnimatedBottomNavBar(
                            currentTab = state.currentTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top App Header & iOS Bouncing Search Bar
                            TopHeaderBar(
                                searchQuery = state.searchQuery,
                                isSearchExpanded = state.isSearchExpanded,
                                onQueryChange = { viewModel.updateSearchQuery(it) },
                                onSearchExpandedChange = { viewModel.setSearchExpanded(it) },
                                onSearchSubmit = { query ->
                                    if (query.isNotEmpty()) {
                                        viewModel.navigateToUrl("https://google.com/search?q=$query")
                                        viewModel.selectTab(NavTab.BROWSER)
                                    }
                                }
                            )

                            // Main Content with Spring Slide/Fade Transitions between tabs
                            AnimatedContent(
                                targetState = state.currentTab,
                                transitionSpec = {
                                    (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                                            slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) { it / 6 })
                                        .togetherWith(
                                            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                                                    slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) { -it / 6 }
                                        )
                                },
                                label = "tab_transition",
                                modifier = Modifier.weight(1f)
                            ) { tab ->
                                when (tab) {
                                    NavTab.YOUTUBE -> YoutubeFeedScreen(
                                        selectedCategory = state.selectedCategory,
                                        onCategorySelect = { viewModel.selectCategory(it) },
                                        onVideoClick = { video -> viewModel.openVideo(video) }
                                    )

                                    NavTab.MUSIC -> MusicScreen(
                                        activeTrack = state.selectedTrack,
                                        isPlaying = state.isMusicPlaying,
                                        onTrackSelect = { track -> viewModel.playTrack(track, openFullscreen = true) }
                                    )

                                    NavTab.BROWSER -> BrowserScreen(
                                        currentUrl = state.currentUrl,
                                        inputUrl = state.browserInput,
                                        browserTitle = state.browserTitle,
                                        onInputChange = { viewModel.updateBrowserInput(it) },
                                        onNavigate = { viewModel.navigateToUrl(it) }
                                    )
                                }
                            }
                        }

                        // Floating Picture-in-Picture YouTube Floating Video Window (Exact match to screenshot)
                        AnimatedVisibility(
                            visible = state.isVideoMiniPlayer && state.selectedVideo != null,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        ) {
                            state.selectedVideo?.let { minVideo ->
                                MiniPlayerBar(
                                    video = minVideo,
                                    isPlaying = state.isVideoPlaying,
                                    progress = state.videoProgress,
                                    onExpand = { viewModel.expandVideoFromMiniPlayer() },
                                    onPlayToggle = { viewModel.toggleVideoPlay() },
                                    onClose = { viewModel.dismissVideoPlayer() }
                                )
                            }
                        }

                        // Fullscreen Interactive Video Player Overlay
                        AnimatedVisibility(
                            visible = state.isVideoFullscreen && state.selectedVideo != null,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut()
                        ) {
                            state.selectedVideo?.let { fullVideo ->
                                InteractiveVideoPlayer(
                                    video = fullVideo,
                                    isPlaying = state.isVideoPlaying,
                                    progress = state.videoProgress,
                                    isLiked = state.isVideoLiked,
                                    onPlayToggle = { viewModel.toggleVideoPlay() },
                                    onProgressChange = { viewModel.updateVideoProgress(it) },
                                    onMinimize = { viewModel.minimizeVideoToMiniPlayer() },
                                    onClose = { viewModel.dismissVideoPlayer() },
                                    onLikeToggle = { viewModel.toggleVideoLike() },
                                    onVideoSelect = { viewModel.openVideo(it) }
                                )
                            }
                        }

                        // Fullscreen Music Player Sheet Overlay
                        AnimatedVisibility(
                            visible = state.isMusicFullscreen && state.selectedTrack != null,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut()
                        ) {
                            state.selectedTrack?.let { fullTrack ->
                                FullMusicPlayerSheet(
                                    track = fullTrack,
                                    isPlaying = state.isMusicPlaying,
                                    progress = state.musicProgress,
                                    onPlayToggle = { viewModel.toggleMusicPlay() },
                                    onProgressChange = { viewModel.updateMusicProgress(it) },
                                    onNext = { viewModel.playNextTrack() },
                                    onPrevious = { viewModel.playPreviousTrack() },
                                    onDismiss = { viewModel.setMusicFullscreen(false) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopHeaderBar(
    searchQuery: String,
    isSearchExpanded: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchExpandedChange: (Boolean) -> Unit,
    onSearchSubmit: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isSearchExpanded) {
                // App Brand Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Orbit Media",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            // Compact / Bouncing Search Bar
            IosBouncingSearchBar(
                query = searchQuery,
                onQueryChange = onQueryChange,
                isExpanded = isSearchExpanded,
                onExpandedChange = onSearchExpandedChange,
                onSearchSubmit = onSearchSubmit,
                modifier = Modifier.testTag("top_ios_search_bar")
            )
        }
    }
}
