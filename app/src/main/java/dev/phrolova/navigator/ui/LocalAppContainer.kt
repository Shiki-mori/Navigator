package dev.phrolova.navigator.ui

import androidx.compose.runtime.staticCompositionLocalOf
import dev.phrolova.navigator.AppContainer

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer 未提供")
}
