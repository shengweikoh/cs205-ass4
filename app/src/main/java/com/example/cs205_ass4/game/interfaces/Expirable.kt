package com.example.cs205_ass4.game.interfaces

interface Expirable {
    val decayDurationSec: Int
    var freshnessPercentage: Float
    fun isExpired(): Boolean
    fun decay()
}
