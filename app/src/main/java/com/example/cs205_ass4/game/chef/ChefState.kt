package com.example.cs205_ass4.game.chef

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size

class ChefState {
    var numberOfChefs by mutableStateOf(ChefConstants.INITIAL_NUMBER_OF_CHEFS)
    
    fun getChefs(screenSize: Size): List<Chef> {
        
        if (numberOfChefs == 1) {
            return List(1) { index ->
                Chef(
                    id = index,
                    xPosition = screenSize.width / 2,
                    yPosition = ChefConstants.TOP_MARGIN
                )
            }
        }
        val availableWidth = screenSize.width - 2 * ChefConstants.LEFT_RIGHT_MARGIN
        // this is the spacing between left margin and the centre of the first chef
        // and between the centres of the chefs
        // and between the centre of the last chef and the right margin
        val spacing = availableWidth / (numberOfChefs + 1)
        return List(numberOfChefs) { index ->
            Chef(
                id = index,
                xPosition = ChefConstants.LEFT_RIGHT_MARGIN + (index + 1) * spacing,
                yPosition = ChefConstants.TOP_MARGIN
            )
        }
    }
} 