package ch.smart.code

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import androidx.multidex.MultiDex
import ch.smart.code.imageloader.FrescoConfig
import ch.smart.code.mvp.app.AppDelegate
import ch.smart.code.network.OkHttpFactory
import ch.smart.code.util.*
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ProcessUtils
import com.blankj.utilcode.util.Utils
import com.getkeepsafe.relinker.ReLinker
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.fresco.FrescoImageLoader
import com.luck.picture.lib.app.IApp
import com.luck.picture.lib.app.PictureAppMaster
import com.luck.picture.lib.engine.PictureSelectorEngine
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
import okhttp3.OkHttpClient
import timber.log.Timber
import zlc.season.rxdownload3.RxDownload
import zlc.season.rxdownload3.core.DownloadConfig
import zlc.season.rxdownload3.extension.ApkInstallExtension
import zlc.season.rxdownload3.http.OkHttpClientFactory

open class SmartCodeApp : Application(), IApp {

    companion object {
        @JvmStatic
        var DEBUG: Boolean = false
    }

    private var appDelegate: AppDelegate? = null

    open fun getAppEnvironment(): AppEnvironment {
        return AppEnvironment.PRODUCTION
    }

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
        initConfig()
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

    open fun initConfig() {
        Utils.init(this)
        if (!ProcessUtils.isMainProcess()) return
        Timber.plant(Timber.DebugTree())
        if (SmartCodeApp.DEBUG) {
            ARouter.openLog() // 打印日志
            ARouter.openDebug() // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(this) // 尽可能早,推荐在Application中初始化
        RxJavaPlugins.setErrorHandler {
            Timber.e(it)
        }
        initMMKV()
        initSMTT()
        initFresco()
        initAutoSize()
        initRxDownload()
        initSmartRefresh()
        initPictureSelector()
    }

    open fun getOvertime(): Int {
        //20200101
        return 0
    }

    private fun initFresco() {
        val imageDir = FileCache.getImageDir()
        if (imageDir == null) {
            Timber.e("初始化Fresco失败")
            return
        }
        val configBuilder = FrescoConfig.getConfigBuilder(
            this,
            imageDir,
            OkHttpFactory.commonHttpClient
        )
        BigImageViewer.initialize(FrescoImageLoader.with(this, configBuilder.build()))
        registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onTrimMemory(level: Int) {
                FrescoConfig.onTrimMemory(level)
            }

            override fun onConfigurationChanged(newConfig: Configuration) {}

            override fun onLowMemory() {
                FrescoConfig.onLowMemory()
            }
        })
    }

    private fun initAutoSize() {
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

    private fun initMMKV() {
        val mmkvDir = FileCache.getMMKVDir()
        if (mmkvDir == null) {
            Timber.e("初始化MMKV失败")
            return
        }
        val rootDir = MMKV.initialize(mmkvDir.absolutePath) { libName ->
            try {
                ReLinker.loadLibrary(this, libName)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        Timber.i("MMKV路径: %s", rootDir)
    }

    private fun initSMTT() {
        QbSdk.initX5Environment(this, object : QbSdk.PreInitCallback {
            override fun onCoreInitFinished() {
                Timber.i("初始化X5内核完成")
            }

            override fun onViewInitFinished(p0: Boolean) {
                Timber.i("初始化X5内核是否成功:%s", p0)
            }
        })
    }

    private fun initSmartRefresh() {
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
    }

    private fun initRxDownload() {
        // 初始化下载器
        val downloadConfig = DownloadConfig.Builder.create(this)
            .setDebug(SmartCodeApp.DEBUG)
            .setMaxMission(3)
            .enableAutoStart(false) // 自动开始下载
            .enableNotification(false) // 启用Notification
            .addExtension(ApkInstallExtension::class.java) // 添加自动安装扩展
            .setOkHttpClientFacotry(object : OkHttpClientFactory {
                override fun build(): OkHttpClient {
                    return OkHttpFactory.commonHttpClient
                }
            })
        FileCache.getDownloadDir()?.absolutePath?.let {
            downloadConfig.setDefaultPath(it)
        }
        DownloadConfig.init(downloadConfig)
        RxDownload.hashCode()
    }

    private fun initPictureSelector() {
        PictureAppMaster.getInstance().app = this
    }

    override fun getAppContext(): Context {
        return this
    }

    override fun getPictureSelectorEngine(): PictureSelectorEngine {
        return PictureSelectorEngineImp()
    }
}