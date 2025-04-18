package com.example.cs205_ass4.game.kitchen

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.PointF
import android.view.View
import android.widget.FrameLayout

class FridgeGridManager(
        private val activity: Activity,
        private val fridgeContainer: View,
        private val burgerContainer: FrameLayout
) {
    private val gridPositions = mutableListOf<PointF>()
    private val maxGridSlots = 20 // 20 slots in the fridge
    // Array to track which burger ID is in which grid position
    private val gridOccupancy = Array<Int?>(maxGridSlots) { null }

    private var onGridSetupComplete: (() -> Unit)? = null

    fun setOnGridSetupCompleteListener(listener: () -> Unit) {
        onGridSetupComplete = listener
    }

    @SuppressLint("DiscouragedApi")
    fun setupGridPositions() {
        gridPositions.clear()
        val tempList = mutableListOf<PointF>()
        var count = 0

        for (i in 0 until maxGridSlots) {
            val slotView =
                    activity.findViewById<View>(
                            activity.resources.getIdentifier(
                                    "fridge_slot_$i",
                                    "id",
                                    activity.packageName
                            )
                    )

            slotView.post {
                val slotLocation = IntArray(2)
                val containerLocation = IntArray(2)

                slotView.getLocationOnScreen(slotLocation)
                burgerContainer.getLocationOnScreen(containerLocation)

                val adjustedX = slotLocation[0] - containerLocation[0] + slotView.width / 2f
                val adjustedY = slotLocation[1] - containerLocation[1] + slotView.height / 2f

                tempList.add(PointF(adjustedX, adjustedY))
                count++

                if (count == maxGridSlots) {
                    gridPositions.addAll(tempList.sortedBy { it.x })
                    onGridSetupComplete?.invoke()
                }
            }
        }
    }

    fun findFirstEmptyGridIndex(): Int {
        // Find the first empty slot in the gridOccupancy array
        return gridOccupancy.indexOfFirst { it == null }
    }

    fun assignBurgerToGridSlot(burgerId: Int, gridIndex: Int) {
        // Store the burger ID in the grid slot
        if (gridIndex >= 0 && gridIndex < maxGridSlots) {
            gridOccupancy[gridIndex] = burgerId
        }
    }

    fun removeBurgerFromGrid(burgerId: Int) {
        // Find and clear the grid slot containing this burger ID
        val index = gridOccupancy.indexOfFirst { it == burgerId }
        if (index >= 0) {
            gridOccupancy[index] = null
        }
    }

    fun getPositionForIndex(gridIndex: Int): PointF? {
        return if (gridIndex == -1) {
            // No available slots
            null
        } else {
            gridPositions[gridIndex]
        }
    }

    fun hasFreeSpace(): Boolean {
        return gridOccupancy.any { it == null }
    }
}
