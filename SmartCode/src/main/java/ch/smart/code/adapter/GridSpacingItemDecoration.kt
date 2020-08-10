package ch.smart.code.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

open class GridSpacingItemDecoration(
        //跟布局里面的spanCount属性是一致的
        private val spanCount: Int,
        //每一个矩形的间距
        private val spacing: Int,
        //如果设置成false那边缘地带就没有间距
        private val includeEdge: Boolean = false,
        //是否有Header
        private val hasHeader: Boolean = false
) : RecyclerView.ItemDecoration() {
    
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        var position = parent.getChildAdapterPosition(view) // item position
        if (hasHeader && position == 0) {
            return
        }
        var column = position % spanCount
        if (hasHeader) {
            position -= 1
            column = position % spanCount
        }
        // item column
        
        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)
            
            if (position < spanCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
            outRect.right = spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = spacing // item top
            }
        }
    }
}