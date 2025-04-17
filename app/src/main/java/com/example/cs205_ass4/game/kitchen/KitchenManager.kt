package com.example.cs205_ass4.game.kitchen

import com.example.cs205_ass4.game.interfaces.Expirable
import com.example.cs205_ass4.game.burger.Burger

class KitchenManager {
    private val kitchenCounter = KitchenCounter()

    // Callbacks for expiry and decay events
    private var onBurgerExpiredCallback: ((List<Int>) -> Unit)? = null
    private var onBurgerDecayedCallback: ((List<Burger>) -> Unit)? = null

    init {
        // Set up callbacks
        kitchenCounter.setOnFoodExpiredCallback { expiredFood ->
            // Filter to get just burgers and extract their IDs
            val expiredBurgers = expiredFood.filterIsInstance<Burger>()
            val expiredBurgerIds = expiredBurgers.map { it.id }

            // Notify listeners
            onBurgerExpiredCallback?.invoke(expiredBurgerIds)
        }

        kitchenCounter.setOnFoodDecayedCallback { updatedFood ->
            // Filter to get just burgers
            val decayedBurgers = updatedFood.filterIsInstance<Burger>()

            // Notify listeners
            onBurgerDecayedCallback?.invoke(decayedBurgers)
        }
    }

    fun addFood(food: Expirable): Boolean {
        return kitchenCounter.addFood(food)
    }

    fun getAllFood(): List<Expirable> {
        return kitchenCounter.getAllFood()
    }

    fun getBurgers(): List<Burger> {
        return kitchenCounter.getAllFood().filterIsInstance<Burger>()
    }

    fun removeFood(food: Expirable): Boolean {
        return kitchenCounter.removeFood(food)
    }

    fun setOnBurgerExpiredCallback(callback: (List<Int>) -> Unit) {
        onBurgerExpiredCallback = callback
    }

    fun setOnBurgerDecayedCallback(callback: (List<Burger>) -> Unit) {
        onBurgerDecayedCallback = callback
    }

    fun stop() {
        kitchenCounter.stop()
    }

    fun removeOrder(burgerId: Int): Boolean {
        val target = kitchenCounter.getAllFood()
            .filterIsInstance<Burger>()
            .find { it.id == burgerId }
        return target?.let { kitchenCounter.removeFood(it) } ?: false
    }

}

//    fun getOrders(): List<Order> {
//        return kitchen.kitchenCounter.getOrders()
//    }

//    // in KitchenManager.kt, below getOrders()
//    fun removeOrder(orderId: Int): Boolean {
//        return kitchen.removeOrder(orderId)
//    }

//    fun setOnOrdersExpiredCallback(callback: (List<Int>) -> Unit) {
//        onOrdersExpiredCallback = callback
//        kitchen.kitchenCounter.setOnOrdersExpiredCallback(callback)
//    }
