package com.example.model

import androidx.compose.ui.graphics.Color

data class VideoItem(
    val id: String,
    val title: String,
    val channelName: String,
    val channelAvatarUrl: String = "",
    val views: String,
    val uploadTime: String,
    val duration: String,
    val description: String,
    val category: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val likesCount: String = "45K",
    val commentsCount: String = "1.2K",
    val tags: List<String> = listOf("Trending", "4K", "Music", "Tech")
)

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val durationSeconds: Int,
    val albumArtGradient: List<Color>,
    val lyrics: List<LyricLine> = emptyList(),
    val isLiked: Boolean = false
)

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class BookmarkItem(
    val title: String,
    val url: String,
    val iconName: String,
    val badgeColor: Color
)

data class SearchSuggestion(
    val text: String,
    val category: String,
    val isTrending: Boolean = false
)
