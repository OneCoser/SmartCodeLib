package ch.smart.code

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import ch.smart.code.adapter.StatusBarAdapter
import ch.smart.code.util.click
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ResourceUtils
import com.qmuiteam.qmui.widget.QMUITopBar

class SCActivityLifecycleCallback : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        ARouter.getInstance().inject(activity)
        var isLightMode = false
        if (activity is StatusBarAdapter) {
            val barAdapter = activity as StatusBarAdapter
            BarUtils.setStatusBarColor(activity, barAdapter.getBarColor())
            isLightMode = barAdapter.isLightMode()
        }
        BarUtils.setStatusBarLightMode(activity, isLightMode)
    }

    override fun onActivityStarted(activity: Activity) {
        if (!activity.intent.getBooleanExtra("isInitToolbar", false)) {
            //由于加强框架的兼容性,故将 setContentView 放到 onActivityCreated 之后,onActivityStarted 之前执行
            //而 findViewById 必须在 Activity setContentView() 后才有效,所以将以下代码从之前的 onActivityCreated 中移动到 onActivityStarted 中执行
            activity.intent.putExtra("isInitToolbar", true)
            //这里全局给Activity设置toolbar和title,你想象力有多丰富,这里就有多强大,以前放到BaseActivity的操作都可以放到这里
            val view = activity.findViewById<View>(ResourceUtils.getIdByName("publicTopBar"))
            if (view is QMUITopBar) {
                view.addLeftBackImageButton().click {
                    activity.onBackPressed()
                }
                val title = activity.title.toString()
                if (!TextUtils.equals(title, AppUtils.getAppName())) {
                    view.setTitle(title)
                }
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        //横竖屏切换或配置改变时, Activity 会被重新创建实例, 但 Bundle 中的基础数据会被保存下来,移除该数据是为了保证重新创建的实例可以正常工作
        activity.intent.removeExtra("isInitToolbar")
    }
}
