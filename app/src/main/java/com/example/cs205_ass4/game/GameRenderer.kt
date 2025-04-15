package com.example.cs205_ass4.game

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.graphics.PointF
import android.graphics.Rect
import com.example.cs205_ass4.R
import com.example.cs205_ass4.game.kitchen.KitchenConstants
import com.example.cs205_ass4.game.chef.ChefConstants
import com.example.cs205_ass4.game.burger.BurgerConstants
class GameRenderer(private val activity: Activity, private val gameEngine: GameEngine) {
    private lateinit var chefImage: ImageView
    private lateinit var chefImage2: ImageView
    private lateinit var burgerContainer: FrameLayout
    private lateinit var kitchenCounter: RelativeLayout

    // Handler to schedule burger spawns on the main thread.
    private val handler = Handler(Looper.getMainLooper())

    // Grid slots on the kitchen counter
    private val gridPositions = mutableListOf<PointF>()
    private val maxGridSlots = 5

    // Custom touch listener class to avoid duplicate class definitions
    private inner class BurgerTouchListener : View.OnTouchListener {
        var dX = 0f
        var dY = 0f
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    view.x = event.rawX + dX
                    view.y = event.rawY + dY
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    val gridIndex = view.getTag(R.id.grid_index_tag) as? Int
                    if (!isViewOverlapping(view, chefImage) && !isViewOverlapping(view, chefImage2)) {
                        // Snap back to grid slot
                        if (gridIndex != null && gridIndex in gridPositions.indices) {
                            val pos = gridPositions[gridIndex]
                            view.x = pos.x - view.width / 2f
                            view.y = pos.y - view.height / 2f
                        }
                    } else {
                        // Logic to assign burger to chef can go here

                        /*
                        gameEngine.startCookingBurger(chefId = 1, burgerId = burgerId)
                        burgerContainer.removeView(view)
                        updateChefImage(chefImage, 1)
                        */
                    }
                    return true
                }
                else -> return false
            }
        }
    }

    fun setupUI() {
        // Bind chef ImageViews
        chefImage = activity.findViewById(R.id.imageViewChef1)
        chefImage2 = activity.findViewById(R.id.imageViewChef2)

        // Elevation so they appear above most things but below dragged burgers
        chefImage.elevation = ChefConstants.CHEF_ELEVATION
        chefImage2.elevation = ChefConstants.CHEF_ELEVATION

        burgerContainer = activity.findViewById(R.id.burgerContainer)
        kitchenCounter = activity.findViewById(R.id.kitchenCounter)

        // Set up grid positions after layout
        kitchenCounter.post {
            setupGridPositions()
        }

        // Register for burger expiration callbacks
        gameEngine.setOnBurgersExpiredCallback { expiredBurgerIds ->
            removeExpiredBurgerViews(expiredBurgerIds)
        }

    }

    private fun setupGridPositions() {
        gridPositions.clear()
        val tempList = mutableListOf<PointF>()
        var count = 0

        for (i in 0 until maxGridSlots) {
            val slotView = activity.findViewById<View>(
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
                    gameEngine.startGame()
                    scheduleBurgerSpawn()
                }
            }
        }
    }

    private fun removeExpiredBurgerViews(expiredBurgerIds: List<Int>) {
        val viewsToRemove = mutableListOf<View>()
        for (i in 0 until burgerContainer.childCount) {
            val view = burgerContainer.getChildAt(i)
            val burgerId = view.tag as? Int
            if (burgerId != null && expiredBurgerIds.contains(burgerId)) {
                viewsToRemove.add(view)
            }
        }
        viewsToRemove.forEach { view ->
            burgerContainer.removeView(view)
        }
    }

    private fun scheduleBurgerSpawn() {
        handler.postDelayed({
            spawnBurgerView()
            scheduleBurgerSpawn()
        }, 3000)
    }

    private fun spawnBurgerView() {
        val burgerId = gameEngine.spawnBurger()

        // Find free grid index
        val gridIndex = findFirstEmptyGridIndex()
        val gridPosition: PointF = if (gridIndex == -1) {
            // Overflow: stack horizontally along the bottom
            val overflowIndex = burgerContainer.childCount - maxGridSlots
            val overflowX = 20f + (overflowIndex * 160f)  // Spacing out each burger
            val overflowY = burgerContainer.height - 150f - 20f  // Fixed bottom Y
            PointF(overflowX, overflowY)
        } else {
            gridPositions[gridIndex]
        }

        val burgerWrapper = RelativeLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(150, 150)
            tag = burgerId
            if (gridIndex != -1) {
                setTag(R.id.grid_index_tag, gridIndex)
            }
            elevation = BurgerConstants.BURGER_ELEVATION // make sure it's above chefs
        }

        val burgerView = ImageView(activity).apply {
            setImageResource(R.drawable.burger_order)
            layoutParams = RelativeLayout.LayoutParams(100, 100).apply {
                addRule(RelativeLayout.CENTER_HORIZONTAL)
            }
        }

        val decayBar = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 100
            layoutParams = RelativeLayout.LayoutParams(100, 20).apply {
                addRule(RelativeLayout.BELOW, burgerView.id)
                addRule(RelativeLayout.CENTER_HORIZONTAL)
                topMargin = 5
            }
        }

        burgerWrapper.addView(burgerView)
        burgerWrapper.addView(decayBar)

        val burgerWidth = 150
        val burgerHeight = 150

        burgerWrapper.x = gridPosition.x - burgerWidth / 2
        burgerWrapper.y = gridPosition.y - burgerHeight / 2

        burgerWrapper.setOnTouchListener(BurgerTouchListener())
        burgerContainer.addView(burgerWrapper)

        startDecayAnimation(decayBar, burgerId)
    }

    private fun findFirstEmptyGridIndex(): Int {
        val usedIndices = mutableSetOf<Int>()
        for (i in 0 until burgerContainer.childCount) {
            val view = burgerContainer.getChildAt(i)
            val idx = view.getTag(R.id.grid_index_tag) as? Int
            if (idx != null) usedIndices.add(idx)
        }
        return (0 until maxGridSlots).firstOrNull { it !in usedIndices } ?: -1
    }

    private fun isViewOverlapping(view1: View, view2: View): Boolean {
        val rect1 = Rect()
        val rect2 = Rect()
        view1.getHitRect(rect1)
        view2.getHitRect(rect2)
        return Rect.intersects(rect1, rect2)
    }

    private fun startDecayAnimation(decayBar: ProgressBar, burgerId: Int) {
        val decayHandler = Handler(Looper.getMainLooper())
        val decayRunnable = object : Runnable {
            override fun run() {
                val order = gameEngine.kitchenManager.getOrders().find { it.id == burgerId }
                if (order != null) {
                    val progress = (order.decay * 100).toInt()
                    decayBar.progress = progress
                    decayBar.progressDrawable.setTint(when {
                        order.decay > 0.7f -> android.graphics.Color.GREEN
                        order.decay > 0.3f -> android.graphics.Color.parseColor("#FFA500")
                        else -> android.graphics.Color.RED
                    })
                    decayHandler.postDelayed(this, 16)
                }
            }
        }
        decayHandler.post(decayRunnable)
    }

    fun cleanup() {
        gameEngine.stopGame()
    }
}

//    // Drag listener on the chef image.
//    private val chefDragListener = View.OnDragListener { _, event ->
//        when (event.action) {
//            DragEvent.ACTION_DRAG_STARTED -> true
//            DragEvent.ACTION_DROP -> {
//                // When a burger is dropped on the chef, retrieve the burger view.
//                val draggedView = event.localState as? View
//                draggedView?.let { view ->
//                    val burgerId = view.tag as? Int
//                    if (burgerId != null) {
//                        // Start cooking the burger with chef id = 1 (adjust as needed).
//                        gameEngine.startCookingBurger(chefId = 1, burgerId = burgerId)
//                        // Remove the burger view from the container.
//                        burgerContainer.removeView(view)
//                        // Update the chef image to reflect the cooking state.
//                        updateChefImage(chefImage, 1)
//                    }
//                }
//                true
//            }
//            else -> true
//        }
//    }
//
//    // Updates the chef's ImageView based on its state.
//    private fun updateChefImage(imageView: ImageView, chefId: Int) {
//        val state = gameEngine.getChefState(chefId)
//        val drawableRes = if (state == ChefState.COOKING) {
//            R.drawable.chef_cooking
//        } else {
//            R.drawable.chef_idle
//        }
//        imageView.setImageResource(drawableRes)
//    }
//}