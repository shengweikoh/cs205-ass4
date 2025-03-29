package com.example.cs205_ass4.game.burger

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

object BurgerDrawer {
    fun drawBurgers(drawScope: DrawScope, burgers: List<Burger>) {
        burgers.forEach { burger ->
            drawScope.drawCircle(
                color = Color.Green,
                radius = BurgerConstants.BURGER_SIZE / 2,
                center = Offset(burger.xPosition, burger.yPosition)
            )
        }
    }
} 