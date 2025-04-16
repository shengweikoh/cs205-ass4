package com.example.cs205_ass4.game.burger

data class Burger(
    val id: Int,
    var percentOfBurgerCooked: Float = 0.0f,
    var burgerCreationTimestamp: Long = System.currentTimeMillis()
) {
    fun cook(increment: Float) {
        percentOfBurgerCooked = (percentOfBurgerCooked + increment).coerceAtMost(1.0f)
    }
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - burgerCreationTimestamp > BurgerConstants.BURGER_EXPIRATION_TIME
    }
}