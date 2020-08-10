package ch.smart.code.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

open class TabPageAdapter(
        fm: FragmentManager,
        private val fragments: List<Fragment>,
        private var titles: List<String>
) : FragmentPagerAdapter(fm) {
    
    override fun getItem(p0: Int): Fragment {
        return fragments[p0]
    }
    
    override fun getCount(): Int {
        return fragments.size
    }
    
    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
    
    fun setPageTitles(titles: List<String>) {
        this.titles = titles
    }
    
}