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
    var cookedBurgerCounter = 0
    var expiredBurgerCounter = 0

    // Handler for the game loop and delayed tasks.
    private val handler = Handler(Looper.getMainLooper())
    private val GAME_ENGINE_UPDATE_INTERVAL = 100L  // 10 updates per second

    // Callback for when burger views must be removed.
    private var onBurgersExpiredCallback: ((List<Int>) -> Unit)? = null
    // Callback for when a burger has finished cooking.
    private var onBurgerCookedCallback: ((Int) -> Unit)? = null
    // Callback for notifying when a chef's state changes.
    private var onChefStateChangedCallback: ((Int, ChefState) -> Unit)? = null
    // Callback for when the expired burger counter changes.
    private var onBurgerExpiredCountChangedCallback: ((Int) -> Unit)? = null

    init {
        // Spawn initial chefs.
        chefManager.spawnChef(id = 1, x = 100f, y = 200f)
        chefManager.spawnChef(id = 2, x = 300f, y = 200f)
        chefManager.spawnChef(id = 3, x = 100f, y = 400f)
        chefManager.spawnChef(id = 4, x = 300f, y = 400f)
    }

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
            // 1) Increment the expired count by however many expired.
            expiredBurgerCounter += expiredBurgerIds.size
            // 2) Notify the "expired burger count changed" callback, so the UI can update.
            onBurgerExpiredCountChangedCallback?.invoke(expiredBurgerCounter)
            onBurgersExpiredCallback?.invoke(expiredBurgerIds)
        }
    }

    // Register for burger expiration callbacks
    // Currently, we are using event-based callbacks to remove expired burgers
    // Meaning game engine will call game renderer remove expired burgers method when burgers expire
    // Might want to use a timer-based approach for future extension if required
    // Callback setter for expired burger views (UI removal).
    fun setOnBurgersExpiredCallback(callback: (List<Int>) -> Unit) {
        onBurgersExpiredCallback = callback
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

    fun toggleChefState(chefId: Int) {
        chefManager.toggleChef(chefId)
    }

    fun incrementBurgerCooked() {
        cookedBurgerCounter++
        onBurgerCookedCallback?.invoke(cookedBurgerCounter)
    }

    fun spawnBurger(): Int {
        burgerCounter++
        val order = Order(burgerCounter, "burger")
        if (kitchenManager.addOrder(order)) {
            burgerManager.spawnBurger(burgerCounter)
        }
        return burgerCounter
    }

    fun stopGame() {
        kitchenManager.stop()
    }
}