package com.example.cs205_ass4.game.chef

enum class ChefState {
    IDLE, COOKING
}

data class Chef(
    val id: Int,
    var chefState: ChefState = ChefState.IDLE
)