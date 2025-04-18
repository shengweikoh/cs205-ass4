package com.example.cs205_ass4.game.burger

import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.RelativeLayout
import com.example.cs205_ass4.R
import com.example.cs205_ass4.game.GameEngine
import com.example.cs205_ass4.game.chef.ChefState
import com.example.cs205_ass4.game.kitchen.GridManager

class BurgerLayeringManager(
        private val gameEngine: GameEngine,
        private var gridManager: GridManager? = null
) {

    interface LayeringListener {
        fun onLayeringComplete(burgerId: Int, chefId: Int, burgerValue: Int, transferred: Boolean)
    }

    private var layeringListener: LayeringListener? = null

    fun setGridManager(manager: GridManager) {
        this.gridManager = manager
    }

    fun setLayeringListener(listener: LayeringListener) {
        layeringListener = listener
    }

    fun startBurgerLayering(burgerWrapper: RelativeLayout, chefId: Int) {
        // Retrieve the burger image from the burgerWrapper
        val burgerView = burgerWrapper.getChildAt(0) as? ImageView ?: return

        // Get the burger ID
        val burgerId = burgerWrapper.tag as? Int ?: return

        // Define the layering sequence
        val layeringSequence =
                listOf(
                        R.drawable.burger_order,
                        R.drawable.burger_bottom,
                        R.drawable.burger_tomato,
                        R.drawable.burger_patty,
                        R.drawable.burger_lettuce,
                        R.drawable.burger_top
                )

        // Start from index 1 (burger_bottom), because burger_order is already displayed
        var currentStep = 1
        val layeringHandler = Handler(Looper.getMainLooper())
        val layeringRunnable =
                object : Runnable {
                    override fun run() {
                        if (currentStep < layeringSequence.size) {
                            // Update the burger image to the next layer
                            burgerView.setImageResource(layeringSequence[currentStep])
                            currentStep++
                            // Schedule next update after 1 second
                            layeringHandler.postDelayed(this, 1000)
                        } else {
                            // Layering complete (burger_top reached); wait a moment then remove the
                            // burger
                            layeringHandler.postDelayed(
                                    {
                                        // Reset chef to idle
                                        gameEngine.setChefState(chefId, ChefState.IDLE)

                                        // Before removal, check if this burger was transferred
                                        val transferred =
                                                burgerWrapper.getTag(R.id.burger_transferred) as?
                                                        Boolean
                                                        ?: false
                                        val burgerValue =
                                                burgerWrapper.getTag(R.id.burger_value) as? Int ?: 0

                                        // Make sure to remove from grid if still there
                                        gridManager?.removeBurgerFromGrid(burgerId)

                                        // Notify the layering listener
                                        layeringListener?.onLayeringComplete(
                                                burgerId,
                                                chefId,
                                                burgerValue,
                                                transferred
                                        )

                                        // Notify the game engine
                                        gameEngine.kitchenManager.removeOrder(burgerId)
                                        gameEngine.incrementBurgerCooked()
                                    },
                                    100
                            )
                        }
                    }
                }
        // Kick off the layering after a 1-second delay
        layeringHandler.postDelayed(layeringRunnable, 1000)
    }
}

// Added ChefState enum here since it was referenced but may not have been imported
enum class ChefState {
    IDLE,
    COOKING
}
