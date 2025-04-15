package com.example.cs205_ass4.game

import android.os.Handler
import android.os.Looper
import com.example.cs205_ass4.game.chef.ChefManager
import com.example.cs205_ass4.game.burger.BurgerManager
import com.example.cs205_ass4.game.chef.ChefState

class GameEngine {

    // Managers for game entities.
    val chefManager = ChefManager()
    val burgerManager = BurgerManager()

    var burgerCounter = 0
    // Counter for burgers that have finished cooking.
    var cookedBurgerCounter = 0
    // Counter for burgers that have expired.
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

    fun startGame() {
        scheduleUpdate()
    }

    private fun scheduleUpdate() {
        handler.postDelayed({
            updateGame()
            scheduleUpdate()  // Schedule the next update
        }, GAME_ENGINE_UPDATE_INTERVAL)
    }

    fun updateGame() {
        // Get expired burger IDs.
        val expiredBurgerIds = burgerManager.getExpiredBurgerIds()

        // Update game entities.
        chefManager.updateChefs()
        burgerManager.updateBurgers()

        // Additional game logic can be added here.

        // If there are expired burgers, update counter and notify.
        if (expiredBurgerIds.isNotEmpty()) {
            expiredBurgerCounter += expiredBurgerIds.size
            onBurgerExpiredCountChangedCallback?.invoke(expiredBurgerCounter)
            // Notify to remove expired burger views.
            onBurgersExpiredCallback?.invoke(expiredBurgerIds)
        }
    }

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

    /**
     * Starts cooking a burger by assigning it to a chef and setting the chef state to COOKING.
     * The chef remains in the COOKING state until the cooking process completes (i.e. after layering).
     */
    fun startCookingBurger(chefId: Int, burgerId: Int) {
        // Check if the chef is available (IDLE) before starting to cook.
        val chef = chefManager.getChefById(chefId)
        if (chef == null || chef.chefState != ChefState.IDLE) {
            // The chef is already busy or doesn't exist.
            return
        }

        // Assign the burger to the chef.
        chefManager.assignBurgerToChef(chefId, burgerId)

        // Mark the burger as cooking so it won’t expire.
        burgerManager.markBurgerAsCooking(burgerId)

        // Update the UI to reflect that the chef is now cooking.
        onChefStateChangedCallback?.invoke(chefId, ChefState.COOKING)

        // Note: Cooking completion will now be triggered externally via the UI
        // (after the layering animation finishes).
    }

    /**
     * Called when the burger's layering animation has completed.
     * This method marks the cooking as complete by updating the chef's state,
     * incrementing the cooked burger counter, and triggering the corresponding callbacks.
     */
    fun completeBurgerCooking(chefId: Int) {
        chefManager.finishCooking(chefId)
        cookedBurgerCounter++
        onBurgerCookedCallback?.invoke(cookedBurgerCounter)
        onChefStateChangedCallback?.invoke(chefId, ChefState.IDLE)
    }

    fun spawnBurger(): Int {
        burgerCounter++
        burgerManager.spawnBurger(burgerCounter, 0f, 0f)
        return burgerCounter
    }
}