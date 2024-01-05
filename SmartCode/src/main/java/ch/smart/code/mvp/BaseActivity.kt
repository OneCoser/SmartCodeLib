package ch.smart.code.mvp

import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.view.InflateException
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import ch.smart.code.mvp.lifecycle.ActivityLifecycleable
import com.trello.rxlifecycle3.android.ActivityEvent
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import me.jessyan.autosize.AutoSizeCompat
import timber.log.Timber

/**
 * 类描述：Activity基类
 */
abstract class BaseActivity<P : IPresenter>() : AppCompatActivity(), IActivity,
    ActivityLifecycleable,
    PageNameable {

    @JvmField
    protected var presenter: P? = this.createPresenter()

    abstract fun createPresenter(): P?

    private val lifecycleSubject = BehaviorSubject.create<ActivityEvent>()

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
        presenter?.onDestroy() // 释放资源
        presenter = null
        (window?.decorView as? ViewGroup)?.let {
            traverse(it)
        }
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun useFragment(): Boolean {
        return true
    }

    override fun getResources(): Resources {
        autoConvertDensityOfGlobal()
        return super.getResources()
    }

    open fun autoConvertDensityOfGlobal() {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
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

    override fun getPageName(): String {
        return title?.toString() ?: this.javaClass.simpleName
    }

    override fun getPagePath(): String {
        return getPageName()
    }
}