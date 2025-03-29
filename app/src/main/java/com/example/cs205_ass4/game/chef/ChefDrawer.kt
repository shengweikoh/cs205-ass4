package com.example.cs205_ass4.game.chef

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

object ChefDrawer {
    fun drawChefs(drawScope: DrawScope, chefs: List<Chef>) {
        chefs.forEach { chef ->
            drawScope.drawCircle(
                color = Color.Red,
                radius = ChefConstants.CHEF_SIZE / 2,
                center = Offset(chef.xPosition, chef.yPosition)
            )
        }
    }
} 