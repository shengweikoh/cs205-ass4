package com.example.cs205_ass4.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.cs205_ass4.game.chef.ChefDrawer
import com.example.cs205_ass4.game.chef.ChefState
import com.example.cs205_ass4.game.burger.BurgerDrawer
import com.example.cs205_ass4.game.burger.BurgerState
import com.example.cs205_ass4.game.kitchen.Kitchen

@Composable
fun GameScreen() {
    val chefState = remember { ChefState() }
    val burgerState = remember { BurgerState() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Draw kitchen as a composable
        Kitchen()
        
        // Draw chefs and burgers in a Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            ChefDrawer.drawChefs(this, chefState.getChefs(size))
            BurgerDrawer.drawBurgers(this, burgerState.getBurgers(size))
        }
    }
} 