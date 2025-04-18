package com.example.cs205_ass4.game.kitchen

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.PointF
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout

class GridManager(
        private val activity: Activity,
        private val kitchenCounter: RelativeLayout,
        private val burgerContainer: FrameLayout
) {
    private val gridPositions = mutableListOf<PointF>()
    private val maxGridSlots = KitchenConstants.MAX_ORDERS
    private val gridOccupancy = Array<Int?>(maxGridSlots) { null }

    private var onGridSetupComplete: (() -> Unit)? = null
    private var onBurgerLost: (() -> Unit)? = null

    fun setOnGridSetupCompleteListener(listener: () -> Unit) {
        onGridSetupComplete = listener
    }

    fun setOnBurgerLostListener(listener: () -> Unit) {
        onBurgerLost = listener
    }

    @SuppressLint("DiscouragedApi")
    fun setupGridPositions() {
        gridPositions.clear()
        val tempList = mutableListOf<PointF>()
        var count = 0

        for (i in 0 until maxGridSlots) {
            val slotView =
                    activity.findViewById<View>(
                            activity.resources.getIdentifier("slot_$i", "id", activity.packageName)
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

    fun findFirstEmptyGridIndex(burgerContainer: FrameLayout): Int {
        return gridOccupancy.indexOfFirst { it == null }
    }

    fun assignBurgerToGridSlot(burgerId: Int, gridIndex: Int) {
        if (gridIndex >= 0 && gridIndex < maxGridSlots) {
            gridOccupancy[gridIndex] = burgerId
        }
    }

    fun removeBurgerFromGrid(burgerId: Int) {
        val index = gridOccupancy.indexOfFirst { it == burgerId }
        if (index >= 0) {
            gridOccupancy[index] = null
        }
    }

    fun getPositionForIndex(gridIndex: Int, childCount: Int): PointF? {
        return if (gridIndex == -1) {
            // Notify that a burger was lost due to overflow
            onBurgerLost?.invoke()
            // Return null to indicate that this burger should not be displayed
            null
        } else {
            gridPositions[gridIndex]
        }
    }
}
