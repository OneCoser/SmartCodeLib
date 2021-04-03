package ch.smart.code.mvp.lifecycle

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ch.smart.code.mvp.IFragment
import com.alibaba.android.arouter.launcher.ARouter
import com.trello.rxlifecycle3.android.FragmentEvent
import org.simple.eventbus.EventBus

/**
 * 类描述：Fragment生命周期监听
 */
class BasicFragmentLifecycle : FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        super.onFragmentAttached(fm, f, context)
        f.lifecycleSubject(FragmentEvent.ATTACH)
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)
        f.lifecycleSubject(FragmentEvent.CREATE)
        if (f is IFragment && f.useEventBus()) {
            EventBus.getDefault().register(f)
        }
        ARouter.getInstance().inject(f)
    }

    override fun onFragmentViewCreated(
        fm: FragmentManager,
        f: Fragment,
        v: View,
        savedInstanceState: Bundle?
    ) {
        super.onFragmentViewCreated(fm, f, v, savedInstanceState)
        f.lifecycleSubject(FragmentEvent.CREATE_VIEW)
        if (f is IFragment) {
            f.initData(savedInstanceState)
        }
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        super.onFragmentStarted(fm, f)
        f.lifecycleSubject(FragmentEvent.START)
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        super.onFragmentResumed(fm, f)
        f.lifecycleSubject(FragmentEvent.RESUME)
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        super.onFragmentPaused(fm, f)
        f.lifecycleSubject(FragmentEvent.PAUSE)
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        super.onFragmentStopped(fm, f)
        f.lifecycleSubject(FragmentEvent.STOP)
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentViewDestroyed(fm, f)
        f.lifecycleSubject(FragmentEvent.DESTROY_VIEW)
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentDestroyed(fm, f)
        f.lifecycleSubject(FragmentEvent.DESTROY)
        if (f is IFragment && f.useEventBus()) {
            EventBus.getDefault().unregister(f)
        }
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        super.onFragmentDetached(fm, f)
        f.lifecycleSubject(FragmentEvent.DETACH)
    }
}