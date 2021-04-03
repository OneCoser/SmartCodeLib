package ch.smart.code.lib

import ch.smart.code.SmartCodeApp
import ch.smart.code.util.AppEnvironment

class CustomApp : SmartCodeApp() {

    override fun getAppEnvironment(): AppEnvironment {
        return AppEnvironment.DEV
    }

    override fun onCreate() {
        DEBUG = BuildConfig.DEBUG
        super.onCreate()
    }
}