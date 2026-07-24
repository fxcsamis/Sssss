package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import coil.compose.SubcomposeAsyncImage
import com.example.ui.CloudVideo
import com.example.ui.CloudihubViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

data class SocialPlatform(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val brandColor: Color,
    val accentColor: Color,
    val supportedFormats: String,
    val logoUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubScreen(
    viewModel: CloudihubViewModel,
    modifier: Modifier = Modifier
) {
    var selectedPlatform by remember { mutableStateOf<SocialPlatform?>(null) }
    val scope = rememberCoroutineScope()

    val platforms = listOf(
        SocialPlatform(
            name = "Pinterest",
            description = "Visual layout image & video saver",
            icon = Icons.Default.Bookmark,
            brandColor = Color(0xFFDC2626),
            accentColor = Color(0xFFFECACA),
            supportedFormats = "MP4, PNG",
            logoUrl = "https://logo.clearbit.com/pinterest.com"
        ),
        SocialPlatform(
            name = "Facebook",
            description = "FHD social stream grabber",
            icon = Icons.Default.ThumbUp,
            brandColor = Color(0xFF3B82F6),
            accentColor = Color(0xFF93C5FD),
            supportedFormats = "MP4, MP3",
            logoUrl = "https://logo.clearbit.com/facebook.com"
        ),
        SocialPlatform(
            name = "Instagram",
            description = "Reels & stories media fetcher",
            icon = Icons.Default.CameraAlt,
            brandColor = Color(0xFFEC4899),
            accentColor = Color(0xFFFBCFE8),
            supportedFormats = "MP4, JPG",
            logoUrl = "https://logo.clearbit.com/instagram.com"
        ),
        SocialPlatform(
            name = "Twitter / X",
            description = "Fast thread video & gif archiver",
            icon = Icons.Default.DeviceHub,
            brandColor = Color(0xFF0F172A),
            accentColor = Color(0xFF94A3B8),
            supportedFormats = "MP4, GIF",
            logoUrl = "https://logo.clearbit.com/x.com"
        ),
        SocialPlatform(
            name = "SoundCloud",
            description = "High-fidelity cloud music grabber",
            icon = Icons.Default.MusicNote,
            brandColor = Color(0xFFFF5500),
            accentColor = Color(0xFFFFCCB3),
            supportedFormats = "MP3, WAV",
            logoUrl = "https://logo.clearbit.com/soundcloud.com"
        ),
        SocialPlatform(
            name = "Streamable",
            description = "Fast direct hosting stream archiver",
            icon = Icons.Default.VideoLibrary,
            brandColor = Color(0xFF00D1B2),
            accentColor = Color(0xFFB3F5EC),
            supportedFormats = "MP4",
            logoUrl = "https://logo.clearbit.com/streamable.com"
        ),
        SocialPlatform(
            name = "Dailymotion",
            description = "Universal web player video saver",
            icon = Icons.Default.PlayArrow,
            brandColor = Color(0xFF0066DC),
            accentColor = Color(0xFFB3D1F9),
            supportedFormats = "MP4, MKV",
            logoUrl = "https://logo.clearbit.com/dailymotion.com"
        ),
        SocialPlatform(
            name = "Bluesky",
            description = "Decentralized post media downloader",
            icon = Icons.Default.Cloud,
            brandColor = Color(0xFF0560FC),
            accentColor = Color(0xFFB3CFFF),
            supportedFormats = "MP4, PNG",
            logoUrl = "https://logo.clearbit.com/bsky.app"
        ),
        SocialPlatform(
            name = "OK.ru",
            description = "Social network video loop downloader",
            icon = Icons.Default.People,
            brandColor = Color(0xFFF58220),
            accentColor = Color(0xFFFFD9B3),
            supportedFormats = "MP4, MP3",
            logoUrl = "https://logo.clearbit.com/ok.ru"
        ),
        SocialPlatform(
            name = "Rutube",
            description = "High definition stream saver",
            icon = Icons.Default.Tv,
            brandColor = Color(0xFFE11D48),
            accentColor = Color(0xFFFECDD3),
            supportedFormats = "MP4, MKV",
            logoUrl = "https://logo.clearbit.com/rutube.ru"
        ),
        SocialPlatform(
            name = "Snapchat",
            description = "Stories & spotlight media fetcher",
            icon = Icons.Default.ChatBubble,
            brandColor = Color(0xFFCA8A04),
            accentColor = Color(0xFFFEF08A),
            supportedFormats = "MP4, JPG",
            logoUrl = "https://logo.clearbit.com/snapchat.com"
        ),
        SocialPlatform(
            name = "Tumblr",
            description = "Blog media post archive utility",
            icon = Icons.Default.FormatQuote,
            brandColor = Color(0xFF35465C),
            accentColor = Color(0xFFCBD5E1),
            supportedFormats = "MP4, GIF, PNG",
            logoUrl = "https://logo.clearbit.com/tumblr.com"
        ),
        SocialPlatform(
            name = "VK",
            description = "Fast social player extractor",
            icon = Icons.Default.Share,
            brandColor = Color(0xFF4C75A3),
            accentColor = Color(0xFFD3DFEE),
            supportedFormats = "MP4, MP3",
            logoUrl = "https://logo.clearbit.com/vk.com"
        ),
        SocialPlatform(
            name = "TikTok",
            description = "Clean sound & video loop saver",
            icon = Icons.Default.Audiotrack,
            brandColor = Color(0xFF111827),
            accentColor = Color(0xFFF43F5E),
            supportedFormats = "MP4 (No Watermark)",
            logoUrl = "https://logo.clearbit.com/tiktok.com"
        ),
        SocialPlatform(
            name = "YouTube",
            description = "High-speed UHD video extractor",
            icon = Icons.Default.PlayArrow,
            brandColor = Color(0xFFEF4444),
            accentColor = Color(0xFFFCA5A5),
            supportedFormats = "MP4, MKV, MP3",
            logoUrl = "https://logo.clearbit.com/youtube.com"
        ),
        SocialPlatform(
            name = "Twitch clip",
            description = "Gaming highlight & clip clipper",
            icon = Icons.Default.Videocam,
            brandColor = Color(0xFF9146FF),
            accentColor = Color(0xFFE5D5FF),
            supportedFormats = "MP4",
            logoUrl = "https://logo.clearbit.com/twitch.tv"
        ),
        SocialPlatform(
            name = "Loom",
            description = "Workspace video sharing recorder saver",
            icon = Icons.Default.Videocam,
            brandColor = Color(0xFF625DF5),
            accentColor = Color(0xFFDDD6FE),
            supportedFormats = "MP4",
            logoUrl = "https://logo.clearbit.com/loom.com"
        ),
        SocialPlatform(
            name = "Newgrounds",
            description = "Creative portal flash & audio saver",
            icon = Icons.Default.Games,
            brandColor = Color(0xFFD97706),
            accentColor = Color(0xFFFDE68A),
            supportedFormats = "MP4, SWF, MP3",
            logoUrl = "https://logo.clearbit.com/newgrounds.com"
        ),
        SocialPlatform(
            name = "Bilibili",
            description = "High-speed anime clip video loader",
            icon = Icons.Default.Tv,
            brandColor = Color(0xFF00AEEC),
            accentColor = Color(0xFFB3E7FC),
            supportedFormats = "MP4, FLV",
            logoUrl = "https://logo.clearbit.com/bilibili.com"
        )
    )

    // Background floating platforms setup (representing 20 cloud platform symbols/nodes)
    val floatingPlatforms = listOf(
        "PT", "FB", "IG", "X", "SC", "ST", "DM", "BS", "OK", "RT", 
        "SN", "TB", "VK", "TT", "YT", "TW", "LM", "NG", "BB", "HUB"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "CloudHubFloatingBg")

    val globalFloatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GlobalFloatBg"
    )

    val gridState = rememberLazyGridState()

    // Smooth nested scroll detection for YouTube-style hiding/showing of header
    var isHeaderVisible by remember { mutableStateOf(true) }
    var previousIndex by remember { mutableStateOf(0) }
    var previousScrollOffset by remember { mutableStateOf(0) }

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (index > previousIndex) {
                    isHeaderVisible = false
                } else if (index < previousIndex) {
                    isHeaderVisible = true
                } else {
                    if (offset > previousScrollOffset + 15) { // Tolerance threshold
                        isHeaderVisible = false
                    } else if (offset < previousScrollOffset - 15) {
                        isHeaderVisible = true
                    }
                }
                previousIndex = index
                previousScrollOffset = offset
            }
    }

    val isAtTop = remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0
        }
    }

    val isHeaderVisibleState by remember {
        derivedStateOf {
            isHeaderVisible || isAtTop.value
        }
    }

    val topBarOffset by animateDpAsState(
        targetValue = if (isHeaderVisibleState) 0.dp else (-130).dp,
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "TopBarOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // --- 20 FLOATING CLOUD-LIKE PLATFORM SYMBOLS ---
        Box(modifier = Modifier.fillMaxSize()) {
            floatingPlatforms.forEachIndexed { index, tag ->
                val angleOffset = index * 18f
                val xPosDp = (40 + (index * 16)).dp

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            // Calculate varying float positions per element on GPU thread inside draw-lambda
                            val speedFactor = 1f + (index % 3) * 0.15f
                            val angle = (globalFloatOffset * speedFactor) + angleOffset
                            val yOffsetDp = (80 + (sin(angle) * 16)).dp
                            translationX = xPosDp.toPx()
                            translationY = yOffsetDp.toPx()
                        }
                        .scale(0.85f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.07f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tag,
                        color = Color.White.copy(alpha = 0.22f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- COLLAPSIBLE TOP APP BAR (With clean frosted glass/blur effect when scrolled) ---
        Box(
            modifier = Modifier
                .offset(y = topBarOffset)
                .fillMaxWidth()
                .zIndex(10f) // Floating above the grid cards
        ) {
            if (!isAtTop.value) {
                // Frosted blur backdrop layers
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(20.dp)
                        .background(Color.White.copy(alpha = 0.72f))
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            width = 0.5.dp,
                            color = Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .background(Color.White.copy(alpha = 0.25f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Media Download Hub",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Seamless video, audio & loop extractor for 19+ sources",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // --- GRID CONTENT (Scrolls underneath the sticky blurred TopAppBar) ---
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = 110.dp, // Give precise space for header
                bottom = 120.dp,
                start = 20.dp,
                end = 20.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            items(platforms, key = { it.name }) { platform ->
                SocialCard(
                    platform = platform,
                    onClick = { selectedPlatform = platform }
                )
            }
        }

        // --- BOTTOM POPUP BOTTOM SHEET FLOW ---
        if (selectedPlatform != null) {
            HubPopupDialog(
                platform = selectedPlatform!!,
                viewModel = viewModel,
                onDismiss = { selectedPlatform = null }
            )
        }
    }
}

@Composable
fun SocialCard(
    platform: SocialPlatform,
    onClick: () -> Unit
) {
    // Beautiful large card with custom asymmetrical cuts and vibrant gradient highlights
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(165.dp)
            .shadow(4.dp, RoundedCornerShape(topStart = 32.dp, bottomEnd = 32.dp))
            .border(
                width = 1.dp, 
                color = platform.brandColor.copy(alpha = 0.25f), 
                shape = RoundedCornerShape(topStart = 32.dp, bottomEnd = 32.dp)
            )
            .clip(RoundedCornerShape(topStart = 32.dp, bottomEnd = 32.dp))
            .clickable { onClick() }
            .testTag("social_card_${platform.name}"),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Branded Platform Icon Bubble using Original High Quality Logo
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = platform.logoUrl,
                        contentDescription = platform.name,
                        modifier = Modifier.fillMaxSize(),
                        error = {
                            Icon(
                                imageVector = platform.icon,
                                contentDescription = platform.name,
                                tint = platform.brandColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                }

                // Tiny supported pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(platform.brandColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "READY",
                        color = platform.brandColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = platform.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Text(
                    text = platform.description,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = platform.supportedFormats,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = platform.brandColor,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

@Composable
fun HubPopupDialog(
    platform: SocialPlatform,
    viewModel: CloudihubViewModel,
    onDismiss: () -> Unit
) {
    // Fixed: Starts completely empty and automatically trims typed/pasted content
    var urlInput by remember { mutableStateOf("") }
    var extractionStep by remember { mutableStateOf(0) } // 0: Input, 1: Extracting/Loading, 2: Preview & Options
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Download Settings States
    var selectedFormat by remember { mutableStateOf("MP4") }
    var selectedResolution by remember { mutableStateOf("1080p") }
    var selectedBitrate by remember { mutableStateOf("320 kbps") }
    var selectedFramerate by remember { mutableStateOf("60 fps") }

    // Consistent Cloud-theme color palette used for all popups
    val cloudThemeColor = Color(0xFF0284C7)
    val cloudLightBg = Color(0xFFE0F2FE)
    val cloudTextDark = Color(0xFF0F172A)
    val cloudSubtitle = Color(0xFF64748B)

    // Smooth iOS bottom-sheet transition states
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val dismissWithAnimation = {
        scope.launch {
            isVisible = false
            delay(200) // Let the faster slide down transition complete beautifully
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { dismissWithAnimation() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val backdropAlpha by animateFloatAsState(
            targetValue = if (isVisible) 0.5f else 0.0f,
            animationSpec = tween(durationMillis = 200, easing = EaseOutQuad),
            label = "BackdropAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Performance optimization: Draw backdrop on canvas to completely skip recompositions
                    drawRect(Color.Black.copy(alpha = backdropAlpha))
                }
                .clickable { dismissWithAnimation() }, // Close when clicking backdrop
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = 0.85f, // Smooth, professional spring
                        stiffness = 240f // Crisp snappier luxury entrance
                    )
                ) + fadeIn(animationSpec = tween(150)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = 0.9f,
                        stiffness = 320f // Fast responsive dismissal glide
                    )
                ) + fadeOut(animationSpec = tween(120)),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Card Content Container representing the elegant BottomSheet
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .clickable(enabled = false) {} // Disable clicks closing bottom container
                        .shadow(16.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.3f),
                            RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp)
                    ) {
                        // --- DRAG INDICATOR HANDLE ---
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(vertical = 12.dp)
                                .size(width = 44.dp, height = 4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFCBD5E1))
                        )

                        // --- HEADER (Single Cloud Theme Color) ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(cloudLightBg)
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SubcomposeAsyncImage(
                                        model = platform.logoUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        error = {
                                            Icon(
                                                imageVector = platform.icon,
                                                contentDescription = null,
                                                tint = cloudThemeColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${platform.name} Portal",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cloudTextDark
                                )
                            }

                            IconButton(
                                onClick = { dismissWithAnimation() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = cloudSubtitle,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrollable dynamic content container
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                    ) {
                        AnimatedContent(
                            targetState = extractionStep,
                            transitionSpec = {
                                slideInVertically { height -> height } + fadeIn() togetherWith
                                        slideOutVertically { height -> -height } + fadeOut()
                            },
                            label = "PopupStateCrossfade"
                        ) { step ->
                            when (step) {
                                0 -> {
                                    // --- STEP 0: LINK INPUT SCREEN ---
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // URL Input Textbox: Perfectly compact, auto-trims, high contrast text
                                        OutlinedTextField(
                                            value = urlInput,
                                            onValueChange = { urlInput = it.trim() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("hub_url_input"),
                                            placeholder = { 
                                                Text(
                                                    text = "Paste video link here...", 
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 14.sp
                                                ) 
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Uri,
                                                imeAction = ImeAction.Done
                                            ),
                                            leadingIcon = {
                                                // Platform logo on the absolute left kinarai with minimal margins
                                                Box(
                                                    modifier = Modifier
                                                        .padding(start = 8.dp, end = 2.dp)
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    SubcomposeAsyncImage(
                                                        model = platform.logoUrl,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        error = {
                                                            Icon(
                                                                imageVector = platform.icon,
                                                                contentDescription = null,
                                                                tint = cloudThemeColor,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    )
                                                }
                                            },
                                            trailingIcon = {
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.getText()?.let {
                                                            urlInput = it.text.trim()
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentPaste,
                                                        contentDescription = "Paste Clipboard",
                                                        tint = cloudThemeColor,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color(0xFF0F172A), // Dark text to stand out
                                                unfocusedTextColor = Color(0xFF0F172A), // Dark text to stand out
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                focusedBorderColor = cloudThemeColor,
                                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                                cursorColor = cloudThemeColor
                                            ),
                                            textStyle = LocalTextStyle.current.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF0F172A)
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Instructions and description placed BELOW the URL box, disappearing in Step 1 & 2
                                        Text(
                                            text = "Verify your video URL",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = cloudTextDark
                                        )
                                        Text(
                                            text = "Cloudihub will automatically verify & parse stream channels from ${platform.name} for extraction.",
                                            fontSize = 11.sp,
                                            color = cloudSubtitle,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = cloudLightBg),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    text = "Extraction Guide:",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0369A1)
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "1. Navigate to ${platform.name} and select the media clip.\n" +
                                                           "2. Click share & copy the target URL link.\n" +
                                                           "3. Paste the clean link into the input box above.\n" +
                                                           "4. Press 'Extract Stream' below to select dynamic settings.",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF0284C7),
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Analyze Trigger Button (Cloud Theme)
                                        Button(
                                            onClick = {
                                                if (urlInput.isNotBlank()) {
                                                    scope.launch {
                                                        extractionStep = 1
                                                        delay(1800) // Simulated link extraction progress
                                                        extractionStep = 2
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(54.dp)
                                                .testTag("analyze_button"),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = cloudThemeColor)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudSync,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Extract Stream",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                1 -> {
                                    // --- STEP 1: MODERN LOADING SCREEN ---
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 48.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        val infiniteLoading = rememberInfiniteTransition(label = "ExtractionLoading")
                                        val spinAngle by infiniteLoading.animateFloat(
                                            initialValue = 0f,
                                            targetValue = 360f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(1000, easing = LinearEasing),
                                                repeatMode = RepeatMode.Restart
                                            ),
                                            label = "SpinLoading"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .rotate(spinAngle)
                                                .border(
                                                    width = 4.dp,
                                                    brush = Brush.sweepGradient(
                                                        listOf(cloudThemeColor, Color.Transparent)
                                                    ),
                                                    shape = CircleShape
                                                )
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Text(
                                            text = "Connecting Cloud Extractor...",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cloudTextDark
                                        )
                                        Text(
                                            text = "Extracting video, audio codecs, and metadata...",
                                            fontSize = 12.sp,
                                            color = cloudSubtitle,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp)
                                        )
                                    }
                                }

                                2 -> {
                                    // --- STEP 2: VIDEO DOWNLOAD PREVIEW & LINE EXPANDABLE OPTIONS ---
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Video Player Cover Preview Card
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color(0xFF0F172A)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Preview Play",
                                                tint = Color.White.copy(alpha = 0.9f),
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .border(2.dp, Color.White, CircleShape)
                                                    .padding(8.dp)
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.BottomStart)
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                                        )
                                                    )
                                                    .padding(12.dp)
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "Nature Timelapse: Majestic Floating Clouds",
                                                        color = Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "Duration: 08:45 • Size: ~32.2 MB",
                                                        color = Color.White.copy(alpha = 0.65f),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Text(
                                            text = "DOWNLOAD OPTIONS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = cloudThemeColor,
                                            letterSpacing = 1.sp
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Line-by-line expandable options list instead of boxes
                                        ExpandableOptionRow(
                                            label = "Select Format",
                                            options = listOf("MP4", "MKV", "MP3"),
                                            selected = selectedFormat,
                                            onSelect = { selectedFormat = it },
                                            themeColor = cloudThemeColor
                                        )

                                        ExpandableOptionRow(
                                            label = "Resolution quality",
                                            options = listOf("1080p", "720p", "480p"),
                                            selected = selectedResolution,
                                            onSelect = { selectedResolution = it },
                                            themeColor = cloudThemeColor
                                        )

                                        ExpandableOptionRow(
                                            label = "Audio Bitrate Speed",
                                            options = listOf("320 kbps", "256 kbps", "128 kbps"),
                                            selected = selectedBitrate,
                                            onSelect = { selectedBitrate = it },
                                            themeColor = cloudThemeColor
                                        )

                                        ExpandableOptionRow(
                                            label = "Video Framerate",
                                            options = listOf("60 fps", "30 fps"),
                                            selected = selectedFramerate,
                                            onSelect = { selectedFramerate = it },
                                            themeColor = cloudThemeColor
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        var downloadingState by remember { mutableStateOf(false) }

                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    downloadingState = true
                                                    delay(1200)
                                                    downloadingState = false
                                                    
                                                    val mockVideo = CloudVideo(
                                                        id = "cloud_extractor_${System.currentTimeMillis()}",
                                                        title = "[Downloaded] Nature Timelapse (${selectedResolution}_${selectedFramerate}_$selectedFormat)",
                                                        duration = "08:45",
                                                        creator = platform.name,
                                                        imageUrl = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=600",
                                                        views = "1 view",
                                                        fileUrl = "https://example.com/v3.mp4",
                                                        sizeMb = 32.2
                                                    )
                                                    
                                                    viewModel.triggerVideoDownload(mockVideo)
                                                    dismissWithAnimation()
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(54.dp)
                                                .testTag("download_now_button"),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = cloudThemeColor)
                                        ) {
                                            if (downloadingState) {
                                                CircularProgressIndicator(
                                                    color = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.CloudDownload,
                                                    contentDescription = null,
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Download Now",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun ExpandableOptionRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    themeColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selected,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand options",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 4.dp)
                ) {
                    options.forEach { option ->
                        val isSelected = option == selected
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(option)
                                    expanded = false
                                }
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) themeColor else Color(0xFF334155)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = themeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
