package com.example.cs205_ass4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.cs205_ass4.ui.theme.Cs205ass4Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cs205ass4Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Kitchen()
                }
            }
        }
    }
}

@Composable
fun Kitchen(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // For now, we'll just draw a white background
        drawRect(
            color = Color.White,
            topLeft = Offset(0f, 0f),
            size = Size(size.width, size.height)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun KitchenPreview() {
    Cs205ass4Theme {
        Kitchen()
    }
}