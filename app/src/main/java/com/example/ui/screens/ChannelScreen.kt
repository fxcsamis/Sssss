package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ui.CloudihubViewModel

private fun formatCount(count: Long): String = when {
    count < 0 -> ""
    count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0)
    count >= 1_000 -> "%.1fK".format(count / 1_000.0)
    else -> count.toString()
}

@Composable
fun ChannelScreen(viewModel: CloudihubViewModel, modifier: Modifier = Modifier) {
    val channel = viewModel.viewingChannel
    var selectedTab by remember(channel) { mutableStateOf(0) } // 0 = Videos, 1 = Shorts

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        if (viewModel.isLoadingChannel) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        viewModel.channelErrorMessage?.let { error ->
            Box(
                modifier = Modifier.fillMaxWidth(0.85f).align(Alignment.Center)
                    .clip(RoundedCornerShape(10.dp)).background(Color.White).padding(16.dp)
            ) {
                Text(error, color = Color(0xFF606060), fontSize = 13.sp)
            }
        }

        if (channel != null) {
            val listState = rememberLazyListState()

            LaunchedEffect(listState, viewModel.channelVideos.size, selectedTab) {
                if (selectedTab != 0) return@LaunchedEffect
                snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                    .collect { lastVisible ->
                        if (lastVisible != null && lastVisible >= viewModel.channelVideos.size - 3) {
                            viewModel.loadMoreChannelVideos()
                        }
                    }
            }

            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                item {
                    // Top bar
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.closeChannel() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }

                    // Channel header
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(channel.avatarUrl),
                            contentDescription = channel.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(84.dp).clip(CircleShape).background(Color(0xFFD9D9D9))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(channel.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F0F0F))
                        if (channel.subscriberCount >= 0) {
                            Text(
                                "${formatCount(channel.subscriberCount)} subscribers",
                                fontSize = 13.sp, color = Color(0xFF606060)
                            )
                        }
                        if (channel.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                channel.description, fontSize = 12.sp, color = Color(0xFF606060),
                                maxLines = 2, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }

                        var isSubscribed by remember(channel.name) { mutableStateOf(false) }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { isSubscribed = !isSubscribed },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSubscribed) Color(0xFFE5E5E5) else Color(0xFF0F0F0F),
                                contentColor = if (isSubscribed) Color(0xFF0F0F0F) else Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(if (isSubscribed) "Subscribed" else "Subscribe")
                        }
                    }

                    // Tabs
                    if (channel.hasShorts) {
                        TabRow(selectedTabIndex = selectedTab, containerColor = Color(0xFFF8F9FA)) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Videos") }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    viewModel.loadChannelShorts(viewModel.viewingChannelUrl ?: "")
                                },
                                text = { Text("Shorts") }
                            )
                        }
                    }
                }

                if (selectedTab == 0) {
                    items(viewModel.channelVideos, key = { it.id }) { video ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            VideoCloudCard(video = video, onDownloadClick = {}, onPlayClick = { viewModel.closeChannel(); viewModel.playVideo(it) })
                        }
                    }
                    if (viewModel.isLoadingMoreChannelVideos) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF38BDF8))
                            }
                        }
                    }
                } else {
                    if (viewModel.isLoadingChannelShorts) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF38BDF8))
                            }
                        }
                    }
                    items(viewModel.channelShorts, key = { it.id }) { short ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            VideoCloudCard(video = short, onDownloadClick = {}, onPlayClick = { viewModel.closeChannel(); viewModel.playShort(it) })
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}
