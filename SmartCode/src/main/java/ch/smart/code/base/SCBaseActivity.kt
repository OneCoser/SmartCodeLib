package ch.smart.code.base

import android.content.res.Resources
import android.os.Bundle
import android.view.InflateException
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.jess.arms.base.delegate.IActivity
import com.jess.arms.di.component.AppComponent
import com.jess.arms.integration.lifecycle.ActivityLifecycleable
import com.jess.arms.mvp.IPresenter
import com.trello.rxlifecycle3.android.ActivityEvent
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import me.jessyan.autosize.AutoSizeCompat
import timber.log.Timber
import javax.inject.Inject

abstract class SCBaseActivity<P : IPresenter> : AppCompatActivity(), IActivity,
    ActivityLifecycleable {

    private val lifecycleSubject = BehaviorSubject.create<ActivityEvent>()

    @JvmField
    @Inject
    protected var mPresenter: P? = null // 如果当前页面逻辑简单, Presenter 可以为 null

    override fun provideLifecycleSubject(): Subject<ActivityEvent> {
        return lifecycleSubject
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val layoutResID = initView(savedInstanceState)
            // 如果initView返回0,框架则不会调用setContentView()
            if (layoutResID != 0) {
                setContentView(layoutResID)
            }
        } catch (e: Exception) {
            Timber.e(e)
            if (e is InflateException) {
                finish()
                return
            }
        }

        initData(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter?.onDestroy() // 释放资源
        this.mPresenter = null
        (window?.decorView as? ViewGroup)?.let {
            traverse(it)
        }
    }

    /**
     * 是否使用 EventBus
     * Arms 核心库现在并不会依赖某个 EventBus, 要想使用 EventBus, 还请在项目中自行依赖对应的 EventBus
     * 现在支持两种 EventBus, greenrobot 的 EventBus 和畅销书 《Android源码设计模式解析与实战》的作者 何红辉 所作的 AndroidEventBus
     * 确保依赖后, 将此方法返回 true, Arms 会自动检测您依赖的 EventBus, 并自动注册
     * 这种做法可以让使用者有自行选择三方库的权利, 并且还可以减轻 Arms 的体积
     *
     * @return 返回 `true` (默认为使用 `true`), Arms 会自动注册 EventBus
     */
    override fun useEventBus(): Boolean {
        return true
    }

    /**
     * 这个Activity是否会使用Fragment,框架会根据这个属性判断是否注册[androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks]
     * 如果返回false,那意味着这个Activity不需要绑定Fragment,那你再在这个Activity中绑定继承于 [SQBaseFragment] 的Fragment将不起任何作用
     *
     * @return
     */
    override fun useFragment(): Boolean {
        return true
    }

    override fun setupActivityComponent(appComponent: AppComponent) {
    }

    override fun getResources(): Resources {
        AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources())
        return super.getResources()
    }

    private fun traverse(root: ViewGroup) {
        try {
            val childCount = root.childCount
            for (i in 0 until childCount) {
                val child = root.getChildAt(i) ?: continue
                if (child is ViewGroup) {
                    child.background = null
                    traverse(child)
                } else {
                    child.background = null
                    (child as? ImageView)?.setImageDrawable(null)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
