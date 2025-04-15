package com.example.cs205_ass4.game.burger

class BurgerManager {
    private val burgers = mutableListOf<Burger>()

    fun spawnBurger(id: Int, x: Float, y: Float) {
        burgers.add(Burger(id, x, y, burgerState = 0.0f))
    }

    fun getBurgerById(id: Int): Burger? {
        return burgers.find { it.id == id }
    }

    // New: Mark the burger as cooking so it is not eligible for expiration.
    fun markBurgerAsCooking(burgerId: Int) {
        getBurgerById(burgerId)?.isCooking = true
    }

    fun updateBurgers() {
        // Remove expired burgers (only those that are not cooking will ever be expired)
        val expiredBurgerIds = getExpiredBurgerIds()
        burgers.removeAll { burger -> expiredBurgerIds.contains(burger.id) }
    }

    fun getExpiredBurgerIds(): List<Int> {
        return burgers.filter { it.isExpired() }.map { it.id }
    }
}