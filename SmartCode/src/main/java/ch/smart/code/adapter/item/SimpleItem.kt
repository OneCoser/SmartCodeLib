package ch.smart.code.adapter.item

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class SimpleItem<T> : AdapterItem<T> {
    
    protected lateinit var rootView: View
    
    
    override fun getItemView(context: Context, parent: ViewGroup): View {
        rootView = LayoutInflater.from(context).inflate(layoutResId, parent, false)
        return rootView
    }
    
    
    override fun setViews() {}
    
    /**
     * Item绑定到视图
     */
    override fun onAttach() {}
    
    /**
     * Item从视图解绑
     */
    override fun onDetach() {}
    
    
}
