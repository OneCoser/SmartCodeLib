package ch.smart.code.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jess.arms.base.delegate.IFragment
import com.jess.arms.di.component.AppComponent
import com.jess.arms.integration.lifecycle.FragmentLifecycleable
import com.jess.arms.mvp.IPresenter
import com.trello.rxlifecycle3.android.FragmentEvent
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

abstract class SCBaseFragment<P : IPresenter> : Fragment(), IFragment, FragmentLifecycleable {
    
    private val lifecycleSubject = BehaviorSubject.create<FragmentEvent>()
    
    //如果当前页面逻辑简单, Presenter 可以为 null
    @JvmField
    @Inject
    protected var mPresenter: P? = null
    
    var currentVisible: Boolean? = null
        private set
    
    override fun provideLifecycleSubject(): Subject<FragmentEvent> {
        return lifecycleSubject
    }
    
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return initView(inflater, container, savedInstanceState)
    }
    
    override fun onResume() {
        super.onResume()
        visibleChanged()
    }
    
    
    override fun onPause() {
        super.onPause()
        visibleChanged()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        visibleChanged()
        (mPresenter as? SCListPresenter<*, *, *>)?.onDestroyView()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        //释放资源
        mPresenter?.onDestroy()
        this.mPresenter = null
    }
    
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        visibleChanged()
        if (context != null) {
            childFragmentManager.fragments.forEach {
                it.userVisibleHint = isVisibleToUser
            }
        }
    }
    
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        visibleChanged()
        if (context != null) {
            childFragmentManager.fragments.forEach {
                it.onHiddenChanged(hidden)
            }
        }
    }
    
    private fun visibleChanged() {
        val userVisible = getUserVisible()
        if (userVisible == currentVisible) {
            return
        }
        currentVisible = userVisible
        onVisibleChanged(userVisible)
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
        return true
    }
    
    
    override fun setupFragmentComponent(appComponent: AppComponent) {
    }
    
    /**
     * 当前 Fragment 是否能被用户看见
     *
     * 1、在Activity使用XML引入，或者使用 FragmentManager 的 addFragment 或者 replaceFragment 动态载入，只要监听Fragment的onResume和onPause方法就能够判断其显隐
     *
     * 2、使用 show 和 hide 来显隐的 Fragment，监听 onHiddenChanged 方法
     *
     * 3、在 ViewPager 中的 Fragment，监听 setUserVisibleHint 来判断到底对用户是否可见
     *
     * @param isVisibleToUser 如果此片段的UI当前对用户可见，则为true（默认），如果不是，则为false。
     *
     */
    protected open fun onVisibleChanged(isVisibleToUser: Boolean) {}
    
    
    override fun setData(data: Any?) {
    }
    
}

/**
 * Fragment是否能被用户看见
 */
fun Fragment.getUserVisible(): Boolean {
    return userVisibleHint && isAdded && !isHidden && isResumed && isParentVisible()
}

/**
 * 判断父fragment是否可见
 */
fun Fragment.isParentVisible(): Boolean {
    val fragment = parentFragment
    return fragment == null || fragment.getUserVisible()
}