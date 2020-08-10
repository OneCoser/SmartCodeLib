package ch.smart.code.adapter.item

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RcvAdapterItem<T>(
        context: Context,
        parent: ViewGroup,
        val item: AdapterItem<T>
) : RecyclerView.ViewHolder(item.getItemView(context, parent)) {
    
    init {
        this.item.setViews()
    }
}
