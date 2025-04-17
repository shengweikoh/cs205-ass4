package com.example.cs205_ass4.game

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
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
import androidx.core.graphics.toColorInt

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

    private val decayHandler = Handler(Looper.getMainLooper())
    private val decayRunnable =
            object : Runnable {
                override fun run() {
                    updateDecay()
                    decayHandler.postDelayed(this, 100) // 10 times per second
                }
            }

    private var grillCount = 20
    private lateinit var grillCapacityTextView: TextView

    private fun teleportBurgerToChef(burgerWrapper: View, chefView: View) {
        // Get chef's location on the screen.
        val chefLocation = IntArray(2)
        chefView.getLocationOnScreen(chefLocation)
        // Get burgerContainer's location on the screen.
        val containerLocation = IntArray(2)
        burgerContainer.getLocationOnScreen(containerLocation)

        // Calculate chef's relative position inside burgerContainer.
        val relativeChefX = chefLocation[0] - containerLocation[0]
        val relativeChefY = chefLocation[1] - containerLocation[1]

        // Center the burger container horizontally under the chef.
        val newX = relativeChefX + chefView.width / 2 - burgerWrapper.width / 2
        // Position the burger container just below the chef with an optional margin.
        val marginBelowChef = 100  // pixels (adjust as needed)
        val newY = relativeChefY + chefView.height + marginBelowChef

        // Reposition the burger container.
        burgerWrapper.x = newX.toFloat()
        burgerWrapper.y = newY.toFloat()
    }


    // Selection manager for burger-chef interactions
    private val selectBurgerToChef = SelectionUtils.SelectionManager<Int>(
        // there's no onItemSelected callback as we don't need to do anything when a burger is selected

        onTargetInteraction = { burgerId, targetView ->

            // TODO: To Change @ShengWei/LeeMin
            // Logic to assign burger to chef goes here
            // Get the burger container using its tag.
            // don’t assign if chef is already cooking
            val chefId = (targetView.tag as? Int) ?: 0
            if (gameEngine.getChefState(chefId) != ChefState.IDLE) return@SelectionManager

            val burgerWrapper = burgerContainer.findViewWithTag<View>(burgerId) as? RelativeLayout
            if (burgerWrapper != null && targetView is ImageView) {
                // Retrieve the burger's numeric value using its tag.
                val burgerValue = burgerWrapper.getTag(R.id.burger_value) as? Int ?: 0


                // Check if deducting this burger's value would cause the grill capacity to go negative.
                if (grillCount - burgerValue >= 0) {
                    // Deduct the burger's value from the grill capacity
                    grillCount -= burgerValue
                    "Capacity: $grillCount".also { grillCapacityTextView.text = it }

                    // Mark this burger as having been transferred to a chef.
                    burgerWrapper.setTag(R.id.burger_transferred, true)

                    // Teleport the burger container so that it appears below the selected chef.
                    teleportBurgerToChef(burgerWrapper, targetView)
                    gameEngine.setChefState(chefId, ChefState.COOKING)
                    // Start the layering process.
                    startBurgerLayering(burgerWrapper, chefId)
                }
            }
        }
    )

    private fun startBurgerLayering(burgerWrapper: RelativeLayout, chefId: Int) {
        // Retrieve the burger image from the burgerWrapper.
        // We assume it's the second child added (index 1).
        val burgerView = burgerWrapper.getChildAt(0) as? ImageView ?: return

        // Define the layering sequence.
        // Note: burger_bottom is initially set in spawnBurgerView, so we start layering from bottom.
        val layeringSequence = listOf(
            R.drawable.burger_order,
            R.drawable.burger_bottom,
            R.drawable.burger_tomato,
            R.drawable.burger_patty,
            R.drawable.burger_lettuce,
            R.drawable.burger_top
        )

        // Start from index 1 (burger_bottom), because burger_order is already displayed.
        var currentStep = 1
        val layeringHandler = Handler(Looper.getMainLooper())
        val layeringRunnable = object : Runnable {
            override fun run() {
                if (currentStep < layeringSequence.size) {
                    // Update the burger image to the next layer.
                    burgerView.setImageResource(layeringSequence[currentStep])
                    currentStep++
                    // Schedule next update after 1 second.
                    layeringHandler.postDelayed(this, 1000)
                } else {
                    // Layering complete (burger_top reached); wait a moment then remove the burger.
                    layeringHandler.postDelayed({
                        // reset chef to idle
                        gameEngine.setChefState(chefId, ChefState.IDLE)
                        // Before removal, check if this burger was transferred.
                        val transferred = burgerWrapper.getTag(R.id.burger_transferred) as? Boolean ?: false
                        val burgerValue = burgerWrapper.getTag(R.id.burger_value) as? Int ?: 0
                        if (transferred && burgerWrapper.parent != null) {
                            grillCount = (grillCount + burgerValue)
                            "Capacity: $grillCount".also { grillCapacityTextView.text = it }
                            burgerWrapper.setTag(R.id.burger_transferred, false)
                        }

                        burgerContainer.removeView(burgerWrapper)
                        // Notify the game engine or update your cooked count.
                        // For example, if your gameEngine has a method:
                        // — FIX A: take the order out of the decay tracker —
                        val burgerId = burgerWrapper.tag as? Int ?: return@postDelayed
                        gameEngine.kitchenManager.removeOrder(burgerId)
                        gameEngine.incrementBurgerCooked()
                        // Alternatively, if you keep a local cooked count variable:
                        // cookedCount++ and then update burgerCounterTextView.text = "Burgers Cooked: $cookedCount"
                    }, 100)
                }
            }
        }
        // Kick off the layering after a 1.5-second delay.
        layeringHandler.postDelayed(layeringRunnable, 1000)
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

        grillCapacityTextView = activity.findViewById(R.id.textViewGrillCapacity)
        "Capacity: $grillCount".also { grillCapacityTextView.text = it }

        burgerContainer = activity.findViewById(R.id.burgerContainer)
        kitchenCounter = activity.findViewById(R.id.kitchenCounter)

        // Bind the counter TextViews.
        burgerCounterTextView = activity.findViewById(R.id.textViewBurgerCounter)
        burgerExpiredTextView = activity.findViewById(R.id.textViewBurgerExpired)

        // Set up grid positions after layout
        kitchenCounter.post { setupGridPositions() }

        // Set callback to update the "cooked" counter.
        gameEngine.setOnBurgerCookedCallback { count ->
            activity.runOnUiThread { "Burgers Cooked: $count".also { burgerCounterTextView.text = it } }
        }

        // Set callback to update the "expired" counter.
        gameEngine.setOnBurgerExpiredCountChangedCallback { count ->
            activity.runOnUiThread { "Burgers Expired: $count".also { burgerExpiredTextView.text = it } }
        }

        // Set callback to update the decay progress bars
        gameEngine.setOnBurgerFreshnessUpdatedCallback { freshnessByBurgerId ->
            activity.runOnUiThread {
                updateBurgerFreshness(freshnessByBurgerId)
            }
        }

        // Set callback to update chef image when state changes.
        gameEngine.setOnChefStateChangedCallback { chefId, _ ->
            activity.runOnUiThread {
                val chefView = chefImageList.find { (it.tag as? Int) == chefId }
                chefView?.let { updateChefImage(it, chefId) }
            }
        }

        // Register for burger expiration callbacks
        gameEngine.setOnBurgersExpiredCallback { expiredBurgerIds ->
            activity.runOnUiThread {
                removeExpiredBurgerViews(expiredBurgerIds)
            }
        }


        // Register chefs as interaction targets
        selectBurgerToChef.registerInteractionTarget(chef1)
        selectBurgerToChef.registerInteractionTarget(chef2)
        selectBurgerToChef.registerInteractionTarget(chef3)
        selectBurgerToChef.registerInteractionTarget(chef4)

        // Start decay updates
        decayHandler.post(decayRunnable)
    }

    // New method to update burger freshness UI
    private fun updateBurgerFreshness(freshnessByBurgerId: Map<Int, Float>) {
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

    private fun updateDecay() {
        try {
            for (burgerId in mapProgressBar.keys.toList()) {
                val burger = gameEngine.burgerManager.getBurgerById(burgerId)
                if (burger == null) {
                    mapProgressBar.remove(burgerId)
                    continue
                }

                val decayBar = mapProgressBar[burgerId] ?: continue
                val freshness = burger.freshnessPercentage
                decayBar.progress = (freshness * 100).toInt()
                decayBar.progressDrawable?.setTint(
                    when {
                        freshness > 0.7f -> Color.GREEN
                        freshness > 0.3f -> "#FFA500".toColorInt()
                        else -> Color.RED
                    }
                )
            }
        } catch (e: Exception) {
            println("💥 Crash caught in updateDecay: ${e.message}")
            e.printStackTrace()
        }
    }

    // Add this method to stop updates when no longer needed
    fun stopDecayUpdates() {
//        decayHandler.removeCallbacks(decayRunnable)
    }

    @SuppressLint("DiscouragedApi")
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
            val burgerId = view.tag as? Int ?: continue

            if (expiredBurgerIds.contains(burgerId)) {
                val transferred = view.getTag(R.id.burger_transferred) as? Boolean ?: false
                if (transferred) {
                    // 1) pull the value back out
                    val burgerValue = view.getTag(R.id.burger_value) as? Int ?: 0
                    // 2) clamp against MAX_GRILL_CAPACITY, not grillCount
                    grillCount = (grillCount + burgerValue)
                    "Capacity: $grillCount".also { grillCapacityTextView.text = it }
                    // 3) clear the flag so we can’
                    view.setTag(R.id.burger_transferred, false)
                }

                mapProgressBar.remove(burgerId)

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

        // Generate a random burger value between 1 and 5
        val burgerValue = (1..5).random()

        // Find free grid index (existing code)
        val gridIndex = findFirstEmptyGridIndex()
        val gridPosition: PointF = if (gridIndex == -1) {
            // Overflow: stack horizontally along the bottom
            val overflowIndex = burgerContainer.childCount - maxGridSlots
            val overflowX = 20f + (overflowIndex * 160f)
            val overflowY = burgerContainer.height - 150f - 20f
            PointF(overflowX, overflowY)
        } else {
            gridPositions[gridIndex]
        }

        // Increase the container height to provide space for the burger number.
        val burgerWrapper = RelativeLayout(activity).apply {
            // Changed height from 150 to 180 to accommodate the burger number below the image.
            layoutParams = FrameLayout.LayoutParams(150, 180)
            tag = burgerId
            if (gridIndex != -1) {
                setTag(R.id.grid_index_tag, gridIndex)
            }
            // Save the burger's numeric value for later interactions.
            setTag(R.id.burger_value, burgerValue)
            elevation = BurgerConstants.BURGER_ELEVATION
        }

        val burgerView = ImageView(activity).apply {
            id = View.generateViewId()
            setImageResource(R.drawable.burger_order)
            layoutParams = RelativeLayout.LayoutParams(100, 100).apply {
                addRule(RelativeLayout.CENTER_HORIZONTAL)
            }
        }

        val decayBar = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
            id = View.generateViewId()          // unique ID
            max = 100
            progress = 100
            layoutParams = RelativeLayout.LayoutParams(100, 20).apply {
                // Pin it to the top, center it horizontally
                addRule(RelativeLayout.ALIGN_PARENT_TOP)
                addRule(RelativeLayout.CENTER_HORIZONTAL)
                topMargin = 5
            }
        }
        mapProgressBar[burgerId] = decayBar

        val burgerValueTextView = TextView(activity).apply {
            id = View.generateViewId()
            text = burgerValue.toString()
            textSize = 15f
            setTextColor(Color.BLACK)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // Pin it BELOW the burger image
                addRule(RelativeLayout.BELOW, burgerView.id)
                addRule(RelativeLayout.CENTER_HORIZONTAL)
                topMargin = 6  // space between image & text
            }
        }

        burgerWrapper.addView(burgerView)
        burgerWrapper.addView(burgerValueTextView)
        burgerWrapper.addView(decayBar)

        val burgerWidth = 150
        val burgerHeight = 150
        burgerWrapper.x = gridPosition.x - burgerWidth / 2
        burgerWrapper.y = gridPosition.y - burgerHeight / 2

        // Register this burger with the selection manager for interaction
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
