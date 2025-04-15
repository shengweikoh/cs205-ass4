package com.example.cs205_ass4.game.kitchen

class KitchenManager {
    private val kitchen = Kitchen()
    private var onOrdersExpiredCallback: ((List<Int>) -> Unit)? = null

    fun addOrder(order: Order): Boolean {
        return kitchen.kitchenCounter.addOrder(order)
    }

    fun getOrders(): List<Order> {
        return kitchen.kitchenCounter.getOrders()
    }

    fun setOnOrdersExpiredCallback(callback: (List<Int>) -> Unit) {
        onOrdersExpiredCallback = callback
        kitchen.kitchenCounter.setOnOrdersExpiredCallback(callback)
    }

    fun stop() {
        kitchen.kitchenCounter.stop()
    }
} 