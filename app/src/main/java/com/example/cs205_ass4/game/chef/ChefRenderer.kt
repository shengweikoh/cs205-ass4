package com.example.cs205_ass4.game.chef

import android.app.Activity
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.cs205_ass4.R
import com.example.cs205_ass4.utils.SelectionUtils

class ChefRenderer(private val activity: Activity) {
    private lateinit var chefImageList: List<ImageView>
    private lateinit var selectionManager: SelectionUtils.SelectionManager<Int>

    fun setSelectionManager(manager: SelectionUtils.SelectionManager<Int>) {
        this.selectionManager = manager
    }

    fun setupChefs() {
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

        // Register chefs as interaction targets
        selectionManager.registerInteractionTarget(chef1)
        selectionManager.registerInteractionTarget(chef2)
        selectionManager.registerInteractionTarget(chef3)
        selectionManager.registerInteractionTarget(chef4)
    }

    fun updateChefImage(chefId: Int, state: ChefState) {
        val chefView = chefImageList.find { (it.tag as? Int) == chefId } ?: return

        if (state == ChefState.COOKING) {
            Glide.with(chefView.context).asGif().load(R.drawable.chef_cooking).into(chefView)
        } else {
            chefView.setImageResource(R.drawable.chef_idle)
        }
    }

    fun getChefLocation(chefId: Int): IntArray? {
        val chefView = chefImageList.find { (it.tag as? Int) == chefId } ?: return null
        val chefLocation = IntArray(2)
        chefView.getLocationOnScreen(chefLocation)
        return chefLocation
    }

    fun getChefView(chefId: Int): ImageView? {
        return chefImageList.find { (it.tag as? Int) == chefId }
    }

    fun getAllChefs(): List<ImageView> {
        return chefImageList
    }
}
