package ch.smart.code.base

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.jess.arms.base.delegate.IFragment
import com.jess.arms.di.component.AppComponent
import com.jess.arms.integration.lifecycle.FragmentLifecycleable
import com.jess.arms.mvp.IPresenter
import com.trello.rxlifecycle3.android.FragmentEvent
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import javax.inject.Inject

abstract class SCBaseDialogFragment<P : IPresenter> : DialogFragment(), IFragment, FragmentLifecycleable {
    
    private val mLifecycleSubject = BehaviorSubject.create<FragmentEvent>()
    protected open var bottomPopUp = false
    
    @JvmField
    @Inject
    protected var mPresenter: P? = null //如果当前页面逻辑简单, Presenter 可以为 null
    
    override fun provideLifecycleSubject(): Subject<FragmentEvent> {
        return mLifecycleSubject
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return initView(inflater, container, savedInstanceState)
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (bottomPopUp) {
            val window = dialog?.window
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setWindowAnimations(ch.smart.code.R.style.public_dialog_anim_bottom)
            dialog?.setCancelable(true)
            dialog?.setCanceledOnTouchOutside(true)
            val params = window?.attributes
            params?.gravity = Gravity.BOTTOM
            params?.width = WindowManager.LayoutParams.MATCH_PARENT
            window?.attributes = params
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mPresenter?.onDestroy() //释放资源
        this.mPresenter = null
    }
    
    /**
     * 是否使用 EventBus
     * Arms 核心库现在并不会依赖某个 EventBus, 要想使用 EventBus, 还请在项目中自行依赖对应的 EventBus
     * 现在支持两种 EventBus, greenrobot 的 EventBus 和畅销书 《Android源码设计模式解析与实战》的作者 何红辉 所作的 AndroidEventBus
     * 确保依赖后, 将此方法返回 true, Arms 会自动检测您依赖的 EventBus, 并自动注册
     * 这种做法可以让使用者有自行选择三方库的权利, 并且还可以减轻 Arms 的体积
     *
     * @return 返回 {@code true} (默认为使用 {@code true}), Arms 会自动注册 EventBus
     */
    override fun useEventBus(): Boolean {
        return false
    }
    
    
    override fun setupFragmentComponent(appComponent: AppComponent) {
    }
    
    override fun setData(data: Any?) {
    }
    
    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
    
    override fun show(transaction: FragmentTransaction, tag: String?): Int {
        return try {
            super.show(transaction, tag)
        } catch (e: Exception) {
            Timber.e(e)
            -1
        }
    }
    
    override fun showNow(manager: FragmentManager, tag: String?) {
        try {
            super.showNow(manager, tag)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
    
    override fun dismiss() {
        try {
            super.dismiss()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
    
    override fun dismissAllowingStateLoss() {
        try {
            super.dismissAllowingStateLoss()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
    
}

/**
 * 防止崩溃的showDialogFragment
 */
fun DialogFragment.safeShow(manager: FragmentManager, tag: String) {
    try {
        this.show(manager, tag)
    } catch (e: Exception) {
        Timber.e(e)
    }
}