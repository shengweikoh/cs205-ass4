package com.example.cs205_ass4.game

import android.app.Activity
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.cs205_ass4.R
import com.example.cs205_ass4.game.burger.BurgerConstants
import com.example.cs205_ass4.game.chef.ChefConstants
import com.example.cs205_ass4.game.chef.ChefState
import com.example.cs205_ass4.utils.SelectionUtils
import com.example.cs205_ass4.game.kitchen.KitchenConstants

class GameRenderer(private val activity: Activity, private val gameEngine: GameEngine) {
    private lateinit var chefImageList: List<ImageView>
    private lateinit var burgerCounterTextView: TextView
    private lateinit var burgerExpiredTextView: TextView
    private lateinit var burgerContainer: FrameLayout
    private lateinit var kitchenCounter: RelativeLayout
    // Map of burger id to progress bar
    private val mapProgressBar = mutableMapOf<Int, ProgressBar>()
    // Handler to schedule burger spawns on the main thread.
    private val handler = Handler(Looper.getMainLooper())
    // Grid slots on the kitchen counter
    private val gridPositions = mutableListOf<PointF>()
    private val maxGridSlots = KitchenConstants.MAX_ORDERS

    // Selection manager for burger-chef interactions
    private val selectBurgerToChef =
            SelectionUtils.SelectionManager<Int>(
                    // there's no onItemSelected callback as we don't need to do anything when a
                    // burger is selected

                    onTargetInteraction = { burgerId, targetView ->

                        // TODO: To Change @ShengWei/LeeMin
                        // Logic to assign burger to chef goes here

                        // For now, default behaviour, remove the burger view
                        val viewToRemove = burgerContainer.findViewWithTag<View>(burgerId)
                        burgerContainer.removeView(viewToRemove)
                        gameEngine.burgerManager.removeBurger(burgerId)
                    }
            )

    private val decayHandler = Handler(Looper.getMainLooper())
    private val decayRunnable =
            object : Runnable {
                override fun run() {
                    updateDecay()
                    decayHandler.postDelayed(this, 100) // 10 times per second
                }
            }

    fun setupUI() {
        // Bind chef ImageViews
        val chef1 = activity.findViewById<ImageView>(R.id.imageViewChef1)
        val chef2 = activity.findViewById<ImageView>(R.id.imageViewChef2)
        val chef3 = activity.findViewById<ImageView>(R.id.imageViewChef3)
        val chef4 = activity.findViewById<ImageView>(R.id.imageViewChef4)
        chef1.tag = 1
        chef2.tag = 2
        chef3.tag = 3
        chef4.tag = 4
        // Elevation so they appear above most things but below dragged burgers
        chef1.elevation = ChefConstants.CHEF_ELEVATION
        chef2.elevation = ChefConstants.CHEF_ELEVATION
        chef3.elevation = ChefConstants.CHEF_ELEVATION
        chef4.elevation = ChefConstants.CHEF_ELEVATION
        chefImageList = listOf(chef1, chef2, chef3, chef4)

        burgerContainer = activity.findViewById(R.id.burgerContainer)
        kitchenCounter = activity.findViewById(R.id.kitchenCounter)

        // Bind the counter TextViews.
        burgerCounterTextView = activity.findViewById(R.id.textViewBurgerCounter)
        burgerExpiredTextView = activity.findViewById(R.id.textViewBurgerExpired)

        // Set up grid positions after layout
        kitchenCounter.post { setupGridPositions() }

        // Set callback to update the "cooked" counter.
        gameEngine.setOnBurgerCookedCallback { count ->
            activity.runOnUiThread { burgerCounterTextView.text = "Burgers Cooked: $count" }
        }

        // Set callback to update the "expired" counter.
        gameEngine.setOnBurgerExpiredCountChangedCallback { count ->
            activity.runOnUiThread { burgerExpiredTextView.text = "Burgers Expired: $count" }
        }

        // Set callback to update chef image when state changes.
        gameEngine.setOnChefStateChangedCallback { chefId, state ->
            activity.runOnUiThread {
                val chefView = chefImageList.find { (it.tag as? Int) == chefId }
                chefView?.let { updateChefImage(it, chefId) }
            }
        }

        // Register for burger expiration callbacks
        gameEngine.setOnBurgersExpiredCallback { expiredBurgerIds ->
            removeExpiredBurgerViews(expiredBurgerIds)
        }

        // Register chefs as interaction targets
        selectBurgerToChef.registerInteractionTarget(chef1)
        selectBurgerToChef.registerInteractionTarget(chef2)
        selectBurgerToChef.registerInteractionTarget(chef3)
        selectBurgerToChef.registerInteractionTarget(chef4)

        // Start decay updates
        decayHandler.post(decayRunnable)
    }

    private fun updateDecay() {
        for (burgerId in mapProgressBar.keys) {
            val decayBar = mapProgressBar[burgerId]
            val burger = gameEngine.burgerManager.getBurgerById(burgerId)
             println("Burger ID: $burgerId, Freshness Percentage: ${burger?.freshnessPercentage}")
            burger ?: continue // Use continue instead of return to process all burgers
            val freshnessPercentage = burger.freshnessPercentage
            val progress = (freshnessPercentage * 100).toInt()
            decayBar?.progress = progress
            decayBar?.progressDrawable?.setTint(
                    when {
                        freshnessPercentage > 0.7f -> android.graphics.Color.GREEN
                        freshnessPercentage > 0.3f -> android.graphics.Color.parseColor("#FFA500")
                        else -> android.graphics.Color.RED
                    }
            )
        }
    }

    // Add this method to stop updates when no longer needed
    fun stopDecayUpdates() {
        decayHandler.removeCallbacks(decayRunnable)
    }

    private fun setupGridPositions() {
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
        viewsToRemove.forEach { view -> burgerContainer.removeView(view) }
    }

    private fun scheduleBurgerSpawn() {
        handler.postDelayed(
                {
                    spawnBurgerView()
                    scheduleBurgerSpawn()
                },
                3000
        )
    }

    private fun spawnBurgerView() {
        val burgerId = gameEngine.spawnBurger()

        // Find free grid index
        val gridIndex = findFirstEmptyGridIndex()
        val gridPosition: PointF =
                if (gridIndex == -1) {
                    // Overflow: stack horizontally along the bottom
                    val overflowIndex = burgerContainer.childCount - maxGridSlots
                    val overflowX = 20f + (overflowIndex * 160f) // Spacing out each burger
                    val overflowY = burgerContainer.height - 150f - 20f // Fixed bottom Y
                    PointF(overflowX, overflowY)
                } else {
                    gridPositions[gridIndex]
                }

        val burgerWrapper =
                RelativeLayout(activity).apply {
                    layoutParams = FrameLayout.LayoutParams(150, 150)
                    tag = burgerId
                    if (gridIndex != -1) {
                        setTag(R.id.grid_index_tag, gridIndex)
                    }
                    elevation = BurgerConstants.BURGER_ELEVATION // make sure it's above chefs
                }

        val burgerView =
                ImageView(activity).apply {
                    setImageResource(R.drawable.burger_order)
                    layoutParams =
                            RelativeLayout.LayoutParams(100, 100).apply {
                                addRule(RelativeLayout.CENTER_HORIZONTAL)
                            }
                }

        val decayBar =
                ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
                    max = 100
                    progress = 100
                    layoutParams =
                            RelativeLayout.LayoutParams(100, 20).apply {
                                addRule(RelativeLayout.BELOW, burgerView.id)
                                addRule(RelativeLayout.CENTER_HORIZONTAL)
                                topMargin = 5
                            }
                }

        mapProgressBar[burgerId] = decayBar

        burgerWrapper.addView(burgerView)
        burgerWrapper.addView(decayBar)

        val burgerWidth = 150
        val burgerHeight = 150

        burgerWrapper.x = gridPosition.x - burgerWidth / 2
        burgerWrapper.y = gridPosition.y - burgerHeight / 2

        // Register this burger with the selection manager
        selectBurgerToChef.registerSelectableItem(burgerWrapper, burgerId)

        burgerContainer.addView(burgerWrapper)
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

    private fun updateChefImage(chefImageView: ImageView, chefId: Int) {
        val state = gameEngine.getChefState(chefId)
        if (state == ChefState.COOKING) {
            Glide.with(chefImageView.context)
                    .asGif()
                    .load(R.drawable.chef_cooking)
                    .into(chefImageView)
        } else {
            chefImageView.setImageResource(R.drawable.chef_idle)
        }
    }

    fun cleanup() {
        // Cancel all burger spawn callbacks
        handler.removeCallbacksAndMessages(null)
        stopDecayUpdates()
        selectBurgerToChef.cleanup()
        gameEngine.quitGame()
    }
}
