package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import com.example.newpipe.NewPipeService
import com.example.ui.CloudihubViewModel
import com.example.ui.components.CloudSkyBackground
import com.example.ui.components.DownloadsHub
import com.example.ui.components.GlassmorphicNavBar
import com.example.ui.components.NavigationTab
import com.example.ui.components.VoiceSearchDialog
import com.example.ui.components.VideoStreamingPlayer
import com.example.ui.screens.ChannelScreen
import com.example.ui.components.MusicBubblePlayer
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MusicScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SitesScreen
import com.example.ui.screens.HubScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CloudihubViewModel by viewModels()

    // Must be registered unconditionally (not inside an if-block) — see AndroidX docs.
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NewPipeService.init()

        // Without this, the "Background" (Convert to audio) feature's notification — showing the
        // app icon + play/pause controls in the system tray and lock screen — silently never
        // appears on Android 13+, since POST_NOTIFICATIONS became a runtime permission there.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Fullscreen edge-to-edge setup
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Wrap all background and screen content in a blurred Box when voice search is active
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(if (viewModel.showVoiceDialog) 12.dp else 0.dp)
                        ) {
                            // 1. Organic drifting cloud background layers
                            CloudSkyBackground()

                            // 2. Active Screen content router
                            Box(modifier = Modifier.fillMaxSize()) {
                                when (viewModel.activeTab) {
                                    NavigationTab.Profile -> ProfileScreen(viewModel = viewModel)
                                    NavigationTab.Home -> HomeScreen(viewModel = viewModel)
                                    NavigationTab.Music -> MusicScreen(viewModel = viewModel)
                                    NavigationTab.Hub -> HubScreen(viewModel = viewModel)
                                    NavigationTab.Browser -> BrowserScreen(viewModel = viewModel)
                                }
                            }

                            // 3. Floating glassmorphic navigation bar
                            val shouldHideNavBar = viewModel.activeTab == NavigationTab.Browser && 
                                    (viewModel.isBrowserFullscreen || viewModel.browserUrl.isNotEmpty())
                            if (!shouldHideNavBar) {
                                GlassmorphicNavBar(
                                    activeTab = viewModel.activeTab,
                                    onTabSelected = { viewModel.selectTab(it) },
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )
                            }
                        }

                        // 4. Floating speech-recognition voice dialog (Perfectly sharp and clean)
                        VoiceSearchDialog(viewModel = viewModel)

                        // 5. Sliding downloads hub list panel
                        DownloadsHub(viewModel = viewModel)

                        // 6. Full-screen Video Streaming Player
                        if (viewModel.playingVideo != null) {
                            VideoStreamingPlayer(viewModel = viewModel)
                        }

                        // 6b. Channel page (opened via "visit channel" from the video player)
                        if (viewModel.viewingChannel != null || viewModel.isLoadingChannel) {
                            ChannelScreen(viewModel = viewModel)
                        }

                        // 7. Floating Music Bubble Player (Available across pages when playing music)
                        MusicBubblePlayer(
                            viewModel = viewModel,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 85.dp, end = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
