package ch.smart.code

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.alibaba.android.arouter.launcher.ARouter

class SCFragmentLifecycleCallback : FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        ARouter.getInstance().inject(f)
    }
}