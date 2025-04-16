package com.example.cs205_ass4.game.kitchen

import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread
import kotlin.math.max

data class Order(
        val id: Int,
        val foodType: String,
        var decay: Float = 1.0f, // 1.0 = fresh, 0.0 = spoiled
        val maxDecayTime: Long = KitchenConstants.MAX_DECAY_TIME
)

class KitchenCounter(
        private val maxOrders: Int = KitchenConstants.MAX_ORDERS,
        private val decayRate: Float = KitchenConstants.DECAY_RATE
) {
    private val orders = LinkedBlockingQueue<Order>()
    private val lock = Any()
    private var isRunning = true
    private var onOrdersExpiredCallback: ((List<Int>) -> Unit)? = null
    private lateinit var decayThread: Thread

    init {
        // Start decay thread
        decayThread = thread {
            val targetDelta = (1000_000_000 / 60).toLong() // 60 FPS in nanoseconds

            while (isRunning) {
                val currentTime = System.nanoTime()

                synchronized(lock) {
                    val expiredOrders = mutableListOf<Int>()
                    orders.forEach { order ->
                        order.decay = max(0f, order.decay - decayRate)
                        if (order.decay <= 0f) {
                            expiredOrders.add(order.id)
                        }
                    }
                    // Remove spoiled orders
                    orders.removeIf { it.decay <= 0f }
                    // Notify about expired orders
                    if (expiredOrders.isNotEmpty()) {
                        onOrdersExpiredCallback?.invoke(expiredOrders)
                    }
                }

                val updateDuration = System.nanoTime() - currentTime
                val sleepTime =
                        (targetDelta - updateDuration) / 1_000_000 // Convert to milliseconds

                if (sleepTime > 0) {
                    Thread.sleep(sleepTime)
                }
            }
        }
    }

    fun addOrder(order: Order): Boolean {
        return if (orders.size < maxOrders) {
            orders.offer(order)
            true
        } else {
            false
        }
    }

    fun getOrders(): List<Order> {
        return orders.toList()
    }

    fun setOnOrdersExpiredCallback(callback: (List<Int>) -> Unit) {
        onOrdersExpiredCallback = callback
    }

    fun stop() {
        isRunning = false

        // Interrupt the thread to wake it from sleep
        decayThread.interrupt()

        // Clear any resources
        orders.clear()
        onOrdersExpiredCallback = null
    }
}
