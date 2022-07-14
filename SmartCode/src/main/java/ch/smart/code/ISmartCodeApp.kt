package ch.smart.code

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import ch.smart.code.imageloader.FrescoConfig
import ch.smart.code.network.OkHttpFactory
import ch.smart.code.util.*
import com.getkeepsafe.relinker.ReLinker
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.fresco.FrescoImageLoader
import com.scwang.smartrefresh.header.MaterialHeader
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.constant.SpinnerStyle
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.tencent.mmkv.MMKV
import com.tencent.smtt.sdk.QbSdk
import io.reactivex.plugins.RxJavaPlugins
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.onAdaptListener
import me.jessyan.autosize.unit.Subunits
import me.jessyan.autosize.utils.ScreenUtils
import timber.log.Timber
import com.tencent.smtt.export.external.TbsCoreSettings

interface ISmartCodeApp {

    fun getAppEnvironment(): AppEnvironment {
        return AppEnvironment.PRODUCTION
    }

    fun initConfig(application: Application) {
        Timber.plant(Timber.DebugTree())
        RxJavaPlugins.setErrorHandler {
            Timber.e(it)
        }
        initMMKV(application)
        initSMTT(application)
        initFresco(application)
        initAutoSize(application)
        initSmartRefresh(application)
    }

    fun initFresco(application: Application) {
        try {
            BigImageViewer.initialize(
                FrescoImageLoader.with(
                    application, FrescoConfig.getConfigBuilder(
                        application,
                        FileCache.getImageDir(),
                        OkHttpFactory.commonHttpClient
                    ).build()
                )
            )
        } catch (e: Exception) {
            Timber.e(e)
        }
        application.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onTrimMemory(level: Int) {
                FrescoConfig.onTrimMemory(level)
            }

            override fun onConfigurationChanged(newConfig: Configuration) {}

            override fun onLowMemory() {
                FrescoConfig.onLowMemory()
            }
        })
    }

    fun initAutoSize(application: Application) {
        AutoSizeConfig.getInstance()
            .setExcludeFontScale(true)
            .unitsManager
            .setSupportDP(false)
            .setSupportSP(false).supportSubunits = Subunits.PT
        AutoSizeConfig.getInstance().onAdaptListener = object : onAdaptListener {
            override fun onAdaptBefore(target: Any, activity: Activity) {
                // 使用以下代码, 可支持 Android 的分屏或缩放模式, 但前提是在分屏或缩放模式下当用户改变您 App 的窗口大小时
                // 系统会重绘当前的页面, 经测试在某些机型, 某些情况下系统不会重绘当前页面, ScreenUtils.getScreenSize(activity) 的参数一定要不要传 Application!!!
                if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    AutoSizeConfig.getInstance().screenWidth =
                        ScreenUtils.getScreenSize(activity)[1]
                    AutoSizeConfig.getInstance().screenHeight =
                        ScreenUtils.getScreenSize(activity)[0]
                } else {
                    AutoSizeConfig.getInstance().screenWidth =
                        ScreenUtils.getScreenSize(activity)[0]
                    AutoSizeConfig.getInstance().screenHeight =
                        ScreenUtils.getScreenSize(activity)[1]
                }
            }

            override fun onAdaptAfter(target: Any, activity: Activity) {
            }
        }
    }

    fun initMMKV(application: Application) {
        try {
            val rootDir =
                MMKV.initialize(application, FileCache.getMMKVDir()?.absolutePath) { libName ->
                    try {
                        ReLinker.loadLibrary(application, libName)
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
            Timber.i("MMKV路径: %s", rootDir)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun initSMTT(application: Application) {
        try {
            QbSdk.initTbsSettings(
                mapOf(
                    TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER to true,
                    TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE to true
                )
            )
            QbSdk.initX5Environment(application, object : QbSdk.PreInitCallback {
                override fun onCoreInitFinished() {
                    Timber.i("初始化X5内核：onCoreInitFinished")
                }

                override fun onViewInitFinished(isX5: Boolean) {
                    Timber.i("初始化X5内核：onViewInitFinished-%s", isX5)
                }
            })
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun initSmartRefresh(application: Application) {
        try {
            // 设置全局的Header构建器
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
                layout.setPrimaryColors(Color.WHITE, Color.WHITE) // 全局设置主题颜色
                MaterialHeader(context).setColorSchemeColors(Color.BLACK)
            }
            // 设置全局的Footer构建器
            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
                ClassicsFooter(context)
                    .setSpinnerStyle(SpinnerStyle.Translate)
                    .setPrimaryColor(Color.WHITE)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}