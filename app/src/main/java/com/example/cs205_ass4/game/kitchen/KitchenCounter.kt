package com.example.cs205_ass4.game.kitchen

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import com.example.cs205_ass4.game.interfaces.Expirable
import java.util.concurrent.LinkedBlockingQueue

class KitchenCounter {
    private val allFood = LinkedBlockingQueue<Expirable>()
    private val lock = Any()
    private var isRunning = true

    // Background thread for processing decay
    private lateinit var decayHandlerThread: HandlerThread
    private lateinit var decayHandler: Handler
    private val decayRunnable =
            object : Runnable {
                override fun run() {
                    if (!isRunning) return

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
                        // Use main thread handler to deliver callbacks on UI thread
                        mainHandler.post { onFoodExpiredCallback?.invoke(expiredFood) }
                    }

                    // Notify listeners about decay update
                    if (currentFood.isNotEmpty()) {
                        // Use main thread handler to deliver callbacks on UI thread
                        mainHandler.post { onFoodDecayedCallback?.invoke(currentFood) }
                    }

                    val timeSpentProcessingDecay = System.currentTimeMillis() - cycleStartTime
                    val targetTimeToSpendProcessing =
                            1000 / KitchenConstants.NUM_UPDATE_DECAY_PER_SEC
                    val sleepTime = targetTimeToSpendProcessing - timeSpentProcessingDecay

                    // Schedule next update with appropriate delay
                    if (isRunning) {
                        decayHandler.postDelayed(this, if (sleepTime > 0) sleepTime else 0)
                    }
                }
            }

    // Handler for posting callbacks to main thread
    private val mainHandler = Handler(Looper.getMainLooper())

    // Callback for expired items - will be invoked with list of expired items
    private var onFoodExpiredCallback: ((List<Expirable>) -> Unit)? = null

    // New callback for decay updates - will be invoked with all current food
    private var onFoodDecayedCallback: ((List<Expirable>) -> Unit)? = null

    init {
        startDecayThread()
    }

    private fun startDecayThread() {
        // Create a dedicated background thread with its own looper
        decayHandlerThread = HandlerThread("DecayThread", Process.THREAD_PRIORITY_BACKGROUND)
        decayHandlerThread.start()

        // Create a handler associated with the background thread
        decayHandler = Handler(decayHandlerThread.looper)

        // Start processing decay
        isRunning = true
        decayHandler.post(decayRunnable)
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
        return synchronized(lock) { allFood.remove(food) }
    }

    fun setOnFoodExpiredCallback(callback: (List<Expirable>) -> Unit) {
        onFoodExpiredCallback = callback
    }

    fun setOnFoodDecayedCallback(callback: (List<Expirable>) -> Unit) {
        onFoodDecayedCallback = callback
    }

    fun stop() {
        isRunning = false

        // Remove any pending decay runnables
        decayHandler.removeCallbacks(decayRunnable)

        // Safely quit the handler thread's looper
        decayHandlerThread.quitSafely()

        try {
            // Wait for the thread to finish
            decayHandlerThread.join(1000)
        } catch (e: InterruptedException) {
            // Ignore interrupted exception
        }

        // Clear any resources
        allFood.clear()
    }
}
