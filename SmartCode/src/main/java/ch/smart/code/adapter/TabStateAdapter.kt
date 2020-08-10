package ch.smart.code.adapter

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TabStateAdapter(
        fm: FragmentManager,
        private val fragments: List<Fragment>,
        private val titles: List<String>,
        private val enabledState: Boolean = true //是否启用状态保存恢复机制
) : FragmentStatePagerAdapter(fm) {
    
    override fun getItem(p0: Int): Fragment {
        return fragments[p0]
    }
    
    override fun getCount(): Int {
        return fragments.size
    }
    
    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
    
    override fun saveState(): Parcelable? {
        return if (enabledState) super.saveState() else null
    }
    
    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        if (enabledState) {
            super.restoreState(state, loader)
        }
    }
    
}