package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * A custom Shape that outlines a beautiful, fluffy cloud using cubic Bezier curves.
 */
class CloudShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = createCloudPath(size.width, size.height)
        return Outline.Generic(path)
    }
}

/**
 * Creates an organic-looking cloud path that fits inside the given width and height.
 */
fun createCloudPath(w: Float, h: Float): Path {
    val path = Path()
    path.moveTo(w * 0.25f, h * 0.75f)
    path.cubicTo(w * 0.05f, h * 0.75f, w * 0.05f, h * 0.45f, w * 0.22f, h * 0.45f)
    path.cubicTo(w * 0.18f, h * 0.20f, w * 0.42f, h * 0.15f, w * 0.48f, h * 0.32f)
    path.cubicTo(w * 0.55f, h * 0.12f, w * 0.82f, h * 0.18f, w * 0.80f, h * 0.40f)
    path.cubicTo(w * 0.95f, h * 0.40f, w * 0.96f, h * 0.68f, w * 0.82f, h * 0.72f)
    path.cubicTo(w * 0.70f, h * 0.82f, w * 0.55f, h * 0.84f, w * 0.45f, h * 0.78f)
    path.cubicTo(w * 0.38f, h * 0.82f, w * 0.30f, h * 0.80f, w * 0.25f, h * 0.75f)
    path.close()
    return path
}

/**
 * High-performance, zero-overhead sky background for the app.
 */
@Composable
fun CloudSkyBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF1F5F9),
                        Color(0xFFE2E8F0),
                        Color(0xFFF8FAFC)
                    )
                )
            )
    )
}
