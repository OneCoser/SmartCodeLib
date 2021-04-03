package ch.smart.code.mvp.app

import android.app.Application
import android.content.Context
import ch.smart.code.util.AppEnvironment
import ch.smart.code.util.initAppEnvironment
import ch.smart.code.BuildConfig
import ch.smart.code.mvp.lifecycle.AppLifecycle
import ch.smart.code.mvp.lifecycle.BasicActivityLifecycle
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.Utils

/**
 * 类描述：Application代理，在Application对应的方法中调用，用于加载项目内各模块注册的配置信息
 */
class AppDelegate(context: Context, private val environment: AppEnvironment) : AppLifecycle {

    private val basicActivityLifecycle by lazy {
        BasicActivityLifecycle()
    }
    private val appLifecycles by lazy {
        arrayListOf<AppLifecycle>()
    }
    private val activityLifecycles by lazy {
        arrayListOf<Application.ActivityLifecycleCallbacks>()
    }

    init {
        try {
            ManifestParser(context).parse().forEach {
                it.injectAppLifecycle(context, appLifecycles)
                it.injectActivityLifecycle(context, activityLifecycles)
                it.injectFragmentLifecycle(context, basicActivityLifecycle.configFragmentLifecycles)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun attachBaseContext(base: Context) {
        try {
            appLifecycles.forEach {
                it.attachBaseContext(base)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(application: Application) {
        //MVP最基础的三方SDK优先初始化
        Utils.init(application)
        initAppEnvironment(environment)
        if (BuildConfig.DEBUG) {
            ARouter.openLog() // 打印日志
//            ARouter.openDebug() // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(application)
        try {
            appLifecycles.forEach {
                it.onCreate(application)
            }
            application.registerActivityLifecycleCallbacks(basicActivityLifecycle)
            activityLifecycles.forEach {
                application.registerActivityLifecycleCallbacks(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onTerminate(application: Application) {
        try {
            application.unregisterActivityLifecycleCallbacks(basicActivityLifecycle)
            basicActivityLifecycle.configFragmentLifecycles.clear()
            activityLifecycles.forEach {
                application.unregisterActivityLifecycleCallbacks(it)
            }
            activityLifecycles.clear()
            appLifecycles.forEach {
                it.onTerminate(application)
            }
            appLifecycles.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}