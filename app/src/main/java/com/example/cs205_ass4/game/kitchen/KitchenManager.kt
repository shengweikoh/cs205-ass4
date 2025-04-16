package com.example.cs205_ass4.game.kitchen

import com.example.cs205_ass4.game.interfaces.Expirable

class KitchenManager {
    private val kitchenCounter = KitchenCounter()


    fun addFood(order: Expirable): Boolean {
        return kitchenCounter.addFood(order)
    }

    fun getAllFood(): List<Expirable> {
        return kitchenCounter.getAllFood()
    }

    fun stop() {
        kitchenCounter.stop()
    }

}
