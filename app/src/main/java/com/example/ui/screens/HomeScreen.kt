package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.CloudVideo
import com.example.ui.CloudihubViewModel
import com.example.ui.DownloadStatus
import com.example.ui.components.CloudShape
import com.example.ui.components.CloudSkyBackground
import com.example.ui.components.LottieDownloadIcon
import com.example.ui.components.ProfileAvatarWithBadge
import com.example.ui.screens.home.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CloudihubViewModel,
    modifier: Modifier = Modifier
) {
    val videos = viewModel.videos
    val searchQuery = viewModel.searchQuery
    val activeDownloads by viewModel.downloads.collectAsState()
    val activeCount = remember(activeDownloads) {
        activeDownloads.count { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.QUEUED }
    }

    val categories = remember {
        listOf("All", "Music", "Rainclouds", "Infrastructure", "Sky Timelapse", "Edge Gaming", "Aesthetics")
    }
    var selectedCategory by remember { mutableStateOf("All") }

    val lazyListState = rememberLazyListState()
    val isDark = viewModel.isDarkTheme
    val keyboardController = LocalSoftwareKeyboardController.current

    var isSearchScreenOpen by remember { mutableStateOf(false) }
    var selectedVideoToShare by remember { mutableStateOf<CloudVideo?>(null) }
    var selectedVideoToDownload by remember { mutableStateOf<CloudVideo?>(null) }
    var selectedVideoForMoreOptions by remember { mutableStateOf<CloudVideo?>(null) }

    BackHandler(enabled = isSearchScreenOpen) {
        isSearchScreenOpen = false
    }

    val isEmptyResults = videos.isEmpty() && !viewModel.isLoadingVideos

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
    ) {
        if (isEmptyResults) {
            CloudSkyBackground(modifier = Modifier.fillMaxSize())
        }

        // VIDEO FEED WITH PULL TO REFRESH
        PullToRefreshBox(
            isRefreshing = viewModel.isLoadingVideos,
            onRefresh = { viewModel.loadHybridFeed() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(top = 136.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (viewModel.isLoadingVideos) {
                    items(3) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ShimmerVideoCloudCard()
                        }
                    }
                } else if (videos.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CloudShape())
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Empty Search",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No sky matches found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "Try searching for Cloud, Rain or Space",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = videos,
                        key = { it.id },
                        contentType = { "video_card" }
                    ) { video ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                        ) {
                            val isVideoDownloading = activeDownloads.any {
                                it.videoId == video.id && (it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.QUEUED)
                            }
                            VideoCloudCard(
                                video = video,
                                isWatchLater = viewModel.isWatchLater(video.id),
                                isDownloading = isVideoDownloading,
                                isCurrentlyPlaying = viewModel.playingVideo?.id == video.id,
                                onWatchLaterClick = { viewModel.toggleWatchLater(video) },
                                onDownloadClick = { selectedVideoToDownload = video },
                                onMoreOptionsClick = { selectedVideoForMoreOptions = video },
                                onPlayClick = { viewModel.playVideo(video) }
                            )
                        }
                    }
                }
            }
        }

        // ULTRA-SMOOTH HARDWARE-ACCELERATED FLOATING TOP BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isEmptyResults) Color.Transparent
                    else if (isDark) Color(0xFF0F172A).copy(alpha = 0.96f)
                    else Color(0xFFF8FAFC).copy(alpha = 0.96f)
                )
                .statusBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            // 1. Search Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(if (isDark) Color(0xFF1E293B) else Color.White)
                        .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(23.dp))
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { isSearchScreenOpen = true }
                        )
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text("Search cloud files...", color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8), fontSize = 14.sp)
                        } else {
                            Text(searchQuery, color = if (isDark) Color.White else Color(0xFF0F172A), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.updateSearchQuery("") },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { viewModel.loadHybridFeed() },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("reload_feed_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload Feed",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.showDownloadHub = true },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("download_icon_button")
                    ) {
                        LottieDownloadIcon(
                            isDownloading = activeCount > 0,
                            size = 20.dp
                        )
                    }

                    if (activeCount > 0) {
                        Box(
                            modifier = Modifier
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeCount.toString(),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        isSearchScreenOpen = true
                        viewModel.startVoiceSearch()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("voice_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice search",
                        tint = Color(0xFF0369A1),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                ProfileAvatarWithBadge(
                    viewModel = viewModel,
                    avatarSize = 38.dp,
                    badgeSize = 14.dp,
                    onClick = { viewModel.showEditProfileScreen = true },
                    modifier = Modifier.testTag("profile_avatar_logo")
                )
            }

            // 2. Quick Category List
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fixed "Shorts" & "Live" Buttons on Left
                Row(
                    modifier = Modifier
                        .zIndex(5f)
                        .padding(start = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .shadow(1.dp, CircleShape)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E293B) else Color.White)
                            .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), CircleShape)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(color = Color(0xFFEF4444)),
                                onClick = {
                                    selectedCategory = "Shorts"
                                    viewModel.openShortsScreen()
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ShortsLogoIcon(modifier = Modifier.size(18.dp))
                        Text(
                            text = "Shorts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .shadow(1.dp, CircleShape)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E293B) else Color.White)
                            .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), CircleShape)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(color = Color(0xFFEF4444)),
                                onClick = {
                                    selectedCategory = "Live"
                                    viewModel.updateSearchQuery("Live")
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LiveLogoIcon(modifier = Modifier.size(20.dp))
                        Text(
                            text = "Live",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }
                }

                // Native smooth categories LazyRow
                LazyRow(
                    contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        val isAllCategory = category == "All"
                        val background = if (isSelected) {
                            if (isAllCategory) (if (isDark) Color(0xFF383838) else Color(0xFF27272A))
                            else Color(0xFF0284C7)
                        } else {
                            if (isDark) Color(0xFF1E293B) else Color.White
                        }
                        val border = if (isSelected) Color.Transparent else if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                        val textCol = if (isSelected) Color.White else if (isDark) Color(0xFFCBD5E1) else Color(0xFF0F172A)

                        Box(
                            modifier = Modifier
                                 .shadow(if (isSelected) 0.dp else 1.dp, CircleShape)
                                 .clip(CircleShape)
                                 .background(background)
                                 .border(1.dp, border, CircleShape)
                                 .clickable(
                                     interactionSource = remember(category) { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                     indication = androidx.compose.material3.ripple(color = Color(0xFF0284C7)),
                                     onClick = {
                                         selectedCategory = category
                                         if (category == "All") {
                                             viewModel.updateSearchQuery("")
                                         } else {
                                             viewModel.updateSearchQuery(category)
                                         }
                                     }
                                 )
                                 .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textCol
                            )
                        }
                    }
                }
            }
        }

        // ANIMATED SEARCH SCREEN OVERLAY
        AnimatedVisibility(
            visible = isSearchScreenOpen,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300, easing = EaseOutQuart)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 250, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(250)),
            modifier = Modifier.fillMaxSize().zIndex(100f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            ) {
                CloudSkyBackground(modifier = Modifier.fillMaxSize())

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isSearchScreenOpen = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back",
                                tint = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(if (isDark) Color(0xFF1E293B) else Color.White)
                                .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                textStyle = TextStyle(
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        if (searchQuery.isNotEmpty()) {
                                            viewModel.triggerDoneSearchKeyboardAction(searchQuery)
                                            isSearchScreenOpen = false
                                            keyboardController?.hide()
                                        }
                                    }
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(if (isDark) Color.White else Color(0xFF0284C7)),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    Box(
                                        contentAlignment = Alignment.CenterStart,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search sky and clouds...",
                                                color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                                fontSize = 13.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )

                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.updateSearchQuery("") },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = {
                                isSearchScreenOpen = true
                                viewModel.startVoiceSearch()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice search",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        ProfileAvatarWithBadge(
                            viewModel = viewModel,
                            avatarSize = 32.dp,
                            badgeSize = 13.dp,
                            onClick = {
                                isSearchScreenOpen = false
                                viewModel.showEditProfileScreen = true
                            }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        if (viewModel.recentSearches.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Searches",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                TextButton(
                                    onClick = { viewModel.clearSearchHistory() }
                                ) {
                                    Text("Clear All", color = Color(0xFFEF4444), fontSize = 12.sp)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                viewModel.recentSearches.forEach { searchItem ->
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0).copy(alpha = 0.6f))
                                            .clickable {
                                                viewModel.addSearchQueryToHistory(searchItem)
                                                viewModel.updateSearchQuery(searchItem)
                                                isSearchScreenOpen = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "History",
                                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = searchItem,
                                            fontSize = 13.sp,
                                            color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { viewModel.removeSearchQueryFromHistory(searchItem) },
                                            modifier = Modifier.size(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text(
                            text = "Trending Searches",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val trendingSearches = listOf("Beautiful Aurora", "Nimbus Clouds", "Cosmic Stardust", "Lightning Strike", "Solar Eclipse")
                        trendingSearches.forEachIndexed { idx, trend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xFF1E293B).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.5f))
                                    .clickable {
                                        viewModel.addSearchQueryToHistory(trend)
                                        viewModel.updateSearchQuery(trend)
                                        isSearchScreenOpen = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (idx) {
                                                0 -> Color(0xFFEF4444)
                                                1 -> Color(0xFFF97316)
                                                2 -> Color(0xFFF59E0B)
                                                else -> if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (idx + 1).toString(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = trend,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B),
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "Trending",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // DIALOGS & BOTTOM SHEETS
        selectedVideoToShare?.let { videoToShare ->
            ShareVideoBottomSheet(
                video = videoToShare,
                onDismiss = { selectedVideoToShare = null }
            )
        }

        selectedVideoToDownload?.let { videoToDownload ->
            DownloadVideoBottomSheet(
                video = videoToDownload,
                viewModel = viewModel,
                onDismiss = { selectedVideoToDownload = null }
            )
        }

        selectedVideoForMoreOptions?.let { videoForMore ->
            VideoMoreOptionsSheet(
                video = videoForMore,
                isWatchLater = viewModel.isWatchLater(videoForMore.id),
                onDismiss = { selectedVideoForMoreOptions = null },
                onPlayClick = { viewModel.playVideo(videoForMore) },
                onShareClick = { selectedVideoToShare = videoForMore },
                onWatchLaterClick = { viewModel.toggleWatchLater(videoForMore) },
                onDownloadClick = { selectedVideoToDownload = videoForMore },
                onNotInterestedClick = {
                    Toast.makeText(viewModel.getApplication(), "Marked as Not Interested", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (viewModel.showCompactProfileDialog) {
            CompactProfilePopupDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showCompactProfileDialog = false }
            )
        }
    }
}
