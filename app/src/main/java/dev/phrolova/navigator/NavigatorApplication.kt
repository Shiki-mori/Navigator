package dev.phrolova.navigator

import android.app.Application

class NavigatorApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
