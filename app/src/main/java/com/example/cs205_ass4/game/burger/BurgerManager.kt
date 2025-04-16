package com.example.cs205_ass4.game.burger

import kotlin.collections.remove

class BurgerManager {
    private val burgers = mutableListOf<Burger>()

    fun spawnBurger(id: Int) {
        burgers.add(Burger(id, 0.0f))
    }

    fun getBurgerById(id: Int): Burger? {
        return burgers.find { it.id == id }
    }

    fun updateBurgers() {
        // TODO: Update burger logic (e.g., cooking timers, quality checks)
        // remove expired burgers
        val expiredBurgerIds = getExpiredBurgerIds()
        burgers.removeAll { burger -> expiredBurgerIds.contains(burger.id) }
    }

    fun getExpiredBurgerIds(): List<Int> {
        return burgers.filter { it.isExpired() }.map { it.id }
    }

    fun removeBurger(id: Int) {
        burgers.removeIf { burger -> burger.id == id }
    }
}