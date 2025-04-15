package com.example.cs205_ass4.game.chef

enum class ChefState {
    IDLE,
    COOKING,
    IO_WAIT  // New state for when I/O interrupts occur (if you choose to implement that logic)
}

data class Chef(
    val id: Int,
    var xPosition: Float,
    var yPosition: Float,
    var chefState: ChefState = ChefState.IDLE,
    var currentBurgerId: Int? = null  // Track the burger (process) being handled
)