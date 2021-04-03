package ch.smart.code.mvp.app

import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import androidx.fragment.app.FragmentManager
import ch.smart.code.mvp.lifecycle.AppLifecycle

/**
 * 类描述：全局配置接口,各模块可添加注册自己的配置
 */
interface ConfigModule {

    /**
     * 使用 [AppLifecycle] 在 [Application] 的生命周期中注入一些操作
     *
     * @param context    [Context]
     * @param lifecycles [Application] 的生命周期容器, 可向框架中添加多个 [Application] 的生命周期类
     */
    fun injectAppLifecycle(context: Context, lifecycles: ArrayList<AppLifecycle>)

    /**
     * 使用 [Application.ActivityLifecycleCallbacks] 在 [Activity] 的生命周期中注入一些操作
     *
     * @param context    [Context]
     * @param lifecycles [Activity] 的生命周期容器, 可向框架中添加多个 [Activity] 的生命周期类
     */
    fun injectActivityLifecycle(context: Context, lifecycles: ArrayList<ActivityLifecycleCallbacks>)

    /**
     * 使用 [FragmentManager.FragmentLifecycleCallbacks] 在 [Fragment] 的生命周期中注入一些操作
     *
     * @param context    [Context]
     * @param lifecycles [Fragment] 的生命周期容器, 可向框架中添加多个 [Fragment] 的生命周期类
     */
    fun injectFragmentLifecycle(
        context: Context,
        lifecycles: ArrayList<FragmentManager.FragmentLifecycleCallbacks>
    )
}