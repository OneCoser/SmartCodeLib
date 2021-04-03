package ch.smart.code.mvp.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import ch.smart.code.adapter.StatusBarAdapter
import ch.smart.code.mvp.IActivity
import ch.smart.code.util.click
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ResourceUtils
import com.qmuiteam.qmui.widget.QMUITopBar
import com.trello.rxlifecycle3.android.ActivityEvent
import org.simple.eventbus.EventBus

/**
 * 类描述：Activity生命周期监听
 */
class BasicActivityLifecycle() : Application.ActivityLifecycleCallbacks {

    private val fragmentLifecycle by lazy {
        BasicFragmentLifecycle()
    }

    val configFragmentLifecycles by lazy {
        arrayListOf<FragmentManager.FragmentLifecycleCallbacks>()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activity.lifecycleSubject(ActivityEvent.CREATE)
        //注册EventBus
        if (activity is IActivity && activity.useEventBus()) {
            EventBus.getDefault().register(activity)
        }
        //注册FragmentLifecycle
        if (activity is FragmentActivity && activity is IActivity && activity.useFragment()) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                fragmentLifecycle, true
            )
            configFragmentLifecycles.forEach {
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                    it, true
                )
            }
        }
        //插入ARouter
        ARouter.getInstance().inject(activity)
        //初始化StatusBar
        if (activity is StatusBarAdapter) {
            BarUtils.setStatusBarColor(activity, activity.getBarColor())
            BarUtils.setStatusBarLightMode(activity, activity.isLightMode())
        }
    }

    override fun onActivityStarted(activity: Activity) {
        activity.lifecycleSubject(ActivityEvent.START)
        // 这里全局给Activity设置toolbar和title
        if (!activity.intent.getBooleanExtra("isInitToolbar", false)) {
            activity.intent.putExtra("isInitToolbar", true)
            val view = activity.findViewById<View>(ResourceUtils.getIdByName("publicTopBar"))
            if (view is QMUITopBar) {
                view.setTitle(activity.title.toString())
                view.addLeftBackImageButton().click(action = activity::onBackPressed)
            }
        }
    }

    override fun onActivityResumed(activity: Activity?) {
        activity.lifecycleSubject(ActivityEvent.RESUME)
    }

    override fun onActivityPaused(activity: Activity?) {
        activity.lifecycleSubject(ActivityEvent.PAUSE)
    }

    override fun onActivityStopped(activity: Activity?) {
        activity.lifecycleSubject(ActivityEvent.STOP)
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        activity.lifecycleSubject(ActivityEvent.DESTROY)
        if (activity is IActivity && activity.useEventBus()) {
            EventBus.getDefault().unregister(activity)
        }
        activity.intent.removeExtra("isInitToolbar")
    }
}