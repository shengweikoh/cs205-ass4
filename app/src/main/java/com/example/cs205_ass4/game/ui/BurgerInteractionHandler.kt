package com.example.cs205_ass4.game.ui

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.cs205_ass4.R
import com.example.cs205_ass4.game.GameEngine
import com.example.cs205_ass4.game.burger.BurgerLayeringManager
import com.example.cs205_ass4.game.burger.BurgerRenderer
import com.example.cs205_ass4.game.chef.ChefRenderer
import com.example.cs205_ass4.game.chef.ChefState
import com.example.cs205_ass4.game.kitchen.FridgeGridManager
import com.example.cs205_ass4.game.kitchen.GridManager
import com.example.cs205_ass4.game.kitchen.GrillManager
import com.example.cs205_ass4.utils.SelectionUtils

class BurgerInteractionHandler(
        private val burgerContainer: FrameLayout,
        private val gameEngine: GameEngine,
        private val burgerRenderer: BurgerRenderer,
        private val chefRenderer: ChefRenderer,
        private val grillManager: GrillManager,
        private val burgerLayeringManager: BurgerLayeringManager,
        private val fridge: View,
        private val gridManager: GridManager,
        private val fridgeGridManager: FridgeGridManager
) {
    // Two separate selection managers - one for kitchen grid burgers, one for fridge burgers
    val kitchenSelectionManager: SelectionUtils.SelectionManager<Int> =
            createKitchenSelectionManager()
    val fridgeSelectionManager: SelectionUtils.SelectionManager<Int> =
            createFridgeSelectionManager()

    // Set to track burger IDs that are in the fridge
    private val burgersInFridge = mutableSetOf<Int>()

    private fun createKitchenSelectionManager(): SelectionUtils.SelectionManager<Int> {
        return SelectionUtils.SelectionManager(
                onTargetInteraction = { burgerId, targetView ->
                    if (targetView == fridge) {
                        // Moving a burger from kitchen to fridge
                        handleFridgeInteraction(burgerId)
                    } else {
                        // Moving a burger from kitchen to chef
                        handleChefInteraction(burgerId, targetView)
                    }
                }
        )
    }

    private fun createFridgeSelectionManager(): SelectionUtils.SelectionManager<Int> {
        return SelectionUtils.SelectionManager(
                onTargetInteraction = { burgerId, targetView ->
                    if (targetView != fridge) {
                        // Attempted to move a burger from fridge to chef - not allowed
                        Toast.makeText(
                                        burgerContainer.context,
                                        "This action not allowed as burger is in fridge (disk)",
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                    }
                    // We don't handle moving from fridge to fridge - it's already there
                }
        )
    }

    fun setup() {
        // Register the fridge as an interaction target for BOTH selection managers
        kitchenSelectionManager.registerInteractionTarget(fridge)

        // Register chef targets only for the kitchen selection manager
        chefRenderer.setSelectionManager(kitchenSelectionManager)

        // Set up the burger layering manager listener
        burgerLayeringManager.setLayeringListener(
                object : BurgerLayeringManager.LayeringListener {
                    override fun onLayeringComplete(
                            burgerId: Int,
                            chefId: Int,
                            burgerValue: Int,
                            transferred: Boolean
                    ) {
                        if (transferred) {
                            grillManager.releaseGrillCapacity(burgerValue)
                        }

                        // Remove from fridge tracking if it was there
                        burgersInFridge.remove(burgerId)

                        burgerContainer.findViewWithTag<View>(burgerId)?.let { view ->
                            burgerContainer.removeView(view)
                        }
                    }
                }
        )

        // Set the kitchen selection manager as the default for new burgers
        burgerRenderer.setDefaultSelectionManager(kitchenSelectionManager)
    }

    private fun handleFridgeInteraction(burgerId: Int) {
        val burgerWrapper =
                burgerContainer.findViewWithTag<View>(burgerId) as? RelativeLayout ?: return

        // Remove burger from kitchen grid
        gridManager.removeBurgerFromGrid(burgerId)

        // Check if there's space in the fridge grid
        if (!fridgeGridManager.hasFreeSpace()) {
            // No space in fridge, don't allow interaction
            return
        }

        // Find the first empty grid in the fridge
        val gridIndex = fridgeGridManager.findFirstEmptyGridIndex()
        val gridPosition = fridgeGridManager.getPositionForIndex(gridIndex) ?: return

        // Assign burger to fridge grid
        fridgeGridManager.assignBurgerToGridSlot(burgerId, gridIndex)

        // Add to tracking set of burgers in fridge
        burgersInFridge.add(burgerId)

        // Move burger to the fridge grid position
        val burgerWidth = 150 // might need to adjust further
        val burgerHeight = 100
        burgerRenderer.moveBurgerToPosition(
                burgerWrapper,
                gridPosition.x - burgerWidth / 2,
                gridPosition.y - burgerHeight / 2
        )

        // Switch the burger to use the fridge selection manager
        kitchenSelectionManager.unregisterSelectableItem(burgerWrapper)
        fridgeSelectionManager.registerSelectableItem(burgerWrapper, burgerId)

        // Mark the burger as transferred
        burgerRenderer.markBurgerTransferred(burgerId, true)
    }

    private fun handleChefInteraction(burgerId: Int, targetView: View) {
        // Don't assign if chef is already cooking
        val chefId = (targetView.tag as? Int) ?: 0
        if (gameEngine.getChefState(chefId) != ChefState.IDLE) return

        val burgerWrapper =
                burgerContainer.findViewWithTag<View>(burgerId) as? RelativeLayout ?: return
        if (targetView !is ImageView) return

        // Retrieve the burger's numeric value using its tag
        val burgerValue = burgerWrapper.getTag(R.id.burger_value) as? Int ?: 0

        // Check if we have enough grill capacity
        if (grillManager.canCookBurger(burgerValue)) {
            // Remove burger from grid (it could be in kitchen grid or fridge grid)
            gridManager.removeBurgerFromGrid(burgerId)
            fridgeGridManager.removeBurgerFromGrid(burgerId)

            // Remove from tracking set if it was in fridge
            burgersInFridge.remove(burgerId)

            // Unregister from both selection managers to be safe
            kitchenSelectionManager.unregisterSelectableItem(burgerWrapper)
            fridgeSelectionManager.unregisterSelectableItem(burgerWrapper)

            // Consume grill capacity
            grillManager.consumeGrillCapacity(burgerValue)

            // Mark this burger as transferred
            burgerRenderer.markBurgerTransferred(burgerId, true)

            // Teleport the burger to appear below the selected chef
            teleportBurgerToChef(burgerWrapper, targetView)

            // Update chef state
            gameEngine.setChefState(chefId, ChefState.COOKING)

            // Start the layering process
            burgerLayeringManager.startBurgerLayering(burgerWrapper, chefId)
        }
    }

    fun clearFromFridge(burgerId: Int) {
        burgersInFridge.remove(burgerId)
        fridgeGridManager.removeBurgerFromGrid(burgerId)
    }

    private fun teleportBurgerToChef(burgerWrapper: View, chefView: View) {
        // Get chef's location on the screen
        val chefLocation = IntArray(2)
        chefView.getLocationOnScreen(chefLocation)

        // Get burgerContainer's location on the screen
        val containerLocation = IntArray(2)
        burgerContainer.getLocationOnScreen(containerLocation)

        // Calculate chef's relative position inside burgerContainer
        val relativeChefX = chefLocation[0] - containerLocation[0]
        val relativeChefY = chefLocation[1] - containerLocation[1]

        // Center the burger wrapper horizontally under the chef
        val newX = relativeChefX + chefView.width / 2 - burgerWrapper.width / 2

        // Position the burger wrapper just below the chef with a margin
        val marginBelowChef = 100 // pixels
        val newY = relativeChefY + chefView.height + marginBelowChef

        // Reposition the burger wrapper
        burgerWrapper.x = newX.toFloat()
        burgerWrapper.y = newY.toFloat()
    }

    fun cleanup() {
        kitchenSelectionManager.cleanup()
        fridgeSelectionManager.cleanup()
        burgersInFridge.clear()
    }
}
