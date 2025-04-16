package com.example.cs205_ass4.game.burger

import kotlin.collections.remove

class BurgerManager {
    // map to store the same burgers in kitchen manager by id
    private val burgers = mutableMapOf<Int, Burger>()

    fun spawnBurger(id: Int, burger: Burger) {
        burgers[id] = burger
    }

    fun getBurgerById(id: Int): Burger? {
        return burgers[id]
    }

    fun updateBurgers() {
        // TODO: Update burger logic (e.g., cooking timers, quality checks)
        // remove expired burgers
        val expiredBurgerIds = getExpiredBurgerIds()
        expiredBurgerIds.forEach { burgers.remove(it) }
    }

    fun getExpiredBurgerIds(): List<Int> {
        return burgers.entries.filter { it.value.isExpired() }.map { it.key }
    }

    fun removeBurger(id: Int) {
        burgers.remove(id)
    }
}