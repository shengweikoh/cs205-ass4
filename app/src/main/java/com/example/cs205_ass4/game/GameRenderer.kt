package com.example.cs205_ass4.game

import android.app.Activity
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.cs205_ass4.R

class GameRenderer(private val activity: Activity, private val gameEngine: GameEngine) {

    private lateinit var chefImage1: ImageView
    private lateinit var chefImage2: ImageView
    private lateinit var burgerContainer: FrameLayout

    // Handler to schedule burger spawns on the main thread.
    private val handler = Handler(Looper.getMainLooper())

    fun setupUI() {
        // Bind chef ImageView and set its drag listener.
        chefImage1 = activity.findViewById(R.id.imageViewChef1)
        chefImage2 = activity.findViewById(R.id.imageViewChef2)
        // (Optional) You can set a drag listener if you want to use dragging with chefs.
        // chefImage.setOnDragListener(chefDragListener)

        // Bind the container where burgers will be added.
        burgerContainer = activity.findViewById(R.id.burgerContainer)

        // Register for burger expiration callbacks.
        // Currently, we are using event-based callbacks to remove expired burgers.
        // GameEngine will call GameRenderer.removeExpiredBurgerViews when burgers expire.
        gameEngine.setOnBurgersExpiredCallback(::removeExpiredBurgerViews)

        // Start the game engine.
        gameEngine.startGame()

        // Start scheduling burger spawns.
        scheduleBurgerSpawn()
    }

    // Removes burger views that have expired.
    private fun removeExpiredBurgerViews(expiredBurgerIds: List<Int>) {
        // Create an empty list to hold views to remove.
        val viewsToRemove = mutableListOf<View>()

        // Add all views whose tag (burger id) is in the expired list.
        for (i in 0 until burgerContainer.childCount) {
            val view = burgerContainer.getChildAt(i)
            val burgerId = view.tag as? Int
            if (burgerId != null && expiredBurgerIds.contains(burgerId)) {
                viewsToRemove.add(view)
            }
        }

        // Remove all identified views.
        viewsToRemove.forEach { view ->
            burgerContainer.removeView(view)
        }
    }

    // Schedule burger spawns every 3 seconds.
    private fun scheduleBurgerSpawn() {
        handler.postDelayed({
            spawnBurgerView()
            scheduleBurgerSpawn() // recursively schedule the next spawn.
        }, 3000)
    }

    // Creates and adds a new burger view to the container.
    private fun spawnBurgerView() {
        // Let the game engine create a burger and return its id.
        val burgerId = gameEngine.spawnBurger()
        // Create an ImageView for the burger, starting with the bottom drawable.
        val burgerView = ImageView(activity).apply {
            setImageResource(R.drawable.bottom)   // starting image: bottom bun
            tag = burgerId // Save the burger's id in the view's tag.
            // Set layout parameters (customize size as needed).
            layoutParams = FrameLayout.LayoutParams(300, 300).apply {
                gravity = android.view.Gravity.CENTER
            }
        }

        // Attach a touch listener to allow moving the burger.
        burgerView.setOnTouchListener(object : View.OnTouchListener {
            var dX = 0f
            var dY = 0f
            // currentStep: 0 = bottom, 1 = tomato, 2 = patty, 3 = lettuce, 4 = top bun
            var currentStep = 0
            var stackingActive = false
            var pendingRunnable: Runnable? = null

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Cancel any pending stacking updates.
                        pendingRunnable?.let { view.removeCallbacks(it) }
                        pendingRunnable = null
                        stackingActive = false
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        view.x = event.rawX + dX
                        view.y = event.rawY + dY
                        // If moved off the chef while stacking is active, cancel pending updates.
                        if (!isViewOverlapping(view, chefImage1) && !isViewOverlapping(view, chefImage2) && stackingActive) {
                            pendingRunnable?.let { view.removeCallbacks(it) }
                            pendingRunnable = null
                            stackingActive = false
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isViewOverlapping(view, chefImage1) || isViewOverlapping(view, chefImage2)) {
                            // When dropped over the chef and not already stacking,
                            // resume the chain based on the current state.
                            val imageView = view as? ImageView
                            imageView?.let {
                                if (!stackingActive) {
                                    stackingActive = true
                                    if (currentStep == 0) {
                                        // Starting fresh: set tomato, update state, and schedule next update.
                                        it.setImageResource(R.drawable.tomato)
                                        currentStep = 1
                                        scheduleStacking(it, view, currentStep)
                                    } else if (currentStep in 1..3) {
                                        // Already advanced; resume the chain
                                        scheduleStacking(it, view, currentStep)
                                    }
                                    // If currentStep == 4 (top bun) then nothing further is scheduled.
                                }
                            }
                        } else {
                            // If not overlapping, cancel any pending tasks.
                            pendingRunnable?.let { view.removeCallbacks(it) }
                            pendingRunnable = null
                            stackingActive = false
                        }
                        return true
                    }
                    else -> return false
                }
            }

            // This helper schedules the next state change.
            // The chain is:
            //  step 0 (bottom) already became tomato,
            //  step 1: tomato -> patty,
            //  step 2: patty -> lettuce,
            //  step 3: lettuce -> top bun.
            fun scheduleStacking(imageView: ImageView, view: View, step: Int) {
                // List of drawable resources for each state.
                val nextStates = listOf(R.drawable.tomato, R.drawable.patty, R.drawable.lettuce, R.drawable.top)

                // If we've reached the final state (state 4), remove the burger from the UI.
                if (step >= nextStates.size) {
                    stackingActive = false
                    // Remove the completed burger from the UI after a short delay (1 second).
                    imageView.postDelayed({
                        if (imageView.parent != null) {
                            burgerContainer.removeView(imageView)
                        }
                    }, 1000)
                    return
                }

                // Continue scheduling the next state change.
                pendingRunnable = Runnable {
                    // Only update if the view is still overlapping the chef.
                    if (isViewOverlapping(view, chefImage1) || isViewOverlapping(view, chefImage2)) {
                        imageView.setImageResource(nextStates[step])
                        currentStep = step + 1
                        // Continue scheduling the next state change.
                        scheduleStacking(imageView, view, currentStep)  // Recursively call to update to next state.
                    } else {
                        stackingActive = false  // Stop if it's no longer overlapping the chef.
                    }
                }

                // Delay for 1.5 seconds before changing the state.
                imageView.postDelayed(pendingRunnable, 1500)
            }

        })



        // Add the burger view to the container.
        burgerContainer.addView(burgerView)
    }


    /**
     * Checks whether two views are overlapping.
     */
    private fun isViewOverlapping(view1: View, view2: View): Boolean {
        val rect1 = Rect()
        view1.getHitRect(rect1)
        val rect2 = Rect()
        view2.getHitRect(rect2)
        return Rect.intersects(rect1, rect2)
    }

    // Optional: Drag listener code for chefImage if you decide to use drag and drop.
    /*
    private val chefDragListener = View.OnDragListener { _, event ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> true
            DragEvent.ACTION_DROP -> {
                val draggedView = event.localState as? View
                draggedView?.let { view ->
                    val burgerId = view.tag as? Int
                    if (burgerId != null) {
                        gameEngine.startCookingBurger(chefId = 1, burgerId = burgerId)
                        burgerContainer.removeView(view)
                        updateChefImage(chefImage, 1)
                    }
                }
                true
            }
            else -> true
        }
    }
    */

    // Optional: Function to update the chef image based on its state.
    /*
    private fun updateChefImage(imageView: ImageView, chefId: Int) {
        val state = gameEngine.getChefState(chefId)
        val drawableRes = if (state == ChefState.COOKING) {
            R.drawable.chef_cooking
        } else {
            R.drawable.chef_idle
        }
        imageView.setImageResource(drawableRes)
    }
    */
}
