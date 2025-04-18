package com.example.cs205_ass4.game.ui

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
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
    val selectionManager: SelectionUtils.SelectionManager<Int> = createSelectionManager()

    private fun createSelectionManager(): SelectionUtils.SelectionManager<Int> {
        return SelectionUtils.SelectionManager(
                onTargetInteraction = { burgerId, targetView ->
                    if (targetView == fridge) {
                        handleFridgeInteraction(burgerId)
                    } else {
                        handleChefInteraction(burgerId, targetView)
                    }
                }
        )
    }

    fun setup() {
        // Register the fridge as an interaction target
        selectionManager.registerInteractionTarget(fridge)

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
                        burgerContainer.findViewWithTag<View>(burgerId)?.let { view ->
                            burgerContainer.removeView(view)
                        }
                    }
                }
        )

        // Pass the selection manager to the burger renderer and chef renderer
        burgerRenderer.setSelectionManager(selectionManager)
        chefRenderer.setSelectionManager(selectionManager)
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
        
        // Move burger to the fridge grid position
        val burgerWidth = 100  // Adjust as needed based on your burger view size
        val burgerHeight = 100
        burgerRenderer.moveBurgerToPosition(
            burgerWrapper, 
            gridPosition.x - burgerWidth / 2, 
            gridPosition.y - burgerHeight / 2
        )
        
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
        selectionManager.cleanup()
    }
}
