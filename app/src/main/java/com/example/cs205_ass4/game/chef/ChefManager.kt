package com.example.cs205_ass4.game.chef

class ChefManager {
    private val chefs = mutableListOf<Chef>()

    fun spawnChef(id: Int, chefState: ChefState) {
        chefs.add(Chef(id, chefState = ChefState.IDLE))
    }

    fun getChefById(id: Int): Chef? {
        return chefs.find { it.id == id }
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