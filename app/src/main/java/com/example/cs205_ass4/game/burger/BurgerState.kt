package com.example.cs205_ass4.game.burger

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size

class BurgerState {
    var numberOfBurgers by mutableStateOf(BurgerConstants.INITIAL_NUMBER_OF_BURGERS)
    
    fun getBurgers(screenSize: Size): List<Burger> {
        if (numberOfBurgers == 1) {
            return List(1) { index ->
                Burger(
                    id = index,
                    xPosition = BurgerConstants.LEFT_MARGIN,
                    yPosition = screenSize.height - BurgerConstants.BOTTOM_MARGIN
                )
            }
        }

        val availableWidth = screenSize.width - 2 * BurgerConstants.LEFT_MARGIN
        val spacing = availableWidth / (BurgerConstants.NUM_BURGERS_PER_ROW + 1)
        val burgers = mutableListOf<Burger>()
        var remainingBurgers = numberOfBurgers
        var row = 0
        
        while (remainingBurgers > 0) {
            val numBurgersInRow = if (BurgerConstants.NUM_BURGERS_PER_ROW <= remainingBurgers) {
                BurgerConstants.NUM_BURGERS_PER_ROW
            } else {
                remainingBurgers
            }
            remainingBurgers -= numBurgersInRow
            
            for (col in 0 until numBurgersInRow) {
                burgers.add(
                    Burger(
                        id = burgers.size,
                        xPosition = BurgerConstants.LEFT_MARGIN + (col + 1) * spacing,
                        yPosition = screenSize.height - BurgerConstants.BOTTOM_MARGIN + row * BurgerConstants.SPACING_BETWEEN_ROWS
                    )
                )
            }
            row++
        }
        return burgers
    }
    
} 