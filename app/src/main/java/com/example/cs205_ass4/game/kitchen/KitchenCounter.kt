package com.example.cs205_ass4.game.kitchen

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.min

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

    init {
        // Start decay thread
        thread {
            var lastUpdateTime = System.nanoTime()
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
                val sleepTime = (targetDelta - updateDuration) / 1_000_000 // Convert to milliseconds
                
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime)
                }
                
                lastUpdateTime = currentTime
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
    }

    fun draw(drawScope: DrawScope, position: Offset, size: Size) {
        val orderWidth = size.width / maxOrders
        val orderHeight = size.height

        orders.forEachIndexed { index, order ->
            val x = position.x + (index * orderWidth)
            

            // Draw decay indicator (Green -> Orange -> Red)
            val decayColor = when {
                order.decay > 0.7f -> Color.Green
                order.decay > 0.3f -> Color(0xFFFFA500) // Orange
                // TODO: this line is causing some issues where new orders are red
                else -> Color.Red
            }

            // Draw decay bar using decay colour
            drawScope.drawRect(
                color = decayColor,
                topLeft = Offset(x, position.y + orderHeight * 0.8f),
                size = Size(orderWidth * order.decay, orderHeight * 0.2f)
            )
        }
    }
} 