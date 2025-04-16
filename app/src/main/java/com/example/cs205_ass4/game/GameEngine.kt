package com.example.cs205_ass4.game

import android.os.Handler
import android.os.Looper
import com.example.cs205_ass4.game.chef.ChefManager
import com.example.cs205_ass4.game.burger.BurgerManager
import com.example.cs205_ass4.game.kitchen.KitchenManager
import com.example.cs205_ass4.game.kitchen.Order
import com.example.cs205_ass4.game.chef.ChefState
import kotlin.concurrent.thread

class GameEngine {

    // Managers for different game entities
    val chefManager = ChefManager()
    val burgerManager = BurgerManager()
    val kitchenManager = KitchenManager()

    var burgerCounter = 0
    
    // Handler for the game loop
    private val handler = Handler(Looper.getMainLooper())
    
    // Game update interval (milliseconds)
    private val GAME_ENGINE_UPDATE_INTERVAL = 100L  // 10 updates per second
    
    // Callback for burger expiration 
    private var onBurgersExpiredCallback: ((List<Int>) -> Unit)? = null

    init {
        // Set up order expiration callback
        kitchenManager.setOnOrdersExpiredCallback { expiredOrderIds ->
            onBurgersExpiredCallback?.invoke(expiredOrderIds)
        }
    }

    // Called to start game loops, timers, etc.
    fun startGame() {
        // TODO: Implement other game loop logic or timer events
        // Start the game loop
        scheduleUpdate()
    }
    
    private fun scheduleUpdate() {
        handler.postDelayed({
            updateGame()
            scheduleUpdate()  // Schedule the next update
        }, GAME_ENGINE_UPDATE_INTERVAL)
    }

    // Called on each game tick/update
    fun updateGame() {
        // Get expired burger IDs before updating
        val expiredBurgerIds = burgerManager.getExpiredBurgerIds()
        
        chefManager.updateChefs()    // Update chef states, positions, etc.
        burgerManager.updateBurgers()  // Deletes expired burgers and updates burger states, positions, etc.

        // Additional game logic (e.g., collision detection, scoring) goes here

        // Notify about expired burgers
        if (expiredBurgerIds.isNotEmpty()) {
            onBurgersExpiredCallback?.invoke(expiredBurgerIds)
        }
    }

    // Register for burger expiration callbacks
    // Currently, we are using event-based callbacks to remove expired burgers
    // Meaning game engine will call game renderer remove expired burgers method when burgers expire
    // Might want to use a timer-based approach for future extension if required
    fun setOnBurgersExpiredCallback(callback: (List<Int>) -> Unit) {
        onBurgersExpiredCallback = callback
    }

    fun getChefState(chefId: Int): ChefState {
        return chefManager.getChefById(chefId)?.chefState ?: ChefState.IDLE
    }

    // Toggle a specific chef's state (for example, when a user taps on a chef)
    fun toggleChefState(chefId: Int) {
        chefManager.toggleChef(chefId)
    }

    fun spawnBurger(): Int {
        burgerCounter++
        val order = Order(burgerCounter, "burger")
        if (kitchenManager.addOrder(order)) {
            burgerManager.spawnBurger(burgerCounter)
        }
        return burgerCounter
    }

    fun quitGame() {
        // Remove all pending callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null)
        
        // Stop the kitchen manager
        kitchenManager.stop()
        // there's no need to stop burger and chef managers as they do not start any threads
    }
}