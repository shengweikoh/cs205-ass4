package com.example.cs205_ass4.game.burger

class BurgerManager {
    // map to store the same burgers in kitchen manager by id
    private val burgers = mutableMapOf<Int, Burger>()

    fun spawnBurger(id: Int, burger: Burger) {
        burgers[id] = burger
    }

    fun getBurgerById(id: Int): Burger? {
        return burgers[id]
    }

    fun getAllBurgers(): List<Burger> {
        return burgers.values.toList()
    }

    fun removeBurger(id: Int) {
        burgers.remove(id)
    }
}