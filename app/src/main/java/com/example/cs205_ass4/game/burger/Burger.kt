package com.example.cs205_ass4.game.burger

import com.example.cs205_ass4.game.burger.BurgerConstants

data class Burger(
    val id: Int,
    val xPosition: Float,
    val yPosition: Float,
    var burgerState: Float = 0.0f,
    var burgerCreationTimestamp: Long = System.currentTimeMillis(),
    var isCooking: Boolean = false  // New flag to indicate that the burger is being cooked
) {
    fun cook(increment: Float) {
        burgerState = (burgerState + increment).coerceAtMost(1.0f)
    }
    fun isExpired(): Boolean {
        // Once cooking has begun, the burger should no longer be considered expired.
        if (isCooking) return false
        return System.currentTimeMillis() - burgerCreationTimestamp > BurgerConstants.BURGER_EXPIRATION_TIME
    }
}