package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.PureBlack

/**
 * Linear/Raycast-inspired subtle grid pattern on pure black (#000000)
 * with a subtle ambient cyan glow at the top.
 */
@Composable
fun SubtleGridBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Grid pattern + subtle top glow canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 32.dp.toPx()
            val gridColor = Color.White.copy(alpha = 0.035f)

            // Draw vertical grid lines
            var x = 0f
            while (x <= size.width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += gridStep
            }

            // Draw horizontal grid lines
            var y = 0f
            while (y <= size.height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // Top ambient subtle radial glow (Linear / Raycast signature spotlight)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonBlue.copy(alpha = 0.07f),
                        Color(0xFF8A2BE2).copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2f, 0f),
                    radius = size.width * 0.9f
                ),
                radius = size.width * 0.9f,
                center = Offset(size.width / 2f, 0f)
            )
        }

        // Content slot
        content()
    }
}
