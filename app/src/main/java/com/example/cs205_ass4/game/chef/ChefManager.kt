package com.example.cs205_ass4.game.chef

class ChefManager {
    private val chefs = mutableListOf<Chef>()

    fun spawnChef(id: Int) {
        chefs.add(Chef(id))
    }

    fun getChefById(id: Int): Chef? {
        return chefs.find { it.id == id }
    }

    fun toggleChef(chefId: Int, chefState: ChefState) {
        getChefById(chefId)?.let { chef ->
            chef.chefState = chefState
        }
    }

}