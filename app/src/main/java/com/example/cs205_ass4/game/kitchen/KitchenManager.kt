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

    fun stop() {
        kitchenCounter.stop()
    }

}
