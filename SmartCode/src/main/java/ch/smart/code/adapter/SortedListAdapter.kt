package ch.smart.code.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import ch.smart.code.adapter.item.RcvAdapterItem
import ch.smart.code.adapter.util.ISortedAdapter
import ch.smart.code.adapter.util.ItemTypeUtil


abstract class SortedListAdapter<T>(private var dataList: SortedList<T>?) : RecyclerView.Adapter<RcvAdapterItem<T>>(), ISortedAdapter<T> {
    private lateinit var type: Any
    private val typeUtil: ItemTypeUtil = ItemTypeUtil()
    private var currentPos: Int = 0
    
    
    override fun getItemCount(): Int {
        return dataList?.size() ?: 0
    }
    
    
    override fun setData(data: SortedList<T>) {
        this.dataList = data
    }
    
    
    override fun getData(): SortedList<T>? {
        return this.dataList
    }
    
    
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    
    
    @Deprecated("see getItemType()")
    override fun getItemViewType(position: Int): Int {
        currentPos = position
        val any = dataList?.get(position) ?: return 0
        type = getItemType(any)
        return typeUtil.getIntType(type)
    }
    
    
    override fun getItemType(t: T): Any {
        return -1
    }
    
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RcvAdapterItem<T> {
        return RcvAdapterItem(parent.context, parent, createItem(type))
    }
    
    
    override fun onBindViewHolder(holder: RcvAdapterItem<T>, position: Int) {
        holder.item.handleData(dataList!!.get(position), position)
    }
    
    
    override fun getCurrentPosition(): Int {
        return this.currentPos
    }
    
    
}
