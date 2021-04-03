package ch.smart.code.mvp.lifecycle

import android.app.Application
import android.content.Context

/**
 * 类描述：Application生命周期
 */
interface AppLifecycle {
    fun attachBaseContext(base: Context)

    fun onCreate(application: Application)

    fun onTerminate(application: Application)
}