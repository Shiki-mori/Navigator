package dev.phrolova.navigator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import dev.phrolova.navigator.ui.LocalAppContainer
import dev.phrolova.navigator.ui.NavigatorApp
import dev.phrolova.navigator.ui.theme.NavigatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as NavigatorApplication
        setContent {
            NavigatorTheme {
                CompositionLocalProvider(LocalAppContainer provides app.container) {
                    NavigatorApp()
                }
            }
        }
    }
}
