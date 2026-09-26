package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

const val LOTTIE_DOWNLOAD_URL = "https://lottie.host/1973d2b1-290a-446a-ac24-9c910a7a82c7/WQsuxbfZuM.lottie"

@Composable
fun LottieDownloadIcon(
    isDownloading: Boolean = false,
    size: Dp = 22.dp,
    pauseDelayMs: Long = 1500L,
    modifier: Modifier = Modifier
) {
    if (!isDownloading) {
        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FileDownload,
                contentDescription = "Download",
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(size)
            )
        }
    } else {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.Url(LOTTIE_DOWNLOAD_URL)
        )

        var isAnimPlaying by remember(isDownloading) { mutableStateOf(isDownloading) }

        val progress by animateLottieCompositionAsState(
            composition = composition,
            isPlaying = isAnimPlaying,
            iterations = 1,
            restartOnPlay = true
        )

        LaunchedEffect(progress, isDownloading) {
            if (isDownloading && isAnimPlaying && progress >= 0.98f) {
                isAnimPlaying = false
                if (pauseDelayMs > 0) {
                    delay(pauseDelayMs)
                }
                if (isDownloading) {
                    isAnimPlaying = true
                }
            }
        }

        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(size)
            )
        }
    }
}
