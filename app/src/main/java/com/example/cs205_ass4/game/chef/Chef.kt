package com.example.cs205_ass4.game.chef

enum class ChefState {
    IDLE, COOKING
}

data class Chef(
    val id: Int,
    var xPosition: Float,
    var yPosition: Float,
    var chefState: ChefState = ChefState.IDLE,
    var currentBurgerId: Int? = null  // Track the burger (process) being handled
)