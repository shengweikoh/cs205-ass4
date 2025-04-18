package com.example.cs205_ass4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class EndGameActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_COMPLETED = "extra_completed"
        const val EXTRA_LOST      = "extra_lost"
        const val EXTRA_EXPIRED   = "extra_expired"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_game)

        // Pull the three counters from the Intent extras
        val completed = intent.getIntExtra(EXTRA_COMPLETED, 0)
        val lost      = intent.getIntExtra(EXTRA_LOST, 0)
        val expired   = intent.getIntExtra(EXTRA_EXPIRED, 0)

        // Find and populate the TextViews
        findViewById<TextView>(R.id.textCompleted).text = "Burgers Completed: $completed"
        findViewById<TextView>(R.id.textLost).text      = "Burgers Lost: $lost"
        findViewById<TextView>(R.id.textExpired).text   = "Burgers Expired: $expired"

        // Your team credits—update these names!
        findViewById<TextView>(R.id.textCredits).text = """
            Game Design & Code:
            • Rui Cong
            • Bridgette 
            • Sheng Wei
            • Wenkai
            • Lee Min
        """.trimIndent()

        // 1) Find the restart button
        findViewById<Button>(R.id.buttonRestart).setOnClickListener {
            // 2) Fire up MainActivity, clearing the back stack
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}
