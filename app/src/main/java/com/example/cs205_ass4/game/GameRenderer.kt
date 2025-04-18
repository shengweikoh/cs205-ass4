package com.example.cs205_ass4.game

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.cs205_ass4.R
import com.example.cs205_ass4.game.burger.BurgerLayeringManager
import com.example.cs205_ass4.game.burger.BurgerRenderer
import com.example.cs205_ass4.game.burger.BurgerSpawner
import com.example.cs205_ass4.game.chef.ChefRenderer
import com.example.cs205_ass4.game.kitchen.FridgeGridManager
import com.example.cs205_ass4.game.kitchen.GridManager
import com.example.cs205_ass4.game.kitchen.GrillManager
import com.example.cs205_ass4.game.ui.BurgerInteractionHandler

class GameRenderer(private val activity: Activity, private val gameEngine: GameEngine) {

    // UI Elements
    private lateinit var burgerCounterTextView: TextView
    private lateinit var burgerExpiredTextView: TextView
    private lateinit var burgerLostTextView: TextView
    private lateinit var burgerContainer: FrameLayout
    private lateinit var kitchenCounter: RelativeLayout
    private lateinit var fridge: View
    private lateinit var fridgeContainer: RelativeLayout
    private lateinit var grillCapacityTextView: TextView

    // Managers and Renderers
    private lateinit var burgerRenderer: BurgerRenderer
    private lateinit var chefRenderer: ChefRenderer
    private lateinit var gridManager: GridManager
    private lateinit var fridgeGridManager: FridgeGridManager
    private lateinit var grillManager: GrillManager
    private lateinit var burgerSpawner: BurgerSpawner
    private lateinit var burgerLayeringManager: BurgerLayeringManager
    private lateinit var burgerInteractionHandler: BurgerInteractionHandler

    fun setupUI() {
        // Initialize UI elements
        burgerContainer = activity.findViewById(R.id.burgerContainer)
        kitchenCounter = activity.findViewById(R.id.kitchenCounter)
        burgerCounterTextView = activity.findViewById(R.id.textViewBurgerCounter)
        burgerExpiredTextView = activity.findViewById(R.id.textViewBurgerExpired)
        burgerLostTextView = activity.findViewById(R.id.textViewBurgersLost)
        fridge = activity.findViewById(R.id.fridge)
        fridgeContainer = activity.findViewById(R.id.fridgeContainer)
        grillCapacityTextView = activity.findViewById(R.id.textViewGrillCapacity)

        // Initialize components
        burgerRenderer = BurgerRenderer(activity, burgerContainer)
        chefRenderer = ChefRenderer(activity)
        gridManager = GridManager(activity, kitchenCounter, burgerContainer)
        fridgeGridManager = FridgeGridManager(activity, fridgeContainer, burgerContainer)
        grillManager = GrillManager(grillCapacityTextView)
        burgerLayeringManager = BurgerLayeringManager(gameEngine)

        // Set the GridManager on BurgerRenderer and BurgerLayeringManager
        burgerRenderer.setGridManager(gridManager)
        burgerRenderer.setFridgeGridManager(fridgeGridManager)
        burgerLayeringManager.setGridManager(gridManager)

        // Initialize interaction handler
        burgerInteractionHandler =
                BurgerInteractionHandler(
                        burgerContainer,
                        gameEngine,
                        burgerRenderer,
                        chefRenderer,
                        grillManager,
                        burgerLayeringManager,
                        fridge,
                        gridManager,
                        fridgeGridManager,
                        kitchenCounter
                )

        // Setup the interaction handler
        burgerInteractionHandler.setup()

        // Set the BurgerInteractionHandler on the BurgerRenderer
        burgerRenderer.setInteractionHandler(burgerInteractionHandler)

        // Initialize burger spawner
        burgerSpawner = BurgerSpawner(gameEngine, burgerRenderer, gridManager, burgerContainer)

        // Setup UI components
        setupCallbacks()

        // Setup chefs
        chefRenderer.setupChefs()

        // Setup grid positions
        gridManager.setOnGridSetupCompleteListener {
            // Also set up the fridge grid positions
            fridgeGridManager.setupGridPositions()

//            gameEngine.startGame()
            burgerSpawner.startSpawning()
        }
        gridManager.setupGridPositions()
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

        // Setup callback to update the "lost" counter
        gameEngine.setOnBurgerLostCallback { count ->
            activity.runOnUiThread { "Burgers Lost: $count".also { burgerLostTextView.text = it } }
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

    fun stopDecayUpdates() {
        // This method is kept for backward compatibility but doesn't do anything now
        // Decay updates are managed by KitchenCounter
    }

    fun cleanup() {
        burgerSpawner.stopSpawning()
        // Removed stopDecayUpdates call
        burgerInteractionHandler.cleanup()
        gameEngine.quitGame()
    }
}
