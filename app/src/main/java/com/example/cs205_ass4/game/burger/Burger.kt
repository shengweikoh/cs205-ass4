package com.example.cs205_ass4.game.burger

import com.example.cs205_ass4.game.burger.BurgerConstants

data class Burger(
    val id: Int,
    val xPosition: Float,
    val yPosition: Float,
    var burgerState: Float = 0.0f,
    var burgerCreationTimestamp: Long = System.currentTimeMillis()
) {
    fun cook(increment: Float) {
        burgerState = (burgerState + increment).coerceAtMost(1.0f)
    }
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - burgerCreationTimestamp > BurgerConstants.BURGER_EXPIRATION_TIME
    }
}