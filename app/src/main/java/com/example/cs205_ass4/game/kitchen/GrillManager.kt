package com.example.cs205_ass4.game.kitchen

import android.widget.TextView

class GrillManager(private val grillCapacityTextView: TextView) {
    private var grillCount = 20

    init {
        updateGrillCapacityText()
    }

    fun canCookBurger(burgerValue: Int): Boolean {
        return grillCount - burgerValue >= 0
    }

    fun consumeGrillCapacity(burgerValue: Int): Boolean {
        if (canCookBurger(burgerValue)) {
            grillCount -= burgerValue
            updateGrillCapacityText()
            return true
        }
        return false
    }

    fun releaseGrillCapacity(burgerValue: Int) {
        grillCount += burgerValue
        updateGrillCapacityText()
    }

    private fun updateGrillCapacityText() {
        "Capacity: $grillCount".also { grillCapacityTextView.text = it }
    }

}
