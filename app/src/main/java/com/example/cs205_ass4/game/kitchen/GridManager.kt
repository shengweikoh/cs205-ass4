package com.example.cs205_ass4.game.kitchen

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.PointF
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.example.cs205_ass4.R

class GridManager(
        private val activity: Activity,
        private val kitchenCounter: RelativeLayout,
        private val burgerContainer: FrameLayout
) {
    private val gridPositions = mutableListOf<PointF>()
    private val maxGridSlots = KitchenConstants.MAX_ORDERS

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
        val usedIndices = mutableSetOf<Int>()
        for (i in 0 until burgerContainer.childCount) {
            val view = burgerContainer.getChildAt(i)
            val idx = view.getTag(R.id.grid_index_tag) as? Int
            if (idx != null) usedIndices.add(idx)
        }
        return (0 until maxGridSlots).firstOrNull { it !in usedIndices } ?: -1
    }

    fun getPositionForIndex(gridIndex: Int, childCount: Int): PointF {
        return if (gridIndex == -1) {
            // Overflow: stack horizontally along the bottom
            val overflowIndex = childCount - maxGridSlots
            val overflowX = 20f + (overflowIndex * 160f)
            val overflowY = burgerContainer.height - 150f - 20f
            PointF(overflowX, overflowY)
        } else {
            gridPositions[gridIndex]
        }
    }
}
