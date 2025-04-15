package com.example.cs205_ass4.game

import android.app.Activity
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.cs205_ass4.R
import com.example.cs205_ass4.game.chef.ChefState

class GameRenderer(private val activity: Activity, private val gameEngine: GameEngine) {

    private lateinit var chefImageList: List<ImageView>
    private lateinit var burgerContainer: FrameLayout
    private lateinit var burgerExpiredTextView: TextView
    private lateinit var burgerCounterTextView: TextView

    // Handler to schedule burger spawns.
    private val handler = Handler(Looper.getMainLooper())

    fun setupUI() {
        // Bind chef ImageViews.
        val chef1 = activity.findViewById<ImageView>(R.id.imageViewChef1)
        val chef2 = activity.findViewById<ImageView>(R.id.imageViewChef2)
        val chef3 = activity.findViewById<ImageView>(R.id.imageViewChef3)
        val chef4 = activity.findViewById<ImageView>(R.id.imageViewChef4)
        chef1.tag = 1
        chef2.tag = 2
        chef3.tag = 3
        chef4.tag = 4
        chefImageList = listOf(chef1, chef2, chef3, chef4)

        // Bind the container for burger views.
        burgerContainer = activity.findViewById(R.id.burgerContainer)
        // Bind the counter TextViews.
        burgerCounterTextView = activity.findViewById(R.id.textViewBurgerCounter)
        burgerExpiredTextView = activity.findViewById(R.id.textViewBurgerExpired)

        // Set callback to update the "cooked" counter.
        gameEngine.setOnBurgerCookedCallback { count ->
            activity.runOnUiThread {
                burgerCounterTextView.text = "Burgers Cooked: $count"
            }
        }

        // Set callback to update the "expired" counter.
        gameEngine.setOnBurgerExpiredCountChangedCallback { count ->
            activity.runOnUiThread {
                burgerExpiredTextView.text = "Burgers Expired: $count"
            }
        }

        // Set callback to update chef image when state changes.
        gameEngine.setOnChefStateChangedCallback { chefId, state ->
            activity.runOnUiThread {
                val chefView = chefImageList.find { (it.tag as? Int) == chefId }
                chefView?.let { updateChefImage(it, chefId) }
            }
        }

        // Callback to remove expired burger views.
        gameEngine.setOnBurgersExpiredCallback(::removeExpiredBurgerViews)

        gameEngine.startGame()
        scheduleBurgerSpawn()
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

    // Schedule burger spawns every 3 seconds.
    private fun scheduleBurgerSpawn() {
        handler.postDelayed({
            spawnBurgerView()
            scheduleBurgerSpawn()
        }, 3000)
    }

    private fun spawnBurgerView() {
        val burgerId = gameEngine.spawnBurger()
        val burgerView = ImageView(activity).apply {
            setImageResource(R.drawable.burger_order)  // Initial static image.
            tag = burgerId
            layoutParams = FrameLayout.LayoutParams(150, 150).apply {
                gravity = Gravity.CENTER
            }
        }

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
                    MotionEvent.ACTION_UP -> {
                        // Find the chef view that the burger overlaps.
                        val overlappedChef = findOverlappedChef(view)
                        if (overlappedChef != null) {
                            val chefId = overlappedChef.tag as? Int
                            val burgerId = view.tag as? Int
                            if (chefId != null && burgerId != null) {
                                if (gameEngine.getChefState(chefId) == ChefState.IDLE) {
                                    // Assign the burger to the chef.
                                    gameEngine.startCookingBurger(chefId, burgerId)
                                    // Start the layering animation and pass the chefId.
                                    animateBurgerLayering(view as ImageView, chefId)
                                    updateChefImage(overlappedChef, chefId)
                                } else {
                                    Toast.makeText(activity,
                                        "Chef $chefId is busy. Please drop the burger on another chef!",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        return true
                    }
                    else -> return false
                }
            }
        })

        burgerContainer.addView(burgerView)
    }

    /**
     * Animates the burger layering process.
     * Displays the layers in order: bottom -> tomato -> patty -> lettuce -> top.
     * Once complete, removes the burger view and notifies the GameEngine to mark the burger as cooked.
     */
    private fun animateBurgerLayering(burgerView: ImageView, chefId: Int) {
        // List of drawable resources for each layer.
        val layerImages = listOf(
            R.drawable.burger_bottom,  // Ensure this resource exists.
            R.drawable.burger_tomato,  // Ensure this resource exists.
            R.drawable.burger_patty,   // Ensure this resource exists.
            R.drawable.burger_lettuce, // Ensure this resource exists.
            R.drawable.burger_top      // Ensure this resource exists.
        )
        val layerDelayMillis = 1000L  // 1 second per layer.
        var currentLayerIndex = 0

        // Use a separate Handler for the layering animation.
        val layeringHandler = Handler(Looper.getMainLooper())
        val layeringRunnable = object : Runnable {
            override fun run() {
                if (currentLayerIndex < layerImages.size) {
                    burgerView.setImageResource(layerImages[currentLayerIndex])
                    currentLayerIndex++
                    layeringHandler.postDelayed(this, layerDelayMillis)
                } else {
                    // Animation complete; remove the burger view.
                    burgerContainer.removeView(burgerView)
                    // Notify the game engine that cooking is complete.
                    gameEngine.completeBurgerCooking(chefId)
                }
            }
        }
        layeringHandler.post(layeringRunnable)
    }

    private fun findOverlappedChef(burgerView: View): ImageView? {
        for (chefView in chefImageList) {
            if (isViewOverlapping(burgerView, chefView)) {
                return chefView
            }
        }
        return null
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

    private fun isViewOverlapping(view1: View, view2: View): Boolean {
        val loc1 = IntArray(2)
        val loc2 = IntArray(2)
        view1.getLocationOnScreen(loc1)
        view2.getLocationOnScreen(loc2)
        val rect1 = android.graphics.Rect(
            loc1[0],
            loc1[1],
            loc1[0] + view1.width,
            loc1[1] + view1.height
        )
        val rect2 = android.graphics.Rect(
            loc2[0],
            loc2[1],
            loc2[0] + view2.width,
            loc2[1] + view2.height
        )
        return rect1.intersect(rect2)
    }
}