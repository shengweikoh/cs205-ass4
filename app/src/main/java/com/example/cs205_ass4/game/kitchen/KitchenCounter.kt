package com.example.cs205_ass4.game.kitchen

import com.example.cs205_ass4.game.interfaces.Expirable
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class KitchenCounter {
    private val allFood = LinkedBlockingQueue<Expirable>()
    private val lock = Any()
    private var isRunning = true
    private lateinit var decayThread: Thread

    // Callback for expired items - will be invoked with list of expired items
    private var onFoodExpiredCallback: ((List<Expirable>) -> Unit)? = null

    // New callback for decay updates - will be invoked with all current food
    private var onFoodDecayedCallback: ((List<Expirable>) -> Unit)? = null

    init {
        // Start decay thread
        decayThread = thread {
            while (isRunning) {
                val cycleStartTime = System.currentTimeMillis()
                val currentFood = mutableListOf<Expirable>()
                val expiredFood = mutableListOf<Expirable>()

                synchronized(lock) {
                    // Update all food freshness
                    allFood.forEach { food ->
                        food.decay()
                        currentFood.add(food)
                    }

                    // Find expired food
                    val expired = allFood.filter { it.isExpired() }
                    if (expired.isNotEmpty()) {
                        expiredFood.addAll(expired)
                        // Remove spoiled food
                        expired.forEach { allFood.remove(it) }
                    }
                }

                // Notify listeners outside the lock to avoid deadlocks
                if (expiredFood.isNotEmpty()) {
                    onFoodExpiredCallback?.invoke(expiredFood)
                }

                // Notify listeners about decay update
                if (currentFood.isNotEmpty()) {
                    onFoodDecayedCallback?.invoke(currentFood)
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

    fun addFood(food: Expirable): Boolean {
        return if (allFood.size < KitchenConstants.MAX_ORDERS) {
            allFood.offer(food)
            true
        } else {
            false
        }
    }

    fun getAllFood(): List<Expirable> {
        return allFood.toList()
    }

    fun removeFood(food: Expirable): Boolean {
        return synchronized(lock) {
            allFood.remove(food)
        }
    }

    fun setOnFoodExpiredCallback(callback: (List<Expirable>) -> Unit) {
        onFoodExpiredCallback = callback
    }

    fun setOnFoodDecayedCallback(callback: (List<Expirable>) -> Unit) {
        onFoodDecayedCallback = callback
    }

    fun stop() {
        isRunning = false

        // Interrupt the thread to wake it from sleep
        decayThread.interrupt()
        // Clear any resources
        allFood.clear()
    }
}