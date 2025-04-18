package com.example.cs205_ass4

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cs205_ass4.game.GameEngine
import com.example.cs205_ass4.game.GameRenderer

class MainActivity : AppCompatActivity() {
    private lateinit var gameEngine: GameEngine
    private lateinit var gameRenderer: GameRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize game components
        gameEngine = GameEngine()
        gameRenderer = GameRenderer(this, gameEngine)
        gameRenderer.setupUI()
    }
    override fun onPause() {
        super.onPause()
        gameEngine.pauseGame()
    }

    override fun onResume() {
        super.onResume()
        gameEngine.resumeGame()
    }

    override fun onDestroy() {
        // Clean up resources to prevent memory leaks
        gameRenderer.stopDecayUpdates()
        gameRenderer.cleanup()
        super.onDestroy()
    }
}
