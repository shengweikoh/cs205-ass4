package com.example.cs205_ass4.game.ui

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.cs205_ass4.R
import com.example.cs205_ass4.game.GameEngine
import com.example.cs205_ass4.game.burger.BurgerLayeringManager
import com.example.cs205_ass4.game.burger.BurgerRenderer
import com.example.cs205_ass4.game.burger.BurgerSpawner
import com.example.cs205_ass4.game.chef.ChefRenderer
import com.example.cs205_ass4.game.kitchen.GridManager
import com.example.cs205_ass4.game.kitchen.GrillManager

class GameRendererRefactored(private val activity: Activity, private val gameEngine: GameEngine) {
    // UI Elements
    private lateinit var burgerCounterTextView: TextView
    private lateinit var burgerExpiredTextView: TextView
    private lateinit var burgerContainer: FrameLayout
    private lateinit var kitchenCounter: RelativeLayout
    private lateinit var fridge: View
    private lateinit var grillCapacityTextView: TextView

    // Managers and Renderers
    private lateinit var burgerRenderer: BurgerRenderer
    private lateinit var chefRenderer: ChefRenderer
    private lateinit var gridManager: GridManager
    private lateinit var grillManager: GrillManager
    private lateinit var burgerSpawner: BurgerSpawner
    private lateinit var burgerLayeringManager: BurgerLayeringManager
    private lateinit var burgerInteractionHandler: BurgerInteractionHandler

    // Decay handler
    private val decayHandler = Handler(Looper.getMainLooper())
    private val decayRunnable =
            object : Runnable {
                override fun run() {
                    updateDecay()
                    decayHandler.postDelayed(this, 100) // 10 times per second
                }
            }

    fun setupUI() {
        // Initialize UI elements
        burgerContainer = activity.findViewById(R.id.burgerContainer)
        kitchenCounter = activity.findViewById(R.id.kitchenCounter)
        burgerCounterTextView = activity.findViewById(R.id.textViewBurgerCounter)
        burgerExpiredTextView = activity.findViewById(R.id.textViewBurgerExpired)
        fridge = activity.findViewById(R.id.fridge)
        grillCapacityTextView = activity.findViewById(R.id.textViewGrillCapacity)

        // Initialize components
        burgerRenderer = BurgerRenderer(activity, burgerContainer)
        chefRenderer = ChefRenderer(activity)
        gridManager = GridManager(activity, kitchenCounter, burgerContainer)
        grillManager = GrillManager(grillCapacityTextView)
        burgerLayeringManager = BurgerLayeringManager(gameEngine)

        // Initialize interaction handler
        burgerInteractionHandler =
                BurgerInteractionHandler(
                        burgerContainer,
                        gameEngine,
                        burgerRenderer,
                        chefRenderer,
                        grillManager,
                        burgerLayeringManager,
                        fridge
                )

        // Setup the interaction handler
        burgerInteractionHandler.setup()

        // Initialize burger spawner
        burgerSpawner = BurgerSpawner(gameEngine, burgerRenderer, gridManager, burgerContainer)

        // Setup UI components
        setupCallbacks()

        // Setup chefs
        chefRenderer.setupChefs()

        // Setup grid positions
        gridManager.setOnGridSetupCompleteListener {
            gameEngine.startGame()
            burgerSpawner.startSpawning()
        }
        gridManager.setupGridPositions()

        // Start decay updates
        decayHandler.post(decayRunnable)
    }

    private fun setupCallbacks() {
        // Setup callback to update the "cooked" counter
        gameEngine.setOnBurgerCookedCallback { count ->
            activity.runOnUiThread {
                "Burgers Cooked: $count".also { burgerCounterTextView.text = it }
            }
        }

        // Setup callback to update the "expired" counter
        gameEngine.setOnBurgerExpiredCountChangedCallback { count ->
            activity.runOnUiThread {
                "Burgers Expired: $count".also { burgerExpiredTextView.text = it }
            }
        }

        // Setup callback to update the decay progress bars
        gameEngine.setOnBurgerFreshnessUpdatedCallback { freshnessByBurgerId ->
            activity.runOnUiThread { burgerRenderer.updateBurgerFreshness(freshnessByBurgerId) }
        }

        // Setup callback to update chef image when state changes
        gameEngine.setOnChefStateChangedCallback { chefId, state ->
            activity.runOnUiThread { chefRenderer.updateChefImage(chefId, state) }
        }

        // Setup callback for burger expiration
        gameEngine.setOnBurgersExpiredCallback { expiredBurgerIds ->
            activity.runOnUiThread {
                burgerRenderer.removeExpiredBurgerViews(expiredBurgerIds) { burgerValue, transferred
                    ->
                    if (transferred) {
                        grillManager.releaseGrillCapacity(burgerValue)
                    }
                }
            }
        }
    }

    private fun updateDecay() {
        // Get freshness percentages from game engine
        val burgerFreshness = mutableMapOf<Int, Float>()
        for (burgerId in gameEngine.burgerManager.getAllBurgerIds()) {
            val burger = gameEngine.burgerManager.getBurgerById(burgerId)
            burger?.let { burgerFreshness[burgerId] = it.freshnessPercentage }
        }

        // Update UI
        burgerRenderer.updateDecay(burgerFreshness)
    }

    fun stopDecayUpdates() {
        decayHandler.removeCallbacks(decayRunnable)
    }

    fun cleanup() {
        burgerSpawner.stopSpawning()
        stopDecayUpdates()
        burgerInteractionHandler.cleanup()
        gameEngine.quitGame()
    }
}
