package com.example.cs205_ass4.game

import com.example.cs205_ass4.game.chef.ChefManager
import com.example.cs205_ass4.game.burger.BurgerManager
import com.example.cs205_ass4.game.chef.ChefState

class GameEngine {

    // Managers for different game entities
    val chefManager = ChefManager()
    val burgerManager = BurgerManager()

    var burgerCounter = 0

    init {
        // Spawn initial game entities
//        chefManager.spawnChef(id = 1, x = 100f, y = 200f)
//        burgerManager.spawnBurger(id = 1, x = 150f, y = 250f)
    }

    // Called to start game loops, timers, etc.
    fun startGame() {
        // TODO: Implement game loop logic or timer events
        
    }

    // Called on each game tick/update
    fun updateGame() {
        chefManager.updateChefs()    // Update chef states, positions, etc.
        burgerManager.updateBurgers()  // Update burger states, positions, etc.

        // Additional game logic (e.g., collision detection, scoring) goes here
    }

    fun getChefState(chefId: Int): ChefState {
        return chefManager.getChefById(chefId)?.chefState ?: ChefState.IDLE
    }

    // Toggle a specific chef's state (for example, when a user taps on a chef)
    fun toggleChefState(chefId: Int) {
        chefManager.toggleChef(chefId)
    }

    fun spawnBurger() {
        return burgerManager.spawnBurger(burgerCounter, 0f, 0f)
    }
}