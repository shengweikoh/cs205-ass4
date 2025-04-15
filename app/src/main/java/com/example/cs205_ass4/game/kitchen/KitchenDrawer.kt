package com.example.cs205_ass4.game.kitchen

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

object KitchenDrawer {
    fun drawKitchen(drawScope: DrawScope, kitchen: Kitchen) {
        // Draw kitchen background
        drawScope.drawRect(
            color = Color.White,
            topLeft = Offset(0f, 0f),
            size = Size(drawScope.size.width, drawScope.size.height)
        )
        
        // Draw kitchen counter
        val counterHeight = drawScope.size.height * KitchenConstants.COUNTER_HEIGHT_RATIO
        val counterY = drawScope.size.height * KitchenConstants.COUNTER_Y_POSITION_RATIO
        kitchen.kitchenCounter.draw(
            drawScope,
            position = Offset(0f, counterY),
            size = Size(drawScope.size.width, counterHeight)
        )
    }
} 