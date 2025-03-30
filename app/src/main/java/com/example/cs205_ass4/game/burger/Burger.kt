package com.example.cs205_ass4.game.burger

data class Burger(
    val id: Int,
    val xPosition: Float,
    val yPosition: Float,
    var burgerState: Float = 0.0f
) {
    fun cook(increment: Float) {
        burgerState = (burgerState + increment).coerceAtMost(1.0f)
    }
}