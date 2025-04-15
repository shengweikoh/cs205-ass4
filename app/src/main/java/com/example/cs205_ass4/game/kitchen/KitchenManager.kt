package com.example.cs205_ass4.game.kitchen

class KitchenManager {
    private val kitchen = Kitchen()
    private var onOrdersExpiredCallback: ((List<Int>) -> Unit)? = null

    fun getKitchen(): Kitchen {
        return kitchen
    }

    fun addOrder(order: Order): Boolean {
        return kitchen.kitchenCounter.addOrder(order)
    }

    fun takeOrder(): Order? {
        return kitchen.kitchenCounter.takeOrder()
    }

    fun getOrders(): List<Order> {
        return kitchen.kitchenCounter.getOrders()
    }

    fun getExpiredOrderIds(): List<Int> {
        return kitchen.kitchenCounter.getExpiredOrderIds()
    }

    fun setOnOrdersExpiredCallback(callback: (List<Int>) -> Unit) {
        onOrdersExpiredCallback = callback
        kitchen.kitchenCounter.setOnOrdersExpiredCallback(callback)
    }

    fun stop() {
        kitchen.kitchenCounter.stop()
    }
} 