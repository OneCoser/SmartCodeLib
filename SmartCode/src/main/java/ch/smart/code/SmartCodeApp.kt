package ch.smart.code

import android.app.Application
import android.content.Context
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.multidex.MultiDex
import ch.smart.code.mvp.app.AppDelegate
import ch.smart.code.util.*
import ch.smart.code.util.selector.PictureSelectorEngineImp
import com.luck.picture.lib.app.IApp
import com.luck.picture.lib.app.PictureAppMaster
import com.luck.picture.lib.engine.PictureSelectorEngine

open class SmartCodeApp : Application(), ISmartCodeApp, IApp, CameraXConfig.Provider {

    companion object {
        @JvmStatic
        var DEBUG: Boolean = false
    }

    private var appDelegate: AppDelegate? = null

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(base)
        if (appDelegate == null) {
            appDelegate = AppDelegate(base, getAppEnvironment())
        }
        appDelegate?.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        try {
            appDelegate?.onCreate(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!isMainProcess()) return
        initConfig(this)
        initPictureSelector()
        val time = getOvertime()
        val now = System.currentTimeMillis().safeTimeStr("yyyyMMdd").safeInt()
        if (time > 0 && now > 0 && time < now) {
            throw RuntimeException("APP已过期")
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        try {
            appDelegate?.onTerminate(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun getOvertime(): Int {
        //20200101
        return 0
    }

    open fun initPictureSelector() {
        PictureAppMaster.getInstance().app = this
    }

    override fun getAppContext(): Context {
        return this
    }

    override fun getPictureSelectorEngine(): PictureSelectorEngine {
        return PictureSelectorEngineImp()
    }

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
}