package com.example.cs205_ass4.game.kitchen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
fun Kitchen(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw kitchen background
        drawRect(
            color = Color.White,
            topLeft = Offset(0f, 0f),
            size = Size(size.width, size.height)
        )
    }
} 