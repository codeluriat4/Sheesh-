package org.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.example.test.ui.screens.LaunchScreen
import org.example.test.ui.theme.DadaTheme

// Every screen the app ever adds is expected to be wrapped exactly once,
// here, in DadaTheme. No other activity or screen composable should call
// DadaTheme again or apply its own colors/typography/shapes. LaunchScreen
// is the sole entry point; it owns the skeleton-to-primary-UI transition.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DadaTheme {
                LaunchScreen()
            }
        }
    }
}
