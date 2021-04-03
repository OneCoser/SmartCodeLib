package ch.smart.code.mvp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ch.smart.code.mvp.lifecycle.FragmentLifecycleable
import com.trello.rxlifecycle3.android.FragmentEvent
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

/**
 * 类描述：Fragment基类
 */
abstract class BaseFragment<P : IPresenter> : Fragment(), IFragment, FragmentLifecycleable,
    PageNameable {

    private val lifecycleSubject = BehaviorSubject.create<FragmentEvent>()

    @JvmField
    protected var presenter: P? = this.createPresenter()

    abstract fun createPresenter(): P?

    var currentVisible: Boolean? = null
        private set

    override fun provideLifecycleSubject(): Subject<FragmentEvent> {
        return lifecycleSubject
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        (presenter as? ListPresenter<*, *, *>)?.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放资源
        presenter?.onDestroy()
        presenter = null
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

    override fun useEventBus(): Boolean {
        return true
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

    override fun getPageName(): String {
        return this.javaClass.simpleName
    }

    override fun getPagePath(): String {
        val act = activity
        return if (act is PageNameable) "${act.getPageName()}-${getPageName()}" else getPageName()
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
