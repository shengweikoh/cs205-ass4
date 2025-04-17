package com.example.cs205_ass4.game.burger

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import com.example.cs205_ass4.game.GameEngine
import com.example.cs205_ass4.game.kitchen.GridManager

class BurgerSpawner(
        private val gameEngine: GameEngine,
        private val burgerRenderer: BurgerRenderer,
        private val gridManager: GridManager,
        private val burgerContainer: FrameLayout
) {
    // Handler to schedule burger spawns on the main thread
    private val handler = Handler(Looper.getMainLooper())

    fun startSpawning() {
        // Set up the burger loss listener
        gridManager.setOnBurgerLostListener { gameEngine.incrementBurgerLost() }

        scheduleBurgerSpawn()
    }

    private fun scheduleBurgerSpawn() {
        handler.postDelayed(
                {
                    spawnBurger()
                    scheduleBurgerSpawn()
                },
                3000
        )
    }

    private fun spawnBurger() {
        // Ask the game engine to create a new burger entity
        val burgerId = gameEngine.spawnBurger()

        // Generate a random burger value between 1 and 5
        val burgerValue = (1..5).random()

        // Find an empty grid slot to place the burger
        val gridIndex = gridManager.findFirstEmptyGridIndex(burgerContainer)

        // Get the position for this grid index
        val gridPosition: PointF? =
                gridManager.getPositionForIndex(gridIndex, burgerContainer.childCount)

        // Create the burger UI only if we have a valid position
        if (gridPosition != null) {
            burgerRenderer.spawnBurgerView(burgerId, burgerValue, gridPosition, gridIndex)
        } else {
            // If no position is available, the burger is lost but the callback
            // in GridManager will already have triggered
        }
    }

    fun stopSpawning() {
        handler.removeCallbacksAndMessages(null)
    }
}
