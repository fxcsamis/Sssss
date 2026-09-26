package com.example.ui.screens.home

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.CloudVideo
import com.example.ui.CloudihubViewModel
import com.example.ui.components.LottieDownloadIcon

@Composable
fun ShortsLogoIcon(modifier: Modifier = Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data("https://i.postimg.cc/KvTkCxmW/You-Tube-Shorts-Logo-PNG-Transparent-(1).jpg")
            .crossfade(true)
            .build(),
        contentDescription = "YouTube Shorts Logo",
        contentScale = ContentScale.Fit,
        modifier = modifier.clip(RoundedCornerShape(4.dp))
    )
}

@Composable
fun LiveLogoIcon(modifier: Modifier = Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data("https://i.postimg.cc/HsWXKjSW/Live-icon-PNG-Transparent-Live-logo-(1).jpg")
            .crossfade(true)
            .build(),
        contentDescription = "Live Logo",
        contentScale = ContentScale.Fit,
        modifier = modifier.clip(RoundedCornerShape(4.dp))
    )
}

private data class ShareAppTarget(
    val name: String,
    val bgColor: Color,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareVideoBottomSheet(
    video: CloudVideo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun launchNativeShare() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "${video.title}\n${video.fileUrl}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share video")
        context.startActivity(shareIntent)
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.45f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFCBD5E1))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Share",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F172A)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(CUSTOM_SHARE_ICON_URL)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = video.fileUrl,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(video.fileUrl))
                            Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Link",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val shareApps = remember {
                listOf(
                    ShareAppTarget("WhatsApp", Color(0xFF25D366), Icons.Default.Chat),
                    ShareAppTarget("Facebook", Color(0xFF1877F2), Icons.Default.Share),
                    ShareAppTarget("Messenger", Color(0xFF0084FF), Icons.Default.Send),
                    ShareAppTarget("Telegram", Color(0xFF229ED9), Icons.Default.Send),
                    ShareAppTarget("Gmail", Color(0xFFEA4335), Icons.Default.Email),
                    ShareAppTarget("Bluetooth", Color(0xFF0082FC), Icons.Default.Bluetooth),
                    ShareAppTarget("More", Color(0xFF64748B), Icons.Default.MoreHoriz)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                shareApps.forEach { app ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { launchNativeShare() }
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(app.bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = app.icon,
                                contentDescription = app.name,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = app.name,
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                ShareActionRow(
                    icon = Icons.Default.ContentCopy,
                    title = "Copy link",
                    onClick = {
                        clipboardManager.setText(AnnotatedString(video.fileUrl))
                        Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                ShareActionRow(
                    imageUrl = CUSTOM_SHARE_ICON_URL,
                    title = "Quick Share / System Share",
                    onClick = { launchNativeShare() }
                )

                ShareActionRow(
                    icon = Icons.Default.Edit,
                    title = "Create post",
                    onClick = {
                        Toast.makeText(context, "Opening post creation...", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun ShareActionRow(
    icon: ImageVector? = null,
    imageUrl: String? = null,
    title: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF1E293B),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B)
        )
    }
}

data class DownloadFormatOption(
    val id: String,
    val title: String,
    val format: String,
    val badge: String? = null,
    val sizeMb: Double,
    val isAudio: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadVideoBottomSheet(
    video: CloudVideo,
    viewModel: CloudihubViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var customTitle by remember(video.title) { mutableStateOf(video.title) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var selectedOptionId by remember { mutableStateOf("v_720p") }
    var storagePath by remember { mutableStateOf("/storage/emulated/0/Cloudihub/download/") }

    val audioOptions = remember(video.sizeMb) {
        listOf(
            DownloadFormatOption("a_48k_m4a", "48K", "(M4A)", null, (video.sizeMb * 0.12), true),
            DownloadFormatOption("a_48k_mp3", "48K", "(MP3)", "SLOW", (video.sizeMb * 0.12), true),
            DownloadFormatOption("a_128k_m4a", "128K", "(M4A)", null, (video.sizeMb * 0.2), true),
            DownloadFormatOption("a_128k_mp3", "128K", "(MP3)", "SLOW", (video.sizeMb * 0.2), true),
            DownloadFormatOption("a_256k_mp3", "256K", "(MP3)", "SLOW", (video.sizeMb * 0.28), true),
            DownloadFormatOption("a_320k_mp3", "320K", "(MP3)", "FAST", (video.sizeMb * 0.35), true)
        )
    }

    val videoOptions = remember(video.sizeMb) {
        listOf(
            DownloadFormatOption("v_144p", "144P", "(MP4)", null, (video.sizeMb * 0.25), false),
            DownloadFormatOption("v_240p", "240P", "(MP4)", null, (video.sizeMb * 0.45), false),
            DownloadFormatOption("v_360p", "360P", "(MP4)", null, (video.sizeMb * 0.65), false),
            DownloadFormatOption("v_480p", "480P", "(MP4)", null, (video.sizeMb * 0.9), false),
            DownloadFormatOption("v_720p", "720P HD", "(MP4)", null, video.sizeMb, false),
            DownloadFormatOption("v_1080p", "1080P", "(MP4)", "FULL HD", (video.sizeMb * 1.8), false)
        )
    }

    val allOptions = remember(audioOptions, videoOptions) { audioOptions + videoOptions }
    val currentSelected = allOptions.find { it.id == selectedOptionId } ?: videoOptions[4]

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF8FAFC),
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFCBD5E1))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 86.dp, height = 54.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(video.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = video.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(3.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = video.duration.ifBlank { "03:49" },
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = customTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = if (isEditingTitle) "Save" else "Rename",
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isEditingTitle = !isEditingTitle }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    AnimatedVisibility(visible = isEditingTitle) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            OutlinedTextField(
                                value = customTitle,
                                onValueChange = { customTitle = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedBorderColor = Color(0xFF0284C7),
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Folder",
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Path:$storagePath",
                                    fontSize = 10.sp,
                                    color = Color(0xFF475569),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "59.6GB FREE / 104.9GB",
                                    fontSize = 9.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Text(
                            text = "Change",
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    Toast.makeText(context, "Storage path updated!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Music",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Music",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val audioRows = audioOptions.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        audioRows.forEach { rowOptions ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowOptions.forEach { opt ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        DownloadOptionItemRow(
                                            option = opt,
                                            isSelected = selectedOptionId == opt.id,
                                            onSelect = { selectedOptionId = opt.id }
                                        )
                                    }
                                }
                                if (rowOptions.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Video",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Video",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val videoRows = videoOptions.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        videoRows.forEach { rowOptions ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowOptions.forEach { opt ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        DownloadOptionItemRow(
                                            option = opt,
                                            isSelected = selectedOptionId == opt.id,
                                            onSelect = { selectedOptionId = opt.id }
                                        )
                                    }
                                }
                                if (rowOptions.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val qualityLabel = "${currentSelected.title} ${currentSelected.format}".trim()
                    viewModel.triggerVideoDownloadWithOptions(
                        video = video,
                        customTitle = customTitle.ifBlank { video.title },
                        qualityLabel = qualityLabel,
                        estimatedSizeMb = currentSelected.sizeMb,
                        isAudioOnly = currentSelected.isAudio
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                LottieDownloadIcon(
                    isDownloading = false,
                    size = 24.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DOWNLOAD",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun DownloadOptionItemRow(
    option: DownloadFormatOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val accentColor = Color(0xFF0284C7)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            contentDescription = option.title,
            tint = if (isSelected) accentColor else Color(0xFF94A3B8),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = option.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelected) accentColor else Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = option.format,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                option.badge?.let { badgeText ->
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFE2E8F0))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = String.format("%.2fMB", option.sizeMb),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) accentColor else Color(0xFF64748B)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoMoreOptionsSheet(
    video: CloudVideo,
    isWatchLater: Boolean,
    onDismiss: () -> Unit,
    onPlayClick: () -> Unit,
    onShareClick: () -> Unit,
    onWatchLaterClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onNotInterestedClick: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(video.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${video.creator} • ${video.views} views",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            MoreOptionRowItem(
                icon = Icons.Default.PlayCircle,
                title = "Play Video",
                subtitle = "Stream video in high quality",
                onClick = {
                    onPlayClick()
                    onDismiss()
                }
            )

            MoreOptionRowItem(
                icon = Icons.Default.Share,
                title = "Share Video",
                subtitle = "Send link to social apps",
                onClick = {
                    onShareClick()
                    onDismiss()
                }
            )

            MoreOptionRowItem(
                icon = if (isWatchLater) Icons.Default.BookmarkRemove else Icons.Default.BookmarkAdd,
                title = if (isWatchLater) "Remove from Watch Later" else "Save to Watch Later",
                subtitle = "Access anytime from Watch Later list",
                onClick = {
                    onWatchLaterClick()
                    onDismiss()
                }
            )

            MoreOptionRowItem(
                icon = Icons.Default.CloudDownload,
                title = "Download Video",
                subtitle = "Save for offline playback",
                onClick = {
                    onDownloadClick()
                    onDismiss()
                }
            )

            MoreOptionRowItem(
                icon = Icons.Default.Block,
                title = "Not Interested",
                subtitle = "Hide similar content from feed",
                onClick = {
                    onNotInterestedClick()
                    onDismiss()
                }
            )

            MoreOptionRowItem(
                icon = Icons.Default.ContentCopy,
                title = "Copy Video Link",
                subtitle = "Copy direct CDN URL to clipboard",
                onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val linkToCopy = if (video.fileUrl.isNotEmpty()) video.fileUrl else "https://youtube.com/watch?v=${video.id}"
                    val clip = android.content.ClipData.newPlainText("Video Link", linkToCopy)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MoreOptionRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0F9FF))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF0F172A)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
