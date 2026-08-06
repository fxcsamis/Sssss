package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.model.BookmarkItem
import com.example.model.LyricLine
import com.example.model.MusicTrack
import com.example.model.SearchSuggestion
import com.example.model.VideoItem

object SampleData {

    val sampleVideos = listOf(
        VideoItem(
            id = "v1",
            title = "Jetpack Compose Shared Element & Orbital Animations Masterclass 2026",
            channelName = "Android Developers Pro",
            views = "342K views",
            uploadTime = "2 hours ago",
            duration = "14:28",
            description = "Learn how to build seamless, fluid morphing animations, bounds transitions, and spring physics in Jetpack Compose using modern Android APIs.",
            category = "Coding",
            primaryColor = Color(0xFF6366F1),
            secondaryColor = Color(0xFF4F46E5),
            likesCount = "28K",
            commentsCount = "1,420"
        ),
        VideoItem(
            id = "v2",
            title = "Chill Lofi Beats for Coding, Focus & Relaxation (24/7 Live Stream)",
            channelName = "Lofi Girl Orbital",
            views = "1.8M views",
            uploadTime = "Streamed live",
            duration = "LIVE",
            description = "Peaceful lofi hip hop beats to study, code, and relax to. Featuring dynamic visualizer waves and smooth color shifts.",
            category = "Music",
            primaryColor = Color(0xFFEC4899),
            secondaryColor = Color(0xFF8B5CF6),
            likesCount = "142K",
            commentsCount = "12.8K"
        ),
        VideoItem(
            id = "v3",
            title = "Building the Future of AI Studio & Android Apps",
            channelName = "Google Tech Talks",
            views = "520K views",
            uploadTime = "1 day ago",
            duration = "22:15",
            description = "An in-depth look into how generative AI and agentic coding workflows are transforming modern mobile application development.",
            category = "Tech",
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF059669),
            likesCount = "41K",
            commentsCount = "3,110"
        ),
        VideoItem(
            id = "v4",
            title = "Cyberpunk Neo Tokyo Cinematic Drone Tour 8K 60FPS",
            channelName = "Visual Journeys",
            views = "980K views",
            uploadTime = "3 days ago",
            duration = "08:45",
            description = "Breathtaking ultra-high definition aerial views of futuristic neon skylines, rain-slicked streets, and architectural wonders.",
            category = "Trending",
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFFD97706),
            likesCount = "89K",
            commentsCount = "2,450"
        ),
        VideoItem(
            id = "v5",
            title = "Next-Gen Mobile Game Engines: Physics & Spring Dynamics Demo",
            channelName = "GameDev Unleashed",
            views = "150K views",
            uploadTime = "5 days ago",
            duration = "11:02",
            description = "Demonstrating high frame-rate particle physics, spring dampening curves, and interactive gesture feedback in mobile games.",
            category = "Gaming",
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFF0891B2),
            likesCount = "15K",
            commentsCount = "890"
        )
    )

    val sampleTracks = listOf(
        MusicTrack(
            id = "m1",
            title = "Orbital Odyssey",
            artist = "Starlight Syndicate",
            album = "Cosmic Waves (2026)",
            duration = "03:45",
            durationSeconds = 225,
            albumArtGradient = listOf(Color(0xFF6366F1), Color(0xFFEC4899)),
            lyrics = listOf(
                LyricLine(0, "Floating through the starlit sky"),
                LyricLine(5, "Chasing orbits as time goes by"),
                LyricLine(10, "Synths aligned in harmony"),
                LyricLine(15, "A fluid spark of energy"),
                LyricLine(20, "Infinite horizons glowing deep inside")
            )
        ),
        MusicTrack(
            id = "m2",
            title = "Midnight Spring Bounce",
            artist = "iOS Synthwave",
            album = "Kinetic Motion",
            duration = "02:50",
            durationSeconds = 170,
            albumArtGradient = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)),
            lyrics = listOf(
                LyricLine(0, "Feel the rhythm in the bouncing motion"),
                LyricLine(4, "Smooth transitions like a quiet ocean"),
                LyricLine(8, "Elastic curves, responsive flow"),
                LyricLine(12, "Watch the glowing meters grow")
            )
        ),
        MusicTrack(
            id = "m3",
            title = "Neon Skyline",
            artist = "Cyber Pulse",
            album = "Tokyo Dusk",
            duration = "04:12",
            durationSeconds = 252,
            albumArtGradient = listOf(Color(0xFFF43F5E), Color(0xFFFB923C)),
            lyrics = listOf(
                LyricLine(0, "Neon lights reflect on urban rain"),
                LyricLine(5, "Washing away the noise and pain"),
                LyricLine(10, "Bassline driving through the avenue")
            )
        ),
        MusicTrack(
            id = "m4",
            title = "Lofi Code Session",
            artist = "Dev Beats",
            album = "Syntax & Coffee",
            duration = "03:18",
            durationSeconds = 198,
            albumArtGradient = listOf(Color(0xFF10B981), Color(0xFF06B6D4)),
            lyrics = listOf(
                LyricLine(0, "Compile success, green status light"),
                LyricLine(4, "Building dreams into the night")
            )
        )
    )

    val sampleBookmarks = listOf(
        BookmarkItem("YouTube", "https://youtube.com", "video", Color(0xFFFF0000)),
        BookmarkItem("Google", "https://google.com", "search", Color(0xFF4285F4)),
        BookmarkItem("Spotify", "https://spotify.com", "music", Color(0xFF1DB954)),
        BookmarkItem("GitHub", "https://github.com", "code", Color(0xFF333333)),
        BookmarkItem("Reddit", "https://reddit.com", "chat", Color(0xFFFF4500)),
        BookmarkItem("Wikipedia", "https://wikipedia.org", "book", Color(0xFF666666))
    )

    val searchSuggestions = listOf(
        SearchSuggestion("Jetpack Compose Orbital animations", "YouTube", isTrending = true),
        SearchSuggestion("iOS bouncing search bar tutorial", "YouTube", isTrending = true),
        SearchSuggestion("Orbital Odyssey - Starlight Syndicate", "Music"),
        SearchSuggestion("Chill Lofi coding stream", "YouTube"),
        SearchSuggestion("Android Studio Kotlin Compose spring physics", "Browser"),
        SearchSuggestion("Shared element morphing transition", "Tech", isTrending = true),
        SearchSuggestion("Cyberpunk Neo Tokyo 8K", "YouTube")
    )
}
