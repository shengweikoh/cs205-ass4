package com.example.cs205_ass4

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cs205_ass4.game.GameEngine
import com.example.cs205_ass4.game.GameRenderer

// class MainActivity : ComponentActivity() {
//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         enableEdgeToEdge()
//         setContent {
//             Cs205ass4Theme {
//                 Surface(
//                     modifier = Modifier.fillMaxSize(),
//                     color = MaterialTheme.colorScheme.background
//                 ) {
//                     GameScreen()
//                 }
//             }
//         }
//     }
// }


class MainActivity : AppCompatActivity() {
    private lateinit var gameEngine: GameEngine
    private lateinit var gameRenderer: GameRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        gameEngine = GameEngine()
        gameRenderer = GameRenderer(this, gameEngine)
        gameRenderer.setupUI()
    }
}