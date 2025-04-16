package com.example.cs205_ass4.game.kitchen

import com.example.cs205_ass4.game.interfaces.Expirable
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class KitchenCounter() {
    private val allFood = LinkedBlockingQueue<Expirable>()
    private val lock = Any()
    private var isRunning = true
    private lateinit var decayThread: Thread
  
    init {
        // Start decay thread
        decayThread = thread {
            while (isRunning) {
                val cycleStartTime = System.currentTimeMillis()

                synchronized(lock) {
                    allFood.forEach { food -> food.decay() }
                    val expiredFood = allFood.filter { it.isExpired()  }
                    // Remove spoiled food
                    expiredFood.forEach { allFood.remove(it) }
                }

                val timeSpentProcessingDecay = System.currentTimeMillis() - cycleStartTime
                val targetTimeToSpendProcessing = 1000 / KitchenConstants.NUM_UPDATE_DECAY_PER_SEC
                val sleepTime = targetTimeToSpendProcessing - timeSpentProcessingDecay
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime)
                }
            }
        }
    }

    fun addFood(order: Expirable): Boolean {
        return if (allFood.size < KitchenConstants.MAX_ORDERS) {
            allFood.offer(order)
            true
        } else {
            false
        }
    }

    fun getAllFood(): List<Expirable> {
        return allFood.toList()
    }

    fun stop() {
        isRunning = false

        // Interrupt the thread to wake it from sleep
        decayThread.interrupt()
        // Clear any resources
        allFood.clear()
    }
}
