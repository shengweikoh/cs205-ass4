package com.example.cs205_ass4.game

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.DragShadowBuilder
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.cs205_ass4.R

class GameRenderer(private val activity: Activity, private val gameEngine: GameEngine) {

    private lateinit var chefImage: ImageView
    private lateinit var burgerContainer: FrameLayout

    // Handler to schedule burger spawns on the main thread.
    private val handler = Handler(Looper.getMainLooper())

    fun setupUI() {
        // Bind chef ImageView and set its drag listener.
        chefImage = activity.findViewById(R.id.imageViewChef1)
//        chefImage.setOnDragListener(chefDragListener)

        // Bind the container where burgers will be added.
        burgerContainer = activity.findViewById(R.id.burgerContainer)

        // Start scheduling burger spawns.
        scheduleBurgerSpawn()
    }

    // Schedule burger spawns every 5 seconds.
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
        // Create an ImageView for the burger.
        val burgerView = ImageView(activity).apply {
            setImageResource(R.drawable.burger_order)
            tag = burgerId // Save the burger's id in the view's tag.
            // Set layout parameters (customize size as needed).
            layoutParams = FrameLayout.LayoutParams(150, 150).apply {
                gravity = android.view.Gravity.CENTER
            }
        }
        // Attach a touch listener to allow moving the burger.
        burgerView.setOnTouchListener(object : View.OnTouchListener {
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
//                    MotionEvent.ACTION_UP -> {
//                        // When released, check if the burger overlaps the chef.
//                        if (isViewOverlapping(view, chefImage)) {
//                            // Start cooking with chef id = 1 (adjust if needed).
//                            gameEngine.startCookingBurger(chefId = 1, burgerId = burgerId)
//                            // Remove the burger view from the container.
//                            burgerContainer.removeView(view)
//                            // Update chef image (if startCookingBurger does not trigger UI update automatically).
//                            updateChefImage(chefImage, 1)
//                        }
//                        return true
//                    }
                    else -> return false
                }
            }
        })

        // Add the burger view to the container.
        burgerContainer.addView(burgerView)
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
}