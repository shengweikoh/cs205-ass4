package com.example.cs205_ass4.game.burger

import com.example.cs205_ass4.game.interfaces.Expirable

data class Burger(
    val id: Int,
    var percentOfBurgerCooked: Float = 0.0f,
    val burgerCreationTimestamp: Long = System.currentTimeMillis(),
    override val decayDurationSec: Int = BurgerConstants.DEFAULT_BURGER_EXPIRATION_TIME_SEC,
    override var freshnessPercentage: Float = 1.0f
) : Expirable {

    override fun isExpired(): Boolean {
        return System.currentTimeMillis() - burgerCreationTimestamp > decayDurationSec * 1000
    }

    override fun decay() {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - burgerCreationTimestamp
        freshnessPercentage = ((decayDurationSec * 1000f - elapsedTime) / (decayDurationSec * 1000f)).coerceIn(0f, 1f)
    }
}