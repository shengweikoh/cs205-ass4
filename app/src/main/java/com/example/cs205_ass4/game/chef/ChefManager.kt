package com.example.cs205_ass4.game.chef

class ChefManager {
    private val chefs = mutableListOf<Chef>()

    fun spawnChef(id: Int, x: Float, y: Float) {
        chefs.add(Chef(id, x, y))
    }

    fun getChefById(id: Int): Chef? {
        return chefs.find { it.id == id }
    }

    // Only assign a burger if the chef is currently idle.
    fun assignBurgerToChef(chefId: Int, burgerId: Int) {
        val chef = getChefById(chefId)
        if (chef != null && chef.chefState == ChefState.IDLE) {
            chef.chefState = ChefState.COOKING
            chef.currentBurgerId = burgerId
        }
    }

    // Mark the chef as finished and reset state.
    fun finishCooking(chefId: Int) {
        getChefById(chefId)?.let { chef ->
            chef.chefState = ChefState.IDLE
            chef.currentBurgerId = null
        }
    }

    fun toggleChef(chefId: Int) {
        val chef = getChefById(chefId)
        chef?.let {
            it.chefState = if (it.chefState == ChefState.IDLE) {
                ChefState.COOKING
            } else {
                ChefState.IDLE
            }
        }
    }

    fun updateChefs() {
        // TODO: Update chef logic (e.g., animation, timing)
    }
}