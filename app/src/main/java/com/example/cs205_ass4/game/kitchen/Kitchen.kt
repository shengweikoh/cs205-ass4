package com.example.cs205_ass4.game.kitchen

data class Kitchen(
    val kitchenCounter: KitchenCounter = KitchenCounter()
) {
    /**
     * Remove the order with the given ID from the counter,
     * so it won’t ever fire an “expired” callback after cooking.
     */
    fun removeOrder(orderId: Int): Boolean {
        return kitchenCounter.removeOrder(orderId)
    }
}

