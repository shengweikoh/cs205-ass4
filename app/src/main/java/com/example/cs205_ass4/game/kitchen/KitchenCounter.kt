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
            while (isRunning) {
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
                Thread.sleep(16) // ~60fps update rate
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

    fun takeOrder(): Order? {
        return orders.poll()
    }

    fun getOrders(): List<Order> {
        return orders.toList()
    }

    fun getExpiredOrderIds(): List<Int> {
        return synchronized(lock) {
            orders.filter { it.decay <= 0f }.map { it.id }
        }
    }

    fun setOnOrdersExpiredCallback(callback: (List<Int>) -> Unit) {
        onOrdersExpiredCallback = callback
    }

    fun stop() {
        isRunning = false
    }

    fun draw(drawScope: DrawScope, position: Offset, size: Size) {
        val maxBufferSpaces = maxOrders
        val orderWidth = size.width / maxOrders
        val orderHeight = size.height

        // Draw grid cells: show the individual cell boundaries on the kitchen counter.
        for (cell in 0 until maxBufferSpaces) {
            val cellX = position.x + cell * orderWidth
            // Draw a gray rectangle as the cell boundary.
            drawScope.drawRect(
                color = Color.Gray,
                topLeft = Offset(cellX, position.y),
                size = Size(orderWidth, orderHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f) // stroke to outline the cell
            )
        }
        
        synchronized(lock) {
            orders.forEachIndexed { index, order ->
                val x = position.x + (index * orderWidth)
                
                // Draw order background
                drawScope.drawRect(
                    color = Color.LightGray,
                    topLeft = Offset(x, position.y),
                    size = Size(orderWidth, orderHeight)
                )
                
                // Draw decay indicator (Green -> Orange -> Red)
                val decayColor = when {
                    order.decay > 0.7f -> Color.Green
                    order.decay > 0.3f -> Color(0xFFFFA500) // Orange
                    else -> Color.Red
                }
                
                drawScope.drawRect(
                    color = decayColor,
                    topLeft = Offset(x, position.y + orderHeight * 0.8f),
                    size = Size(orderWidth * order.decay, orderHeight * 0.2f)
                )
                
                // Draw food icon (simplified as a colored circle)
                drawScope.drawCircle(
                    color = Color(0xFF8B4513), // Brown color for burger
                    radius = min(orderWidth, orderHeight) * 0.3f,
                    center = Offset(x + orderWidth/2, position.y + orderHeight/2)
                )
            }
        }
    }
} 