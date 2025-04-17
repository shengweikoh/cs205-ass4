package com.example.cs205_ass4.game

import android.os.Handler
import android.os.Looper
import com.example.cs205_ass4.game.burger.Burger
import com.example.cs205_ass4.game.burger.BurgerManager
import com.example.cs205_ass4.game.chef.ChefManager
import com.example.cs205_ass4.game.chef.ChefState
import com.example.cs205_ass4.game.kitchen.KitchenManager

class GameEngine {

    // Managers for different game entities
    val chefManager = ChefManager()
    val burgerManager = BurgerManager()
    val kitchenManager = KitchenManager()

    var burgerCounter = 0
    var cookedBurgerCounter = 0
    var expiredBurgerCounter = 0

    // Handler for the game loop and delayed tasks.
    private val handler = Handler(Looper.getMainLooper())
    private val GAME_ENGINE_UPDATE_INTERVAL = 100L // 10 updates per second

    // Callback for when burger views must be removed.
    private var onBurgersExpiredCallback: ((List<Int>) -> Unit)? = null
    // Callback for when burger freshness is updated
    private var onBurgerFreshnessUpdatedCallback: ((Map<Int, Float>) -> Unit)? = null
    // Callback for when a burger has finished cooking.
    private var onBurgerCookedCallback: ((Int) -> Unit)? = null
    // Callback for notifying when a chef's state changes.
    private var onChefStateChangedCallback: ((Int, ChefState) -> Unit)? = null
    // Callback for when the expired burger counter changes.
    private var onBurgerExpiredCountChangedCallback: ((Int) -> Unit)? = null

    init {
        // Spawn initial chefs.
        chefManager.spawnChef(id = 1)
        chefManager.spawnChef(id = 2)
        chefManager.spawnChef(id = 3)
        chefManager.spawnChef(id = 4)

        // Set up kitchen manager callbacks
        kitchenManager.setOnBurgerExpiredCallback { expiredBurgerIds ->
            // When burgers expire, remove them from BurgerManager
            expiredBurgerIds.forEach { burgerId ->
                burgerManager.removeBurger(burgerId)
            }

            // Update counters
            expiredBurgerCounter += expiredBurgerIds.size

            // Notify listeners
            onBurgerExpiredCountChangedCallback?.invoke(expiredBurgerCounter)
            onBurgersExpiredCallback?.invoke(expiredBurgerIds)
        }

        kitchenManager.setOnBurgerDecayedCallback { decayedBurgers ->
            // Update UI with current freshness values
            onBurgerFreshnessUpdatedCallback?.invoke(
                decayedBurgers.associate { it.id to it.freshnessPercentage }
            )
        }
    }

    // Called to start game loops, timers, etc.
    fun startGame() {
        // Start the game loop
        scheduleUpdate()
    }

    private fun scheduleUpdate() {
        handler.postDelayed(
            {
                updateGame()
                scheduleUpdate() // Schedule the next update
            },
            GAME_ENGINE_UPDATE_INTERVAL
        )
    }

    // Called on each game tick/update
    fun updateGame() {
        chefManager.updateChefs() // Update chef states, positions, etc.

        // Additional game logic (e.g., collision detection, scoring) goes here
    }

    // Register for burger expiration callbacks
    fun setOnBurgersExpiredCallback(callback: (List<Int>) -> Unit) {
        onBurgersExpiredCallback = callback
    }

    // New method for updating burger freshness UI
    fun setOnBurgerFreshnessUpdatedCallback(callback: (Map<Int, Float>) -> Unit) {
        onBurgerFreshnessUpdatedCallback = callback
    }

    // Callback setter for cooked burger count.
    fun setOnBurgerCookedCallback(callback: (Int) -> Unit) {
        onBurgerCookedCallback = callback
    }

    // Callback setter for chef state changes.
    fun setOnChefStateChangedCallback(callback: (Int, ChefState) -> Unit) {
        onChefStateChangedCallback = callback
    }

    // Callback setter for expired burger count changes.
    fun setOnBurgerExpiredCountChangedCallback(callback: (Int) -> Unit) {
        onBurgerExpiredCountChangedCallback = callback
    }

    fun getChefState(chefId: Int): ChefState {
        return chefManager.getChefById(chefId)?.chefState ?: ChefState.IDLE
    }

    fun setChefState(chefId: Int, chefState: ChefState) {
        chefManager.toggleChef(chefId, chefState)
        onChefStateChangedCallback?.invoke(chefId, chefState)
    }

    fun incrementBurgerCooked() {
        cookedBurgerCounter++
        onBurgerCookedCallback?.invoke(cookedBurgerCounter)
    }

    fun spawnBurger(): Int {
        burgerCounter++
        val burger = Burger(burgerCounter)
        if (kitchenManager.addFood(burger)) {
            burgerManager.spawnBurger(burgerCounter, burger)
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