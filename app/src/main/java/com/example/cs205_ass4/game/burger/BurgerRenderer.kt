package com.example.cs205_ass4.game.burger

import android.app.Activity
import android.graphics.Color
import android.graphics.PointF
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.example.cs205_ass4.R
import com.example.cs205_ass4.game.kitchen.FridgeGridManager
import com.example.cs205_ass4.game.kitchen.GridManager
import com.example.cs205_ass4.game.ui.BurgerInteractionHandler
import com.example.cs205_ass4.utils.SelectionUtils

class BurgerRenderer(
        private val activity: Activity,
        private val burgerContainer: FrameLayout,
        private var gridManager: GridManager? = null,
        private var fridgeGridManager: FridgeGridManager? = null
) {
    // Map of burger id to progress bar
    private val mapProgressBar = mutableMapOf<Int, ProgressBar>()

    // Callback to handle burger selection
    private var onBurgerSelected: ((Int, Int) -> Unit)? = null

    // Reference to the interaction handler
    private var interactionHandler: BurgerInteractionHandler? = null

    // Default selection manager for new burgers
    private var defaultSelectionManager: SelectionUtils.SelectionManager<Int>? = null

    fun setInteractionHandler(handler: BurgerInteractionHandler) {
        this.interactionHandler = handler
    }

    fun setGridManager(manager: GridManager) {
        this.gridManager = manager
    }

    fun setFridgeGridManager(manager: FridgeGridManager) {
        this.fridgeGridManager = manager
    }

    fun setSelectionManager(manager: SelectionUtils.SelectionManager<Int>) {
        this.defaultSelectionManager = manager
    }

    fun setDefaultSelectionManager(manager: SelectionUtils.SelectionManager<Int>) {
        this.defaultSelectionManager = manager
    }

    fun setOnBurgerSelectedListener(listener: (burgerId: Int, burgerValue: Int) -> Unit) {
        this.onBurgerSelected = listener
    }

    fun spawnBurgerView(burgerId: Int, burgerValue: Int, gridPosition: PointF, gridIndex: Int) {
        // Increase the container height to provide space for the burger number
        val burgerWrapper =
                RelativeLayout(activity).apply {
                    layoutParams = FrameLayout.LayoutParams(150, 180)
                    tag = burgerId
                    // Save the burger's numeric value for later interactions
                    setTag(R.id.burger_value, burgerValue)
                    elevation = BurgerConstants.BURGER_ELEVATION
                }

        val burgerView =
                ImageView(activity).apply {
                    id = View.generateViewId()
                    setImageResource(R.drawable.burger_order)
                    layoutParams =
                            RelativeLayout.LayoutParams(100, 100).apply {
                                addRule(RelativeLayout.CENTER_HORIZONTAL)
                            }
                }

        val decayBar =
                ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
                    id = View.generateViewId() // unique ID
                    max = 100
                    progress = 100
                    layoutParams =
                            RelativeLayout.LayoutParams(100, 20).apply {
                                // Pin it to the top, center it horizontally
                                addRule(RelativeLayout.ALIGN_PARENT_TOP)
                                addRule(RelativeLayout.CENTER_HORIZONTAL)
                                topMargin = 5
                            }
                }
        mapProgressBar[burgerId] = decayBar

        val burgerValueTextView =
                TextView(activity).apply {
                    id = View.generateViewId()
                    text = burgerValue.toString()
                    textSize = 15f
                    setTextColor(Color.BLACK)
                    layoutParams =
                            RelativeLayout.LayoutParams(
                                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                                            RelativeLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    .apply {
                                        // Pin it BELOW the burger image
                                        addRule(RelativeLayout.BELOW, burgerView.id)
                                        addRule(RelativeLayout.CENTER_HORIZONTAL)
                                        topMargin = 6 // space between image & text
                                    }
                }

        burgerWrapper.addView(burgerView)
        burgerWrapper.addView(burgerValueTextView)
        burgerWrapper.addView(decayBar)

        val burgerWidth = 150
        val burgerHeight = 150
        burgerWrapper.x = gridPosition.x - burgerWidth / 2
        burgerWrapper.y = gridPosition.y - burgerHeight / 2

        // Register this burger with the default selection manager for interaction
        defaultSelectionManager?.registerSelectableItem(burgerWrapper, burgerId)
        burgerContainer.addView(burgerWrapper)
    }

    fun updateBurgerFreshness(freshnessByBurgerId: Map<Int, Float>) {
        for ((burgerId, freshness) in freshnessByBurgerId) {
            val decayBar = mapProgressBar[burgerId] ?: continue
            val progress = (freshness * 100).toInt()
            decayBar.progress = progress
            decayBar.progressDrawable?.setTint(
                    when {
                        freshness > 0.7f -> Color.GREEN
                        freshness > 0.3f -> "#FFA500".toColorInt()
                        else -> Color.RED
                    }
            )
        }
    }

    fun removeExpiredBurgerViews(
            expiredBurgerIds: List<Int>,
            onRemoveBurger: (Int, Boolean) -> Unit
    ) {
        val viewsToRemove = mutableListOf<View>()
        for (i in 0 until burgerContainer.childCount) {
            val view = burgerContainer.getChildAt(i)
            val burgerId = view.tag as? Int ?: continue

            if (expiredBurgerIds.contains(burgerId)) {
                // Remove from both grids if it's still there
                gridManager?.removeBurgerFromGrid(burgerId)
                fridgeGridManager?.removeBurgerFromGrid(burgerId)

                // Also notify the interaction handler to clear from fridge tracking
                interactionHandler?.clearFromFridge(burgerId)

                val transferred = view.getTag(R.id.burger_transferred) as? Boolean ?: false
                val burgerValue = view.getTag(R.id.burger_value) as? Int ?: 0

                onRemoveBurger(burgerValue, transferred)

                if (transferred) {
                    view.setTag(R.id.burger_transferred, false)
                }
                viewsToRemove.add(view)
                mapProgressBar.remove(burgerId)
            }
        }
        viewsToRemove.forEach { view -> burgerContainer.removeView(view) }
    }

    fun moveBurgerToPosition(burgerView: View, x: Float, y: Float) {
        burgerView.x = x
        burgerView.y = y
    }

    fun markBurgerTransferred(burgerId: Int, transferred: Boolean) {
        val burgerView = burgerContainer.findViewWithTag<View>(burgerId)
        burgerView?.setTag(R.id.burger_transferred, transferred)
    }

}
