package com.example.cs205_ass4

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.cs205_ass4.game.GameEngine
import com.example.cs205_ass4.game.GameRenderer

class MainActivity : AppCompatActivity() {
    private lateinit var gameEngine: GameEngine
    private lateinit var gameRenderer: GameRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Edge‑to‑edge and layout
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        // 2) Hide status & nav bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, findViewById(R.id.main)).apply {
            hide(
                WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.navigationBars()
            )
            // allow swipe to temporarily reveal bars
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // 3) Keep screen on while this Activity is visible
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
